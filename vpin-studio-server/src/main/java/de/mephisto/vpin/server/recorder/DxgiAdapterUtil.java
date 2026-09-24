package de.mephisto.vpin.server.recorder;

import com.sun.jna.Function;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Enumerates DXGI adapters (GPUs) for resolving the adapter_idx placeholder
 * used in FFmpeg's -init_hw_device d3d11va:[adapter_idx] option.
 *
 * Two implementations are provided:
 *   - WMI (via PowerShell) — simple fallback
 *   - DXGI (via JNA)       — exact same enumeration order FFmpeg uses
 */
public class DxgiAdapterUtil {

  private static final Logger LOG = LoggerFactory.getLogger(DxgiAdapterUtil.class);

  // IID_IDXGIFactory = {7b7166ec-21c7-44ae-b21a-c9ae321ae369}
  private static final byte[] IID_IDXGI_FACTORY = {
      (byte) 0xec, (byte) 0x66, (byte) 0x71, (byte) 0x7b,  // Data1 (little-endian)
      (byte) 0xc7, (byte) 0x21,                              // Data2 (little-endian)
      (byte) 0xae, (byte) 0x44,                              // Data3 (little-endian)
      (byte) 0xb2, (byte) 0x1a, (byte) 0xc9, (byte) 0xae,  // Data4
      (byte) 0x32, (byte) 0x1a, (byte) 0xe3, (byte) 0x69
  };

  // DXGI_ADAPTER_DESC layout (x64):
  //   WCHAR Description[128]  = 256 bytes  @ offset   0
  //   UINT  VendorId          =   4 bytes  @ offset 256
  //   UINT  DeviceId          =   4 bytes  @ offset 260
  //   UINT  SubSysId          =   4 bytes  @ offset 264
  //   UINT  Revision          =   4 bytes  @ offset 268
  //   SIZE_T DedicatedVideoMemory =8 bytes @ offset 272
  //   SIZE_T DedicatedSystemMemory=8 bytes @ offset 280
  //   SIZE_T SharedSystemMemory   =8 bytes @ offset 288
  //   LUID  AdapterLuid       =   8 bytes  @ offset 296
  //   Total: 304 bytes
  private static final int ADAPTER_DESC_SIZE = 304;
  private static final int DEDICATED_VRAM_OFFSET = 272;

  public static class AdapterInfo {
    public final int index;
    public final String name;
    public final long dedicatedVideoMemoryMb;

    AdapterInfo(int index, String name, long dedicatedVideoMemoryMb) {
      this.index = index;
      this.name = name;
      this.dedicatedVideoMemoryMb = dedicatedVideoMemoryMb;
    }

    @Override
    public String toString() {
      return "[" + index + "] " + name + " (" + dedicatedVideoMemoryMb + " MB VRAM)";
    }
  }

  /**
   * Option 1: enumerate adapters via PowerShell WMI.
   * Simple and reliable, but enumeration order may not exactly match DXGI.
   */
  public static List<AdapterInfo> getAdaptersViaWMI() {
    List<AdapterInfo> result = new ArrayList<>();
    try {
      ProcessBuilder pb = new ProcessBuilder(
          "powershell", "-NoProfile", "-Command",
          "Get-WmiObject Win32_VideoController | Select-Object Name,AdapterRAM | ConvertTo-Csv -NoTypeInformation"
      );
      pb.redirectErrorStream(true);
      Process p = pb.start();
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
        boolean header = true;
        int idx = 0;
        String line;
        while ((line = reader.readLine()) != null) {
          line = line.trim();
          if (line.isEmpty()) continue;
          if (header) {
            header = false;
            continue;
          }
          // CSV columns: "Name","AdapterRAM"
          String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
          if (parts.length >= 2) {
            String name = parts[0].replaceAll("^\"|\"$", "");
            long ramMb = 0;
            try {
              ramMb = Long.parseLong(parts[1].replaceAll("^\"|\"$", "").trim()) / (1024L * 1024L);
            }
            catch (NumberFormatException ignored) {
            }
            result.add(new AdapterInfo(idx++, name, ramMb));
          }
        }
      }
      p.waitFor();
    }
    catch (Exception e) {
      LOG.error("WMI adapter enumeration failed: {}", e.getMessage(), e);
    }
    return result;
  }

  /**
   * Option 2: enumerate adapters via JNA + DXGI COM interfaces.
   * Returns adapters in the exact same order as DXGI (and therefore FFmpeg).
   *
   * COM vtable layout used:
   *   IUnknown     : 0=QueryInterface, 1=AddRef, 2=Release
   *   IDXGIObject  : 3=SetPrivateData, 4=SetPrivateDataInterface, 5=GetPrivateData, 6=GetParent
   *   IDXGIFactory : 7=EnumAdapters, 8=MakeWindowAssociation, 9=GetWindowAssociation, ...
   *   IDXGIAdapter : 7=EnumOutputs,  8=GetDesc,  9=CheckInterfaceSupport
   */
  public static List<AdapterInfo> getAdaptersViaDxgi() {
    List<AdapterInfo> result = new ArrayList<>();
    Pointer pFactory = null;
    try {
      NativeLibrary dxgi = NativeLibrary.getInstance("dxgi");
      Function createFactory = dxgi.getFunction("CreateDXGIFactory");

      Memory iid = new Memory(16);
      iid.write(0, IID_IDXGI_FACTORY, 0, 16);

      PointerByReference ppFactory = new PointerByReference();
      int hr = createFactory.invokeInt(new Object[]{iid, ppFactory});
      if (hr != 0) {
        LOG.warn("CreateDXGIFactory failed: HRESULT=0x{}", Integer.toHexString(hr));
        return result;
      }

      pFactory = ppFactory.getValue();

      for (int i = 0; ; i++) {
        PointerByReference ppAdapter = new PointerByReference();
        // IDXGIFactory::EnumAdapters at vtable slot 7
        int enumHr = vtableCall(pFactory, 7, i, ppAdapter);
        if (enumHr != 0) break; // DXGI_ERROR_NOT_FOUND

        Pointer pAdapter = ppAdapter.getValue();
        try {
          Memory desc = new Memory(ADAPTER_DESC_SIZE);
          desc.clear();
          // IDXGIAdapter::GetDesc at vtable slot 8
          int descHr = vtableCall(pAdapter, 8, desc);
          if (descHr == 0) {
            String name = desc.getWideString(0);
            long vramMb = desc.getLong(DEDICATED_VRAM_OFFSET) / (1024L * 1024L);
            result.add(new AdapterInfo(i, name, vramMb));
          }
        }
        finally {
          // IDXGIAdapter::Release at vtable slot 2
          vtableCall(pAdapter, 2);
        }
      }
    }
    catch (Exception e) {
      LOG.error("DXGI adapter enumeration failed: {}", e.getMessage(), e);
    }
    finally {
      if (pFactory != null) {
        // IDXGIFactory::Release at vtable slot 2
        vtableCall(pFactory, 2);
      }
    }
    return result;
  }

  // DXGI_OUTPUT_DESC layout (x64):
  //   WCHAR DeviceName[32]    = 64 bytes @ offset  0
  //   RECT  DesktopCoordinates= 16 bytes @ offset 64 (left, top, right, bottom)
  //   BOOL  AttachedToDesktop =  4 bytes @ offset 80
  //   DXGI_MODE_ROTATION      =  4 bytes @ offset 84
  //   HMONITOR Monitor        =  8 bytes @ offset 88
  //   Total: 96 bytes
  private static final int OUTPUT_DESC_SIZE = 96;
  private static final int DESKTOP_COORDINATES_OFFSET = 64;
  private static final int ATTACHED_TO_DESKTOP_OFFSET = 80;

  public static class OutputInfo {
    public final int adapterIndex;
    public final int outputIndex;
    public final String deviceName;
    public final Rectangle bounds;

    OutputInfo(int adapterIndex, int outputIndex, String deviceName, Rectangle bounds) {
      this.adapterIndex = adapterIndex;
      this.outputIndex = outputIndex;
      this.deviceName = deviceName;
      this.bounds = bounds;
    }

    @Override
    public String toString() {
      return "[adapter " + adapterIndex + ", output " + outputIndex + "] " + deviceName + " " + bounds.x + "," + bounds.y + " " + bounds.width + "x" + bounds.height;
    }
  }

  /**
   * Enumerates the desktop-attached outputs (monitors) of all adapters via DXGI.
   * The adapter/output indexes are exactly the ones ffmpeg's -init_hw_device d3d11va:N
   * and ddagrab=output_idx=M expect, which is not necessarily the order of Java's GraphicsDevices.
   * IDXGIOutput vtable: 0-2=IUnknown, 3-6=IDXGIObject, 7=GetDesc
   */
  public static List<OutputInfo> getOutputsViaDxgi() {
    List<OutputInfo> result = new ArrayList<>();
    Pointer pFactory = null;
    try {
      NativeLibrary dxgi = NativeLibrary.getInstance("dxgi");
      Function createFactory = dxgi.getFunction("CreateDXGIFactory");

      Memory iid = new Memory(16);
      iid.write(0, IID_IDXGI_FACTORY, 0, 16);

      PointerByReference ppFactory = new PointerByReference();
      int hr = createFactory.invokeInt(new Object[]{iid, ppFactory});
      if (hr != 0) {
        LOG.warn("CreateDXGIFactory failed: HRESULT=0x{}", Integer.toHexString(hr));
        return result;
      }
      pFactory = ppFactory.getValue();

      for (int a = 0; ; a++) {
        PointerByReference ppAdapter = new PointerByReference();
        if (vtableCall(pFactory, 7, a, ppAdapter) != 0) break;

        Pointer pAdapter = ppAdapter.getValue();
        try {
          for (int o = 0; ; o++) {
            PointerByReference ppOutput = new PointerByReference();
            // IDXGIAdapter::EnumOutputs at vtable slot 7
            if (vtableCall(pAdapter, 7, o, ppOutput) != 0) break;

            Pointer pOutput = ppOutput.getValue();
            try {
              Memory desc = new Memory(OUTPUT_DESC_SIZE);
              desc.clear();
              // IDXGIOutput::GetDesc at vtable slot 7
              if (vtableCall(pOutput, 7, desc) == 0 && desc.getInt(ATTACHED_TO_DESKTOP_OFFSET) != 0) {
                int left = desc.getInt(DESKTOP_COORDINATES_OFFSET);
                int top = desc.getInt(DESKTOP_COORDINATES_OFFSET + 4);
                int right = desc.getInt(DESKTOP_COORDINATES_OFFSET + 8);
                int bottom = desc.getInt(DESKTOP_COORDINATES_OFFSET + 12);
                result.add(new OutputInfo(a, o, desc.getWideString(0), new Rectangle(left, top, right - left, bottom - top)));
              }
            }
            finally {
              vtableCall(pOutput, 2);
            }
          }
        }
        finally {
          vtableCall(pAdapter, 2);
        }
      }
    }
    catch (Throwable e) {
      LOG.error("DXGI output enumeration failed: {}", e.getMessage(), e);
    }
    finally {
      if (pFactory != null) {
        vtableCall(pFactory, 2);
      }
    }
    return result;
  }

  /**
   * Returns the DXGI output whose desktop area contains the given point, or null if none does.
   */
  public static OutputInfo resolveOutput(int x, int y) {
    List<OutputInfo> outputs = getOutputsViaDxgi();
    for (OutputInfo output : outputs) {
      if (output.bounds.contains(x, y)) {
        return output;
      }
    }
    LOG.warn("No DXGI output found containing {},{}, available outputs: {}", x, y, outputs);
    return null;
  }

  private static volatile Boolean nvidiaGpuPresent;

  /**
   * Whether any enumerated GPU adapter is an NVIDIA card, used to decide whether ffmpeg
   * can use the h264_nvenc hardware encoder instead of software libx264. The result is
   * cached since the installed hardware can't change while the server is running.
   */
  public static boolean isNvidiaGpuPresent() {
    Boolean result = nvidiaGpuPresent;
    if (result == null) {
      synchronized (DxgiAdapterUtil.class) {
        result = nvidiaGpuPresent;
        if (result == null) {
          List<AdapterInfo> adapters = getAdaptersViaDxgi();
          if (adapters.isEmpty()) {
            adapters = getAdaptersViaWMI();
          }
          result = adapters.stream().anyMatch(a -> a.name != null && a.name.toUpperCase().contains("NVIDIA"));
          nvidiaGpuPresent = result;
          LOG.info("NVIDIA GPU detection result: {} ({})", result, adapters);
        }
      }
    }
    return result;
  }

  /**
   * Returns the adapter index for the given name (case-insensitive).
   * Tries DXGI first, falls back to WMI. Returns 0 if not found.
   */
  public static int resolveAdapterIndex(String adapterName) {
    List<AdapterInfo> adapters = getAdaptersViaDxgi();
    if (adapters.isEmpty()) {
      adapters = getAdaptersViaWMI();
    }
    for (AdapterInfo a : adapters) {
      if (a.name.equalsIgnoreCase(adapterName)) {
        return a.index;
      }
    }
    return 0;
  }

  /**
   * Calls a COM vtable method on obj, automatically prepending obj as the
   * implicit 'this' pointer. Returns the HRESULT.
   */
  private static int vtableCall(Pointer obj, int slot, Object... args) {
    Pointer vtable = obj.getPointer(0);
    Pointer fnPtr = vtable.getPointer((long) slot * Native.POINTER_SIZE);
    Object[] callArgs = new Object[args.length + 1];
    callArgs[0] = obj;
    System.arraycopy(args, 0, callArgs, 1, args.length);
    return Function.getFunction(fnPtr, Function.ALT_CONVENTION).invokeInt(callArgs);
  }

  public static void main(String[] args) {
    System.out.println(getAdaptersViaWMI());
    System.out.println(getAdaptersViaDxgi());
    System.out.println(getOutputsViaDxgi());
  }
}

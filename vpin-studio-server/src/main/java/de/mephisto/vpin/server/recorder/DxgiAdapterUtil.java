package de.mephisto.vpin.server.recorder;

import com.sun.jna.Function;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
   *   IDXGIAdapter : 7=EnumOutputs,  8=CheckInterfaceSupport,  9=GetDesc
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
          // IDXGIAdapter::GetDesc at vtable slot 9
          int descHr = vtableCall(pAdapter, 9, desc);
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
  }
}

package de.mephisto.vpin.server.util;


import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class WindowsVolumeControl {
    private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // CLSID and IID for Windows Core Audio API
    private static final Guid.CLSID CLSID_MMDeviceEnumerator =
            new Guid.CLSID("BCDE0395-E52F-467C-8E3D-C4579291692E");

    private static final Guid.GUID IID_IMMDeviceEnumerator =
            new Guid.GUID("A95664D2-9614-4F35-A746-DE8DB63617E6");

    private static final Guid.GUID IID_IAudioEndpointVolume =
            new Guid.GUID("5CDF2C82-841E-4546-9722-0CF74078229A");

    // Constants for device role and data flow
    private static final int eRender = 0;
    private static final int eConsole = 0;
    private static final int CLSCTX_ALL = 23;

    /**
     * Gets the current master volume level for the default audio output device
     *
     * @return Volume level as a float between 0.0 (0%) and 1.0 (100%)
     *         Returns -1.0f if an error occurs
     */
    public static float getMasterVolume() {
        try {
            // Initialize COM
            LOG.info("Inside getMasterVolume");
            WinNT.HRESULT hr = Ole32.INSTANCE.CoInitializeEx(null, Ole32.COINIT_APARTMENTTHREADED);
            if (!hr.equals(WinNT.S_OK) && !hr.equals(WinNT.S_FALSE)) {
                System.err.println("Failed to initialize COM: " + hr);
                return -1.0f;
            }

            try {
                // Create device enumerator
                PointerByReference ppDeviceEnumerator = new PointerByReference();
                hr = Ole32.INSTANCE.CoCreateInstance(
                        CLSID_MMDeviceEnumerator,
                        null,
                        CLSCTX_ALL,
                        IID_IMMDeviceEnumerator,
                        ppDeviceEnumerator
                );

                if (!hr.equals(WinNT.S_OK)) {
                    System.err.println("Failed to create device enumerator: " + hr);
                    return -1.0f;
                }

                IMMDeviceEnumerator deviceEnumerator =
                        new IMMDeviceEnumerator(ppDeviceEnumerator.getValue());

                // Get default audio endpoint
                PointerByReference ppDevice = new PointerByReference();
                hr = deviceEnumerator.GetDefaultAudioEndpoint(eRender, eConsole, ppDevice);

                if (!hr.equals(WinNT.S_OK)) {
                    System.err.println("Failed to get default audio endpoint: " + hr);
                    System.err.println("Make sure you have an audio device connected and enabled");
                    deviceEnumerator.Release();
                    return -1.0f;
                }

                IMMDevice device = new IMMDevice(ppDevice.getValue());

                // Activate audio endpoint volume interface
                PointerByReference ppEndpointVolume = new PointerByReference();
                hr = device.Activate(
                        IID_IAudioEndpointVolume,
                        CLSCTX_ALL,
                        null,
                        ppEndpointVolume
                );

                if (!hr.equals(WinNT.S_OK)) {
                    System.err.println("Failed to activate endpoint volume: " + hr);
                    device.Release();
                    deviceEnumerator.Release();
                    return -1.0f;
                }

                IAudioEndpointVolume endpointVolume =
                        new IAudioEndpointVolume(ppEndpointVolume.getValue());

                // Get the master volume level
                float[] volumeLevel = new float[1];
                hr = endpointVolume.GetMasterVolumeLevelScalar(volumeLevel);

                // Clean up
                endpointVolume.Release();
                device.Release();
                deviceEnumerator.Release();

                if (hr.equals(WinNT.S_OK)) {
                    LOG.info("Got Volume-------------------------------------------");
                    return volumeLevel[0];
                } else {
                    System.err.println("Failed to get master volume level: " + hr);
                    return -1.0f;
                }

            } finally {
                Ole32.INSTANCE.CoUninitialize();
            }

        } catch (Exception e) {
            System.err.println("Exception occurred: " + e.getMessage());
            LOG.error("Problem Getting Volume: {}", e.getMessage());
            return -1.0f;
        }
    }

    /**
     * Gets the current master volume level as a percentage
     *
     * @return Volume level as an integer between 0 and 100
     *         Returns -1 if an error occurs
     */
    public static int getMasterVolumePercent() {
        float volume = getMasterVolume();
        if (volume < 0) {
            return -1;
        }
        return Math.round(volume * 100);
    }

    /**
     * Checks if the default audio device is muted
     *
     * @return true if muted, false if not muted, null if error occurs
     */
    public static Boolean isMuted() {
        try {
            WinNT.HRESULT hr = Ole32.INSTANCE.CoInitializeEx(null, Ole32.COINIT_APARTMENTTHREADED);
            if (!hr.equals(WinNT.S_OK) && !hr.equals(WinNT.S_FALSE)) {
                return null;
            }

            try {
                PointerByReference ppDeviceEnumerator = new PointerByReference();
                hr = Ole32.INSTANCE.CoCreateInstance(
                        CLSID_MMDeviceEnumerator,
                        null,
                        CLSCTX_ALL,
                        IID_IMMDeviceEnumerator,
                        ppDeviceEnumerator
                );

                if (!hr.equals(WinNT.S_OK)) {
                    return null;
                }

                IMMDeviceEnumerator deviceEnumerator =
                        new IMMDeviceEnumerator(ppDeviceEnumerator.getValue());

                PointerByReference ppDevice = new PointerByReference();
                hr = deviceEnumerator.GetDefaultAudioEndpoint(eRender, eConsole, ppDevice);

                if (!hr.equals(WinNT.S_OK)) {
                    deviceEnumerator.Release();
                    return null;
                }

                IMMDevice device = new IMMDevice(ppDevice.getValue());

                PointerByReference ppEndpointVolume = new PointerByReference();
                hr = device.Activate(
                        IID_IAudioEndpointVolume,
                        CLSCTX_ALL,
                        null,
                        ppEndpointVolume
                );

                if (!hr.equals(WinNT.S_OK)) {
                    device.Release();
                    deviceEnumerator.Release();
                    return null;
                }

                IAudioEndpointVolume endpointVolume =
                        new IAudioEndpointVolume(ppEndpointVolume.getValue());

                int[] mute = new int[1];
                hr = endpointVolume.GetMute(mute);

                endpointVolume.Release();
                device.Release();
                deviceEnumerator.Release();

                if (hr.equals(WinNT.S_OK)) {
                    return mute[0] != 0;
                } else {
                    return null;
                }

            } finally {
                Ole32.INSTANCE.CoUninitialize();
            }

        } catch (Exception e) {
            System.err.println("Exception occurred: " + e.getMessage());
            LOG.error("Problem Checking Mute State: {}", e.getMessage());
            return null;
        }
    }

    // COM Interface definitions with correct vtable offsets

    private static class IMMDeviceEnumerator extends com.sun.jna.platform.win32.COM.Unknown {
        public IMMDeviceEnumerator(Pointer pointer) {
            super(pointer);
        }

        // IUnknown has 3 methods (QueryInterface, AddRef, Release) at indices 0, 1, 2
        // GetDefaultAudioEndpoint is at index 4 (after EnumAudioEndpoints at index 3)
        public WinNT.HRESULT GetDefaultAudioEndpoint(int dataFlow, int role,
                                                     PointerByReference ppDevice) {
            return (WinNT.HRESULT) _invokeNativeObject(4,
                    new Object[]{this.getPointer(), dataFlow, role, ppDevice},
                    WinNT.HRESULT.class);
        }
    }

    private static class IMMDevice extends com.sun.jna.platform.win32.COM.Unknown {
        public IMMDevice(Pointer pointer) {
            super(pointer);
        }

        // IUnknown: 0-2, Activate is at index 3
        public WinNT.HRESULT Activate(Guid.GUID iid, int dwClsCtx, Pointer pActivationParams,
                                      PointerByReference ppInterface) {
            return (WinNT.HRESULT) _invokeNativeObject(3,
                    new Object[]{this.getPointer(), iid, dwClsCtx, pActivationParams, ppInterface},
                    WinNT.HRESULT.class);
        }
    }

    private static class IAudioEndpointVolume extends com.sun.jna.platform.win32.COM.Unknown {
        public IAudioEndpointVolume(Pointer pointer) {
            super(pointer);
        }

        // IUnknown: 0-2
        // RegisterControlChangeNotify: 3
        // UnregisterControlChangeNotify: 4
        // GetChannelCount: 5
        // SetMasterVolumeLevel: 6
        // SetMasterVolumeLevelScalar: 7
        // GetMasterVolumeLevel: 8
        // GetMasterVolumeLevelScalar: 9
        public WinNT.HRESULT GetMasterVolumeLevelScalar(float[] pfLevel) {
            return (WinNT.HRESULT) _invokeNativeObject(9,
                    new Object[]{this.getPointer(), pfLevel},
                    WinNT.HRESULT.class);
        }

        // SetChannelVolumeLevel: 10
        // SetChannelVolumeLevelScalar: 11
        // GetChannelVolumeLevel: 12
        // GetChannelVolumeLevelScalar: 13
        // SetMute: 14
        // GetMute: 15
        public WinNT.HRESULT GetMute(int[] pbMute) {
            return (WinNT.HRESULT) _invokeNativeObject(15,
                    new Object[]{this.getPointer(), pbMute},
                    WinNT.HRESULT.class);
        }
    }

    private interface Ole32 extends com.sun.jna.platform.win32.Ole32 {
        Ole32 INSTANCE = Native.load("ole32", Ole32.class);

        int COINIT_APARTMENTTHREADED = 0x2;
        int COINIT_MULTITHREADED = 0x0;

        WinNT.HRESULT CoInitializeEx(Pointer pvReserved, int dwCoInit);
    }

    // Example usage and diagnostics
    public static void main(String[] args) {
        System.out.println("=== Windows Volume Control ===");
        System.out.println("OS: " + System.getProperty("os.name"));
        System.out.println("Architecture: " + System.getProperty("os.arch"));
        System.out.println();

        float volume = getMasterVolume();
        if (volume >= 0) {
            System.out.printf("Master Volume: %.2f (%.0f%%)%n", volume, volume * 100);
        } else {
            System.out.println("Failed to get master volume");
            System.out.println("\nTroubleshooting:");
            System.out.println("1. Ensure you have an audio output device connected");
            System.out.println("2. Check Windows Sound Settings (right-click speaker icon)");
            System.out.println("3. Verify the default playback device is enabled");
        }

        int volumePercent = getMasterVolumePercent();
        if (volumePercent >= 0) {
            System.out.println("Master Volume Percent: " + volumePercent + "%");
        }

        Boolean muted = isMuted();
        if (muted != null) {
            System.out.println("Is Muted: " + muted);
        }
    }
}
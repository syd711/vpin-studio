package de.mephisto.vpin.commons;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.win32.W32APIOptions;

/**
 * Utility class for resolving Windows monitor DPI scaling
 */
public class WindowsDPIScaling {

  // Extended GDI32 interface with GetDeviceCaps
  private interface GDI32 extends com.sun.jna.Library {
    GDI32 INSTANCE = Native.load("gdi32", GDI32.class, W32APIOptions.DEFAULT_OPTIONS);
    int GetDeviceCaps(HDC hdc, int index);
  }

  // Constants
  private static final int LOGPIXELSX = 88;
  private static final int LOGPIXELSY = 90;
  private static final int DEFAULT_DPI = 96;

  /**
   * Get the current display scaling factor
   * @return scaling factor (e.g., 1.0, 1.25, 1.5, 2.0)
   */
  public static double getScalingFactor(HDC hdc) {
    try {
      int dpiX = GDI32.INSTANCE.GetDeviceCaps(hdc, LOGPIXELSX);
      return dpiX / (double) DEFAULT_DPI;
    } finally {
      User32.INSTANCE.ReleaseDC(null, hdc);
    }
  }
}
package de.mephisto.vpin.server.util;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.VPinStudioServer;
import de.mephisto.vpin.server.preferences.PreferencesService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;

public class SystemUtil {
  private final static Logger LOG = LoggerFactory.getLogger(SystemUtil.class);

  public static String getVersion() {
    try {
      final Properties properties = new Properties();
      InputStream resourceAsStream = VPinStudioServer.class.getClassLoader().getResourceAsStream("version.properties");
      properties.load(resourceAsStream);
      resourceAsStream.close();
      return properties.getProperty("vpin.studio.version");
    } catch (IOException e) {
      LOG.error("Failed to read version number: " + e.getMessage(), e);
    }
    return null;
  }

  /**
   * Extract from Preferences the installation mode
   * @param preferencesService The PreferencesService to extract SYSTEM_PRESET
   * @return true for 64bit, false otherwise
   */
  public static final boolean is64Bit(PreferencesService preferencesService) {
    String systemPreset = (String) preferencesService.getPreferenceValue(PreferenceNames.SYSTEM_PRESET);
    return (systemPreset == null || systemPreset.equals(PreferenceNames.SYSTEM_PRESET_64_BIT));
  }

  interface User32 extends StdCallLibrary {
    User32 INSTANCE = Native.loadLibrary("user32", User32.class);

    interface WNDENUMPROC extends StdCallCallback {
      boolean callback(Pointer hWnd, Pointer arg);
    }

    boolean EnumWindows(WNDENUMPROC lpEnumFunc, Pointer userData);

    int GetWindowTextA(Pointer hWnd, byte[] lpString, int nMaxCount);

    Pointer GetWindow(Pointer hWnd, int uCmd);
  }



  private static final ExecutorService scheduler = Executors.newSingleThreadExecutor();
  private static int threadId = 0;

  public static List<String> getAllWindowNames() {
    final List<String> windowNames = new ArrayList<>();
    final User32 user32 = User32.INSTANCE;

    Future<?> submit = scheduler.submit(new Runnable() {
      @Override
      public void run() {
        user32.EnumWindows(new User32.WNDENUMPROC() {

          @Override
          public boolean callback(Pointer hWnd, Pointer arg) {
            try {
              threadId++;
              Thread.currentThread().setName("All-Window-Names-Native-" + threadId);
              byte[] windowText = new byte[512];
              user32.GetWindowTextA(hWnd, windowText, 512);
              String wText = Native.toString(windowText).trim();
              if (!wText.isEmpty()) {
                windowNames.add(wText);
              }
            }
            catch (Exception e) {
              LOG.error("Failed to read window name: " + e.getMessage(), e);
            }
            return true;
          }
        }, null);
      }
    });
    try {
      submit.get(100, TimeUnit.MILLISECONDS);
    }
    catch (Exception e) {
      //ignore
    }
    return windowNames;
  }
}

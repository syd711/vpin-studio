package de.mephisto.vpin.server.system;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import org.junit.jupiter.api.Test;

public class SystemTest {

  public static interface User32 extends Library {
    User32 INSTANCE = (User32) Native.loadLibrary("user32",User32.class, W32APIOptions.DEFAULT_OPTIONS);
    boolean SystemParametersInfo (int one, int two, String s ,int three);
  }
  @Test
  public void testMaintenanceMode() {
//    User32.INSTANCE.SystemParametersInfo(0x0014, 0, "E:\\downloads\\background.jpg" , 1);
  }
}

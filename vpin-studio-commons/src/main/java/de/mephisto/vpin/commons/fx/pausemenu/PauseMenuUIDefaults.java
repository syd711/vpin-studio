package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.restclient.system.MonitorInfo;

public class PauseMenuUIDefaults {
  private static double screenX = 0;
  private static double screenWidth = 0;
  private static double screenHeight = 0;

  public static void init(MonitorInfo monitorInfo) {
    double scaling = monitorInfo.getScaling();
    if (scaling <= 0) {
      scaling = 1;
    }
    screenWidth = (int) (monitorInfo.getWidth() / scaling);
    screenHeight = (int) (monitorInfo.getHeight() / scaling);
    screenX = monitorInfo.getScaledX();
  }

  public static double getScaledScreenX() {
    return screenX;
  }

  public static double getScreenWidth() {
    return screenWidth;
  }

  public static double getScreenHeight() {
    return screenHeight;
  }

  public static double SELECTION_SCALE = 0.60;
  public static double SELECTION_SCALE_DEFAULT = -SELECTION_SCALE;
  public static int SELECTION_HEIGHT_OFFSET = 10;
  public static int SELECTION_SCALE_DURATION = 200;

  public static int THUMBNAIL_SIZE = 240;
  public static int SCROLL_OFFSET = 120;

  public static int MAX_REFRESH_COUNT = 10;
}

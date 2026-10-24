package de.mephisto.vpin.commons;

import de.mephisto.vpin.restclient.system.MonitorInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MonitorInfoUtilTest {

  @Test
  void assignDeviceNames_matchingIds_copiesDeviceNames() {
    List<MonitorInfo> gde = List.of(monitor(3, 0, 3840, 2160, null), monitor(2, 3840, 1920, 1080, null), monitor(1, 5760, 1920, 1080, null));
    List<MonitorInfo> jna = List.of(monitor(3, 0, 3840, 2160, "\\\\.\\DISPLAY2"), monitor(2, 3840, 1920, 1080, "\\\\.\\DISPLAY3"), monitor(1, 5760, 1920, 1080, "\\\\.\\DISPLAY1"));

    MonitorInfoUtil.assignDeviceNames(gde, jna);

    assertEquals("\\\\.\\DISPLAY2", gde.get(0).getDeviceName());
    assertEquals("\\\\.\\DISPLAY3", gde.get(1).getDeviceName());
    assertEquals("\\\\.\\DISPLAY1", gde.get(2).getDeviceName());
  }

  @Test
  void assignDeviceNames_differentResolution_keepsDeviceNameEmpty() {
    List<MonitorInfo> gde = List.of(monitor(1, 0, 1920, 1080, null));
    List<MonitorInfo> jna = List.of(monitor(1, 0, 2560, 1440, "\\\\.\\DISPLAY1"));

    MonitorInfoUtil.assignDeviceNames(gde, jna);

    assertNull(gde.get(0).getDeviceName());
  }

  private static MonitorInfo monitor(int id, double x, int width, int height, String deviceName) {
    MonitorInfo m = new MonitorInfo();
    m.setId(id);
    m.setX(x);
    m.setWidth(width);
    m.setHeight(height);
    m.setDeviceName(deviceName);
    return m;
  }
}

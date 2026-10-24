package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.restclient.directb2s.DirectB2sScreenRes;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import de.mephisto.vpin.server.directb2s.BackglassService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vpx.VPXService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class VPinScreenServiceTest {

  @Mock
  private VPXService vpxService;

  @Mock
  private BackglassService backglassService;

  @Mock
  private FrontendService frontendService;

  @Mock
  private SystemService systemService;

  @InjectMocks
  private VPinScreenService service;

  // ---- checkMonitors ----

  @Test
  void checkMonitors_singleMonitor_noErrors() {
    List<String> errors = new ArrayList<>();
    MonitorInfo monitor = monitor(0, 1920, 1080, true);

    service.checkMonitors(errors, Collections.singletonList(monitor));

    assertThat(errors).isEmpty();
  }

  @Test
  void checkMonitors_twoMonitorsAlignedY_noErrors() {
    List<String> errors = new ArrayList<>();
    MonitorInfo primary = monitor(0, 1920, 1080, true);
    MonitorInfo secondary = monitor(1920, 1920, 1080, false);

    when(systemService.getFrontendType()).thenReturn(FrontendType.PinballX);

    service.checkMonitors(errors, Arrays.asList(primary, secondary));

    assertThat(errors).isEmpty();
  }

  @Test
  void checkMonitors_twoMonitorsMisalignedY_addsError() {
    List<String> errors = new ArrayList<>();
    MonitorInfo primary = monitor(0, 1920, 1080, true);
    MonitorInfo secondary = monitorWithY(1920, 100, 1920, 1080, false);

    when(systemService.getFrontendType()).thenReturn(FrontendType.PinballX);

    service.checkMonitors(errors, Arrays.asList(primary, secondary));

    assertThat(errors).hasSize(1);
    assertThat(errors.get(0)).contains("not aligned on top");
  }

  @Test
  void checkMonitors_popperFrontend_monitorLeftOfPrimary_addsError() {
    List<String> errors = new ArrayList<>();
    MonitorInfo leftMonitor = monitor(-1920, 1920, 1080, false);
    MonitorInfo primary = monitor(0, 1920, 1080, true);

    when(systemService.getFrontendType()).thenReturn(FrontendType.Popper);

    service.checkMonitors(errors, Arrays.asList(leftMonitor, primary));

    assertThat(errors).hasSize(1);
    assertThat(errors.get(0)).contains("left to the primary one");
  }

  @Test
  void checkMonitors_popperFrontend_primaryFirst_noPositionError() {
    List<String> errors = new ArrayList<>();
    MonitorInfo primary = monitor(0, 1920, 1080, true);
    MonitorInfo secondary = monitor(1920, 1920, 1080, false);

    when(systemService.getFrontendType()).thenReturn(FrontendType.Popper);

    service.checkMonitors(errors, Arrays.asList(primary, secondary));

    // no "left to the primary" errors, though y-alignment is checked
    long positionErrors = errors.stream().filter(e -> e.contains("left to the primary")).count();
    assertThat(positionErrors).isEqualTo(0);
  }

  // ---- checkDisplays ----

  @Test
  void checkDisplays_matchingDisplays_noErrors() {
    List<String> errors = new ArrayList<>();

    FrontendPlayerDisplay vpxPlayfield = display(VPinScreen.PlayField, 0, 0, 1920, 1080);
    FrontendPlayerDisplay vpxBackglass = display(VPinScreen.BackGlass, 1920, 0, 1024, 768);

    FrontendPlayerDisplay screenResPlayfield = display(VPinScreen.PlayField, 0, 0, 1920, 1080);
    FrontendPlayerDisplay screenResBackglass = display(VPinScreen.BackGlass, 1920, 0, 1024, 768);

    FrontendPlayerDisplay frontendPlayfield = display(VPinScreen.PlayField, 0, 0, 1920, 1080);
    FrontendPlayerDisplay frontendBackglass = display(VPinScreen.BackGlass, 1920, 0, 1024, 768);
    FrontendPlayerDisplay frontendFullDmd = display(VPinScreen.Menu, 1920, 600, 1024, 168);

    when(frontendService.getFrontendName()).thenReturn("TestFrontend");

    service.checkDisplays(errors,
        Arrays.asList(vpxPlayfield, vpxBackglass),
        Arrays.asList(screenResPlayfield, screenResBackglass, frontendFullDmd),
        Arrays.asList(frontendPlayfield, frontendBackglass, frontendFullDmd));

    assertThat(errors).isEmpty();
  }

  @Test
  void checkDisplays_playfieldXMismatch_addsError() {
    List<String> errors = new ArrayList<>();

    FrontendPlayerDisplay vpxPlayfield = display(VPinScreen.PlayField, 0, 0, 1920, 1080);
    FrontendPlayerDisplay frontendPlayfield = display(VPinScreen.PlayField, 10, 0, 1920, 1080);

    when(frontendService.getFrontendName()).thenReturn("TestFrontend");

    service.checkDisplays(errors,
        Collections.singletonList(vpxPlayfield),
        Collections.emptyList(),
        Collections.singletonList(frontendPlayfield));

    assertThat(errors).anyMatch(e -> e.contains("x position") && e.contains("mismatch"));
  }

  @Test
  void checkDisplays_backglassHeightMismatch_addsError() {
    List<String> errors = new ArrayList<>();

    FrontendPlayerDisplay screenResBackglass = display(VPinScreen.BackGlass, 1920, 0, 1024, 768);
    FrontendPlayerDisplay frontendBackglass = display(VPinScreen.BackGlass, 1920, 0, 1024, 800);

    when(frontendService.getFrontendName()).thenReturn("TestFrontend");

    service.checkDisplays(errors,
        Collections.emptyList(),
        Collections.singletonList(screenResBackglass),
        Collections.singletonList(frontendBackglass));

    assertThat(errors).anyMatch(e -> e.contains("height") && e.contains("mismatch"));
  }

  @Test
  void checkDisplays_noDisplayData_noErrors() {
    List<String> errors = new ArrayList<>();

    when(frontendService.getFrontendName()).thenReturn("TestFrontend");

    service.checkDisplays(errors,
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList());

    assertThat(errors).isEmpty();
  }

  // ---- getBackglassMonitor / getScreenResDisplays ----

  /**
   * Enumeration order (= id) differs from the Windows display number:
   * \\.\DISPLAY2 (playfield, primary) at 0, \\.\DISPLAY3 at 3840, \\.\DISPLAY1 at 5760
   */
  private static List<MonitorInfo> enumeratedMonitors() {
    return Arrays.asList(
        osMonitor(3, "\\\\.\\DISPLAY2", 0, 3840, 2160, true),
        osMonitor(2, "\\\\.\\DISPLAY3", 3840, 1920, 1080, false),
        osMonitor(1, "\\\\.\\DISPLAY1", 5760, 1920, 1080, false));
  }

  @Test
  void getBackglassMonitor_displayNumber_matchesWindowsDeviceName() {
    MonitorInfo monitor = VPinScreenService.getBackglassMonitor(screenRes("3", 0, 0), enumeratedMonitors());

    assertThat(monitor).isNotNull();
    assertThat(monitor.getDeviceName()).isEqualTo("\\\\.\\DISPLAY3");
    assertThat(monitor.getX()).isEqualTo(3840);
  }

  @Test
  void getBackglassMonitor_displayNumberWithoutDeviceNames_fallsBackToId() {
    List<MonitorInfo> monitors = enumeratedMonitors();
    monitors.forEach(m -> m.setDeviceName(null));

    MonitorInfo monitor = VPinScreenService.getBackglassMonitor(screenRes("2", 0, 0), monitors);

    assertThat(monitor).isNotNull();
    assertThat(monitor.getId()).isEqualTo(2);
  }

  @Test
  void getBackglassMonitor_unknownDisplayNumber_fallsBackToId() {
    List<MonitorInfo> monitors = Arrays.asList(
        osMonitor(1, "\\\\.\\DISPLAY5", 0, 1920, 1080, true),
        osMonitor(2, "\\\\.\\DISPLAY6", 1920, 1920, 1080, false));

    MonitorInfo monitor = VPinScreenService.getBackglassMonitor(screenRes("2", 0, 0), monitors);

    assertThat(monitor).isNotNull();
    assertThat(monitor.getX()).isEqualTo(1920);
  }

  @Test
  void getBackglassMonitor_screenIndex_usesPositionOrder() {
    MonitorInfo monitor = VPinScreenService.getBackglassMonitor(screenRes("=3", 0, 0), enumeratedMonitors());

    assertThat(monitor).isNotNull();
    assertThat(monitor.getX()).isEqualTo(5760);
  }

  @Test
  void getBackglassMonitor_screenCoordinates_matchesX() {
    MonitorInfo monitor = VPinScreenService.getBackglassMonitor(screenRes("@3840", 0, 0), enumeratedMonitors());

    assertThat(monitor).isNotNull();
    assertThat(monitor.getDeviceName()).isEqualTo("\\\\.\\DISPLAY3");
  }

  @Test
  void getScreenResDisplays_backglassAndDmdRelativeToBackglassMonitor() {
    DirectB2sScreenRes screenres = screenRes("3", 1920, 0);
    when(backglassService.getGlobalScreenRes()).thenReturn(screenres);
    when(systemService.getMonitorInfos()).thenReturn(enumeratedMonitors());

    List<FrontendPlayerDisplay> displays = service.getScreenResDisplays();

    FrontendPlayerDisplay backglass = FrontendPlayerDisplay.valueOfScreen(displays, VPinScreen.BackGlass);
    assertThat(backglass.getX()).isEqualTo(3840);
    assertThat(backglass.getY()).isEqualTo(0);

    FrontendPlayerDisplay fullDmd = FrontendPlayerDisplay.valueOfScreen(displays, VPinScreen.Menu);
    assertThat(fullDmd.getX()).isEqualTo(5760);
    assertThat(fullDmd.getY()).isEqualTo(0);
  }

  // ---- helpers ----

  private static MonitorInfo osMonitor(int id, String deviceName, double x, int width, int height, boolean primary) {
    MonitorInfo m = monitor(x, width, height, primary);
    m.setId(id);
    m.setDeviceName(deviceName);
    return m;
  }

  private static DirectB2sScreenRes screenRes(String backglassDisplay, int dmdX, int dmdY) {
    DirectB2sScreenRes res = new DirectB2sScreenRes();
    res.setVersion("");
    res.setPlayfieldWidth(3840);
    res.setPlayfieldHeight(2160);
    res.setBackglassDisplay(backglassDisplay);
    res.setBackglassWidth(1920);
    res.setBackglassHeight(1080);
    res.setBackglassX(0);
    res.setBackglassY(0);
    res.setDmdWidth(1920);
    res.setDmdHeight(1080);
    res.setDmdX(dmdX);
    res.setDmdY(dmdY);
    return res;
  }

  private static MonitorInfo monitor(double x, int width, int height, boolean primary) {
    return monitorWithY(x, 0, width, height, primary);
  }

  private static MonitorInfo monitorWithY(double x, double y, int width, int height, boolean primary) {
    MonitorInfo m = new MonitorInfo();
    m.setX(x);
    m.setY(y);
    m.setWidth(width);
    m.setHeight(height);
    m.setPrimary(primary);
    m.setName("\\\\.\\DISPLAY1");
    return m;
  }

  private static FrontendPlayerDisplay display(VPinScreen screen, int x, int y, int width, int height) {
    FrontendPlayerDisplay d = new FrontendPlayerDisplay(screen);
    d.setX(x);
    d.setY(y);
    d.setWidth(width);
    d.setHeight(height);
    return d;
  }
}

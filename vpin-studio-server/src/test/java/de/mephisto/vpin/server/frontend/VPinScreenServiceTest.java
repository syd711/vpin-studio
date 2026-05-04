package de.mephisto.vpin.server.frontend;

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

  // ---- helpers ----

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

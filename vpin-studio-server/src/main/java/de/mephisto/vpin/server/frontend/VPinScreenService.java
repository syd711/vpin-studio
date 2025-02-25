package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.restclient.directb2s.DirectB2sScreenRes;
import de.mephisto.vpin.restclient.frontend.FrontendScreenSummary;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.server.directb2s.BackglassService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vpx.VPXService;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Add a uniform way to access to screen dimensions, abstracting the source of the information
 * <p>
 * playfield : %appdata%/vpinballX/VpinballX.ini, can be fullscreen or windowed with specific dimensions
 * backglass: screenres.txt or %appdata%/vpinballX/VpinballX.ini whenstudio will support standalone mode
 * dmd : from freezy or registry
 * full dmd: screenres.txt if B2SDMD activated else existing frontend service
 * all others (Topper, help, ...) from FrontendService
 */
@Service
public class VPinScreenService {

  private final static Logger LOG = LoggerFactory.getLogger(VPinScreenService.class);

  @Autowired
  private VPXService vpxService;

  @Autowired
  private BackglassService backglassService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private SystemService systemService;

  //---------------------------------------------------

  public FrontendPlayerDisplay getScreenDisplay(VPinScreen screen) {
    switch (screen) {
      case PlayField:
        return firstDefined(screen, getVpxDisplays(), getFrontendDisplays(false));
      case BackGlass:
        return firstDefined(screen, getVpxDisplays(), getScreenResDisplays(), getFrontendDisplays(false));
      case Menu:
        return firstDefined(screen, getVpxDisplays(), getScreenResDisplays(), getFrontendDisplays(false));
      default:
        return firstDefined(screen, getFrontendDisplays(false));
    }
  }

  private FrontendPlayerDisplay firstDefined(VPinScreen screen,
                                             List<FrontendPlayerDisplay> displays1) {
    return firstDefined(screen, displays1, null, null);
  }

  private FrontendPlayerDisplay firstDefined(VPinScreen screen,
                                             List<FrontendPlayerDisplay> displays1, List<FrontendPlayerDisplay> displays2) {
    return firstDefined(screen, displays1, displays2, null);
  }

  private FrontendPlayerDisplay firstDefined(VPinScreen screen,
                                             List<FrontendPlayerDisplay> displays1, List<FrontendPlayerDisplay> displays2, List<FrontendPlayerDisplay> displays3) {
    if (displays1 != null) {
      FrontendPlayerDisplay display1 = FrontendPlayerDisplay.valueOfScreen(displays1, screen);
      return display1 != null ? display1 : firstDefined(screen, displays2, displays3, null);
    }
    return null;
  }

  //---------------------------------------------------

  public List<String> checkDisplays() {
    List<String> errors = new ArrayList<>();

    List<MonitorInfo> monitors = systemService.getMonitorInfos();
    checkMonitors(errors, monitors);

    checkDisplays(errors, getVpxDisplays(), getScreenResDisplays(), getFrontendDisplays(true));
    return errors;
  }

  public void checkMonitors(List<String> errors, List<MonitorInfo> monitors) {
    if (monitors.size() > 1) {
      MonitorInfo monitor = monitors.get(0);
      for (int i = 1; i < monitors.size(); i++) {
        MonitorInfo second = monitors.get(i);
        if (monitor.getY() !=  second.getY()) {
          errors.add("Monitor " + (i + 1) + ", named " + monitor.getName() + "is not aligned on top with first one");
        }
      }
    }
  }

  public void checkDisplays(List<String> errors, List<FrontendPlayerDisplay> vpxDisplays,
      List<FrontendPlayerDisplay> screenresDisplays, List<FrontendPlayerDisplay> frontendDisplays) {

    String frontend = frontendService.getFrontendName();

    compare(errors, VPinScreen.PlayField, vpxDisplays, "VPinballX.ini", screenresDisplays, "screenres.txt");
    compare(errors, VPinScreen.PlayField, vpxDisplays, "VPinballX.ini", frontendDisplays, frontend);
    compare(errors, VPinScreen.BackGlass, screenresDisplays, "screenres.txt", frontendDisplays, frontend);
    compare(errors, VPinScreen.Menu, screenresDisplays, "screenres.txt", frontendDisplays, frontend);
  }

  private void compare(List<String> errors, VPinScreen screen,
                       List<FrontendPlayerDisplay> displays1, String name1,
                       List<FrontendPlayerDisplay> displays2, String name2) {
    comparePositions(errors, screen, displays1, name1, displays2, name2);
    compareDimensions(errors, screen, displays1, name1, displays2, name2);
  }

  private void comparePositions(List<String> errors, VPinScreen screen,
                                List<FrontendPlayerDisplay> displays1, String name1,
                                List<FrontendPlayerDisplay> displays2, String name2) {

    FrontendPlayerDisplay display1 = FrontendPlayerDisplay.valueOfScreen(displays1, screen);
    FrontendPlayerDisplay display2 = FrontendPlayerDisplay.valueOfScreen(displays2, screen);
    if (display1 != null && display2 != null) {
      if (display1.getX() != display2.getX()) {
        errors.add(screen.name() + " x position in " + name1 + " mismatch with x position defined in " + name2 + ": " +
            display1.getX() + " vs " + display2.getX());
      }
      if (display1.getY() != display2.getY()) {
        errors.add(screen.name() + " y position in " + name1 + " mismatch with y position defined in " + name2 + ": " +
            display1.getY() + " vs " + display2.getY());
      }
    }
  }

  private void compareDimensions(List<String> errors, VPinScreen screen,
                                 List<FrontendPlayerDisplay> displays1, String name1,
                                 List<FrontendPlayerDisplay> displays2, String name2) {

    FrontendPlayerDisplay display1 = FrontendPlayerDisplay.valueOfScreen(displays1, screen);
    FrontendPlayerDisplay display2 = FrontendPlayerDisplay.valueOfScreen(displays2, screen);
    if (display1 != null && display2 != null) {
      if (display1.getWidth() != display2.getWidth()) {
        errors.add(screen.name() + " width in " + name1 + " mismatch with width defined in " + name2 + ": " +
            display1.getWidth() + " vs " + display2.getWidth());
      }
      if (display1.getHeight() != display2.getHeight()) {
        errors.add(screen.name() + " height in " + name1 + " mismatch with height defined in " + name2 + ": " +
            display1.getHeight() + " vs " + display2.getHeight());
      }
    }
  }

  public FrontendScreenSummary getScreenSummary() {

    List<MonitorInfo> monitors = systemService.getMonitorInfos();

    FrontendScreenSummary summary = new FrontendScreenSummary();
    summary.setScreenResDisplays(addMonitorInfo(getScreenResDisplays()));
    //we do not want the cached version here
    //do not add monitorInfo as it i salready done  by the connector
    summary.setFrontendDisplays(getFrontendDisplays(true));
    summary.setVpxDisplaysDisplays(addMonitorInfo(getVpxDisplays()));

    List<String> errors = new ArrayList<>();
    checkMonitors(errors, monitors);
    checkDisplays(errors, summary.getVpxDisplaysDisplays(), summary.getScreenResDisplays(), summary.getFrontendDisplays());
    summary.setErrors(errors);
    return summary;
  }

  public List<FrontendPlayerDisplay> addMonitorInfo(List<FrontendPlayerDisplay> displays) {
    List<MonitorInfo> monitors = systemService.getMonitorInfos();
    for (FrontendPlayerDisplay display : displays) {
      MonitorInfo monitor = null;
      for (MonitorInfo m : monitors) {
        if (monitor == null || m.getName().endsWith(Integer.toString(display.getMonitor()))) {
          monitor = m;
        }
      }
      if (monitor != null) {
        display.setX((int) monitor.getX() + display.getX());
        display.setY((int) monitor.getY() + display.getY());

        // full screen indicator
        if (display.getWidth() < 0) {
          display.setWidth(monitor.getWidth());
        }
        // full screen indicator
        if (display.getHeight() < 0) {
          display.setHeight(monitor.getHeight());
        }
      }
    }
    return displays;
  }
  
  //------------------------------------------------------ VPINBALLX.INI ---

  /**
   * Reads the VPinballX.ini file and creates a list of displays
   * Supported screen is Playfield but should be extended for standalone and get Backglass, B2SDMD and DMD positions
   *
   * @return a List of FrontendPlayerDisplay
   */
  public List<FrontendPlayerDisplay> getVpxDisplays() {
    List<FrontendPlayerDisplay> displayList = new ArrayList<>();

    Configuration vpxConfiguration = vpxService.getPlayerConfiguration();
    if (vpxConfiguration != null && !vpxConfiguration.isEmpty()) {
      createVpxPlayfieldDisplay(vpxConfiguration, displayList);
      //createDisplay(iniConfiguration, displayList, "BackGlass", VPinScreen.BackGlass, true);
      //createDisplay(iniConfiguration, displayList, "DMD", VPinScreen.DMD, false);
    }
    else {
      LOG.warn("Unable to create displays from VPinball.ini");
    }
    return displayList;
  }

  /**
   * Display = 2
   * FullScreen = 0
   * WindowPosX = -1900
   * WindowPosY = 30
   * Width = 1500
   * Height = 900
   */
  private void createVpxPlayfieldDisplay(Configuration vpxConfiguration, List<FrontendPlayerDisplay> players) {
    FrontendPlayerDisplay player = new FrontendPlayerDisplay(VPinScreen.PlayField);
    int monitor = safeGetInteger(vpxConfiguration, "Display", 0);
    player.setMonitor(monitor);
    player.setRotation(safeGetInteger(vpxConfiguration, "Rotate", 0));
    player.setInverted(true);

    int fullscreened = safeGetInteger(vpxConfiguration, "FullScreen", 1);
    if (fullscreened == 0) {
      player.setX(safeGetInteger(vpxConfiguration, "WindowPosX", 0));
      player.setY(safeGetInteger(vpxConfiguration, "WindowPosY", 0));
      player.setWidth(safeGetInteger(vpxConfiguration, "Width", 0));
      player.setHeight(safeGetInteger(vpxConfiguration, "Height", 0));
    }
    else {
      player.setX(0);
      player.setY(0);
      player.setWidth(-1);
      player.setHeight(-1);
    }
    LOG.info("Created vPinballX player display {}", player);

    players.add(player);
  }

  private int safeGetInteger(Configuration configuration, String key, int defaultValue) {
    String value = configuration.getString(key, null);
    return StringUtils.isNotBlank(value) ? Integer.parseInt(value) : defaultValue;
  }

  /*
   COPIED FROM FRONTENDCONNECTORS TO BE ADAPTED

  private void createDisplay(INIConfiguration iniConfiguration, List<FrontendPlayerDisplay> players, String sectionName, VPinScreen screen, boolean defaultEnabled) {
    SubnodeConfiguration display = iniConfiguration.getSection(sectionName);
    if (!display.isEmpty()) {
      boolean enabled = display.getBoolean("Enabled", defaultEnabled);

      int monitor = Integer.parseInt(display.getString("Monitor", display.getString("monitor", "0")));
      GraphicsDevice[] gds = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();

      if (enabled && monitor < gds.length) {
        Rectangle bounds = gds[monitor].getDefaultConfiguration().getBounds();
        int mX = (int) bounds.getX();
        int mY = (int) bounds.getY();

        FrontendPlayerDisplay player = new FrontendPlayerDisplay();
        player.setName(sectionName);
        player.setScreen(screen);
        player.setMonitor(monitor);
        player.setX(mX + Integer.parseInt(display.getString("x", "0")));
        player.setY(mY + Integer.parseInt(display.getString("y", "0")));
        player.setWidth(Integer.parseInt(display.getString("width", "0")));
        player.setHeight(Integer.parseInt(display.getString("height", "0")));

        LOG.info("Created player display {}", player);
        players.add(player);
      }
    }
  }
  */

  //----------------------------------------------------------------- SCREENRES.TXT ---

  public List<FrontendPlayerDisplay> getScreenResDisplays(Game game, boolean absoluteCoordinate) {
    DirectB2sScreenRes screenres = backglassService.getScreenRes(game, false);
    if (absoluteCoordinate) {
      addDeviceOffsets(screenres);
    }
    return screenResToDisplays(screenres);
  }

  public List<FrontendPlayerDisplay> getScreenResDisplays() {
    DirectB2sScreenRes screenres = backglassService.getGlobalScreenRes();
    return screenResToDisplays(screenres);
  }

  private List<FrontendPlayerDisplay> screenResToDisplays(DirectB2sScreenRes screenres) {
    List<FrontendPlayerDisplay> displays = new ArrayList<>();
    if (screenres != null) {
      FrontendPlayerDisplay playfield = new FrontendPlayerDisplay(VPinScreen.PlayField);
      playfield.setWidth(screenres.getPlayfieldWidth());
      playfield.setHeight(screenres.getPlayfieldHeight());
      displays.add(playfield);

      FrontendPlayerDisplay backglass = new FrontendPlayerDisplay(VPinScreen.BackGlass);
      backglass.setMonitor(Integer.parseInt(screenres.getBackglassDisplay()));
      backglass.setX(screenres.getBackglassX());
      backglass.setY(screenres.getBackglassY());
      backglass.setWidth(screenres.getBackglassWidth());
      backglass.setHeight(screenres.getBackglassHeight());
      displays.add(backglass);

      if (screenres.hasDMD()) {
        FrontendPlayerDisplay fulldmd = new FrontendPlayerDisplay(VPinScreen.DMD);
        // DMD is relative to backglass so use same monitor
        fulldmd.setMonitor(Integer.parseInt(screenres.getBackglassDisplay()));
        fulldmd.setX(screenres.getBackglassX() + screenres.getDmdX());
        fulldmd.setY(screenres.getBackglassY() + screenres.getDmdY());
        fulldmd.setWidth(screenres.getDmdWidth());
        fulldmd.setHeight(screenres.getDmdHeight());
        displays.add(fulldmd);
      }
    }
    return displays;
  }

  public void addDeviceOffsets(DirectB2sScreenRes screenres) {
    List<MonitorInfo> monitors = systemService.getMonitorInfos();

    MonitorInfo monitor = null;

    // screen number (\\.\DISPLAY)x or screen coordinates (@x) or screen index (=x)
    String backglassDisplay = screenres.getBackglassDisplay();
    if (backglassDisplay.startsWith("@")) {
      int xPos = Integer.parseInt(backglassDisplay.substring(1));
      for (MonitorInfo m : monitors) {
        if (m.getX() == xPos) {
          monitor = m;
        }
      }
    }
    else if (backglassDisplay.startsWith("=")) {
      int idx = Integer.parseInt(backglassDisplay.substring(1)) - 1;
      monitor = idx < monitors.size() ? monitors.get(idx) : null;
    }
    else {
      for (MonitorInfo m : monitors) {
        if (m.getName().endsWith(backglassDisplay)) {
          monitor = m;
        }
      }
    }

    if (monitor != null) {
      screenres.setBackglassDisplayX((int) monitor.getX());
      screenres.setBackglassDisplayY((int) monitor.getY());
    }
  }

  //----------------------------------------------------------------- FRONTEND ---

  private List<FrontendPlayerDisplay> getFrontendDisplays(boolean forceReload) {
    if (forceReload) {
      return frontendService.getFrontendConnector().getFrontendPlayerDisplays();
    }
    else {
      return frontendService.getFrontendPlayerDisplays();
    }
  }
}
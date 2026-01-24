package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.commons.MonitorInfoUtil;
import de.mephisto.vpin.restclient.directb2s.DirectB2sScreenRes;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.FrontendScreenSummary;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import de.mephisto.vpin.server.directb2s.BackglassService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vpx.VPXService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.mephisto.vpin.server.directb2s.BackglassService.parseIntSafe;


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
public class VPinScreenService implements InitializingBean {

  private final static Logger LOG = LoggerFactory.getLogger(VPinScreenService.class);

  @Autowired
  private VPXService vpxService;

  @Autowired
  private BackglassService backglassService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private SystemService systemService;

  /**
   * Wether to read the VPX playfield data or not
   */
  private final static boolean USE_VPX_PLAYFIELD = false;

  //---------------------------------------------------

  public FrontendPlayerDisplay getScreenDisplay(VPinScreen screen) {
    switch (screen) {
      case PlayField:
        return firstDefined(screen, getVpxDisplays(false), getFrontendDisplays(false));
      case BackGlass:
        return firstDefined(screen, getVpxDisplays(false), getScreenResDisplays(), getFrontendDisplays(false));
      case Menu:
        return firstDefined(screen, getVpxDisplays(false), getScreenResDisplays(), getFrontendDisplays(false));
      default:
        return firstDefined(screen, getFrontendDisplays(false));
    }
  }

  public FrontendPlayerDisplay getRecordingScreenDisplay(VPinScreen screen) {
    return firstDefined(screen, Collections.emptyList(), getFrontendDisplays(false));
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

    checkDisplays(errors, getVpxDisplays(true), getScreenResDisplays(), getFrontendDisplays(true));
    return errors;
  }

  protected void checkMonitors(List<String> errors, List<MonitorInfo> monitors) {
    if (monitors.size() > 1) {

      //for pinup popper, all monitors left to the primary are irrelevant
      if (systemService.getFrontendType().equals(FrontendType.Popper)) {
        boolean primaryFound = false;
        int i = 0;
        for (MonitorInfo monitor : monitors) {
          if (!monitor.isPrimary() && !primaryFound) {
            errors.add("Monitor " + (i + 1) + ", named " + monitor.getFormattedName() + " is left to the primary one, this is not supported in Popper");
          }
          primaryFound |= monitor.isPrimary();
        }
      }

      MonitorInfo monitor = monitors.get(0);
      for (int i = 1; i < monitors.size(); i++) {
        MonitorInfo second = monitors.get(i);
        if (monitor.getY() != second.getY()) {
          errors.add("Monitor " + (i + 1) + ", named " + second.getFormattedName() + " is not aligned on top with first one: " + monitor.getY() + "px vs. " + second.getY() + "px.");
        }
      }
    }
  }

  protected void checkDisplays(List<String> errors, List<FrontendPlayerDisplay> vpxDisplays,
                               List<FrontendPlayerDisplay> screenresDisplays, List<FrontendPlayerDisplay> frontendDisplays) {
    String frontend = frontendService.getFrontendName();

    // VPX is master when defining playfield dimensions, so is the main point of comparison
    // screenres does not contain display number nor x/X so just compare size
    compareDimensions(errors, VPinScreen.PlayField, null, vpxDisplays, "VPinballX.ini", screenresDisplays, "screenres.txt");
    compare(errors, VPinScreen.PlayField, null, vpxDisplays, "VPinballX.ini", frontendDisplays, frontend);

    // screenres / B2S is master for Backglass and fullDmd
    compare(errors, VPinScreen.BackGlass, null, screenresDisplays, "screenres.txt", frontendDisplays, frontend);
    compare(errors, VPinScreen.Menu, "FullDMD", screenresDisplays, "screenres.txt", frontendDisplays, frontend);
  }

  private void compare(List<String> errors, VPinScreen screen, String name,
                       List<FrontendPlayerDisplay> displays1, String name1,
                       List<FrontendPlayerDisplay> displays2, String name2) {
    comparePositions(errors, screen, name, displays1, name1, displays2, name2);
    compareDimensions(errors, screen, name, displays1, name1, displays2, name2);
  }

  private void comparePositions(List<String> errors, VPinScreen screen, String name,
                                List<FrontendPlayerDisplay> displays1, String name1,
                                List<FrontendPlayerDisplay> displays2, String name2) {

    FrontendPlayerDisplay display1 = FrontendPlayerDisplay.valueOfScreen(displays1, screen);
    FrontendPlayerDisplay display2 = FrontendPlayerDisplay.valueOfScreen(displays2, screen);
    if (display1 != null && display2 != null) {
      if (display1.getX() != display2.getX()) {
        errors.add(StringUtils.defaultString(name, screen.name()) + " x position in " + name1 + " mismatch with x position defined in " + name2 + ": " +
            display1.getX() + " vs " + display2.getX());
      }
      if (display1.getY() != display2.getY()) {
        errors.add(StringUtils.defaultString(name, screen.name()) + " y position in " + name1 + " mismatch with y position defined in " + name2 + ": " +
            display1.getY() + " vs " + display2.getY());
      }
    }
  }

  private void compareDimensions(List<String> errors, VPinScreen screen, String name,
                                 List<FrontendPlayerDisplay> displays1, String name1,
                                 List<FrontendPlayerDisplay> displays2, String name2) {

    FrontendPlayerDisplay display1 = FrontendPlayerDisplay.valueOfScreen(displays1, screen);
    FrontendPlayerDisplay display2 = FrontendPlayerDisplay.valueOfScreen(displays2, screen);
    if (display1 != null && display2 != null) {
      if (display1.getWidth() != display2.getWidth()) {
        errors.add(StringUtils.defaultString(name, screen.name()) + " width in " + name1 + " mismatch with width defined in " + name2 + ": " +
            display1.getWidth() + " vs " + display2.getWidth());
      }
      if (display1.getHeight() != display2.getHeight()) {
        errors.add(StringUtils.defaultString(name, screen.name()) + " height in " + name1 + " mismatch with height defined in " + name2 + ": " +
            display1.getHeight() + " vs " + display2.getHeight());
      }
    }
  }

  public FrontendScreenSummary getScreenSummary() {
    List<MonitorInfo> monitors = systemService.getMonitorInfos();

    FrontendScreenSummary summary = new FrontendScreenSummary();
    summary.setScreenResDisplays(getScreenResDisplays());
    //we do not want the cached version here
    //do not add monitorInfo as it is already done  by the connector
    summary.setFrontendDisplays(getFrontendDisplays(true));
    summary.setVpxDisplaysDisplays(getVpxDisplays(true));

    List<String> errors = new ArrayList<>();
    checkMonitors(errors, monitors);
    checkDisplays(errors, summary.getVpxDisplaysDisplays(), summary.getScreenResDisplays(), summary.getFrontendDisplays());
    summary.setErrors(errors);
    return summary;
  }

  //------------------------------------------------------ VPINBALLX.INI ---

  /**
   * Reads the VPinballX.ini file and creates a list of displays
   * Supported screen is Playfield but should be extended for standalone and get Backglass, B2SDMD and DMD positions
   *
   * @return a List of FrontendPlayerDisplay
   */
  protected List<FrontendPlayerDisplay> getVpxDisplays(boolean forceReload) {
    List<FrontendPlayerDisplay> displayList = new ArrayList<>();

    if (USE_VPX_PLAYFIELD) {
      Configuration vpxConfiguration = vpxService.getPlayerConfiguration(forceReload);
      if (vpxConfiguration != null && !vpxConfiguration.isEmpty()) {
        createVpxPlayfieldDisplay(vpxConfiguration, displayList);

        //createDisplay(iniConfiguration, displayList, "BackGlass", VPinScreen.BackGlass, true);
        //createDisplay(iniConfiguration, displayList, "DMD", VPinScreen.DMD, false);
      }
      else {
        LOG.warn("Unable to create displays from VPinball.ini");
      }
      _addMonitorInfo(displayList);
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
    try {
      FrontendPlayerDisplay player = new FrontendPlayerDisplay(VPinScreen.PlayField);

      // the windows index, but how to match it with MonitorInfoUtils.getMonitor(), this is still unknows
      int monitor = safeGetInteger(vpxConfiguration, "Display", 0);
      MonitorInfo monitorInfo = systemService.getMonitorFromOS(monitor);

      player.setMonitor(monitorInfo.getId());
      player.setRotation(safeGetInteger(vpxConfiguration, "Rotate", 0));
      player.setInverted(true);


      int fullscreened = safeGetInteger(vpxConfiguration, "FullScreen", 1);
      if (fullscreened == 0) {
        player.setX(safeGetInteger(vpxConfiguration, "WindowPosX", 0));
        player.setY(safeGetInteger(vpxConfiguration, "WindowPosY", 0));
        player.setWidth(safeGetIntegerLargerNull(vpxConfiguration, "Width", monitorInfo.getWidth()));
        player.setHeight(safeGetIntegerLargerNull(vpxConfiguration, "Height", monitorInfo.getHeight()));

        //center window
        if (player.getX() == 0 && player.getY() == 0) {
          player.setX(monitorInfo.getWidth() / 2 - player.getWidth() / 2);
          player.setY(monitorInfo.getHeight() / 2 - player.getHeight() / 2);
        }
      }
      else {
        player.setX(0);
        player.setY(0);
        player.setWidth(monitorInfo.getWidth());
        player.setHeight(monitorInfo.getHeight());
      }
      LOG.info("Created vPinballX player display {}", player);

      players.add(player);
    }
    catch (Exception e) {
      LOG.error("Failed to resolve VPX playfield display info: {}", e.getMessage(), e);
    }
  }

  private int safeGetInteger(Configuration configuration, String key, int defaultValue) {
    String value = configuration.getString(key, null);
    String formattedValue = value != null ? value.replaceAll("\"", "") : null;
    try {
      return StringUtils.isNotBlank(formattedValue) ? parseIntSafe(formattedValue) : defaultValue;
    }
    catch (NumberFormatException e) {
      LOG.warn("Invalid number read from VPinballX.ini file. Unable to parse " + value + " to a valid integer number, assuming '" + defaultValue + "' instead.");
    }
    return defaultValue;
  }


  private int safeGetIntegerLargerNull(Configuration configuration, String key, int defaultValue) {
    int i = safeGetInteger(configuration, key, defaultValue);
    if (i > 0) {
      return i;
    }
    return defaultValue;
  }

  public List<FrontendPlayerDisplay> _addMonitorInfo(List<FrontendPlayerDisplay> displays) {
    List<MonitorInfo> monitors = systemService.getMonitorInfos();
    for (FrontendPlayerDisplay display : displays) {
      MonitorInfo monitor = null;
      for (MonitorInfo m : monitors) {
        // tbd match by windows number, but don't know how to get it yet
        // see 
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

  public List<FrontendPlayerDisplay> getScreenResDisplays(@Nullable Game game) {
    DirectB2sScreenRes screenres = backglassService.getScreenRes(game, false);
    return screenResToDisplays(screenres);
  }

  public List<FrontendPlayerDisplay> getScreenResDisplays() {
    DirectB2sScreenRes screenres = backglassService.getGlobalScreenRes();
    return screenResToDisplays(screenres);
  }

  private List<FrontendPlayerDisplay> screenResToDisplays(@Nullable DirectB2sScreenRes screenres) {
    List<FrontendPlayerDisplay> displays = new ArrayList<>();
    List<MonitorInfo> monitors = systemService.getMonitorInfos();

    if (screenres != null && monitors.size() > 0) {
      MonitorInfo monitor = getBackglassMonitor(screenres, monitors);

      FrontendPlayerDisplay playfield = new FrontendPlayerDisplay(VPinScreen.PlayField);
      MonitorInfo firstMonitor = monitors.get(0);
      playfield.setX((int) firstMonitor.getX());
      playfield.setY((int) firstMonitor.getY());
      playfield.setWidth(screenres.getPlayfieldWidth());
      playfield.setHeight(screenres.getPlayfieldHeight());
      displays.add(playfield);

      FrontendPlayerDisplay backglass = new FrontendPlayerDisplay(VPinScreen.BackGlass);
      backglass.setX(screenres.getFullBackglassX());
      backglass.setY(screenres.getFullBackglassY());
      backglass.setWidth(screenres.getFullBackglassWidth());
      backglass.setHeight(screenres.getFullBackglassHeight());
      displays.add(backglass);
      if (monitor != null) {
        backglass.setX((int) monitor.getX() + backglass.getX());
        backglass.setY((int) monitor.getY() + backglass.getY());
      }

      if (screenres.hasFullDmd()) {
        FrontendPlayerDisplay fulldmd = new FrontendPlayerDisplay(VPinScreen.Menu);
        // override the name
        fulldmd.setName("FullDMD");
        // DMD is relative to backglass, not background so cannot use prevously calculated coordinates
        fulldmd.setX(screenres.getBackglassX() + screenres.getDmdX());
        fulldmd.setY(screenres.getBackglassY() + screenres.getDmdY());
        if (monitor != null) {
          fulldmd.setX((int) monitor.getX() + fulldmd.getX());
          fulldmd.setY((int) monitor.getY() + fulldmd.getY());
        }
        fulldmd.setWidth(screenres.getDmdWidth());
        fulldmd.setHeight(screenres.getDmdHeight());
        displays.add(fulldmd);
      }
    }
    return displays;
  }

  @Nullable
  private static MonitorInfo getBackglassMonitor(@NonNull DirectB2sScreenRes screenres, List<MonitorInfo> monitors) {
    // screen number (\\.\DISPLAY)x or screen coordinates (@x) or screen index (=x)
    MonitorInfo monitor = null;
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
        if (String.valueOf(m.getId()).equals(backglassDisplay)) {
          monitor = m;
        }
      }
    }
    return monitor;
  }

  //TODO move dmdInfo and FrameRes to use vpinScreenService and remove that method
  public void addDeviceOffsets(DirectB2sScreenRes screenres) {
    List<MonitorInfo> monitors = systemService.getMonitorInfos();

    MonitorInfo monitor = null;

    // screen number (\\.\DISPLAY)x or screen coordinates (@x) or screen index (=x)
    String backglassDisplay = screenres.getBackglassDisplay();
    if (backglassDisplay.startsWith("@")) {
      int xPos = parseIntSafe(backglassDisplay);
      for (MonitorInfo m : monitors) {
        if (m.getX() == xPos) {
          monitor = m;
        }
      }
    }
    else if (backglassDisplay.startsWith("=")) {
      int idx = parseIntSafe(backglassDisplay.substring(1)) - 1;
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
    return frontendService.getFrontendPlayerDisplays(forceReload);
  }

  @Override
  public void afterPropertiesSet() {
    boolean isHeadless = GraphicsEnvironment.isHeadless();
    if (!isHeadless) {
      List<FrontendPlayerDisplay> displays = getScreenResDisplays();
      LOG.info("######################## Offset Frontend Screen Summary ##################################");
      DirectB2sScreenRes screenres = backglassService.getGlobalScreenRes();
      if (screenres != null) {
        MonitorInfo backglassMonitor = getBackglassMonitor(screenres, MonitorInfoUtil.getMonitors());
        LOG.info("Backglass Monitor: {}", backglassMonitor);
        LOG.info("------------------------------------------------------------------------------------------");
        for (FrontendPlayerDisplay frontendPlayerDisplay : displays) {
          LOG.info(frontendPlayerDisplay.toString());
        }
      }
      else {
        LOG.error("Reading frontend screen summary failed.");
      }
      LOG.info("####################### /Offset  Frontend Screen Summary #################################");
    }
  }
}
package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.restclient.directb2s.DirectB2sScreenRes;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.server.directb2s.BackglassService;
import de.mephisto.vpin.server.vpx.VPXService;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Add a uniform way to access to screen dimensions, abstracting the source of the information

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

  //---------------------------------------------------

  public FrontendPlayerDisplay getScreenDisplay(VPinScreen screen) {
    switch (screen) {
      case PlayField:
        return firstDefined(screen, getVpxDisplays(), getFrontendDisplays());
      case BackGlass:
        return firstDefined(screen, getVpxDisplays(), getScreenResDisplays(), getFrontendDisplays());
      case Menu:
        return firstDefined(screen, getVpxDisplays(), getScreenResDisplays(), getFrontendDisplays());
      default:
        return firstDefined(screen, getFrontendDisplays());
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

    List<FrontendPlayerDisplay> vpxDisplays = getVpxDisplays();
    List<FrontendPlayerDisplay> screenresDisplays = getScreenResDisplays();
    String frontend = frontendService.getFrontendName();
    List<FrontendPlayerDisplay> frontendDisplays = getFrontendDisplays();

    compareDimensions(errors, VPinScreen.PlayField, vpxDisplays, "VPinballX.ini", screenresDisplays, "screenres.txt");

    compare(errors, VPinScreen.PlayField, vpxDisplays, "VPinballX.ini", frontendDisplays, frontend);
    compare(errors, VPinScreen.BackGlass, screenresDisplays, "screenres.txt", frontendDisplays, frontend);
    compare(errors, VPinScreen.Menu, screenresDisplays, "screenres.txt", frontendDisplays, frontend);

    return errors;
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

  //------------------------------------------------------ VPINBALLX.INI ---
    
  /**
   * Reads the VPinballX.ini file and creates a list of displays
   * Supported screen is Playfield but should be extended for standalone and get Backglass, B2SDMD and DMD positions
   * @return a List of FrontendPlayerDisplay
   */
  public List<FrontendPlayerDisplay> getVpxDisplays() {
    List<FrontendPlayerDisplay> displayList = new ArrayList<>();

    Configuration vpxConfiguration = vpxService.getPlayerConfiguration();
    if (!vpxConfiguration.isEmpty()) {
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
  Display = 2
  FullScreen = 0
  WindowPosX = -1900
  WindowPosY = 30
  Width = 1500
  Height = 900
  */
  private void createVpxPlayfieldDisplay(Configuration vpxConfiguration, List<FrontendPlayerDisplay> players) {
    int monitor = Integer.parseInt(vpxConfiguration.getString("Display", "0"));

    GraphicsDevice[] gds = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();

    if (monitor < gds.length) {
      java.awt.Rectangle bounds = gds[monitor].getDefaultConfiguration().getBounds();
      int mX = (int) bounds.getX();
      int mY = (int) bounds.getY();

      FrontendPlayerDisplay player = new FrontendPlayerDisplay(VPinScreen.PlayField);
      player.setMonitor(monitor);
      player.setRotation(Integer.parseInt(vpxConfiguration.getString("Rotate", "0")));
      player.setInverted(true);

      int fullscreened = vpxConfiguration.getInt("FullScreen", 1);
      if (fullscreened == 0) {
        player.setX(mX + Integer.parseInt(vpxConfiguration.getString("WindowPosX", "0")));
        player.setY(mY + Integer.parseInt(vpxConfiguration.getString("WindowPosY", "0")));
        player.setWidth(Integer.parseInt(vpxConfiguration.getString("Width", "0")));
        player.setHeight(Integer.parseInt(vpxConfiguration.getString("Height", "0")));
      }
      else {
        player.setX(mX);
        player.setY(mY);
        player.setWidth((int) bounds.getWidth());
        player.setHeight((int) bounds.getHeight());
      }
      LOG.info("Created vPinballX player display {}", player);

      players.add(player);
    }
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

  private List<FrontendPlayerDisplay> getScreenResDisplays() {
    List<FrontendPlayerDisplay> displays = new ArrayList<>();
    DirectB2sScreenRes screenres = backglassService.getGlobalScreenRes();
    if (screenres != null) {
      FrontendPlayerDisplay playfield = new FrontendPlayerDisplay(VPinScreen.PlayField);
      playfield.setWidth(screenres.getPlayfieldWidth());
      playfield.setHeight(screenres.getPlayfieldHeight());  
      displays.add(playfield);

      FrontendPlayerDisplay backglass = new FrontendPlayerDisplay(VPinScreen.BackGlass);
      backglass.setX(screenres.getBackglassX());
      backglass.setY(screenres.getBackglassY());  
      backglass.setWidth(screenres.getBackglassWidth());
      backglass.setHeight(screenres.getBackglassHeight());  
      displays.add(backglass);

      if (screenres.hasDMD()) {
        FrontendPlayerDisplay fulldmd = new FrontendPlayerDisplay(VPinScreen.Menu);
        fulldmd.setX(screenres.getDmdX());
        fulldmd.setY(screenres.getDmdY());  
        fulldmd.setWidth(screenres.getDmdWidth());
        fulldmd.setHeight(screenres.getDmdHeight());  
        displays.add(fulldmd);
      }
    }
    return displays;
  }

  //----------------------------------------------------------------- FRONTEND ---

  private List<FrontendPlayerDisplay> getFrontendDisplays() {
    List<FrontendPlayerDisplay> frontendDisplays = frontendService.getFrontendPlayerDisplays();
    return frontendDisplays;
  }
}
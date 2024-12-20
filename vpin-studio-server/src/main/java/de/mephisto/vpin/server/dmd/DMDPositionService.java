package de.mephisto.vpin.server.dmd;

import de.mephisto.vpin.commons.MonitorInfo;
import de.mephisto.vpin.commons.MonitorInfoUtil;
import de.mephisto.vpin.restclient.directb2s.DirectB2sScreenRes;
import de.mephisto.vpin.restclient.dmd.DMDInfo;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.mame.MameOptions;
import de.mephisto.vpin.server.directb2s.BackglassService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.mame.MameService;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.imageio.ImageIO;

@Service
public class DMDPositionService {
  private final static Logger LOG = LoggerFactory.getLogger(DMDPositionService.class);

  @Autowired
  private GameService gameService;
  @Autowired
  private BackglassService backglassService;
  @Autowired
  private MameService mameService;


  public DMDInfo getDMDInfo(int gameId) {
    Game game = gameService.getGame(gameId);

    DMDInfo dmdinfo = new DMDInfo();
    dmdinfo.setGameId(game.getId());
    dmdinfo.setGameRom(game.getRom());

    boolean useExternalDmd = useExternalDmd(game.getRom());
    boolean forceAspectRatio = false;

    if (useExternalDmd) {
      INIConfiguration iniConfiguration = loadDmdDeviceIni(game.getEmulator());
      if (iniConfiguration != null) {
        boolean hasRomInDmdDeviceIni = fillDMDInfoFromIni(dmdinfo, iniConfiguration);
        if (!hasRomInDmdDeviceIni && useRegistry(iniConfiguration)) {
          fillDMDInfoFromRegistry(dmdinfo);
        }
        forceAspectRatio = keepAspectRatio(iniConfiguration);
      }
    }
    else {
      fillDMDInfoFromRegistry(dmdinfo);
    }

    if (dmdinfo.getWidth() == 0) {
      dmdinfo.setWidth(100);
    }
    if (dmdinfo.getHeight() == 0) {
      dmdinfo.setHeight(dmdinfo.getWidth() / 4);
    }

    dmdinfo.setForceAspectRatio(forceAspectRatio);
    if (forceAspectRatio) {
      dmdinfo.setSelectedAspectRatio(true);
    } else {
      // if existing dmd size ratio is close to 4:1, activate the checkbox
      double ratio = dmdinfo.getWidth() / dmdinfo.getHeight();
      boolean aspectRatio = (Math.abs(ratio - 4) < 0.01);
      dmdinfo.setSelectedAspectRatio(aspectRatio);
    }

    // then add screen information, must be done after x,y are set
    fillScreenInfo(dmdinfo);
    // enforce aspect ratio if selected
    dmdinfo.adjustAspectRatio();
    return dmdinfo;
  }

  private void fillDMDInfoFromRegistry(DMDInfo dmdinfo) {
    if (dmdinfo.getGameRom() != null) {
      dmdinfo.setUseRegistry(true);
      mameService.fillDmdPosition(dmdinfo);
    }
  }

  private boolean fillDMDInfoFromIni(DMDInfo info, INIConfiguration iniConfiguration) {
    if (info.getGameRom() != null) {
      SubnodeConfiguration conf = iniConfiguration.getSection(info.getGameRom());
      if (!conf.isEmpty()) {
        info.setLocallySaved(true);
        info.setUseRegistry(false);
        info.setX(safeGet(conf, "virtualdmd left"));
        info.setY(safeGet(conf, "virtualdmd top"));
        info.setWidth(safeGet(conf, "virtualdmd width"));
        info.setHeight(safeGet(conf, "virtualdmd height"));
        return true;
      }
    }
    //else take default...
    SubnodeConfiguration conf = iniConfiguration.getSection("virtualdmd");
    info.setLocallySaved(false);
    info.setUseRegistry(false);
    if (!conf.isEmpty()) {
      info.setX(safeGet(conf, "left"));
      info.setY(safeGet(conf, "top"));
      info.setWidth(safeGet(conf, "width"));
      info.setHeight(safeGet(conf, "height"));
    } 
    return false;
  }

  private void fillScreenInfo(DMDInfo dmdinfo) {
    DirectB2sScreenRes screenres = backglassService.getScreenRes(dmdinfo.getGameId(), false);
    addDeviceOffsets(screenres);
  
    // determine on which screen the DMD is positionned onto
    if (dmdinfo.getCenterX() < 0) {
      fillScreenInfo(dmdinfo, screenres, VPinScreen.PlayField);
    }
    else if (screenres.isOnBackglass(dmdinfo.getCenterX(), dmdinfo.getCenterY())) {
      fillScreenInfo(dmdinfo, screenres, VPinScreen.BackGlass);
    }
    else if (screenres.hasDMD() && screenres.isOnDmd(dmdinfo.getCenterX(), dmdinfo.getCenterY())) {
      fillScreenInfo(dmdinfo, screenres, VPinScreen.DMD);
    }
    else {
      fillScreenInfo(dmdinfo, screenres, VPinScreen.BackGlass);
      dmdinfo.centerOnScreen();
    }

    dmdinfo.setDmdScreenSet(screenres.hasDMD());
  }

  private void addDeviceOffsets(DirectB2sScreenRes screenres) {
    List<MonitorInfo> monitors = MonitorInfoUtil.getMonitors();
    // sort by xPosition
    monitors.sort((m1, m2) -> m1.getScreenX() - m2.getScreenX());

    MonitorInfo monitor = null;

    // screen number (\\.\DISPLAY)x or screen coordinates (@x) or screen index (=x)
    String backglassDisplay = screenres.getBackglassDisplay();
    if (backglassDisplay.startsWith("@")) {
      int xPos = Integer.parseInt(backglassDisplay.substring(1)); 
      for (MonitorInfo m : monitors) {
        if (m.getScreenX() == xPos) {
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
        if (m.getDeviceName().endsWith(backglassDisplay)) {
          monitor = m;
        }
      }
    }

    if (monitor != null) {
      screenres.setBackglassDisplayX(monitor.getScreenX());
      screenres.setBackglassDisplayY(monitor.getScreenY());
    }
  }
  
  private void fillScreenInfo(DMDInfo dmdinfo, DirectB2sScreenRes screenres, VPinScreen onScreen) {
    // All coordinates in DMDInfo are relative to display
    if (VPinScreen.PlayField.equals(onScreen)) {
      dmdinfo.setOnScreen(VPinScreen.PlayField);
      dmdinfo.setScreenWidth(screenres.getPlayfieldWidth());
      dmdinfo.setScreenHeight(screenres.getPlayfieldHeight());
      dmdinfo.setImageCentered(false);
    }
    else if (VPinScreen.BackGlass.equals(onScreen)) {
      dmdinfo.setOnScreen(VPinScreen.BackGlass);
      if (screenres.hasFrame()) {
        dmdinfo.setX(dmdinfo.getX() - screenres.getBackgroundX());
        dmdinfo.setY(dmdinfo.getY() - screenres.getBackgroundY());
        dmdinfo.setScreenWidth(screenres.getBackgroundWidth());
        dmdinfo.setScreenHeight(screenres.getBackgroundHeight());  
        dmdinfo.setImageCentered(screenres.isBackglassCentered());
      }
      else {
        dmdinfo.setX(dmdinfo.getX() - screenres.getBackglassMinX());
        dmdinfo.setY(dmdinfo.getY() - screenres.getBackglassMinY());
        dmdinfo.setScreenWidth(screenres.getBackglassWidth());
        dmdinfo.setScreenHeight(screenres.getBackglassHeight());  
      }
    }
    else if (VPinScreen.DMD.equals(onScreen)) {
      dmdinfo.setOnScreen(VPinScreen.DMD);
      dmdinfo.setX(dmdinfo.getX() - screenres.getDmdMinX());
      dmdinfo.setY(dmdinfo.getY() - screenres.getDmdMinY());
      dmdinfo.setScreenWidth(screenres.getDmdWidth());
      dmdinfo.setScreenHeight(screenres.getDmdHeight());
      dmdinfo.setImageCentered(false);
    }

    // optionally reposition the dmd within the bound of the new screen
    // mind dmdinfo coordinates are relatives so no need to consider x,y of the screen 
    if (dmdinfo.getX() < 0) {
      dmdinfo.setX(0);
    }
    if (dmdinfo.getX() + dmdinfo.getWidth() >= dmdinfo.getScreenWidth()) {
      if (dmdinfo.getScreenWidth() < dmdinfo.getWidth()) {
        dmdinfo.setWidth(dmdinfo.getScreenWidth());
      }
      dmdinfo.setX(dmdinfo.getScreenWidth() - dmdinfo.getWidth());
    }

    if (dmdinfo.getY() < 0) {
      dmdinfo.setY(0);
    }
    if (dmdinfo.getY() + dmdinfo.getHeight() >= dmdinfo.getScreenHeight()) {
      if (dmdinfo.getScreenHeight() < dmdinfo.getHeight()) {
        dmdinfo.setHeight(dmdinfo.getScreenHeight());
      }
      dmdinfo.setY(dmdinfo.getScreenHeight() - dmdinfo.getHeight());
    }
  }

  //------------------------------------
  // MOVE AND POSITION

  public DMDInfo moveDMDInfo(DMDInfo dmdinfo, VPinScreen targetScreen) {
    DirectB2sScreenRes screenres = backglassService.getScreenRes(dmdinfo.getGameId(), false);
    if (screenres != null) {
      fillScreenInfo(dmdinfo, screenres, targetScreen);
    }
    else {
      dmdinfo.setOnScreen(targetScreen);
    }
    return dmdinfo;
  }

  public DMDInfo autoPositionDMDInfo(DMDInfo dmdinfo) {
    DirectB2sScreenRes screenres = backglassService.getScreenRes(dmdinfo.getGameId(), false);

    byte[] image = null;
    double factorX = 0, factorY = 0;
    if (VPinScreen.BackGlass.equals(dmdinfo.getOnScreen())) {
      image = backglassService.getPreviewBackground(dmdinfo.getGameId(), false);
      factorX = screenres.getBackglassWidth();
      factorY = screenres.getBackglassHeight();
    }
    else if (VPinScreen.DMD.equals(dmdinfo.getOnScreen())) {
      image = backglassService.getPreviewDmd(dmdinfo.getGameId());
      factorX = screenres.getDmdWidth();
      factorY = screenres.getDmdHeight();
    }

    if (image != null) {
      try {
        BufferedImage buffered = ImageIO.read(new ByteArrayInputStream(image));
        DMDPositionDetector detector = new DMDPositionDetector();
        List<Integer> position = detector.processImage(buffered);
        if (position != null && position.size() == 4) {
          // coordinates are in pixels, transform in screen coordinate
          factorX /= buffered.getWidth();
          factorY /= buffered.getHeight();
          dmdinfo.setX(position.get(0) * factorX);
          dmdinfo.setY(position.get(1) * factorY);
          dmdinfo.setWidth((position.get(2) - position.get(0)) * factorX);
          dmdinfo.setHeight((position.get(3) - position.get(1)) * factorY);
        }
      }
      catch (IOException ioe) {
        LOG.error("cannot generate image from dmd base64 data", ioe);
      }
    }

    // enforce aspect ratio is selected
    dmdinfo.adjustAspectRatio();
    return dmdinfo;
  }

  //------------------------------------
  // SAVE

  public boolean saveDMDInfo(DMDInfo dmdinfo) {
    DirectB2sScreenRes screenres = backglassService.getScreenRes(dmdinfo.getGameId(), false);
    addDeviceOffsets(screenres);

    // enforce aspect ratio
    dmdinfo.adjustAspectRatio();

    // Reposition DMD relative to upper left corner of the backglass screen
    if (VPinScreen.PlayField.equals(dmdinfo.getOnScreen())) {
      dmdinfo.setX(dmdinfo.getX() + screenres.getPlayfieldWidth());
      dmdinfo.setScreenWidth(screenres.getPlayfieldWidth());
      dmdinfo.setScreenHeight(screenres.getPlayfieldHeight());
    }
    else if (VPinScreen.BackGlass.equals(dmdinfo.getOnScreen())) {
      dmdinfo.setX(dmdinfo.getX() + screenres.getBackglassMinX());
      dmdinfo.setY(dmdinfo.getY() + screenres.getBackglassMinY());
    }
    if (VPinScreen.DMD.equals(dmdinfo.getOnScreen())) {
      dmdinfo.setX(dmdinfo.getX() + screenres.getDmdMinX());
      dmdinfo.setY(dmdinfo.getY() + screenres.getDmdMinY());
    }

    // round to int all number
    dmdinfo.setX(Math.round(dmdinfo.getX()));
    dmdinfo.setY(Math.round(dmdinfo.getY()));
    dmdinfo.setWidth(Math.round(dmdinfo.getWidth()));
    dmdinfo.setHeight(Math.round(dmdinfo.getHeight()));

    Game game = gameService.getGame(dmdinfo.getGameId());

    boolean useExternalDmd = useExternalDmd(dmdinfo.getGameRom());

    if (useExternalDmd) {
      INIConfiguration iniConfiguration = loadDmdDeviceIni(game.getEmulator());
      if (iniConfiguration != null) {
        if (useRegistry(iniConfiguration)) {
          return saveDMDInfoInRegistry(game, dmdinfo, iniConfiguration);
        }
        else {
          return saveDMDInfoInIni(game, dmdinfo, iniConfiguration);
        }
      }
    }
    else {
      return saveDMDInfoInRegistry(game, dmdinfo, null);      
    }
    return false;
  }

  private boolean saveDMDInfoInRegistry(Game game, DMDInfo dmdinfo, INIConfiguration iniConfiguration) {
    // clear any values in dmddevice that could overwrite registry values
    String rom = game.getRom();

    // mind that iniConfiguration can be null id externalDMD is not used
    if (iniConfiguration != null) {
      SubnodeConfiguration conf = iniConfiguration.getSection(rom);
      conf.clear();
      saveDmdDeviceIni(game.getEmulator(), iniConfiguration);
    }

    return mameService.saveDmdPosition(dmdinfo);
  }

  private boolean saveDMDInfoInIni(Game game, DMDInfo dmdinfo, INIConfiguration iniConfiguration) {
    String rom = game.getRom();
    SubnodeConfiguration conf = iniConfiguration.getSection(rom);

    if (dmdinfo.isLocallySaved()) {
      conf.setProperty("virtualdmd left", (int) dmdinfo.getX());
      conf.setProperty("virtualdmd top", (int) dmdinfo.getY());
      conf.setProperty("virtualdmd width", (int) dmdinfo.getWidth());
      conf.setProperty("virtualdmd height", (int) dmdinfo.getHeight());
    }
    else {
      // first clear local values if any
      conf.clear();
      // then update the global ones
      conf = iniConfiguration.getSection("virtualdmd");
      conf.setProperty("left", (int) dmdinfo.getX());
      conf.setProperty("top", (int) dmdinfo.getY());
      conf.setProperty("width", (int) dmdinfo.getWidth());
      conf.setProperty("height", (int) dmdinfo.getHeight());
    }
    return saveDmdDeviceIni(game.getEmulator(), iniConfiguration);
  }

  //------------------------------------
  // Utilities

  private INIConfiguration loadDmdDeviceIni(GameEmulator emulator) {
    File iniFile = new File(emulator.getMameFolder(), "DmdDevice.ini");
    if (!iniFile.exists()) {
      return null;
    }

    INIConfiguration iniConfiguration = new INIConfiguration();
    iniConfiguration.setCommentLeadingCharsUsedInInput(";");
    iniConfiguration.setSeparatorUsedInOutput(" = ");
    iniConfiguration.setSeparatorUsedInInput("=");

    String defaultCharset = "UTF-8";
    try (FileInputStream in = new FileInputStream(iniFile)) {
      try (BOMInputStream bOMInputStream = BOMInputStream.builder().setInputStream(in).get()) {
        ByteOrderMark bom = bOMInputStream.getBOM();
        String charsetName = bom == null ? defaultCharset : bom.getCharsetName();
        try (InputStreamReader reader = new InputStreamReader(new BufferedInputStream(bOMInputStream), charsetName)) {
          iniConfiguration.read(reader);
          return iniConfiguration;
        }
        catch (Exception e) {
          LOG.error("Cannot parse {}", iniFile.getAbsolutePath(), e);
        }
      }
      catch (Exception e) {
        LOG.error("Cannot decode charset of {}}", iniFile.getAbsolutePath(), e);
      }
    }
    catch (Exception e) {
      LOG.error("Cannot open {}", iniFile.getAbsolutePath(), e);
    }
    return null;
  }

  private boolean saveDmdDeviceIni(GameEmulator emulator, INIConfiguration iniConfiguration) {
    File iniFile = new File(emulator.getMameFolder(), "DmdDevice.ini");
    if (!iniFile.exists()) {
      return false;
    }

    try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(iniFile), StandardCharsets.UTF_8);
          BufferedWriter writer = new BufferedWriter(osw)) {
      writer.write('\ufeff');
      iniConfiguration.write(writer);
      return true;
    }
    catch (Exception e) {
      LOG.error("Failed to write dmddevice.ini", e);
      return false;
    }
  }

  private boolean useExternalDmd(String rom) {
    return rom != null ? mameService.getOptions(rom).isUseExternalDmd() : false;
  }

  private boolean useRegistry(INIConfiguration iniConfiguration) {
    SubnodeConfiguration conf = iniConfiguration.getSection("virtualdmd");
    return conf.containsKey("useregistry") ? conf.getBoolean("useregistry") : true;
  }
  private boolean keepAspectRatio(INIConfiguration iniConfiguration) {
    SubnodeConfiguration conf = iniConfiguration.getSection("virtualdmd");
    return conf.containsKey("ignorear") ? !conf.getBoolean("ignorear") : true;
  }

  private double safeGet(SubnodeConfiguration conf, String key) {
    return safeGet(conf, key, 0);
  }
  private double safeGet(SubnodeConfiguration conf, String key, double defValue) {
    return conf.containsKey(key) ? conf.getDouble(key) : defValue;
  }
  
}

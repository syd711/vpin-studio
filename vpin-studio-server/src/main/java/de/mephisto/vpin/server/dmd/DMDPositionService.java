package de.mephisto.vpin.server.dmd;

import de.mephisto.vpin.restclient.directb2s.DirectB2sScreenRes;
import de.mephisto.vpin.restclient.dmd.DMDInfo;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.server.directb2s.BackglassService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

@Service
public class DMDPositionService {
  private final static Logger LOG = LoggerFactory.getLogger(DMDPositionService.class);

  @Autowired
  private GameService gameService;
  @Autowired
  private BackglassService backglassService;


  public DMDInfo getDMDInfo(int gameId) {
    Game game = gameService.getGame(gameId);
    INIConfiguration iniConfiguration = loadDmdDeviceIni(game.getEmulator());
    if (iniConfiguration != null) {
      DMDInfo info = new DMDInfo();
      info.setGameId(game.getId());
      info.setGameRom(game.getRom());
      info.setKeepAspectRatio(keepAspectRatio(iniConfiguration));
      info.setUseRegistry(useregistry(iniConfiguration));
      if (info.isUseRegistry()) {
        fillDMDInfoFromRegistry(info);
      }
      else {
        fillDMDInfoFromIni(info, iniConfiguration);
      }
      // then add screen information, must be done after x,y are set
      fillScreenInfo(info);
      return info;
    }
    return null;
  }

  private void fillDMDInfoFromRegistry(DMDInfo dmdinfo) {
  }

  private void fillDMDInfoFromIni(DMDInfo info, INIConfiguration iniConfiguration) {
    SubnodeConfiguration conf = iniConfiguration.getSection(info.getGameRom());
    if (!conf.isEmpty()) {
      info.setLocallySaved(true);
      info.setX(safeGet(conf, "virtualdmd left"));
      info.setY(safeGet(conf, "virtualdmd top"));
      info.setWidth(safeGet(conf, "virtualdmd width"));
      info.setHeight(safeGet(conf, "virtualdmd height"));
    }
    else {
      // take default
      conf = iniConfiguration.getSection("virtualdmd");
      info.setLocallySaved(false);
      if (!conf.isEmpty()) {
        info.setX(safeGet(conf, "left"));
        info.setY(safeGet(conf, "top"));
        info.setWidth(safeGet(conf, "width"));
        info.setHeight(safeGet(conf, "height"));
      }  
    }
  }

  private void fillScreenInfo(DMDInfo dmdinfo) {
    DirectB2sScreenRes screenres = backglassService.getScreenRes(dmdinfo.getGameId(), false);
    // determine on which screen the DMD is positionned onto
    VPinScreen onScreen = null;
    if (dmdinfo.getCenterX() < 0) {
      onScreen = VPinScreen.PlayField;
    }
    else if (screenres.isOnBackglass(dmdinfo.getCenterX(), dmdinfo.getCenterY())) {
      onScreen = VPinScreen.BackGlass;
    }
    else if (screenres.isOnDmd(dmdinfo.getCenterX(), dmdinfo.getCenterY())) {
      onScreen = VPinScreen.DMD;
    }
    fillScreenInfo(dmdinfo, screenres, onScreen);
  }

  private void fillScreenInfo(DMDInfo dmdinfo, DirectB2sScreenRes screenres, VPinScreen onScreen) {
    // All coordinates in DMDInfo are relative to display
    if (VPinScreen.PlayField.equals(onScreen)) {
      dmdinfo.setOnScreen(VPinScreen.PlayField);
      dmdinfo.setX(dmdinfo.getX() + screenres.getPlayfieldWidth());
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
      dmdinfo.setX(dmdinfo.getX() - screenres.getDmdMinX() - screenres.getBackglassMinX());
      dmdinfo.setY(dmdinfo.getY() - screenres.getDmdMinY() - screenres.getBackglassMinY());
      dmdinfo.setScreenWidth(screenres.getDmdWidth());
      dmdinfo.setScreenHeight(screenres.getDmdHeight());
      dmdinfo.setImageCentered(false);
    }
    else {
      // moveback on backglass
      dmdinfo.setX(0);
      dmdinfo.setY(0);
      dmdinfo.setOnScreen(VPinScreen.BackGlass);
      dmdinfo.setScreenWidth(screenres.getBackglassWidth());
      dmdinfo.setScreenHeight(screenres.getBackglassHeight());
    }

    // optionally reposition the dmd within the bound of the new screen
    // mind dmdinfo coordinates are relatives so no need to consider x,y of the screen 
    if (dmdinfo.getX() + dmdinfo.getWidth() >= dmdinfo.getScreenWidth()) {
      if (dmdinfo.getScreenWidth() < dmdinfo.getWidth()) {
        dmdinfo.setWidth(dmdinfo.getScreenWidth());
      }
      dmdinfo.setX(dmdinfo.getScreenWidth() - dmdinfo.getWidth());
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
    fillScreenInfo(dmdinfo, screenres, targetScreen);
    return dmdinfo;
  }

  public DMDInfo autoPositionDMDInfo(DMDInfo dmdinfo) {
    //TODO do the real magic
    dmdinfo.centerOnScreen();
    return dmdinfo;
  }

  //------------------------------------
  // SAVE

  public boolean saveDMDInfo(DMDInfo dmdinfo) {
    DirectB2sScreenRes screenres = backglassService.getScreenRes(dmdinfo.getGameId(), false);

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
      dmdinfo.setX(dmdinfo.getX() + screenres.getDmdMinX() + screenres.getBackglassMinX());
      dmdinfo.setY(dmdinfo.getY() + screenres.getDmdMinY() + screenres.getBackglassMinY());
    }

    // round to int all number
    dmdinfo.setX(Math.round(dmdinfo.getX()));
    dmdinfo.setY(Math.round(dmdinfo.getY()));
    dmdinfo.setWidth(Math.round(dmdinfo.getWidth()));
    dmdinfo.setHeight(Math.round(dmdinfo.getHeight()));

    Game game = gameService.getGame(dmdinfo.getGameId());
    INIConfiguration iniConfiguration = loadDmdDeviceIni(game.getEmulator());
    if (iniConfiguration != null) {
      if (useregistry(iniConfiguration)) {
        return saveDMDInfoInRegistry(game, dmdinfo);
      }
      else {
        return saveDMDInfoInIni(game, dmdinfo, iniConfiguration);
      }
    }
    return false;
  }

  private boolean saveDMDInfoInRegistry(Game game, DMDInfo dmdinfo) {
    return false;
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

  private boolean useregistry(INIConfiguration iniConfiguration) {
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

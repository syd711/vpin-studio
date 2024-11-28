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
      if (useregistry(iniConfiguration)) {
        return getDMDInfoFromRegistry(game);
      }
      else {
        return getDMDInfoFromIni(game, iniConfiguration);
      }
    }
    return null;
  }

  private DMDInfo getDMDInfoFromRegistry(Game game) {
    //TODO
    throw new UnsupportedOperationException("Unimplemented method 'getDMDInfoFromRegistry'");
  }

  private DMDInfo getDMDInfoFromIni(Game game, INIConfiguration iniConfiguration) {
    String rom = game.getRom();
    SubnodeConfiguration conf = iniConfiguration.getSection(rom);
    if (!conf.isEmpty()) {
      DMDInfo info = new DMDInfo();
      info.setGameId(game.getId());
      info.setX(safeGet(conf, "virtualdmd left"));
      info.setY(safeGet(conf, "virtualdmd top"));
      info.setWidth(safeGet(conf, "virtualdmd width"));
      info.setHeight(safeGet(conf, "virtualdmd height"));
      return addScreenInfo(info);
    }
    else {
      // take default
      conf = iniConfiguration.getSection("virtualdmd");
      if (!conf.isEmpty()) {
        DMDInfo info = new DMDInfo();
        info.setGameId(game.getId());
        info.setX(safeGet(conf, "left"));
        info.setY(safeGet(conf, "top"));
        info.setWidth(safeGet(conf, "width"));
        info.setHeight(safeGet(conf, "height"));
        return addScreenInfo(info);
      }  
    }    
    return null;
  }

  private DMDInfo addScreenInfo(DMDInfo dmdinfo) {
    DirectB2sScreenRes screenres = backglassService.getScreenRes(dmdinfo.getGameId(), false);

    // determine on which screen the DMD is positionned onto
    // then relativize to display
    if (dmdinfo.getCenterX() < 0) {
      dmdinfo.setX(dmdinfo.getX() + screenres.getPlayfieldWidth());
      dmdinfo.setOnScreen(VPinScreen.PlayField);
      dmdinfo.setScreenWidth(screenres.getPlayfieldWidth());
      dmdinfo.setScreenHeight(screenres.getPlayfieldHeight());
    }
    else if (screenres.isOnBackglass(dmdinfo.getCenterX(), dmdinfo.getCenterY())) {
      dmdinfo.setX(dmdinfo.getX() - screenres.getBackglassMinX());
      dmdinfo.setY(dmdinfo.getY() - screenres.getBackglassMinY());
      dmdinfo.setOnScreen(VPinScreen.BackGlass);
      dmdinfo.setScreenWidth(screenres.getBackglassWidth());
      dmdinfo.setScreenHeight(screenres.getBackglassHeight());
    }
    else if (screenres.isOnDmd(dmdinfo.getCenterX(), dmdinfo.getCenterY())) {
      dmdinfo.setX(dmdinfo.getX() - screenres.getDmdMinX() - screenres.getBackglassMinX());
      dmdinfo.setY(dmdinfo.getY() - screenres.getDmdMinY() - screenres.getBackglassMinY());
      dmdinfo.setOnScreen(VPinScreen.DMD);
      dmdinfo.setScreenWidth(screenres.getDmdWidth());
      dmdinfo.setScreenHeight(screenres.getDmdHeight());
    }
    else {
      // moveback on backglass
      dmdinfo.setX(0);
      dmdinfo.setY(0);
      dmdinfo.setOnScreen(VPinScreen.BackGlass);
      dmdinfo.setScreenWidth(screenres.getBackglassWidth());
      dmdinfo.setScreenHeight(screenres.getBackglassHeight());
    }

    return dmdinfo;
  }

  public DMDInfo moveDMDInfo(DMDInfo dmdinfo, VPinScreen targetScreen) {
    DirectB2sScreenRes screenres = backglassService.getScreenRes(dmdinfo.getGameId(), false);
    
    // optionally reposition the dmd within the bound of the new screen
    // mind dmdinfo coordinates are relatives so no need to consider x,y of the screen 
    if (VPinScreen.PlayField.equals(targetScreen)) {
      reposition(dmdinfo, screenres.getPlayfieldWidth(), screenres.getPlayfieldHeight());
    }
    else if (VPinScreen.DMD.equals(targetScreen)) {
      reposition(dmdinfo, screenres.getDmdWidth(), screenres.getDmdHeight());
    }
    else {
      reposition(dmdinfo, screenres.getBackglassWidth(), screenres.getBackglassHeight());
    }
    dmdinfo.setOnScreen(targetScreen);
    return dmdinfo;
  }

  public void reposition(DMDInfo dmdinfo, double displayWidth, double displayHeight) {
    if (dmdinfo.getX() + dmdinfo.getWidth() >= displayWidth) {
      if (displayWidth < dmdinfo.getWidth()) {
        dmdinfo.setWidth(displayWidth);
      }
      dmdinfo.setX(displayWidth - dmdinfo.getWidth());
    }
    if (dmdinfo.getY() + dmdinfo.getHeight() >= displayHeight) {
      if (displayHeight < dmdinfo.getHeight()) {
        dmdinfo.setHeight(displayHeight);
      }
      dmdinfo.setY(displayHeight - dmdinfo.getHeight());
    }
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
    if (VPinScreen.PlayField.equals(dmdinfo.getOnScreen())) {
      dmdinfo.setX(dmdinfo.getX() + screenres.getDmdMinX() + screenres.getBackglassMinX());
      dmdinfo.setY(dmdinfo.getY() + screenres.getDmdMinY() + screenres.getBackglassMinY());
    }

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

    conf.setProperty("virtualdmd left", dmdinfo.getX());
    conf.setProperty("virtualdmd top", dmdinfo.getY());
    conf.setProperty("virtualdmd width", dmdinfo.getWidth());
    conf.setProperty("virtualdmd height", dmdinfo.getHeight());

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
    iniConfiguration.setSeparatorUsedInOutput("=");
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
          LOG.error("Cannot parse DMDDevice.ini", e);
        }
      }
      catch (Exception e) {
        LOG.error("Cannot decode charset of DMDDevice.ini", e);
      }
    }
    catch (Exception e) {
      LOG.error("Cannot open DMDDevice.ini", e);
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

  private double safeGet(SubnodeConfiguration conf, String key) {
    return safeGet(conf, key, 0);
  }
  private double safeGet(SubnodeConfiguration conf, String key, double defValue) {
    return conf.containsKey(key) ? conf.getDouble(key) : defValue;
  }
  
}

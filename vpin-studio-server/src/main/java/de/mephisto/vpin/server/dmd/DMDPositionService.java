package de.mephisto.vpin.server.dmd;

import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.dmd.DMDInfo;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.server.directb2s.BackglassService;
import de.mephisto.vpin.server.frontend.FrontendService;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

@Service
public class DMDPositionService {
  private final static Logger LOG = LoggerFactory.getLogger(DMDPositionService.class);

  @Autowired
  private GameService gameService;
  @Autowired
  private BackglassService backglassService;
  @Autowired
  private FrontendService frontendService;

  public DMDInfo getDMDInfo(int gameId) {
    Game game = gameService.getGame(gameId);
    GameEmulator emulator = game.getEmulator();
  
    File iniFile = new File(emulator.getMameFolder(), "DmdDevice.ini");
    if (!iniFile.exists()) {
      return null;
    }
    String defaultEncoding = "UTF-8";
    try (FileInputStream in = new FileInputStream(iniFile)) {
      try (BOMInputStream bOMInputStream = BOMInputStream.builder().setInputStream(in).get()) {
        ByteOrderMark bom = bOMInputStream.getBOM();
        String charsetName = bom == null ? defaultEncoding : bom.getCharsetName();
        try (InputStreamReader reader = new InputStreamReader(new BufferedInputStream(bOMInputStream), charsetName)) {
          INIConfiguration iniConfiguration = new INIConfiguration();
          iniConfiguration.setCommentLeadingCharsUsedInInput(";");
          iniConfiguration.setSeparatorUsedInOutput("=");
          iniConfiguration.setSeparatorUsedInInput("=");

          iniConfiguration.read(reader);
          SubnodeConfiguration virtualdmdSection = iniConfiguration.getSection("virtualdmd");
          boolean useregistry = virtualdmdSection.getBoolean("useregistry");
          if (useregistry) {
            return getDMDInfoFromRegistry(game);
          }
          else {
            return getDMDInfoFromIni(game, iniConfiguration);
          }
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
      info.setX(conf.getInt("virtualdmd left"));
      info.setY(conf.getInt("virtualdmd top"));
      info.setWidth(conf.getInt("virtualdmd width"));
      info.setHeight(conf.getInt("virtualdmd height"));
      return addImageUrl(info);
    }
    else {
      // take default
      conf = iniConfiguration.getSection("virtualdmd");
      if (!conf.isEmpty()) {
        DMDInfo info = new DMDInfo();
        info.setGameId(game.getId());
        info.setX(conf.getInt("left"));
        info.setY(conf.getInt("top"));
        info.setWidth(conf.getInt("width"));
        info.setHeight(conf.getInt("height"));
        return addImageUrl(info);
      }  
    }    
    return null;
  }

  private DMDInfo addImageUrl(DMDInfo info) {

    DirectB2SData data = backglassService.getDirectB2SData(info.getGameId());
    if (data != null) {
      FrontendPlayerDisplay bg = frontendService.getFrontendPlayerDisplays(VPinScreen.BackGlass);
      FrontendPlayerDisplay dmd = frontendService.getFrontendPlayerDisplays(VPinScreen.DMD);
      FrontendPlayerDisplay display = bg;
      // determine if DMD is positionned on DMD screen ?
      if (dmd != null && dmd.contains(info.getX(), info.getY())) {
        display = dmd;
      }

      // relativize to display
      info.setX(info.getX() - display.getX());
      info.setY(info.getY() - display.getY());

      info.setOnScreen(display.getScreen());
      String url = "directb2s/" + (display == dmd? "croppedDmd/" : "croppedBackground/") + info.getGameId();
      info.setBackgroundUrl(url);

      //DirectB2STableSettings tableSettings = backglassService.getTableSettings(info.getGameId());

    }
    return info;
  }

  public boolean saveDMDInfo(DMDInfo dmdInfo) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'saveDMDInfo'");
  }

}

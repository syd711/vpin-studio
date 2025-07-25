package de.mephisto.vpin.server.dmd;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2SDataScore;
import de.mephisto.vpin.restclient.directb2s.DirectB2sScreenRes;
import de.mephisto.vpin.restclient.dmd.DMDAspectRatio;
import de.mephisto.vpin.restclient.dmd.DMDInfo;
import de.mephisto.vpin.restclient.dmd.DMDInfoZone;
import de.mephisto.vpin.restclient.dmd.DMDPackageTypes;
import de.mephisto.vpin.restclient.dmd.DMDType;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.mame.MameOptions;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.server.directb2s.BackglassService;
import de.mephisto.vpin.server.frontend.VPinScreenService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameLifecycleService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.mame.MameService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.DefaultPictureService;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class DMDPositionService {
  private final static Logger LOG = LoggerFactory.getLogger(DMDPositionService.class);

  @Autowired
  private GameService gameService;
  @Autowired
  private BackglassService backglassService;
  @Autowired
  private MameService mameService;
  @Autowired
  private VPinScreenService screenService;
  @Autowired
  private PreferencesService preferenceService;
  @Autowired
  private GameLifecycleService gameLifecycleService;
  @Autowired
  private DefaultPictureService defaultPictureService;

  public DMDInfo getDMDInfo(int gameId) {
    Game game = gameService.getGame(gameId);
    String rom = StringUtils.defaultString(game.getRomAlias(), game.getRom());
    String storeName = rom;
    if (DMDPackageTypes.UltraDMD.equals(game.getDMDType())) {
      storeName = FilenameUtils.getBaseName(game.getGameFileName());
    }
    else if (DMDPackageTypes.FlexDMD.equals(game.getDMDType())) {
      storeName = game.getDMDGameName();
    }

    DMDInfo dmdinfo = new DMDInfo();
    dmdinfo.setGameId(game.getId());
    dmdinfo.setGameRom(rom);
    dmdinfo.setDmdStoreName(storeName);

    DMDType type = null;
    if (useExternalDmd(rom)) {
      // leave undertermined
    }
    else if (isShowDmd(rom)) {
      type = DMDType.VpinMAMEDMD;
    }
    else {
      type = DMDType.NoDMD;
    }
    return loadDMDInfo(dmdinfo, game, type);
  }

  public DMDInfo switchDMDInfo(DMDInfo dmdinfo, DMDType type) {
    Game game = gameService.getGame(dmdinfo.getGameId());
    return loadDMDInfo(dmdinfo, game, type);
  }

  /**
   * Reset the DMD zones to the positions stored in the backglass
   * Works only for Alphanumeric zones
   */
  public DMDInfo resetToScores(DMDInfo dmdinfo) {
    if (DMDType.AlphaNumericDMD.equals(dmdinfo.getDMDType())) {
      Game game = gameService.getGame(dmdinfo.getGameId());
      List<FrontendPlayerDisplay> screenResDisplays = screenService.getScreenResDisplays(game);
      List<DMDInfoZone> alphaNumZones = getAlphaNumericScores(game, screenResDisplays);
      dmdinfo.setZones(alphaNumZones);

      // then add screen information, must be done after x,y are set
      for (DMDInfoZone zone : dmdinfo.getZones()) {
        fillScreenInfo(zone, screenResDisplays);
      }
    }
    return dmdinfo;
  }

  private DMDInfo loadDMDInfo(DMDInfo dmdinfo, Game game, DMDType type) {

    List<FrontendPlayerDisplay> screenResDisplays = screenService.getScreenResDisplays(game);

    DMDInfoZone mainZone = new DMDInfoZone();
    List<DMDInfoZone> alphaNumZones = getAlphaNumericScores(game, screenResDisplays);

    INIConfiguration iniConfiguration = loadDmdDeviceIni(game.getEmulator());

    boolean forceAspectRatio = false;
    if (type == null || type.equals(DMDType.VirtualDMD) || type.equals(DMDType.AlphaNumericDMD)) {
      if (iniConfiguration != null) {

        // now determine type if not set
        if (type == null) {
          SubnodeConfiguration virtualdmdConf = iniConfiguration.getSection("virtualdmd");
          SubnodeConfiguration alphaNumericConf = iniConfiguration.getSection("alphanumeric");
          SubnodeConfiguration conf = iniConfiguration.getSection(dmdinfo.getDmdStoreName().replace(".", ".."));
          boolean virtualDmdEnabled = safeGetBoolean(conf, "virtualdmd enabled", safeGetBoolean(virtualdmdConf, "enabled", false));
          boolean alphaNumericEnabled = safeGetBoolean(conf, "alphanumeric enabled", safeGetBoolean(alphaNumericConf, "enabled", false));
          type = alphaNumericEnabled ? DMDType.AlphaNumericDMD :
              virtualDmdEnabled ? DMDType.VirtualDMD : DMDType.VpinMAMEDMD;
        }

        boolean hasRomInDmdDeviceIni = fillDMDInfoFromIni(dmdinfo, mainZone, alphaNumZones, iniConfiguration);
        if (!hasRomInDmdDeviceIni && useRegistry(iniConfiguration)) {
          fillDMDInfoFromRegistry(dmdinfo, mainZone);
        }
        forceAspectRatio = keepAspectRatio(iniConfiguration);
      }
      else {
        type = DMDType.VpinMAMEDMD;
      }
    }

    if (type.equals(DMDType.VpinMAMEDMD)) {
      fillDMDInfoFromRegistry(dmdinfo, mainZone);
    }

    // set the type
    dmdinfo.setDMDType(type);

    // for mono zone DMD 
    if (type.equals(DMDType.VirtualDMD) || type.equals(DMDType.VpinMAMEDMD)) {
      // ensure minimum dimension
      if (mainZone.getWidth() == 0) {
        mainZone.setWidth(100);
      }
      if (mainZone.getHeight() == 0) {
        mainZone.setHeight(mainZone.getWidth() / 4);
      }

      // if Force AspectRatio is selected, select current ratio
      dmdinfo.setForceAspectRatio(forceAspectRatio);
      DMDAspectRatio aspectRatio = DMDAspectRatio.ratioOff;
      if (forceAspectRatio) {
        double ratio = mainZone.getWidth() / mainZone.getHeight();
        for (DMDAspectRatio ar : DMDAspectRatio.values()) {
          // if existing dmd size ratio is close to a standard AR, activate the checkbox
          if (ar.isKeepRatio() && Math.abs(ratio - ar.getValue()) < 0.01) {
            aspectRatio = ar;
          }
        }
        dmdinfo.setAspectRatio(aspectRatio);
      }
      // enforce aspect ratio if selected
      mainZone.adjustAspectRatio(aspectRatio);

      dmdinfo.setZones(Collections.singletonList(mainZone));
    }
    if (type.equals(DMDType.AlphaNumericDMD)) {
      dmdinfo.setZones(alphaNumZones);
    }

    // then add screen information, must be done after x,y are set
    for (DMDInfoZone zone : dmdinfo.getZones()) {
      fillScreenInfo(zone, screenResDisplays);
    }

    // check presence of fullDmd screen
    FrontendPlayerDisplay fulldmdDisplay = FrontendPlayerDisplay.valueOfScreen(screenResDisplays, VPinScreen.Menu);
    dmdinfo.setSupportFullDmd(fulldmdDisplay != null);

    dmdinfo.setSupportExtDmd(iniConfiguration != null);

    dmdinfo.setSupportAlphaNumericDmd(alphaNumZones != null && alphaNumZones.size() > 0);

    // load the preference on how to save dmd disablement
    ServerSettings serverSettings = preferenceService.getJsonPreference(PreferenceNames.SERVER_SETTINGS);
    dmdinfo.setDisableViaIni(serverSettings.isDisableDmdViaIni());
    dmdinfo.setDisableInVpinMame(serverSettings.isDisableDmdInMame());
    dmdinfo.setDisableBackglassScores(serverSettings.isDisableBackglassScore());

    return dmdinfo;
  }

  private List<DMDInfoZone> getAlphaNumericScores(Game game, List<FrontendPlayerDisplay> screenResDisplays) {
    List<DMDInfoZone> alphaNumZones = new ArrayList<>();

    // load alphanumeric scores
    DirectB2SData data = backglassService.getDirectB2SData(game);
    if (data != null && data.getNbScores() > 0) {
      DirectB2sScreenRes screenres = backglassService.getScreenRes(game, false);
      if (screenres != null) {
        for (DirectB2SDataScore score : data.getScores()) {
          VPinScreen screen = VPinScreen.BackGlass;
          double imageWidth = data.getBackgroundWidth();
          double imageHeight = data.getBackgroundHeight();

          if ("DMD".equalsIgnoreCase(score.getParent())) {
            screen = VPinScreen.Menu;
            imageWidth = data.getDmdWidth();
            imageHeight = data.getDmdHeight();
          }

          // DMD not supported =, bring all scores on backglass
          FrontendPlayerDisplay dmdDisplay = FrontendPlayerDisplay.valueOfScreen(screenResDisplays, screen);
          // DMD not supported =, bring all scores on backglass
          if (dmdDisplay == null) {
            screen = VPinScreen.BackGlass;
            dmdDisplay = FrontendPlayerDisplay.valueOfScreen(screenResDisplays, screen);
          }

          double x = VPinScreen.Menu.equals(screen) ? screenres.getDmdMinX() : screenres.getBackglassMinX();
          double y = VPinScreen.Menu.equals(screen) ? screenres.getDmdMinY() : screenres.getBackglassMinY();

          double ratioX = VPinScreen.Menu.equals(screen) ? screenres.getDmdWidth() : screenres.getBackglassWidth();
          ratioX /= imageWidth;
          double ratioY = VPinScreen.Menu.equals(screen) ? screenres.getDmdHeight() : screenres.getBackglassHeight();
          ratioY /= imageHeight;

          DMDInfoZone zone = new DMDInfoZone(screen, (int) (score.getX() * ratioX + x), (int) (score.getY() * ratioY + y),
              (int) (score.getWidth() * ratioX), (int) (score.getHeight() * ratioY));
          alphaNumZones.add(zone);
        }
      }
    }
    return alphaNumZones;
  }

  private void fillDMDInfoFromRegistry(DMDInfo dmdinfo, DMDInfoZone zone) {
    if (dmdinfo.getGameRom() != null) {
      dmdinfo.setUseRegistry(true);
      boolean existInRegistry = mameService.fillDmdPosition(dmdinfo.getGameRom(), zone);
      dmdinfo.setLocallySaved(existInRegistry);
    }
  }

  private boolean fillDMDInfoFromIni(DMDInfo info, DMDInfoZone main, List<DMDInfoZone> alphaNumZones, INIConfiguration iniConfiguration) {
    if (info.getDmdStoreName() != null) {
      SubnodeConfiguration virtualdmdConf = iniConfiguration.getSection("virtualdmd");
      SubnodeConfiguration conf = iniConfiguration.getSection(info.getDmdStoreName().replace(".", ".."));

      info.setLocallySaved(!conf.isEmpty());
      info.setUseRegistry(false);

      main.setX(safeGet(conf, "virtualdmd left", safeGet(virtualdmdConf, "left")));
      main.setY(safeGet(conf, "virtualdmd top", safeGet(virtualdmdConf, "top")));
      main.setWidth(safeGet(conf, "virtualdmd width", safeGet(virtualdmdConf, "width")));
      main.setHeight(safeGet(conf, "virtualdmd height", safeGet(virtualdmdConf, "height")));

      // load alphanumeric scores if any
      int pos = 0;
      for (DMDInfoZone zone : alphaNumZones) {
        zone.setX(safeGet(conf, "alphanumeric pos.." + pos + "..left", zone.getX()));
        zone.setY(safeGet(conf, "alphanumeric pos.." + pos + "..top", zone.getY()));
        zone.setHeight(safeGet(conf, "alphanumeric pos.." + pos + "..height", zone.getHeight()));
        // width is not used in dmddevice.ini but persisted by studio
        zone.setWidth(safeGet(conf, "alphanumeric pos.." + pos + "..width", zone.getWidth()));
        pos++;
      }

      return !conf.isEmpty();
    }
    return false;
  }

  private void fillScreenInfo(DMDInfoZone dmdinfo, List<FrontendPlayerDisplay> screenResDisplays) {
    // determine on which screen the DMD is positionned onto
    boolean ondisplay = false;
    for (FrontendPlayerDisplay display : screenResDisplays) {
      if (display.contains(dmdinfo.getCenterX(), dmdinfo.getCenterY())) {
        fillScreenInfo(dmdinfo, display);
        ondisplay = true;
        break;
      }
    }

    FrontendPlayerDisplay bgDisplay = FrontendPlayerDisplay.valueOfScreen(screenResDisplays, VPinScreen.BackGlass);
    if (!ondisplay && bgDisplay != null) {
      fillScreenInfo(dmdinfo, bgDisplay);
      //FIXME dmdinfo.centerOnScreen();
      //setX(getScreenWidth() / 2 - getWidth() / 2);
      //setY(getScreenHeight() / 2 - getHeight() / 2);
    }
  }

  private void fillScreenInfo(DMDInfoZone dmdinfo, FrontendPlayerDisplay display) {

    dmdinfo.setOnScreen(display.getScreen());

    dmdinfo.setX(dmdinfo.getX() - display.getMinX());
    dmdinfo.setY(dmdinfo.getY() - display.getMinY());

    reposition(dmdinfo, display.getWidth(), display.getHeight());
  }

  private void reposition(DMDInfoZone dmdinfo, int screenWidth, int screenHeight) {
    // optionally reposition the dmd within the bound of the new screen
    // mind dmdinfo coordinates are relatives so no need to consider x,y of the screen 
    if (dmdinfo.getX() < 0) {
      dmdinfo.setX(0);
    }
    if (dmdinfo.getX() + dmdinfo.getWidth() >= screenWidth) {
      if (screenWidth < dmdinfo.getWidth()) {
        dmdinfo.setWidth(screenWidth);
      }
      dmdinfo.setX(screenWidth - dmdinfo.getWidth());
    }

    if (dmdinfo.getY() < 0) {
      dmdinfo.setY(0);
    }
    if (dmdinfo.getY() + dmdinfo.getHeight() >= screenHeight) {
      if (screenHeight < dmdinfo.getHeight()) {
        dmdinfo.setHeight(screenHeight);
      }
      dmdinfo.setY(screenHeight - dmdinfo.getHeight());
    }
  }

  //------------------------------------
  // MOVE AND POSITION

  public DMDInfoZone moveDMDInfo(int gameId, DMDInfoZone dmdinfo, VPinScreen targetScreen) {
    Game game = gameService.getGame(gameId);

    List<FrontendPlayerDisplay> screenResDisplays = screenService.getScreenResDisplays(game);

    // Reposition DMD relative to upper left corner of the backglass screen
    //FrontendPlayerDisplay display = FrontendPlayerDisplay.valueOfScreen(screenResDisplays, dmdinfo.getOnScreen());

    // now position on the new target screen
    if (targetScreen != null) {
      dmdinfo.setOnScreen(targetScreen);
      FrontendPlayerDisplay display = FrontendPlayerDisplay.valueOfScreen(screenResDisplays, targetScreen);
      //DirectB2sScreenRes screenres = backglassService.getScreenRes(game, false);
      if (display != null) {
        reposition(dmdinfo, display.getWidth(), display.getHeight());
      }
    }
    return dmdinfo;
  }

  public DMDInfoZone autoPositionDMDInfo(int gameId, DMDInfoZone dmdinfo) {
    Game game = gameService.getGame(gameId);
    List<FrontendPlayerDisplay> screenResDisplays = screenService.getScreenResDisplays(game);
    //DirectB2sScreenRes screenres = backglassService.getScreenRes(game, false);

    FrontendPlayerDisplay display = FrontendPlayerDisplay.valueOfScreen(screenResDisplays, dmdinfo.getOnScreen());
    if (display != null) {
      double factorX = display.getWidth(), factorY = display.getHeight();

      byte[] image = defaultPictureService.getPicture(game, dmdinfo.getOnScreen());

      if (image != null) {
        try {
          BufferedImage buffered = ImageIO.read(new ByteArrayInputStream(image));
          DMDPositionDetector detector = new DMDPositionDetector();
          List<Integer> position = detector.processImage(buffered);
          if (position != null && position.size() == 4) {
            // coordinates are in pixels, transform in screen coordinate
            factorX /= buffered.getWidth();
            factorY /= buffered.getHeight();
            dmdinfo.setX((int) (position.get(0) * factorX + dmdinfo.getMargin()));
            dmdinfo.setY((int) (position.get(1) * factorY + dmdinfo.getMargin()));
            dmdinfo.setWidth((int) ((position.get(2) - position.get(0)) * factorX - 2 * dmdinfo.getMargin()));
            dmdinfo.setHeight((int) ((position.get(3) - position.get(1)) * factorY - 2 * dmdinfo.getMargin()));
          }
        }
        catch (IOException ioe) {
          LOG.error("cannot generate image from dmd base64 data", ioe);
        }
      }
    }

    // enforce aspect ratio if selected
    //dmdinfo.adjustAspectRatio();
    return dmdinfo;
  }

  //------------------------------------
  // SAVE

  public boolean saveDMDInfo(DMDInfo dmdinfo) {
    Game game = gameService.getGame(dmdinfo.getGameId());
    String rom = StringUtils.defaultString(game.getRomAlias(), game.getRom());
    String storeName = rom;
    if (DMDPackageTypes.UltraDMD.equals(game.getDMDType())) {
      storeName = FilenameUtils.getBaseName(game.getGameFileName());
    }
    else if (DMDPackageTypes.FlexDMD.equals(game.getDMDType())) {
      storeName = game.getDMDGameName();
    }
    dmdinfo.setDmdStoreName(storeName);

    List<FrontendPlayerDisplay> screenResDisplays = screenService.getScreenResDisplays(game);
    //DirectB2sScreenRes screenres = backglassService.getScreenRes(game, false);
    //screenService.addDeviceOffsets(screenres);

    for (DMDInfoZone zone : dmdinfo.getZones()) {
      // enforce aspect ratio
      //zone.adjustAspectRatio(dmdinfo.getAspectRatio());

      // Reposition DMD relative to upper left corner of the backglass screen
      FrontendPlayerDisplay display = FrontendPlayerDisplay.valueOfScreen(screenResDisplays, zone.getOnScreen());
      if (display != null) {
        zone.setX(zone.getX() + display.getMinX());
        zone.setY(zone.getY() + display.getMinY());
      }

      // round to int all number
      zone.setX(Math.round(zone.getX()));
      zone.setY(Math.round(zone.getY()));
      zone.setWidth(Math.round(zone.getWidth()));
      zone.setHeight(Math.round(zone.getHeight()));
    }

    //----------
    // Case of NO DMD
    if (dmdinfo.getDMDType().equals(DMDType.NoDMD)) {
      if (dmdinfo.isDisableViaIni()) {
        disableDMDInIni(game, rom);
      }
      if (dmdinfo.isDisableInVpinMame()) {
        setMameOptions(rom, false, false);
      }

      // recativate scores if they were disabled
      backglassService.upddateScoresDisplayState(game, false);

      // also persist preference for next dmd
      try {
        ServerSettings serverSettings = preferenceService.getJsonPreference(PreferenceNames.SERVER_SETTINGS);
        serverSettings.setDisableDmdViaIni(dmdinfo.isDisableViaIni());
        serverSettings.setDisableDmdInMame(dmdinfo.isDisableInVpinMame());
        preferenceService.savePreference(PreferenceNames.SERVER_SETTINGS, serverSettings);
      }
      catch (Exception e) {
        LOG.error("Cannot save server preference for disable options, exception ignored : " + e.getMessage());
      }

      return true;
    }
    //----------
    // Case of PinMAME DMD
    else if (dmdinfo.getDMDType().equals(DMDType.VpinMAMEDMD)) {
      setMameOptions(rom, true, false);
      //... and disable dmdext
      //disableDMDInIni(game, rom);

      // then save positions in registry
      DMDInfoZone mainZone = getMainZone(dmdinfo);
      return mameService.saveDmdPosition(rom, mainZone);
    }
    //----------
    // case of VirtualDMD
    else if (dmdinfo.getDMDType().equals(DMDType.VirtualDMD)) {
      setMameOptions(rom, false, true);

      INIConfiguration iniConfiguration = loadDmdDeviceIni(game.getEmulator());
      if (iniConfiguration != null) {
        DMDInfoZone mainZone = getMainZone(dmdinfo);
        if (useRegistry(iniConfiguration)) {
          return saveDMDInfoInRegistry(game, mainZone, iniConfiguration);
        }
        else {
          return saveVirtualDMDInfoInIni(game, dmdinfo, mainZone, iniConfiguration, dmdinfo.isLocallySaved());
        }
      }
    }
    //----------
    // case of AlphaNumericDMD
    else if (dmdinfo.getDMDType().equals(DMDType.AlphaNumericDMD)) {

      // update scores in backglass
      backglassService.upddateScoresDisplayState(game, dmdinfo.isDisableBackglassScores());

      // also persist preference for next dmd
      try {
        ServerSettings serverSettings = preferenceService.getJsonPreference(PreferenceNames.SERVER_SETTINGS);
        serverSettings.setDisableBackglassScore(dmdinfo.isDisableBackglassScores());
        preferenceService.savePreference(PreferenceNames.SERVER_SETTINGS, serverSettings);
      }
      catch (Exception e) {
        LOG.error("Cannot save server preference for disable options, exception ignored : " + e.getMessage());
      }

      setMameOptions(rom, false, true);
      INIConfiguration iniConfiguration = loadDmdDeviceIni(game.getEmulator());
      if (iniConfiguration != null) {
        return saveAlphaNumericDMDInfoInIni(game, dmdinfo, iniConfiguration);
      }
    }
    else {
      throw new RuntimeException("DMD Type " + dmdinfo.getDMDType() + " not supported");
    }
    return false;
  }

  private DMDInfoZone getMainZone(DMDInfo dmdinfo) {
    List<DMDInfoZone> zones = dmdinfo.getZones();
    return zones.size() > 0 ? zones.get(0) : null;
  }

  private void disableDMDInIni(Game game, String rom) {
    // the deactivation is always stored in the rom section
    INIConfiguration iniConfiguration = loadDmdDeviceIni(game.getEmulator());
    if (iniConfiguration != null && rom != null) {
      SubnodeConfiguration virtualdmdConf = iniConfiguration.getSection("virtualdmd");
      SubnodeConfiguration alphaNumericConf = iniConfiguration.getSection("alphanumeric");
      SubnodeConfiguration conf = iniConfiguration.getSection(rom.replace(".", ".."));

      // if the global virtualconf is enabled, force enable=false
      if (safeGetBoolean(virtualdmdConf, "enabled", false)) {
        conf.setProperty("virtualdmd enabled", false);
      }
      else {
        // as the virtualdmd is globally deactivated, no need to add a property
        conf.clearProperty("virtualdmd enabled");
      }

      // if the global alphaNumeric is enabled, force enable=false
      if (safeGetBoolean(alphaNumericConf, "enabled", false)) {
        conf.setProperty("alphanumeric enabled", false);
      }
      else {
        // as the virtualdmd is globally deactivated, no need to add a property
        conf.clearProperty("alphanumeric enabled");
      }
    }
  }

  private boolean saveDMDInfoInRegistry(Game game, DMDInfoZone dmdinfo, INIConfiguration iniConfiguration) {
    // clear any values in dmddevice that could overwrite registry values
    String rom = StringUtils.defaultString(game.getRomAlias(), game.getRom());

    // mind that iniConfiguration can be null if externalDMD is not used
    if (iniConfiguration != null) {
      SubnodeConfiguration conf = iniConfiguration.getSection(rom.replace(".", ".."));
      conf.clear();

      SubnodeConfiguration virtualdmdConf = iniConfiguration.getSection("virtualdmd");
      // if the global virtualconf is not enabled, force enable=true
      if (!safeGetBoolean(virtualdmdConf, "enabled", false)) {
        conf.setProperty("virtualdmd enabled", true);
      }
      saveDmdDeviceIni(game.getEmulator(), iniConfiguration);
    }

    return mameService.saveDmdPosition(rom, dmdinfo);
  }

  private boolean saveVirtualDMDInfoInIni(Game game, DMDInfo dmdinfo, DMDInfoZone dmdinfoZone, INIConfiguration iniConfiguration, boolean locallySaved) {
    SubnodeConfiguration virtualdmdConf = iniConfiguration.getSection("virtualdmd");
    SubnodeConfiguration alphaNumericConf = iniConfiguration.getSection("alphanumeric");
    SubnodeConfiguration conf = iniConfiguration.getSection(dmdinfo.getDmdStoreName().replace(".", ".."));

    // if the global virtualDMD is not enabled, force enable=true
    if (!safeGetBoolean(virtualdmdConf, "enabled", false)) {
      conf.setProperty("virtualdmd enabled", true);
    }
    // else as virtual dmd is enabled, no need for key in the rom section
    else {
      conf.clearProperty("virtualdmd enabled");
    }
    // if the global alphaNumeric is enabled, force enable=false
    if (safeGetBoolean(alphaNumericConf, "enabled", false)) {
      conf.setProperty("alphanumeric enabled", false);
    }
    // else globally not enabled, no need for key in rom
    else {
      conf.clearProperty("alphanumeric enabled");
    }

    // now store the positions in the good section
    if (locallySaved) {
      conf.setProperty("virtualdmd left", (int) dmdinfoZone.getX());
      conf.setProperty("virtualdmd top", (int) dmdinfoZone.getY());
      conf.setProperty("virtualdmd width", (int) dmdinfoZone.getWidth());
      conf.setProperty("virtualdmd height", (int) dmdinfoZone.getHeight());
    }
    else {
      // else update the global ones
      virtualdmdConf.setProperty("left", (int) dmdinfoZone.getX());
      virtualdmdConf.setProperty("top", (int) dmdinfoZone.getY());
      virtualdmdConf.setProperty("width", (int) dmdinfoZone.getWidth());
      virtualdmdConf.setProperty("height", (int) dmdinfoZone.getHeight());
    }

    return saveDmdDeviceIni(game.getEmulator(), iniConfiguration);
  }

  private boolean saveAlphaNumericDMDInfoInIni(Game game, DMDInfo dmdinfo, INIConfiguration iniConfiguration) {
    String rom = StringUtils.defaultString(game.getRomAlias(), game.getRom());

    SubnodeConfiguration virtualdmdConf = iniConfiguration.getSection("virtualdmd");
    SubnodeConfiguration alphaNumericConf = iniConfiguration.getSection("alphanumeric");
    SubnodeConfiguration conf = iniConfiguration.getSection(rom.replace(".", ".."));

    // if the global virtualDMD is enabled, force enable=false
    if (safeGetBoolean(virtualdmdConf, "enabled", false)) {
      conf.setProperty("virtualdmd enabled", false);
    }
    // else as virtual dmd is not enabled, no need for key in the rom section
    else {
      conf.clearProperty("virtualdmd enabled");
    }
    // if the global alphaNumeric is not enabled, force enable=true
    if (!safeGetBoolean(alphaNumericConf, "enabled", false)) {
      conf.setProperty("alphanumeric enabled", true);
    }
    // else as alphanumeric dmd is enabled globally, no need for key in the rom section
    else {
      conf.clearProperty("alphanumeric enabled");
    }

    int pos = 0;
    for (DMDInfoZone score : dmdinfo.getZones()) {
      conf.setProperty("alphanumeric pos.." + pos + "..left", Math.round(score.getX()));
      conf.setProperty("alphanumeric pos.." + pos + "..top", Math.round(score.getY()));
      conf.setProperty("alphanumeric pos.." + pos + "..height", Math.round(score.getHeight()));
      // width is not used by external dmd but persisted by studio
      conf.setProperty("alphanumeric pos.." + pos + "..width", Math.round(score.getWidth()));
      pos++;
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

  private boolean isShowDmd(String rom) {
    return rom != null ? mameService.getOptions(rom).isShowDmd() : false;
  }

  private boolean useExternalDmd(String rom) {
    return rom != null ? mameService.getOptions(rom).isUseExternalDmd() : true;
  }

  private void setMameOptions(String rom, boolean showDmd, boolean useExternalDmd) {
    if (rom != null) {
      MameOptions options = mameService.getOptions(rom);
      options.setShowDmd(showDmd);
      options.setUseExternalDmd(useExternalDmd);
      mameService.saveOptions(options);
      gameLifecycleService.notifyGameAssetsChanged(AssetType.DMD_PACK, rom);
    }
  }

  private boolean useRegistry(INIConfiguration iniConfiguration) {
    SubnodeConfiguration conf = iniConfiguration.getSection("virtualdmd");
    return conf.containsKey("useregistry") ? conf.getBoolean("useregistry") : true;
  }

  private boolean keepAspectRatio(INIConfiguration iniConfiguration) {
    SubnodeConfiguration conf = iniConfiguration.getSection("virtualdmd");
    return conf.containsKey("ignorear") ? !conf.getBoolean("ignorear") : true;
  }

  private int safeGet(SubnodeConfiguration conf, String key) {
    return safeGet(conf, key, 0);
  }

  private int safeGet(SubnodeConfiguration conf, String key, int defValue) {
    if (conf != null && !conf.isEmpty() && conf.containsKey(key)) {
      try {
        double value = conf.getDouble(key);
        return (int) value;
      }
      catch (Exception e) {
        return defValue;
      }
    }
    return defValue;
  }

  private boolean safeGetBoolean(SubnodeConfiguration conf, String key, boolean defValue) {
    return conf != null && !conf.isEmpty() && conf.containsKey(key) ? conf.getBoolean(key) : defValue;
  }
}

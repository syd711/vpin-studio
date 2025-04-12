package de.mephisto.vpin.server.dmd;

import com.google.common.io.Files;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.dmd.DMDAspectRatio;
import de.mephisto.vpin.restclient.dmd.DMDInfo;
import de.mephisto.vpin.restclient.dmd.DMDInfoZone;
import de.mephisto.vpin.restclient.dmd.DMDType;
import de.mephisto.vpin.restclient.frontend.FrontendMedia;
import de.mephisto.vpin.restclient.frontend.FrontendMediaItem;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.mame.MameOptions;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.util.MimeTypeUtil;
import de.mephisto.vpin.restclient.converter.MediaConversionCommand;
import de.mephisto.vpin.server.directb2s.BackglassService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.VPinScreenService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.mame.MameService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.converter.MediaConverterService;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
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
  private FrontendService frontendService;
  @Autowired
  private MediaConverterService mediaConverterService;
  @Autowired
  private VPinScreenService screenService;
  @Autowired
  private PreferencesService preferenceService;


  public DMDInfo getDMDInfo(int gameId) {
    Game game = gameService.getGame(gameId);
    String rom = StringUtils.defaultString(game.getRomAlias(), game.getRom());

    DMDInfo dmdinfo = new DMDInfo();
    dmdinfo.setGameId(game.getId());
    dmdinfo.setGameRom(rom);

    boolean forceAspectRatio = false;
    if (useExternalDmd(rom)) {
      INIConfiguration iniConfiguration = loadDmdDeviceIni(game.getEmulator());
      if (iniConfiguration != null) {
        boolean hasRomInDmdDeviceIni = fillDMDInfoFromIni(dmdinfo, game, iniConfiguration);
        if (!hasRomInDmdDeviceIni && useRegistry(iniConfiguration)) {
          fillDMDInfoFromRegistry(dmdinfo);
        }
        forceAspectRatio = keepAspectRatio(iniConfiguration);
      }
    }
    else if (isShowDmd(rom)) {
      dmdinfo.setDMDType(DMDType.VpinMAMEDMD);
      fillDMDInfoFromRegistry(dmdinfo);
    }
    else {
      dmdinfo.setDMDType(DMDType.NoDMD);
    }

    if (dmdinfo.getWidth() == 0) {
      dmdinfo.setWidth(100);
    }
    if (dmdinfo.getHeight() == 0) {
      dmdinfo.setHeight(dmdinfo.getWidth() / 4);
    }

    dmdinfo.setForceAspectRatio(forceAspectRatio);
    if (forceAspectRatio) {
      //TODO: dmdinfo.setSelectedAspectRatio(true);
    }
    else {
      dmdinfo.setAspectRatio(DMDAspectRatio.ratioOff);
      // if existing dmd size ratio is close to 4:1, activate the checkbox
      double ratio = dmdinfo.getWidth() / dmdinfo.getHeight();
      if (Math.abs(ratio - 4) < 0.01) {
        dmdinfo.setAspectRatio(DMDAspectRatio.ratio4x1);
      }
    }

    // then add screen information, must be done after x,y are set
    fillScreenInfo(dmdinfo, game);
    // enforce aspect ratio if selected
    dmdinfo.adjustAspectRatio();

    // load the preference on how to save dmd disablement
    ServerSettings serverSettings = preferenceService.getJsonPreference(PreferenceNames.SERVER_SETTINGS);
    dmdinfo.setDisableViaIni(serverSettings.isDisableDmdViaIni());
    dmdinfo.setDisableInVpinMame(serverSettings.isDisableDmdInMame());

    return dmdinfo;
  }

  private void fillDMDInfoFromRegistry(DMDInfo dmdinfo) {
    if (dmdinfo.getGameRom() != null) {
      dmdinfo.setUseRegistry(true);
      mameService.fillDmdPosition(dmdinfo);
    }
  }

  private boolean fillDMDInfoFromIni(DMDInfo info, Game game, INIConfiguration iniConfiguration) {
    if (info.getGameRom() != null) {
      SubnodeConfiguration virtualdmdConf = iniConfiguration.getSection("virtualdmd");
      SubnodeConfiguration alphaNumericConf = iniConfiguration.getSection("alphanumeric");
      SubnodeConfiguration conf = iniConfiguration.getSection(info.getGameRom());

      info.setLocallySaved(!conf.isEmpty());
      info.setUseRegistry(false);
      info.setX(safeGet(conf, "virtualdmd left", safeGet(virtualdmdConf, "left")));
      info.setY(safeGet(conf, "virtualdmd top", safeGet(virtualdmdConf, "top")));
      info.setWidth(safeGet(conf, "virtualdmd width", safeGet(virtualdmdConf, "width")));
      info.setHeight(safeGet(conf, "virtualdmd height", safeGet(virtualdmdConf, "height")));

      // load alphanumeric scores
      DirectB2SData data = backglassService.getDirectB2SData(game);
      if (data !=  null) {
        int nbScores = data.getScores();
        for (int pos = 0; pos < nbScores; pos++) {
          double alphaLeft = safeGet(conf, "alphanumeric pos." + pos + ".left", (pos + 1) * 10);
          double alphaTop = safeGet(conf, "alphanumeric pos." + pos + ".top", (pos + 1) * 10);
          double alphaHeight = safeGet(conf, "alphanumeric pos." + pos + ".height", 100);
    
          DMDInfoZone rect = new DMDInfoZone(alphaLeft, alphaTop, alphaHeight / 4, alphaHeight);
          info.addAlphaNumericScore(rect);  
        }
      }

      boolean virtualDmdEnabled = safeGetBoolean(conf, "virtualdmd enabled", safeGetBoolean(virtualdmdConf, "enabled", false));
      boolean alphaNumericEnabled = safeGetBoolean(conf, "alphanumeric enabled", safeGetBoolean(alphaNumericConf, "enabled", false));
      info.setDMDType(virtualDmdEnabled ? DMDType.VirtualDMD : 
          alphaNumericEnabled ? DMDType.AlphaNumericDMD : DMDType.NoDMD);

      return !conf.isEmpty();
    }
    return false;
  }

  private void fillScreenInfo(DMDInfo dmdinfo, Game game) {
    List<FrontendPlayerDisplay> screenResDisplays = screenService.getScreenResDisplays(game);
    //DirectB2sScreenRes screenres = backglassService.getScreenRes(game, false);
    //screenService.addDeviceOffsets(screenres);

    // determine on which screen the DMD is positionned onto
    boolean ondisplay = false;
    for (FrontendPlayerDisplay display : screenResDisplays) {
      if (display.contains(dmdinfo.getCenterX(), dmdinfo.getCenterY())) {
        fillScreenInfo(dmdinfo, display);
        ondisplay = true;
      }
    }

    FrontendPlayerDisplay bgDisplay = FrontendPlayerDisplay.valueOfScreen(screenResDisplays, VPinScreen.BackGlass);
    if (!ondisplay && bgDisplay != null) {
      fillScreenInfo(dmdinfo, bgDisplay);
      dmdinfo.centerOnScreen();
    }

    FrontendPlayerDisplay fulldmdDisplay = FrontendPlayerDisplay.valueOfScreen(screenResDisplays, VPinScreen.Menu);
    dmdinfo.setDmdScreenSet(fulldmdDisplay != null);
  }

  private void fillScreenInfo(DMDInfo dmdinfo, FrontendPlayerDisplay display) {

    dmdinfo.setOnScreen(display.getScreen());

    dmdinfo.setX(dmdinfo.getX() - display.getMinX());
    dmdinfo.setY(dmdinfo.getY() - display.getMinY());
    dmdinfo.setScreenWidth(display.getWidth());
    dmdinfo.setScreenHeight(display.getHeight());

    dmdinfo.setImageCentered(false);

    reposition(dmdinfo);
  }

  private void reposition(DMDInfo dmdinfo) {
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
    Game game = gameService.getGame(dmdinfo.getGameId());
    
    List<FrontendPlayerDisplay> screenResDisplays = screenService.getScreenResDisplays(game);

    // Reposition DMD relative to upper left corner of the backglass screen
    //FrontendPlayerDisplay display = FrontendPlayerDisplay.valueOfScreen(screenResDisplays, dmdinfo.getOnScreen());

    // now position on the new target screen
    if (targetScreen != null) {
      dmdinfo.setOnScreen(targetScreen);
      FrontendPlayerDisplay display = FrontendPlayerDisplay.valueOfScreen(screenResDisplays, targetScreen);
      //DirectB2sScreenRes screenres = backglassService.getScreenRes(game, false);
      if (display != null) {
        dmdinfo.setScreenWidth(display.getWidth());
        dmdinfo.setScreenHeight(display.getHeight());
    
        dmdinfo.setImageCentered(false);
    
        reposition(dmdinfo);
      }
    }
    return dmdinfo;
  }

  public DMDInfo autoPositionDMDInfo(DMDInfo dmdinfo) {
    Game game = gameService.getGame(dmdinfo.getGameId());
    List<FrontendPlayerDisplay> screenResDisplays = screenService.getScreenResDisplays(game);
    //DirectB2sScreenRes screenres = backglassService.getScreenRes(game, false);

    FrontendPlayerDisplay display = FrontendPlayerDisplay.valueOfScreen(screenResDisplays, dmdinfo.getOnScreen());
    if (display != null) {
      double factorX = display.getWidth(), factorY = display.getHeight();

      byte[] image = null;
      if (VPinScreen.BackGlass.equals(dmdinfo.getOnScreen())) {
        image = backglassService.getPreviewBackground(game, false);
      }
      else if (VPinScreen.Menu.equals(dmdinfo.getOnScreen())) {
        image = backglassService.getPreviewDmd(game);
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
            dmdinfo.setX(position.get(0) * factorX + dmdinfo.getMargin());
            dmdinfo.setY(position.get(1) * factorY + dmdinfo.getMargin());
            dmdinfo.setWidth((position.get(2) - position.get(0)) * factorX - 2 * dmdinfo.getMargin());
            dmdinfo.setHeight((position.get(3) - position.get(1)) * factorY - 2 * dmdinfo.getMargin());
          }
        }
        catch (IOException ioe) {
          LOG.error("cannot generate image from dmd base64 data", ioe);
        }
      }
    }

    // enforce aspect ratio if selected
    dmdinfo.adjustAspectRatio();
    return dmdinfo;
  }

  //------------------------------------
  // SAVE

  public boolean saveDMDInfo(DMDInfo dmdinfo) {
    Game game = gameService.getGame(dmdinfo.getGameId());
    String rom = StringUtils.defaultString(game.getRomAlias(), game.getRom());

    List<FrontendPlayerDisplay> screenResDisplays = screenService.getScreenResDisplays(game);
    //DirectB2sScreenRes screenres = backglassService.getScreenRes(game, false);
    //screenService.addDeviceOffsets(screenres);

    // enforce aspect ratio
    dmdinfo.adjustAspectRatio();

    // Reposition DMD relative to upper left corner of the backglass screen
    FrontendPlayerDisplay display = FrontendPlayerDisplay.valueOfScreen(screenResDisplays, dmdinfo.getOnScreen());
    if (display != null) {
      dmdinfo.setX(dmdinfo.getX() + display.getMinX());
      dmdinfo.setY(dmdinfo.getY() + display.getMinY());
      dmdinfo.setScreenWidth(display.getWidth());
      dmdinfo.setScreenHeight(display.getHeight());
    }

    // round to int all number
    dmdinfo.setX(Math.round(dmdinfo.getX()));
    dmdinfo.setY(Math.round(dmdinfo.getY()));
    dmdinfo.setWidth(Math.round(dmdinfo.getWidth()));
    dmdinfo.setHeight(Math.round(dmdinfo.getHeight()));

    //----------
    // Case of NO DMD
    if (dmdinfo.getDMDType().equals(DMDType.NoDMD)) {
      if (dmdinfo.isDisableViaIni()) {
        disableDMDInIni(game, rom);
      }
      if (dmdinfo.isDisableInVpinMame()) {
        setMameOptions(rom, false, false);
      }

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
      return mameService.saveDmdPosition(dmdinfo);
    }
    //----------
    // case of VirtualDMD
    else if (dmdinfo.getDMDType().equals(DMDType.VirtualDMD)) {
      setMameOptions(rom, false, true);

      INIConfiguration iniConfiguration = loadDmdDeviceIni(game.getEmulator());
      if (iniConfiguration != null) {
        if (useRegistry(iniConfiguration)) {
          return saveDMDInfoInRegistry(game, dmdinfo, iniConfiguration);
        }
        else {
          return saveVirtualDMDInfoInIni(game, dmdinfo, iniConfiguration);
        }
      }
    }
    //----------
    // case of AlphaNumericDMD
    else if (dmdinfo.getDMDType().equals(DMDType.AlphaNumericDMD)) {
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

  private void disableDMDInIni(Game game, String rom) {
    // the deactivation is always stored in the rom section
    INIConfiguration iniConfiguration = loadDmdDeviceIni(game.getEmulator());
    if (iniConfiguration != null && rom != null) {
      SubnodeConfiguration virtualdmdConf = iniConfiguration.getSection("virtualdmd");
      SubnodeConfiguration alphaNumericConf = iniConfiguration.getSection("alphanumeric");
      SubnodeConfiguration conf = iniConfiguration.getSection(rom);

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

  private boolean saveDMDInfoInRegistry(Game game, DMDInfo dmdinfo, INIConfiguration iniConfiguration) {
    // clear any values in dmddevice that could overwrite registry values
    String rom = StringUtils.defaultString(game.getRomAlias(), game.getRom());

    // mind that iniConfiguration can be null if externalDMD is not used
    if (iniConfiguration != null) {
      SubnodeConfiguration conf = iniConfiguration.getSection(rom);
      conf.clear();

      SubnodeConfiguration virtualdmdConf = iniConfiguration.getSection("virtualdmd");
      // if the global virtualconf is not enabled, force enable=true
      if (!safeGetBoolean(virtualdmdConf, "enabled", false)) {
        conf.setProperty("virtualdmd enabled", true);
      }
      saveDmdDeviceIni(game.getEmulator(), iniConfiguration);
    }

    return mameService.saveDmdPosition(dmdinfo);
  }

  private boolean saveVirtualDMDInfoInIni(Game game, DMDInfo dmdinfo, INIConfiguration iniConfiguration) {
    String rom = StringUtils.defaultString(game.getRomAlias(), game.getRom());
    SubnodeConfiguration virtualdmdConf = iniConfiguration.getSection("virtualdmd");
    SubnodeConfiguration alphaNumericConf = iniConfiguration.getSection("alphanumeric");
    SubnodeConfiguration conf = iniConfiguration.getSection(rom);

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
    if (dmdinfo.isLocallySaved()) {
      conf.setProperty("virtualdmd left", (int) dmdinfo.getX());
      conf.setProperty("virtualdmd top", (int) dmdinfo.getY());
      conf.setProperty("virtualdmd width", (int) dmdinfo.getWidth());
      conf.setProperty("virtualdmd height", (int) dmdinfo.getHeight());
    }
    else {
      // else update the global ones
      virtualdmdConf.setProperty("left", (int) dmdinfo.getX());
      virtualdmdConf.setProperty("top", (int) dmdinfo.getY());
      virtualdmdConf.setProperty("width", (int) dmdinfo.getWidth());
      virtualdmdConf.setProperty("height", (int) dmdinfo.getHeight());
    }

    return saveDmdDeviceIni(game.getEmulator(), iniConfiguration);
  }

  private boolean saveAlphaNumericDMDInfoInIni(Game game, DMDInfo dmdinfo, INIConfiguration iniConfiguration) {
    String rom = StringUtils.defaultString(game.getRomAlias(), game.getRom());
    SubnodeConfiguration virtualdmdConf = iniConfiguration.getSection("virtualdmd");
    SubnodeConfiguration alphaNumericConf = iniConfiguration.getSection("alphanumeric");
    SubnodeConfiguration conf = iniConfiguration.getSection(rom);

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
    for (DMDInfoZone score : dmdinfo.getAlphaNumericScores()) {
      conf.setProperty("alphanumeric pos." + pos + ".left", Math.round(score.getX()));
      conf.setProperty("alphanumeric pos." + pos + ".top", Math.round(score.getY()));
      conf.setProperty("alphanumeric pos." + pos + ".height", Math.round(score.getHeight()));
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
    return rom != null ? mameService.getOptions(rom).isUseExternalDmd() : false;
  }

  private void setMameOptions(String rom, boolean showDmd, boolean useExternalDmd) {
    if (rom != null) {
      MameOptions options = mameService.getOptions(rom);
      options.setShowDmd(showDmd);
      options.setUseExternalDmd(useExternalDmd);
      mameService.saveOptions(options);
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

  private double safeGet(SubnodeConfiguration conf, String key) {
    return safeGet(conf, key, 0);
  }

  private double safeGet(SubnodeConfiguration conf, String key, double defValue) {
    return !conf.isEmpty() && conf.containsKey(key) ? conf.getDouble(key) : defValue;
  }

  private boolean safeGetBoolean(SubnodeConfiguration conf, String key, boolean defValue) {
    return conf.containsKey(key) ? conf.getBoolean(key) : defValue;
  }

  public byte[] getPicture(int gameId, VPinScreen onScreen) {
    Game game = gameService.getGame(gameId);
    if (VPinScreen.BackGlass.equals(onScreen)) {
      return backglassService.getPreviewBackground(game, true);
    }
    else if (VPinScreen.Menu.equals(onScreen)) {
      DirectB2STableSettings tableSettings = backglassService.getTableSettings(game);
      String base64 = backglassService.getDmdBase64(game.getEmulatorId(), game.getDirectB2SFilename());

      // use B2S DMD image if present and not hidden
      if (base64 != null && !(tableSettings != null && tableSettings.isHideB2SDMD())) {
        return DatatypeConverter.parseBase64Binary(base64);
      }
      else {
        TableDetails tableDetails = frontendService.getTableDetails(gameId);
        String keepDisplays = tableDetails != null ? tableDetails.getKeepDisplays() : null;
        if (StringUtils.isNotEmpty(keepDisplays)) {
          boolean keepFullDmd = VPinScreen.keepDisplaysContainsScreen(keepDisplays, VPinScreen.Menu);
          if (keepFullDmd) {
            FrontendMedia frontendMedia = frontendService.getGameMedia(gameId);
            FrontendMediaItem item = frontendMedia.getDefaultMediaItem(VPinScreen.Menu);
            if (item != null) {
              String baseType = MimeTypeUtil.determineBaseType(item.getMimeType());
              if ("video".equals(baseType)) {
                return extractFrame(item.getFile());
              }
              else if ("image".equals(baseType)) {
                return extractImage(item.getFile());
              }
            }
          }
        }
      }
    }
    // else all other cases
    return null;
  }

  /**
   * Extracts a frame from a video file.
   * ffmpeg -i vido.mp4 -ss 00:00:05 -vframes 1 frame_out.jpg
   *
   * @param file the video file
   */
  private byte[] extractFrame(File file) {
    MediaConversionCommand cmd = new MediaConversionCommand("Extract Frame").setFFmpegArgs("-ss 00:00:01 -vframes 1");
    File targetFile = null;
    try {
      targetFile = File.createTempFile("ef_", ".png");
      mediaConverterService.convertWithFfmpeg(cmd, file, targetFile);
      return Files.toByteArray(targetFile);
    }
    catch (Exception e) {
      LOG.error("Cannot extract frame from video", e);
    }
    finally {
      if (targetFile != null) {
        targetFile.delete();
      }
    }
    return null;
  }

  private byte[] extractImage(File file) {
    try {
      return Files.toByteArray(file);
    }
    catch (IOException e) {
      LOG.error("Cannot read image file", e);
    }
    return null;
  }
}

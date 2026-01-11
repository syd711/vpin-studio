package de.mephisto.vpin.server.dmd;

import de.mephisto.vpin.restclient.dmd.DMDDeviceIniConfiguration;
import de.mephisto.vpin.restclient.dmd.DMDInfo;
import de.mephisto.vpin.restclient.dmd.DMDInfoZone;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.mame.MameService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static de.mephisto.vpin.server.ini.IniUtil.safeGetBoolean;

@Service
public class DMDDeviceIniService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static final String DMD_DEVICE_INI = "DmdDevice.ini";

  private final Map<Integer, INIConfiguration> dmdDeviceIniFiles = new HashMap<>();

  @Autowired
  private EmulatorService emulatorService;

  @Lazy
  @Autowired
  private MameService mameService;


  public DMDDeviceIniConfiguration getDmdDeviceIni(@NonNull GameEmulator gameEmulator) {
    loadDmdDeviceIni(gameEmulator);

    INIConfiguration dmdDeviceIni = dmdDeviceIniFiles.get(gameEmulator.getId());

    DMDDeviceIniConfiguration config = new DMDDeviceIniConfiguration();
    config.setEmulatorId(gameEmulator.getId());

    SubnodeConfiguration section = dmdDeviceIni.getSection("networkstream");
    if (section != null) {
      config.setNetworkStreamEnabled(section.getBoolean("enabled", false));
      config.setWebSocketUrl(section.getString("url", "ws://127.0.0.1/dmd"));
    }

    section = dmdDeviceIni.getSection("virtualdmd");
    if (section != null) {
      config.setUseRegistry(section.getBoolean("useregistry", false));
      config.setStayOnTop(section.getBoolean("stayontop", false));
      config.setEnabled(section.getBoolean("enabled", false));
    }
    return config;
  }

  public DMDDeviceIniConfiguration save(DMDDeviceIniConfiguration dmddeviceini) {
    GameEmulator gameEmulator = emulatorService.getGameEmulator(dmddeviceini.getEmulatorId());
    if (!dmdDeviceIniFiles.containsKey(dmddeviceini.getEmulatorId())) {
      loadDmdDeviceIni(gameEmulator);
    }

    INIConfiguration dmdDeviceIni = dmdDeviceIniFiles.get(gameEmulator.getId());
    SubnodeConfiguration section = dmdDeviceIni.getSection("networkstream");
    boolean dirty = false;
    if (section != null) {
      section.setProperty("enabled", dmddeviceini.isNetworkStreamEnabled());
      section.setProperty("url", dmddeviceini.getWebSocketUrl());
      dirty = true;
    }

    section = dmdDeviceIni.getSection("virtualdmd");
    if (section != null) {
      section.setProperty("useregistry", dmddeviceini.isUseRegistry());
      section.setProperty("stayontop", dmddeviceini.isStayOnTop());
      section.setProperty("enabled", dmddeviceini.isEnabled());
      dirty = true;
    }

    if (dirty) {
      saveDmdDeviceIni(gameEmulator, dmdDeviceIni);
    }

    return getDmdDeviceIni(gameEmulator);
  }

  public boolean saveVirtualDMDInfoInIni(@NonNull Game game, DMDInfo dmdinfo, DMDInfoZone dmdinfoZone, boolean locallySaved) {
    INIConfiguration iniConfiguration = getIniConfiguration(game);
    String dmdStoreName = dmdinfo.getDmdStoreName();
    if (dmdinfo.getDmdStoreName().contains(".")) {
      dmdStoreName = dmdStoreName.replace(".", "..");
    }

    SubnodeConfiguration virtualdmdConf = iniConfiguration.getSection("virtualdmd");
    SubnodeConfiguration alphaNumericConf = iniConfiguration.getSection("alphanumeric");
    //SubnodeConfiguration conf = iniConfiguration.getSection();

    // if the global virtualDMD is not enabled, force enable=true
    if (!safeGetBoolean(virtualdmdConf, "enabled", false)) {
      iniConfiguration.setProperty(dmdStoreName + ".virtualdmd enabled", true);
    }
    // else as virtual dmd is enabled, no need for key in the rom section
    else {
      iniConfiguration.clearProperty(dmdStoreName + ".virtualdmd enabled");
    }
    // if the global alphaNumeric is enabled, force enable=false
    if (safeGetBoolean(alphaNumericConf, "enabled", false)) {
      iniConfiguration.setProperty(dmdStoreName + ".alphanumeric enabled", false);
    }
    // else globally not enabled, no need for key in rom
    else {
      iniConfiguration.clearProperty(dmdStoreName + ".alphanumeric enabled");
    }

    // now store the positions in the good section
    if (locallySaved) {
      iniConfiguration.setProperty(dmdStoreName + ".virtualdmd left", (int) dmdinfoZone.getX());
      iniConfiguration.setProperty(dmdStoreName + ".virtualdmd top", (int) dmdinfoZone.getY());
      iniConfiguration.setProperty(dmdStoreName + ".virtualdmd width", (int) dmdinfoZone.getWidth());
      iniConfiguration.setProperty(dmdStoreName + ".virtualdmd height", (int) dmdinfoZone.getHeight());
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

  public boolean saveAlphaNumericDMDInfoInIni(Game game, DMDInfo dmdinfo) {
    INIConfiguration iniConfiguration = getIniConfiguration(game);
    if (iniConfiguration == null) {
      return false;
    }

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


  public void disableDMDInIni(Game game, String rom) {
    INIConfiguration iniConfiguration = getIniConfiguration(game);
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

  public boolean saveDMDInfoInRegistry(Game game, DMDInfoZone dmdinfo) {
    // clear any values in dmddevice that could overwrite registry values
    String rom = StringUtils.defaultString(game.getRomAlias(), game.getRom());

    // mind that iniConfiguration can be null if externalDMD is not used
    INIConfiguration iniConfiguration = getIniConfiguration(game);
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

  @Nullable
  public INIConfiguration getIniConfiguration(Game game) {
    GameEmulator gameEmulator = game.getEmulator();

    // the deactivation is always stored in the rom section
    if (!dmdDeviceIniFiles.containsKey(gameEmulator.getId())) {
      loadDmdDeviceIni(gameEmulator);
    }

    return dmdDeviceIniFiles.get(gameEmulator.getId());
  }

  public boolean saveDmdDeviceIni(GameEmulator emulator, INIConfiguration iniConfiguration) {
    File iniFile = new File(emulator.getMameFolder(), DMD_DEVICE_INI);
    if (!iniFile.exists()) {
      return false;
    }

    try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(iniFile), StandardCharsets.UTF_8);
         BufferedWriter writer = new BufferedWriter(osw)) {
      writer.write('\ufeff');
      iniConfiguration.write(writer);
      LOG.info("Written DMD configuration {}", iniFile.getAbsolutePath());
      return true;
    }
    catch (Exception e) {
      LOG.error("Failed to write dmddevice.ini", e);
      return false;
    }
  }

  private void loadDmdDeviceIni(@NonNull GameEmulator gameEmulator) {
    File iniFile = new File(gameEmulator.getMameFolder(), DMD_DEVICE_INI);
    if (!iniFile.exists()) {
      LOG.error("No {} file found.", DMD_DEVICE_INI);
      return;
    }

    INIConfiguration dmdDeviceIni = new INIConfiguration();
    dmdDeviceIni.setCommentLeadingCharsUsedInInput(";");
    dmdDeviceIni.setSeparatorUsedInOutput(" = ");
    dmdDeviceIni.setSeparatorUsedInInput("=");

    String defaultCharset = "UTF-8";
    try (FileInputStream in = new FileInputStream(iniFile)) {
      try (BOMInputStream bOMInputStream = BOMInputStream.builder().setInputStream(in).get()) {
        ByteOrderMark bom = bOMInputStream.getBOM();
        String charsetName = bom == null ? defaultCharset : bom.getCharsetName();
        try (InputStreamReader reader = new InputStreamReader(new BufferedInputStream(bOMInputStream), charsetName)) {
          dmdDeviceIni.read(reader);
        }
        catch (Exception e) {
          LOG.error("Cannot parse {}", iniFile.getAbsolutePath(), e);
        }

        dmdDeviceIniFiles.put(gameEmulator.getId(), dmdDeviceIni);
      }
      catch (Exception e) {
        LOG.error("Cannot decode charset of {}}", iniFile.getAbsolutePath(), e);
      }
    }
    catch (Exception e) {
      LOG.error("Cannot open {}", iniFile.getAbsolutePath(), e);
    }
  }
}

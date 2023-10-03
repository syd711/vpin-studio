package de.mephisto.vpin.server.games;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.popper.Emulator;
import de.mephisto.vpin.server.roms.RomService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

public class GameEmulator {
  private final static Logger LOG = LoggerFactory.getLogger(RomService.class);

  private Map<String, String> aliasToRomMapping = new HashMap<>();

  private final static String VPREG_STG = "VPReg.stg";
  private final static String VPM_ALIAS = "VPMAlias.txt";

  private final File installationFolder;
  private final File tablesFolder;
  private final File gameMediaFolder;
  private final File mameFolder;
  private final File userFolder;
  private final File altSoundFolder;
  private final File altColorFolder;
  private final File musicFolder;
  private final File nvramFolder;

  private File romFolder;

  private final String name;
  private final String description;
  private final String installationDirectory;
  private final String tablesDirectory;
  private final String altSoundDirectory;
  private final String altColorDirectory;
  private final String nvramDirectory;
  private final String mameDirectory;
  private final String userDirectory;
  private final String mediaDirectory;
  private final int id;

  public GameEmulator(@NonNull Emulator emulator) {
    this.id = emulator.getId();
    this.name = emulator.getName();
    this.description = emulator.getDescription();

    this.installationDirectory = emulator.getEmuLaunchDir();
    this.tablesDirectory = emulator.getDirGames();
    this.mediaDirectory = emulator.getDirMedia();

    this.installationFolder = new File(emulator.getEmuLaunchDir());
    this.tablesFolder = new File(emulator.getDirGames());
    this.gameMediaFolder = new File(emulator.getDirMedia());
    this.musicFolder = new File(installationFolder, "Music");

    this.mameFolder = new File(installationFolder, "VPinMAME");
    this.mameDirectory = this.mameFolder.getAbsolutePath();

    this.userFolder = new File(installationFolder, "User");
    this.userDirectory = userFolder.getAbsolutePath();

    this.nvramFolder = new File(mameFolder, "nvram");
    this.nvramDirectory = this.nvramFolder.getAbsolutePath();

    this.altSoundFolder = new File(mameFolder, "altsound");
    this.altSoundDirectory = this.altSoundFolder.getAbsolutePath();

    this.altColorFolder = new File(mameFolder, "altcolor");
    this.altColorDirectory = this.altColorFolder.getAbsolutePath();

    this.romFolder = new File(mameFolder, "roms");
    if (!StringUtils.isEmpty(emulator.getDirRoms())) {
      this.romFolder = new File(emulator.getDirRoms());
    }

    loadAliasMapping();
  }

  public String getMediaDirectory() {
    return mediaDirectory;
  }

  public String getMameDirectory() {
    return mameDirectory;
  }

  public String getUserDirectory() {
    return userDirectory;
  }

  public String getNvramDirectory() {
    return nvramDirectory;
  }

  public String getAltSoundDirectory() {
    return altSoundDirectory;
  }

  public String getAltColorDirectory() {
    return altColorDirectory;
  }

  public String getInstallationDirectory() {
    return installationDirectory;
  }

  public String getTablesDirectory() {
    return tablesDirectory;
  }

  public String getRomForAlias(@Nullable String romAlias) {
    if (romAlias == null) {
      return null;
    }

    Set<Map.Entry<String, String>> entries = aliasToRomMapping.entrySet();
    for (Map.Entry<String, String> entry : entries) {
      String alias = entry.getKey();
      String romName = entry.getValue();

      if (alias.equals(romAlias)) {
        return romName;
      }
    }
    return null;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public int getId() {
    return id;
  }

  public File getVPXExe() {
    return new File(installationFolder, "VPinballX.exe");
  }

  public boolean isVpx() {
    return true;
  }

  public List<String> getAltExeNames() {
    String[] exeFiles = installationFolder.list((dir, name) -> name.endsWith(".exe") && name.toLowerCase().contains("vpin"));
    if (exeFiles == null) {
      exeFiles = new String[]{};
    }
    return Arrays.asList(exeFiles);
  }

  @NonNull
  @JsonIgnore
  public File getB2STableSettingsXml() {
    return new File(this.tablesFolder, "B2STableSettings.xml");
  }

  @NonNull
  @JsonIgnore
  public File getNvramFolder() {
    return nvramFolder;
  }

  @NonNull
  @JsonIgnore
  public File getVPRegFile() {
    return new File(userFolder, VPREG_STG);
  }

  @NonNull
  @JsonIgnore
  public File getMusicFolder() {
    return musicFolder;
  }

  @NonNull
  @JsonIgnore
  public File getAltSoundFolder() {
    return altSoundFolder;
  }

  @NonNull
  @JsonIgnore
  public File getAltColorFolder() {
    return altColorFolder;
  }

  @NonNull
  @JsonIgnore
  public File getRomFolder() {
    return romFolder;
  }

  @NonNull
  @JsonIgnore
  public File getMameFolder() {
    return mameFolder;
  }

  @NonNull
  @JsonIgnore
  public File getUserFolder() {
    return userFolder;
  }

  @NonNull
  @JsonIgnore
  public File getInstallationFolder() {
    return installationFolder;
  }

  @NonNull
  @JsonIgnore
  public File getGameMediaFolder() {
    return gameMediaFolder;
  }

  @NonNull
  @JsonIgnore
  public File getTablesFolder() {
    return tablesFolder;
  }

  @NonNull
  @JsonIgnore
  public File getVPMAliasFile() {
    return new File(mameFolder, VPM_ALIAS);
  }


  public boolean clearCache() {
    this.aliasToRomMapping.clear();
    this.loadAliasMapping();
    return true;
  }

  public boolean deleteAliasMapping(String alias) throws IOException {
    if (!StringUtils.isEmpty(alias) && aliasToRomMapping.containsKey(alias)) {
      aliasToRomMapping.remove(alias);
      LOG.info("Removed alias mapping '" + alias + "'");
      saveMapping();
    }
    return false;
  }

  public boolean saveAliasMapping(Map<String, Object> values) throws IOException {
    String oldValue = (String) values.get("#oldValue");
    System.out.println(values);
    if (!StringUtils.isEmpty(oldValue) && aliasToRomMapping.containsKey(oldValue)) {
      aliasToRomMapping.remove(oldValue);
      LOG.info("Removed old alias mapping '" + oldValue + "'");
    }
    values.remove("#oldValue");


    Set<Map.Entry<String, Object>> entries = values.entrySet();
    for (Map.Entry<String, Object> entry : entries) {
      String alias = entry.getKey();
      String romName = (String) entry.getValue();

      if (!aliasToRomMapping.containsValue(alias)) {
        aliasToRomMapping.put(alias.trim(), romName.trim());
      }
    }

    saveMapping();
    return true;
  }

  private void saveMapping() throws IOException {
    String mapAsString = aliasToRomMapping.keySet().stream().map(key -> key.trim() + "," + aliasToRomMapping.get(key).trim()).sorted().collect(Collectors.joining("\n"));
    File vpmAliasFile = getVPMAliasFile();
    FileUtils.writeStringToFile(vpmAliasFile, mapAsString, Charset.defaultCharset());
    loadAliasMapping();
  }

  private void loadAliasMapping() {
    File vpmAliasFile = getVPMAliasFile();
    try {
      if (vpmAliasFile.exists()) {
        FileInputStream fileInputStream = new FileInputStream(vpmAliasFile);
        List<String> mappings = IOUtils.readLines(fileInputStream, "utf8");
        fileInputStream.close();

        for (String mapping : mappings) {
          if (mapping.contains(",")) {
            String[] split = mapping.split(",");
            String[] aliases = Arrays.copyOfRange(split, 0, split.length - 1);
            String originalRom = split[split.length - 1];

            for (String alias : aliases) {
              aliasToRomMapping.put(alias, originalRom);
            }
          }
        }
        LOG.info("Loaded " + aliasToRomMapping.size() + " alias mappings.");
      }
    } catch (IOException e) {
      LOG.error("Error loading " + vpmAliasFile.getAbsolutePath() + ": " + e.getMessage(), e);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GameEmulator)) return false;

    GameEmulator that = (GameEmulator) o;

    return id == that.id;
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public String toString() {
    return "\"" + this.name + "\" (ID: " + this.id + ")";
  }
}

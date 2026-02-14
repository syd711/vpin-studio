package de.mephisto.vpin.restclient.system;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ScoringDB {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static String SCORING_DB_NAME = "scoringdb.json";
  public final static String URL = "https://raw.githubusercontent.com/syd711/vpin-studio/main/resources/scoringdb.json";

  private static final ObjectMapper objectMapper;

  static {
    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  private static File getScoringDBFile() {
    //TODO why not SystemService.RESOURCES ???
    // OR SYstemService.getScoringDatabase()
    File resFolder = new File("./resources/");
    if (!resFolder.exists()) {
      resFolder = new File("../resources/");
    }
    return new File(resFolder, SCORING_DB_NAME);
  }

  public static ScoringDB load() {
    FileInputStream in = null;
    ScoringDB db = null;
    try {
      File dbFile = getScoringDBFile();
      in = new FileInputStream(dbFile);
      db = objectMapper.readValue(in, ScoringDB.class);
      LOG.info("Loaded " + dbFile.getName() + ", last updated: " + SimpleDateFormat.getDateTimeInstance().format(new Date(dbFile.lastModified())));
    }
    catch (Exception e) {
      db = new ScoringDB();
      LOG.error("Failed to load scoring DB json: " + e.getMessage());
    }
    finally {
      try {
        if (in != null) {
          in.close();
        }
      }
      catch (IOException e) {
        //ignore
      }
    }
    return db;
  }

  public static void update() {
    try {
      LOG.info("Downloading " + SCORING_DB_NAME);
      java.net.URL url = new URL(ScoringDB.URL);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);
      BufferedInputStream in = new BufferedInputStream(url.openStream());
      File dbFile = getScoringDBFile();
      File tmp = new File(dbFile.getParentFile(), SCORING_DB_NAME + ".tmp");
      if (tmp.exists() && !tmp.delete()) {
        LOG.error("Failed to delete existing tmp file " + SCORING_DB_NAME + ".tmp");
        return;
      }
      FileOutputStream fileOutputStream = new FileOutputStream(tmp);
      byte dataBuffer[] = new byte[1024];
      int bytesRead;
      while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
        fileOutputStream.write(dataBuffer, 0, bytesRead);
      }
      in.close();
      fileOutputStream.close();

      long oldSize = 0;
      if (dbFile.exists()) {
        oldSize = dbFile.length();
      }

      if (tmp.length() > 0) {
        if (dbFile.exists() && !dbFile.delete()) {
          LOG.error("Failed to delete " + SCORING_DB_NAME);
        }

        if (!tmp.renameTo(dbFile)) {
          LOG.error("Failed to rename " + SCORING_DB_NAME);
          return;
        }
        LOG.info("Written " + dbFile.getAbsolutePath() + ", (" + oldSize + " vs " + dbFile.length() + " bytes)");
      }
      else {
        tmp.delete();
      }
    }
    catch (IOException e) {
      LOG.error(SCORING_DB_NAME + " download failed: " + e.getMessage());
    }
  }

  private String pinemhiVersion;

  private List<String> highscoreTitles = new ArrayList<>();

  private List<Map<String, Object>> highscoreTextParsers = new ArrayList<>();

  private List<Map<String, Object>> highscoreIniParsers = new ArrayList<>();

  private List<Map<String, Object>> highscoreVPRegParsers = new ArrayList<>();

  public List<Map<String, Object>> getHighscoreVPRegParsers() {
    return highscoreVPRegParsers;
  }

  public void setHighscoreVPRegParsers(List<Map<String, Object>> highscoreVPRegParsers) {
    this.highscoreVPRegParsers = highscoreVPRegParsers;
  }

  public List<Map<String, Object>> getHighscoreIniParsers() {
    return highscoreIniParsers;
  }

  public void setHighscoreIniParsers(List<Map<String, Object>> highscoreIniParsers) {
    this.highscoreIniParsers = highscoreIniParsers;
  }

  private List<ScoringDBMapping> highscoreMappings = new ArrayList<>();

  private List<String> supportedNvRams = new ArrayList<>();

  private List<String> notSupported = new ArrayList<>();

  private List<String> allRoms = new ArrayList<>();

  private List<String> ignoredVPRegEntries = new ArrayList<>();

  private List<String> ignoredTextFiles = new ArrayList<>();


  public List<String> getAllRoms() {
    return allRoms;
  }

  public void setAllRoms(List<String> allRoms) {
    this.allRoms = allRoms;
  }

  public List<String> getIgnoredTextFiles() {
    return ignoredTextFiles;
  }

  public void setIgnoredTextFiles(List<String> ignoredTextFiles) {
    this.ignoredTextFiles = ignoredTextFiles;
  }

  public List<String> getNotSupported() {
    return notSupported;
  }

  public List<String> getIgnoredVPRegEntries() {
    return ignoredVPRegEntries;
  }

  public List<String> getHighscoreTitles() {
    return highscoreTitles;
  }

  public void setHighscoreTitles(List<String> highscoreTitles) {
    this.highscoreTitles = highscoreTitles;
  }

  public String getPinemhiVersion() {
    return pinemhiVersion;
  }

  public void setPinemhiVersion(String pinemhiVersion) {
    this.pinemhiVersion = pinemhiVersion;
  }


  public List<Map<String, Object>> getHighscoreTextParsers() {
    return highscoreTextParsers;
  }

  public void setHighscoreTextParsers(List<Map<String, Object>> highscoreTextParsers) {
    this.highscoreTextParsers = highscoreTextParsers;
  }

  public void setIgnoredVPRegEntries(List<String> ignoredVPRegEntries) {
    this.ignoredVPRegEntries = ignoredVPRegEntries;
  }

  public void setNotSupported(List<String> notSupported) {
    this.notSupported = notSupported;
  }

  public List<ScoringDBMapping> getHighscoreMappings() {
    return highscoreMappings;
  }

  public void setHighscoreMappings(List<ScoringDBMapping> highscoreMappings) {
    this.highscoreMappings = highscoreMappings;
  }

  public List<String> getSupportedNvRams() {
    return supportedNvRams;
  }

  public boolean isNvRam(@Nullable String rom) {
    if (StringUtils.isEmpty(rom)) {
      return false;
    }
    return getAllRoms().contains(rom);
  }

  public void setSupportedNvRams(List<String> supportedNvRams) {
    this.supportedNvRams = supportedNvRams;
  }

  public ScoringDBMapping getHighscoreMapping(String rom) {
    if (StringUtils.isEmpty(rom)) {
      return null;
    }

    for (ScoringDBMapping highscoreMapping : highscoreMappings) {
      if (highscoreMapping.getRom().equalsIgnoreCase(rom)) {
        return highscoreMapping;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return "Scoring Database (" + supportedNvRams.size() + " supported nvrams, " + highscoreMappings.size() + " mappings)";
  }
}

package de.mephisto.vpin.restclient.system;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ScoringDB {
  private final static Logger LOG = LoggerFactory.getLogger(ScoringDB.class);

  public static String SCORING_DB_NAME = "scoringdb.json";
  public final static String URL = "https://raw.githubusercontent.com/syd711/vpin-studio/main/resources/scoringdb.json";

  private static final ObjectMapper objectMapper;

  static {
    objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public static ScoringDB load() {
    FileInputStream in = null;
    ScoringDB db = null;
    try {
      File dbFile = new File("./resources/", SCORING_DB_NAME);
      in = new FileInputStream(dbFile);
      db = objectMapper.readValue(in, ScoringDB.class);
      LOG.info("Loaded " + dbFile.getName() + ", last updated: " + SimpleDateFormat.getDateTimeInstance().format(new Date(dbFile.lastModified())));
    } catch (Exception e) {
      db = new ScoringDB();
      LOG.error("Failed to load scoring DB json: " + e.getMessage());
    } finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (IOException e) {
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
      File tmp = new File("./resources/", SCORING_DB_NAME + ".tmp");
      if (tmp.exists() && !tmp.delete()) {
        LOG.error("Failed to delete existing tmp file " + SCORING_DB_NAME + ".tmp");
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
      File dbFile = new File("./resources/", SCORING_DB_NAME);
      if (dbFile.exists()) {
        oldSize = dbFile.length();
      }

      if (dbFile.exists() && !dbFile.delete()) {
        LOG.error("Failed to delete " + SCORING_DB_NAME);
      }

      if (!tmp.renameTo(dbFile)) {
        LOG.error("Failed to rename " + SCORING_DB_NAME);
        return;
      }

      LOG.info("Written " + dbFile.getAbsolutePath() + ", (" + oldSize + " vs " + dbFile.length() + " bytes)");
    } catch (IOException e) {
      LOG.error(SCORING_DB_NAME + " download failed: " + e.getMessage());
    }
  }

  private List<ScoringDBMapping> highscoreMappings = new ArrayList<>();

  private List<String> supportedNvRams = new ArrayList<>();

  private List<String> notSupported = new ArrayList<>();

  public List<String> getNotSupported() {
    return notSupported;
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

  public void setSupportedNvRams(List<String> supportedNvRams) {
    this.supportedNvRams = supportedNvRams;
  }

  @Override
  public String toString() {
    return "Scoring Database (" + supportedNvRams.size() + " supported nvrams, " + highscoreMappings.size() + " mappings)";
  }
}

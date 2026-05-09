package de.mephisto.vpin.restclient.system;

import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScoringDB {
    private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static String SCORING_DB_NAME = "scoringdb.json";
    public final static String URL = "https://raw.githubusercontent.com/syd711/vpin-studio/main/resources/scoringdb.json";

    private static final JsonMapper objectMapper;

    static {
        objectMapper = JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
                .disable(EnumFeature.READ_ENUMS_USING_TO_STRING)
                .build();
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
            LOG.info("Loaded " + dbFile.getName() + ", last updated: " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(OffsetDateTime.ofInstant(Instant.ofEpochMilli(dbFile.lastModified()), ZoneId.systemDefault())));
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
            LOG.info("Updating Scoring Database " + SCORING_DB_NAME);
            URL url = URI.create(ScoringDB.URL).toURL();
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

  private List<String> highscoreSkipTitlesCheck = new ArrayList<>();

  private List<Map<String, Object>> highscoreTextParsers = new ArrayList<>();

    private List<Map<String, Object>> highscoreIniParsers = new ArrayList<>();

  public List<Map<String, Object>> getHighscoreIniParsers() {
    return highscoreIniParsers;
  }

    public void setHighscoreIniParsers(List<Map<String, Object>> highscoreIniParsers) {
        this.highscoreIniParsers = highscoreIniParsers;
    }

    private List<ScoringDBMapping> highscoreMappings = new ArrayList<>();

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

  public List<String> getIgnoredVPRegEntries() {
    return ignoredVPRegEntries;
  }

    public List<String> getHighscoreTitles() {
        return highscoreTitles;
    }

    public void setHighscoreTitles(List<String> highscoreTitles) {
        this.highscoreTitles = highscoreTitles;
    }

  public List<String> getHighscoreSkipTitlesCheck() {
    return highscoreSkipTitlesCheck;
  }

  public void setHighscoreSkipTitlesCheck(List<String> highscoreSkipTitlesCheck) {
    this.highscoreSkipTitlesCheck = highscoreSkipTitlesCheck;
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

  public List<ScoringDBMapping> getHighscoreMappings() {
    return highscoreMappings;
  }

    public void setHighscoreMappings(List<ScoringDBMapping> highscoreMappings) {
        this.highscoreMappings = highscoreMappings;
    }

  public boolean isNvRam(@Nullable String rom) {
    if (StringUtils.isEmpty(rom)) {
      return false;
    }
    return getAllRoms().contains(rom);
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
}

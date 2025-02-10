package de.mephisto.vpin.server.highscores.parsing.text;

import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.server.highscores.HighscoreMetadata;
import de.mephisto.vpin.server.highscores.parsing.text.adapters.*;
import de.mephisto.vpin.server.highscores.parsing.text.adapters.customized.Route66Adapter;
import de.mephisto.vpin.server.highscores.parsing.text.adapters.customized.SpongebobAdapter;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TextHighscoreAdapters implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(TextHighscoreAdapters.class);

  protected final static List<ScoreTextFileAdapter> adapters = new ArrayList<>();

  @Autowired
  private SystemService systemService;

  static {
    adapters.add(new SpongebobAdapter());
    adapters.add(new Route66Adapter());
  }

  public boolean resetHighscores(@NonNull ScoringDB scoringDB, @NonNull File file, long score) {
    if (scoringDB.getIgnoredTextFiles().contains(file.getName())) {
      LOG.info("\"" + file.getName() + "\" was marked as to be ignored for text file based and will not be resetted.");
      return false;
    }

    FileInputStream fileInputStream = null;
    try {
      fileInputStream = new FileInputStream(file);
      List<String> lines = IOUtils.readLines(fileInputStream, Charset.defaultCharset());
      fileInputStream.close();
      for (ScoreTextFileAdapter adapter : adapters) {
        if (adapter.isApplicable(file, lines)) {
          LOG.info("Resetting \"" + file.getAbsolutePath() + "\" using " + adapter.getClass().getSimpleName());
          List<String> resetHighscoreText = adapter.resetHighscore(file, lines, score);
          if (resetHighscoreText != null) {
            FileUtils.writeLines(file, StandardCharsets.UTF_8.name(), resetHighscoreText);
            LOG.info("Resetted \"" + file.getAbsolutePath() + "\"");
            return true;
          }
        }
      }
    }
    catch (IOException e) {
      LOG.error("Error reading EM highscore file: " + e.getMessage(), e);
    }
    return false;
  }

  public String convertTextFileTextToMachineReadable(@NonNull HighscoreMetadata metadata, @NonNull ScoringDB scoringDB, @NonNull File file) {
    if (scoringDB.getIgnoredTextFiles().contains(file.getName())) {
      SLOG.info("\"" + file.getName() + "\" was marked as to be ignored for text file based highscores.");
      return null;
    }

    FileInputStream fileInputStream = null;
    try {
      fileInputStream = new FileInputStream(file);
      List<String> lines = IOUtils.readLines(fileInputStream, Charset.defaultCharset());
      for (ScoreTextFileAdapter adapter : adapters) {
        if (adapter.isApplicable(file, lines)) {
          LOG.info("Converted score with converter class name \"" + adapter.getClass().getSimpleName() + "\", " + lines.size() + " lines.");
          SLOG.info("Converted score with converter class name \"" + adapter.getClass().getSimpleName() + "\", " + lines.size() + " lines.");
          return adapter.convert(file, lines);
        }
      }
      LOG.info("No parser found for " + file.getName() + ", length: " + lines.size() + " rows.");
      metadata.setStatus("No parser found for highscore file \"" + file.getName() + "\". Please report this table.");
    }
    catch (IOException e) {
      SLOG.error("Error reading EM highscore file: " + e.getMessage());
      LOG.error("Error reading EM highscore file: " + e.getMessage(), e);
    }
    finally {
      if (fileInputStream != null) {
        try {
          fileInputStream.close();
        }
        catch (IOException e) {
          //ignore
        }
      }
    }
    return null;
  }

  @Override
  public void afterPropertiesSet() {
    ScoringDB scoringDatabase = systemService.getScoringDatabase();
    loadParsers(scoringDatabase);
  }

  public void loadParsers(ScoringDB scoringDatabase) {
    try {
      List<Map<String, Object>> highscoreTextParsers = scoringDatabase.getHighscoreTextParsers();
      for (Map<String, Object> params : highscoreTextParsers) {
        String parser = (String) params.get("parser");
        String className = "de.mephisto.vpin.server.highscores.parsing.text.adapters." + parser + "Adapter";
        ScoreTextFileAdapter adapter = null;
        try {
          adapter = (ScoreTextFileAdapter) Class.forName(className).getDeclaredConstructor().newInstance();
        }
        catch (Exception e) {
          LOG.warn("Invalid text file parser: {}", className);
          continue;
        }

        BeanWrapper bean = new BeanWrapperImpl(adapter);

        setPropertyValue(bean, "lineCount", params);
        setPropertyValue(bean, "fileNames", params);
        setPropertyValue(bean, "size", params);
        setPropertyValue(bean, "start", params);
        setPropertyValue(bean, "offset", params);
        setPropertyValue(bean, "scoreLine", params);
        setPropertyValue(bean, "scoreLine1", params);
        setPropertyValue(bean, "scoreLine2", params);

        adapters.add(adapter);
      }
      LOG.info("Text parser creation finished, loaded " + highscoreTextParsers.size() + " parsers.");
    }
    catch (Exception e) {
      LOG.error(this.getClass().getSimpleName() + " initialization failed: " + e.getMessage(), e);
    }
  }

  private void setPropertyValue(BeanWrapper bean, String key, Map<String, Object> params) {
    try {
      if (params.containsKey(key) && bean.isWritableProperty(key)) {
        bean.setPropertyValue(key, params.get(key));
      }
    }
    catch (Exception e) {
      LOG.error("Error setting parser param '" + key + "': " + e.getMessage(), e);
    }
  }
}

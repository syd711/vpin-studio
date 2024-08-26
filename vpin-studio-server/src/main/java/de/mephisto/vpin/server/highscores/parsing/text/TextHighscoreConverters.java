package de.mephisto.vpin.server.highscores.parsing.text;

import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.server.highscores.parsing.text.adapters.*;
import de.mephisto.vpin.server.highscores.parsing.text.adapters.customized.SpongebobAdapter;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TextHighscoreConverters {
  private final static Logger LOG = LoggerFactory.getLogger(TextHighscoreConverters.class);

  protected final static List<ScoreTextFileAdapter> adapters = new ArrayList<>();

  static {
    adapters.add(new SpongebobAdapter());
    adapters.add(new SinglePlayerScoreAdapter("JacksOpen.txt", 1));
    adapters.add(new SinglePlayerScoreAdapter("thunderbirds.txt", 5));
    adapters.add(new TwoPlayersAdapter("Strip_JP_EM_1978.txt", 1, 3));
    adapters.add(new TwoPlayersAdapter("The_Fog_1979.txt", 1, 3));
    adapters.add(new AlteringScoreInitialsBlocksWithOffsetAdapter("BountyHunter.txt", 1, 5, 2));
    adapters.add(new AlteringScoreInitialsBlocksAdapter("CanadaDry_76VPX.txt", 7, 5));
    adapters.add(new AlteringScoreInitialsBlocksAdapter("MagicCity_67VPX.txt", 6, 5));
    adapters.add(new AlteringScoreInitialsBlocksAdapter("WorldSeries_72VPX.txt", 6, 5));
    adapters.add(new AlteringScoreInitialsBlocksAdapter("MountainClimbingHS.txt", 3, 5));
    adapters.add(new AlteringScoreInitialsBlocksAdapter(33, 0, 5));
    adapters.add(new AlteringScoreInitialsBlocksAdapter(32, 0, 5));
    adapters.add(new AlteringScoreInitialsBlocksAdapter(31, 0, 5));
    adapters.add(new AlteringScoreInitialsLinesAdapter(26, 0, 3));
    adapters.add(new AlteringScoreInitialsBlocksAdapter(27, 7, 5)); //space mission
    adapters.add(new AlteringScoreInitialsBlocksAdapter(25, 5, 5));
    adapters.add(new AlteringScoreInitialsBlocksAdapter(18, 8, 5));
    adapters.add(new AlteringScoreInitialsBlocksAdapter(17, 7, 5));
    adapters.add(new AlteringScoreInitialsBlocksAdapter(16, 6, 5));
    adapters.add(new AlteringScoreInitialsBlocksAdapter(15, 5, 5));
    adapters.add(new AlteringScoreInitialsBlocksAdapter(14, 4, 5));
    adapters.add(new AlteringScoreInitialsBlocksAdapter(12, 2, 5));
    adapters.add(new AlteringScoreInitialsBlocksAdapter(11, 3, 4));//woz
    adapters.add(new AlteringScoreInitialsLinesAdapter(10, 0, 5));
    adapters.add(new SinglePlayerScoreAdapter());
    adapters.add(new TwoPlayersAdapter(8));
  }

  public static boolean resetHighscores(@NonNull ScoringDB scoringDB, @NonNull File file) {
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
          List<String> resetHighscoreText = adapter.resetHighscore(file, lines);
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

  public static String convertTextFileTextToMachineReadable(@NonNull ScoringDB scoringDB, @NonNull File file) {
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
          SLOG.info("Converted score with converter class name \"" + adapter.getClass().getSimpleName() + "\"");
          return adapter.convert(file, lines);
        }
      }
      LOG.info("No parser found for " + file.getName() + ", length: " + lines.size() + " rows.");
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
}

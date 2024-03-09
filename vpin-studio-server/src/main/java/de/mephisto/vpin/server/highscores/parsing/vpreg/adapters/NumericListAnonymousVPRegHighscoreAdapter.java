package de.mephisto.vpin.server.highscores.parsing.vpreg.adapters;

import de.mephisto.vpin.server.highscores.parsing.ScoreParsingEntry;
import de.mephisto.vpin.server.highscores.parsing.ScoreParsingSummary;
import org.apache.poi.poifs.filesystem.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class NumericListAnonymousVPRegHighscoreAdapter extends VPRegHighscoreAdapterImpl {
  public static final String HIGH_SCORE = "HighScore";

  @Override
  public boolean isApplicable(DirectoryEntry gameFolder) {
    if (gameFolder.hasEntry(HIGH_SCORE + "1") && !gameFolder.hasEntry(HIGH_SCORE + "1" + NumericListVPRegHighscoreAdapter.NAME_SUFFIX)) {
      return true;
    }
    return false;
  }

  @Override
  public ScoreParsingSummary readHighscore(DirectoryEntry gameFolder) throws IOException {
    ScoreParsingSummary summary = new ScoreParsingSummary();
    int index = 1;
    String prefix = HIGH_SCORE;
    while (gameFolder.hasEntry(prefix + index)) {
      DocumentEntry scoreEntry = (DocumentEntry) gameFolder.getEntry(prefix + index);
      String scoreString = super.getScoreEntry(scoreEntry);

      ScoreParsingEntry score = new ScoreParsingEntry();
      score.setInitials("???");
      score.setScore(parseScoreString(scoreString));
      score.setPos(index);
      summary.getScores().add(score);
      index++;
    }
    return summary;
  }

  @Override
  public boolean resetHighscore(POIFSFileSystem fs, DirectoryEntry gameFolder) throws IOException {
    int index = 1;
    while (gameFolder.hasEntry(HIGH_SCORE + index)) {
      DocumentNode scoreEntry = (DocumentNode) gameFolder.getEntry(HIGH_SCORE + index);
      POIFSDocument scoreDocument = new POIFSDocument(scoreEntry);
      scoreDocument.replaceContents(new ByteArrayInputStream("\0".getBytes()));

      index++;
      fs.writeFilesystem();
    }
    return true;
  }
}

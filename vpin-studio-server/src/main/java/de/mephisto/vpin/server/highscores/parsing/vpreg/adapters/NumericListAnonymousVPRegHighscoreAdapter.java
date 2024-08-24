package de.mephisto.vpin.server.highscores.parsing.vpreg.adapters;

import de.mephisto.vpin.server.highscores.parsing.ScoreParsingEntry;
import de.mephisto.vpin.server.highscores.parsing.ScoreParsingSummary;
import org.apache.poi.poifs.filesystem.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NumericListAnonymousVPRegHighscoreAdapter extends VPRegHighscoreAdapterImpl {
  private static final String HIGH_SCORE = "HighScore";
  private static final String NAME_SUFFIX = "Name";

  @Override
  public boolean isApplicable(DirectoryEntry gameFolder) {
    if (gameFolder.hasEntry(HIGH_SCORE + "1") && !gameFolder.hasEntry(HIGH_SCORE + "1" + NAME_SUFFIX)) {
      return true;
    }
    return false;
  }

  @Override
  public ScoreParsingSummary readHighscore(DirectoryEntry gameFolder) throws IOException {
    ScoreParsingSummary summary = new ScoreParsingSummary();
    int index = 1;
    String prefix = HIGH_SCORE;
    List<ScoreParsingEntry> entries = new ArrayList<>();
    while (gameFolder.hasEntry(prefix + index)) {
      DocumentEntry scoreEntry = (DocumentEntry) gameFolder.getEntry(prefix + index);
      String scoreString = super.getScoreEntry(scoreEntry);

      ScoreParsingEntry score = new ScoreParsingEntry();
      score.setInitials("???");
      score.setScore(parseScoreString(scoreString));
      entries.add(score);
      index++;
    }

    entries.sort((o1, o2) -> (int) (o2.getScore() - o1.getScore()));
    index = 1;
    for (ScoreParsingEntry entry : entries) {
      entry.setPos(index);
      index++;
    }
    summary.getScores().addAll(entries);
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

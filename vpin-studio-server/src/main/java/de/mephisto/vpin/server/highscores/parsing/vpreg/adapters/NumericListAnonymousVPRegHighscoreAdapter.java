package de.mephisto.vpin.server.highscores.parsing.vpreg.adapters;

import de.mephisto.vpin.server.highscores.parsing.ScoreParsingEntry;
import de.mephisto.vpin.server.highscores.parsing.ScoreParsingSummary;
import org.apache.poi.poifs.filesystem.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.apache.naming.SelectorContext.prefix;

public class NumericListAnonymousVPRegHighscoreAdapter extends VPRegHighscoreAdapterImpl {
  private static final String NAME_SUFFIX = "Name";

  private String scoreKey;

  public NumericListAnonymousVPRegHighscoreAdapter(String scoreKey) {
    this.scoreKey = scoreKey;
  }

  @Override
  public boolean isApplicable(DirectoryEntry gameFolder) {
    String key = String.format(scoreKey, "1");
    if (gameFolder.hasEntry(key) && !gameFolder.hasEntry(key + NAME_SUFFIX)) {
      return true;
    }
    return false;
  }

  @Override
  public ScoreParsingSummary readHighscore(DirectoryEntry gameFolder) throws IOException {
    ScoreParsingSummary summary = new ScoreParsingSummary();
    int index = 1;
    List<ScoreParsingEntry> entries = new ArrayList<>();
    String key = String.format(scoreKey, index);
    while (gameFolder.hasEntry(key)) {
      DocumentEntry scoreEntry = (DocumentEntry) gameFolder.getEntry(key);
      String scoreString = super.getScoreEntry(scoreEntry);

      ScoreParsingEntry score = new ScoreParsingEntry();
      score.setInitials("???");
      score.setScore(parseScoreString(scoreString));
      entries.add(score);
      index++;
      key = String.format(scoreKey, index);
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
    String key = String.format(scoreKey, index);
    while (gameFolder.hasEntry(key)) {
      DocumentNode scoreEntry = (DocumentNode) gameFolder.getEntry(key);
      POIFSDocument scoreDocument = new POIFSDocument(scoreEntry);
      scoreDocument.replaceContents(new ByteArrayInputStream("\0".getBytes()));

      index++;
      key = String.format(scoreKey, index);
      fs.writeFilesystem();
    }
    return true;
  }
}

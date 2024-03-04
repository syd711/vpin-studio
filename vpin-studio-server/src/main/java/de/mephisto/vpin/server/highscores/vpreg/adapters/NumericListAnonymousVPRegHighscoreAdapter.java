package de.mephisto.vpin.server.highscores.vpreg.adapters;

import de.mephisto.vpin.server.highscores.vpreg.VPRegScoreEntry;
import de.mephisto.vpin.server.highscores.vpreg.VPRegScoreSummary;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.POIFSDocument;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static de.mephisto.vpin.server.highscores.vpreg.adapters.NumericListVPRegHighscoreAdapter.NAME_SUFFIX;

public class NumericListAnonymousVPRegHighscoreAdapter extends VPRegHighscoreAdapterImpl {
  public static final String HIGH_SCORE = "HighScore";

  @Override
  public boolean isApplicable(DirectoryEntry gameFolder) {
    if (gameFolder.hasEntry(HIGH_SCORE + "1") && !gameFolder.hasEntry(HIGH_SCORE + "1" + NAME_SUFFIX)) {
      return true;
    }
    return false;
  }

  @Override
  public VPRegScoreSummary readHighscore(DirectoryEntry gameFolder) throws IOException {
    VPRegScoreSummary summary = new VPRegScoreSummary();
    int index = 1;
    String prefix = HIGH_SCORE;
    while (gameFolder.hasEntry(prefix + index)) {
      DocumentEntry scoreEntry = (DocumentEntry) gameFolder.getEntry(prefix + index);
      String scoreString = super.getScoreEntry(scoreEntry);

      VPRegScoreEntry score = new VPRegScoreEntry();
      score.setInitials("???");
      score.setScore(StringUtils.isEmpty(scoreString) ? 0 : Long.parseLong(scoreString));
      score.setPos(index);
      summary.getScores().add(score);
      index++;
    }
    return summary;
  }

  @Override
  public boolean resetHighscore(DirectoryEntry gameFolder) throws IOException {
    int index = 1;
    while (gameFolder.hasEntry(HIGH_SCORE + index)) {
      DocumentNode scoreEntry = (DocumentNode) gameFolder.getEntry(HIGH_SCORE + index);
      POIFSDocument scoreDocument = new POIFSDocument(scoreEntry);
      scoreDocument.replaceContents(new ByteArrayInputStream("0".getBytes()));

      index++;
    }
    return true;
  }
}

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

public class SingleEntryAnonymousVPRegHighscoreAdapter extends VPRegHighscoreAdapterImpl {

  @Override
  public boolean isApplicable(DirectoryEntry gameFolder) throws IOException {
    if (getHighscoreEntry(gameFolder) != null && !gameFolder.hasEntry("hsa1") && !gameFolder.hasEntry("HSA1")) {
      return true;
    }
    return false;
  }

  @Override
  public VPRegScoreSummary readHighscore(DirectoryEntry gameFolder) throws IOException {
    VPRegScoreSummary summary = new VPRegScoreSummary();

    DocumentEntry scoreEntry = getHighscoreEntry(gameFolder);
    String scoreString = super.getScoreEntry(scoreEntry);
    VPRegScoreEntry score = new VPRegScoreEntry();
    score.setInitials("???");
    score.setScore(StringUtils.isEmpty(scoreString) ? 0 : Long.parseLong(scoreString));
    score.setPos(1);
    summary.getScores().add(score);

    return summary;
  }

  @Override
  public boolean resetHighscore(DirectoryEntry gameFolder) throws IOException {
    DocumentNode highscoreEntry = getHighscoreEntry(gameFolder);
    POIFSDocument scoreDocument = new POIFSDocument(highscoreEntry);
    scoreDocument.replaceContents(new ByteArrayInputStream("0".getBytes()));
    return true;
  }
}

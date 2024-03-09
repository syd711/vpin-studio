package de.mephisto.vpin.server.highscores.parsing.vpreg.adapters;

import de.mephisto.vpin.server.highscores.parsing.ScoreParsingEntry;
import de.mephisto.vpin.server.highscores.parsing.ScoreParsingSummary;
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
  public ScoreParsingSummary readHighscore(DirectoryEntry gameFolder) throws IOException {
    ScoreParsingSummary summary = new ScoreParsingSummary();

    DocumentEntry scoreEntry = getHighscoreEntry(gameFolder);
    String scoreString = super.getScoreEntry(scoreEntry);
    ScoreParsingEntry score = new ScoreParsingEntry();
    score.setInitials("???");
    score.setScore(parseScoreString(scoreString));
    score.setPos(1);
    summary.getScores().add(score);

    return summary;
  }

  @Override
  public boolean resetHighscore(DirectoryEntry gameFolder) throws IOException {
    DocumentNode highscoreEntry = getHighscoreEntry(gameFolder);
    POIFSDocument scoreDocument = new POIFSDocument(highscoreEntry);
    scoreDocument.replaceContents(new ByteArrayInputStream("\0".getBytes()));
    return true;
  }
}

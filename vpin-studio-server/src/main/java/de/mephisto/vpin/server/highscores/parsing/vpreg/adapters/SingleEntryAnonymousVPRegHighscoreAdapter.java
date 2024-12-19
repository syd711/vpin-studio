package de.mephisto.vpin.server.highscores.parsing.vpreg.adapters;

import de.mephisto.vpin.server.highscores.parsing.ScoreParsingEntry;
import de.mephisto.vpin.server.highscores.parsing.ScoreParsingSummary;
import org.apache.poi.poifs.filesystem.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SingleEntryAnonymousVPRegHighscoreAdapter extends VPRegHighscoreAdapterImpl {

  private String gameName;
  private String key;

  public SingleEntryAnonymousVPRegHighscoreAdapter(String gameName, String key) {
    this.gameName = gameName;
    this.key = key;
  }

  public SingleEntryAnonymousVPRegHighscoreAdapter() {
  }

  @Override
  public boolean isApplicable(DirectoryEntry gameFolder) throws IOException {
    if (gameName != null) {
      return gameFolder.getName().equalsIgnoreCase(gameName);
    }

    if (getHighscoreEntry(gameFolder) != null && !gameFolder.hasEntry("hsa1") && !gameFolder.hasEntry("HSA1") && !gameFolder.hasEntry("Initial1")) {
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
  protected DocumentNode getHighscoreEntry(DirectoryEntry gameFolder) throws IOException {
    if(key != null && gameName != null) {
      return (DocumentNode) gameFolder.getEntry(key);
    }

    return super.getHighscoreEntry(gameFolder);
  }

  @Override
  public boolean resetHighscore(POIFSFileSystem fs, DirectoryEntry gameFolder, long score) throws IOException {
    DocumentNode highscoreEntry = getHighscoreEntry(gameFolder);
    POIFSDocument scoreDocument = new POIFSDocument(highscoreEntry);
    byte[] array = StandardCharsets.UTF_16LE.encode(String.valueOf(score)).array();
    scoreDocument.replaceContents(new ByteArrayInputStream(array));
    return true;
  }
}

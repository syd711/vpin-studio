package de.mephisto.vpin.server.highscores.parsing.vpreg.adapters;

import de.mephisto.vpin.server.highscores.parsing.ScoreParsingEntry;
import de.mephisto.vpin.server.highscores.parsing.ScoreParsingSummary;
import org.apache.poi.poifs.filesystem.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MultiEntryWithLettersVPRegHighscoreAdapter extends SingleEntryWithLettersVPRegHighscoreAdapter {

  private List<String> gameNames;
  private final int scoreCount;

  public MultiEntryWithLettersVPRegHighscoreAdapter(List<String> gameNames, int scoreCount) {
    this.gameNames = gameNames;
    this.scoreCount = scoreCount;
  }

  @Override
  public boolean isApplicable(DirectoryEntry gameFolder) throws IOException {
    for (String gameName : gameNames) {
      if (gameFolder.getName().equalsIgnoreCase(gameName)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public ScoreParsingSummary readHighscore(DirectoryEntry gameFolder) throws IOException {
    ScoreParsingSummary summary = new ScoreParsingSummary();

    for (int i = 0; i < scoreCount; i++) {
      DocumentEntry scoreEntry = getHighscoreEntry(gameFolder, i);
      String scoreString = super.getScoreEntry(scoreEntry);
      ScoreParsingEntry score = new ScoreParsingEntry();

      String letter1 = getNameEntry(gameFolder, 1, i);
      String letter2 = getNameEntry(gameFolder, 2, i);
      String letter3 = getNameEntry(gameFolder, 3, i);
      score.setInitials(letter1 + letter2 + letter3);
      score.setScore(parseScoreString(scoreString));
      score.setPos((i + 1));
      summary.getScores().add(score);
    }

    return summary;
  }

  @Override
  public boolean resetHighscore(POIFSFileSystem fs, DirectoryEntry gameFolder) throws IOException {
    for (int i = 0; i < scoreCount; i++) {
      if (gameFolder.hasEntry("Highscore(" + i + ")")) {
        DocumentNode entry = (DocumentNode) gameFolder.getEntry("Highscore(" + i + ")");
        POIFSDocument scoreDocument = new POIFSDocument(entry);
        scoreDocument.replaceContents(new ByteArrayInputStream("".getBytes()));
        fs.writeFilesystem();
      }
      if (gameFolder.hasEntry("HighScore(" + i + ")")) {
        DocumentNode entry = (DocumentNode) gameFolder.getEntry("HighScore(" + i + ")");
        POIFSDocument scoreDocument = new POIFSDocument(entry);
        scoreDocument.replaceContents(new ByteArrayInputStream("".getBytes()));
        fs.writeFilesystem();
      }

      for (int letterIndex = 0; letterIndex < 3; letterIndex++) {
        if (gameFolder.hasEntry("Initials(" + i + "," + letterIndex + ")")) {
          DocumentNode entry = (DocumentNode) gameFolder.getEntry("Initials(" + i + "," + letterIndex + ")");
          POIFSDocument scoreDocument = new POIFSDocument(entry);
          scoreDocument.replaceContents(new ByteArrayInputStream("".getBytes()));
          fs.writeFilesystem();
        }
      }
    }
    return true;
  }
}

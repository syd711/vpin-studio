package de.mephisto.vpin.server.highscores.parsing.vpreg.adapters;

import de.mephisto.vpin.server.highscores.parsing.ScoreParsingEntry;
import de.mephisto.vpin.server.highscores.parsing.ScoreParsingSummary;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.POIFSDocument;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class SingleEntryWithLettersVPRegHighscoreAdapter extends SingleEntryAnonymousVPRegHighscoreAdapter {
  private final static char[] ALPHABET = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};


  @Override
  public boolean isApplicable(DirectoryEntry gameFolder) throws IOException {
    if ((gameFolder.hasEntry("hsa1") || gameFolder.hasEntry("HSA1")) && getHighscoreEntry(gameFolder) != null) {
      return true;
    }
    return false;
  }

  @Override
  public ScoreParsingSummary readHighscore(DirectoryEntry gameFolder) throws IOException {
    ScoreParsingSummary summary = super.readHighscore(gameFolder);

    String letter1 = getNameEntry(gameFolder, 1);
    String letter2 = getNameEntry(gameFolder, 2);
    String letter3 = getNameEntry(gameFolder, 3);

    ScoreParsingEntry entry = summary.getScores().get(0);
    entry.setInitials(letter1 + letter2 + letter3);
    return summary;
  }

  @Override
  public boolean resetHighscore(DirectoryEntry gameFolder) throws IOException {
    super.resetHighscore(gameFolder);

    for (int i = 0; i < 3; i++) {
      int index = i + 1;
      if (gameFolder.hasEntry("HSA" + index)) {
        DocumentNode entry = (DocumentNode) gameFolder.getEntry("HSA" + index);
        POIFSDocument scoreDocument = new POIFSDocument(entry);
        scoreDocument.replaceContents(new ByteArrayInputStream("".getBytes()));
      }
      else if (gameFolder.hasEntry("hsa" + i)) {
        DocumentNode entry = (DocumentNode) gameFolder.getEntry("hsa" + index);
        POIFSDocument scoreDocument = new POIFSDocument(entry);
        scoreDocument.replaceContents(new ByteArrayInputStream("".getBytes()));
      }
    }
    return false;
  }

  private String getNameEntry(DirectoryEntry gameFolder, int i) throws IOException {
    String nameString = "?";
    if (gameFolder.hasEntry("HSA" + i)) {
      DocumentNode entry = (DocumentNode) gameFolder.getEntry("HSA" + i);
      nameString = super.getNameString(entry);
    }
    else if (gameFolder.hasEntry("hsa" + i)) {
      DocumentNode entry = (DocumentNode) gameFolder.getEntry("hsa" + i);
      nameString = super.getNameString(entry);
    }

    try {
      int pos = Integer.parseInt(nameString);
      return String.valueOf(ALPHABET[pos-1]);
    } catch (Exception e) {
      //ignore
    }

    return nameString;
  }
}

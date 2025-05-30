package de.mephisto.vpin.server.highscores.parsing.vpreg.adapters;

import de.mephisto.vpin.server.highscores.parsing.ScoreParsingEntry;
import de.mephisto.vpin.server.highscores.parsing.ScoreParsingSummary;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.POIFSDocument;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SingleEntryWithLettersVPRegHighscoreAdapter extends SingleEntryAnonymousVPRegHighscoreAdapter {
  private final static char[] ALPHABET = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};


  @Override
  public boolean isApplicable(DirectoryEntry gameFolder) throws IOException {
    if ((gameFolder.hasEntry("Initial1") || gameFolder.hasEntry("hsa1") || gameFolder.hasEntry("HSA1")) && getHighscoreEntry(gameFolder) != null) {
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
  public boolean resetHighscore(POIFSFileSystem fs, DirectoryEntry gameFolder, long score) throws IOException {
    super.resetHighscore(fs, gameFolder, score);

    for (int i = 0; i < 3; i++) {
      int index = i + 1;
      if (gameFolder.hasEntry("HSA" + index)) {
        DocumentNode entry = (DocumentNode) gameFolder.getEntry("HSA" + index);
        POIFSDocument scoreDocument = new POIFSDocument(entry);
        byte[] array = StandardCharsets.UTF_16LE.encode(String.valueOf(score)).array();
        scoreDocument.replaceContents(new ByteArrayInputStream(array));
        fs.writeFilesystem();
      }
      else if (gameFolder.hasEntry("hsa" + i)) {
        DocumentNode entry = (DocumentNode) gameFolder.getEntry("hsa" + index);
        POIFSDocument scoreDocument = new POIFSDocument(entry);
        byte[] array = StandardCharsets.UTF_16LE.encode(String.valueOf(score)).array();
        scoreDocument.replaceContents(new ByteArrayInputStream(array));
        fs.writeFilesystem();
      }
    }
    return false;
  }

  protected String getNameEntry(DirectoryEntry gameFolder, int index) throws IOException {
    return getNameEntry(gameFolder, index, -1);
  }

  protected String getNameEntry(DirectoryEntry gameFolder, int index, int scoreIndex) throws IOException {
    String nameString = "?";
    if (gameFolder.hasEntry("HSA" + index)) {
      DocumentNode entry = (DocumentNode) gameFolder.getEntry("HSA" + index);
      nameString = super.getNameString(entry);
    }
    else if (gameFolder.hasEntry("hsa" + index)) {
      DocumentNode entry = (DocumentNode) gameFolder.getEntry("hsa" + index);
      nameString = super.getNameString(entry);
    }
    else if (gameFolder.hasEntry("Initial" + index)) {
      DocumentNode entry = (DocumentNode) gameFolder.getEntry("Initial" + index);
      nameString = super.getNameString(entry);
    }
    else if (gameFolder.hasEntry("Initial(" + scoreIndex + "," + index + ")")) {
      DocumentNode entry = (DocumentNode) gameFolder.getEntry("Initial(" + scoreIndex + "," + index + ")");
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

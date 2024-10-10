package de.mephisto.vpin.server.highscores.parsing.vpreg.adapters;

import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.DocumentNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

abstract public class VPRegHighscoreAdapterImpl implements VPRegHighscoreAdapter {

  protected String getNameString(DocumentEntry nameEntry) throws IOException {
    DocumentInputStream nameEntryStream = new DocumentInputStream(nameEntry);
    byte[] nameContent = new byte[nameEntryStream.available()];
    nameEntryStream.read(nameContent);
    nameEntryStream.close();

    String nameString = new String(nameContent, StandardCharsets.UTF_8);
    nameString = nameString.replace("\0", "").trim();
    return nameString;
  }

  protected String getScoreEntry(DocumentEntry scoreEntry) throws IOException {
    DocumentInputStream scoreEntryStream = new DocumentInputStream(scoreEntry);
    byte[] scoreContent = new byte[scoreEntryStream.available()];
    scoreEntryStream.read(scoreContent);
    scoreEntryStream.close();

    String scoreString = new String(scoreContent, StandardCharsets.UTF_8);
    scoreString = scoreString.replace("\0", "");
    while (scoreString.contains(".")) {
      scoreString = scoreString.substring(0, scoreString.indexOf("."));
    }
    while (scoreString.contains(",")) {
      scoreString = scoreString.substring(0, scoreString.indexOf("."));
    }
    return scoreString;
  }

  protected DocumentNode getHighscoreEntry(DirectoryEntry gameFolder) throws IOException {
    if (gameFolder.hasEntry("hiscore")) {
      return (DocumentNode) gameFolder.getEntry("hiscore");
    }
    if (gameFolder.hasEntry("HighScore")) {
      return (DocumentNode) gameFolder.getEntry("HighScore");
    }
    if (gameFolder.hasEntry("HiScore")) {
      return (DocumentNode) gameFolder.getEntry("HiScore");
    }
    if (gameFolder.hasEntry("DBHiScore")) {
      return (DocumentNode) gameFolder.getEntry("DBHiScore");
    }
    return null;
  }

  protected DocumentNode getHighscoreEntry(DirectoryEntry gameFolder, int index) throws IOException {
    if (gameFolder.hasEntry("Highscore(" + index + ")")) {
      return (DocumentNode) gameFolder.getEntry("Highscore(" + index + ")");
    }
    if (gameFolder.hasEntry("HighScore(" + index + ")")) {
      return (DocumentNode) gameFolder.getEntry("HighScore(" + index + ")");
    }
    return null;
  }

  protected long parseScoreString(String scoreString) {
    try {
      return Long.parseLong(scoreString);
    }
    catch (Exception e) {
      return 0;
    }
  }
}
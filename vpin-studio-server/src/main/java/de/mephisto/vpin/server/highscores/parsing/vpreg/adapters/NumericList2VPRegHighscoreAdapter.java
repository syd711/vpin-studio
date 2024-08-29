package de.mephisto.vpin.server.highscores.parsing.vpreg.adapters;

import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.apache.poi.poifs.filesystem.POIFSDocument;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class NumericList2VPRegHighscoreAdapter extends NumericListVPRegHighscoreAdapter {

  @Override
  public boolean isApplicable(DirectoryEntry gameFolder) {
    if (gameFolder.hasEntry(getScoreKey(getStartIndex())) && gameFolder.hasEntry(getNameKey(getStartIndex()))) {
      return true;
    }
    return false;
  }

  @Override
  public boolean resetHighscore(POIFSFileSystem fs, DirectoryEntry gameFolder) throws IOException {
    int index = getStartIndex();
    while (gameFolder.hasEntry(getScoreKey(index))) {
      DocumentNode scoreEntry = (DocumentNode) gameFolder.getEntry(getScoreKey(index));
      POIFSDocument scoreDocument = new POIFSDocument(scoreEntry);
      scoreDocument.replaceContents(new ByteArrayInputStream("\0".getBytes()));

      index++;
      fs.writeFilesystem();
    }
    return true;
  }

  @Override
  protected int getStartIndex() {
    return 0;
  }

  protected String getNameKey(int index) {
    return getNamePrefix() + index;
  }

  protected String getScoreKey(int index) {
    return getHighScorePrefix() + index;
  }

  protected String getHighScorePrefix() {
    return "HSPoints";
  }

  protected String getNamePrefix() {
    return "HSName";
  }
}

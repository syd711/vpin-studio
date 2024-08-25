package de.mephisto.vpin.server.highscores.parsing.vpreg.adapters;

import org.apache.poi.poifs.filesystem.DirectoryEntry;

public class NumericList2VPRegHighscoreAdapter extends NumericListVPRegHighscoreAdapter {

  @Override
  public boolean isApplicable(DirectoryEntry gameFolder) {
    if (gameFolder.hasEntry(getScoreKey(getStartIndex())) && gameFolder.hasEntry(getNameKey(getStartIndex()))) {
      return true;
    }
    return false;
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

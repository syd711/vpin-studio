package de.mephisto.vpin.restclient.highscores;

import java.util.List;

public class HighscoreFiles {
  private List<String> textFiles;
  private List<String> vpRegEntries;
  private List<String> nvRams;

  public List<String> getTextFiles() {
    return textFiles;
  }

  public void setTextFiles(List<String> textFiles) {
    this.textFiles = textFiles;
  }

  public List<String> getVpRegEntries() {
    return vpRegEntries;
  }

  public void setVpRegEntries(List<String> vpRegEntries) {
    this.vpRegEntries = vpRegEntries;
  }

  public List<String> getNvRams() {
    return nvRams;
  }

  public void setNvRams(List<String> nvRams) {
    this.nvRams = nvRams;
  }
}

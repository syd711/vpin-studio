package de.mephisto.vpin.restclient.highscores;

import java.util.ArrayList;
import java.util.List;

public class HighscoreFiles {
  private List<String> textFiles = new ArrayList<>();
  private List<String> vpRegEntries = new ArrayList<>();
  private List<String> nvRams = new ArrayList<>();

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

  public boolean contains(String hsName) {
    for (String textFile : textFiles) {
      if (textFile.equalsIgnoreCase(hsName) || textFile.equalsIgnoreCase(hsName + ".txt")) {
        return true;
      }
    }
    return false;
  }
}

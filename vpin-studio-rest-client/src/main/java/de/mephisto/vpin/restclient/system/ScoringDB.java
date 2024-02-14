package de.mephisto.vpin.restclient.system;

import java.util.ArrayList;
import java.util.List;

public class ScoringDB {

  private List<ScoringDBMapping> highscoreMappings =new ArrayList<>();

  private List<String> supportedNvRams = new ArrayList<>();

  public List<ScoringDBMapping> getHighscoreMappings() {
    return highscoreMappings;
  }

  public void setHighscoreMappings(List<ScoringDBMapping> highscoreMappings) {
    this.highscoreMappings = highscoreMappings;
  }

  public List<String> getSupportedNvRams() {
    return supportedNvRams;
  }

  public void setSupportedNvRams(List<String> supportedNvRams) {
    this.supportedNvRams = supportedNvRams;
  }

  @Override
  public String toString() {
    return "Scoring Database (" + supportedNvRams.size() + " supported nvrams, " + highscoreMappings.size() + " mappings)";
  }
}

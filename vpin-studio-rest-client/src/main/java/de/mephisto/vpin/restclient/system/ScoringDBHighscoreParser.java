package de.mephisto.vpin.restclient.system;

import java.util.HashMap;
import java.util.Map;

public class ScoringDBHighscoreParser {
  private String name;
  private Map<String,String> parameters = new HashMap<>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, String> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, String> parameters) {
    this.parameters = parameters;
  }
}

package de.mephisto.vpin.server.nvrams.parser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * A collection of memory areas used during a game to store the state of the game (e.g., player #, ball #, 
 * progressive jackpot value, etc.).
 */
public class NVRamGameState extends NVRamObject {

  private Map<String, Object> mappings = new LinkedHashMap<>();

  @JsonAnySetter
  void setMapping(String key, NVRamMapping value) {
    mappings.put(key, value);
  }

  @JsonSetter("scores")
  void setScores(List<NVRamMapping> values) {
    mappings.put("scores", values);
  }

  @JsonSetter("final_scores")
  void setFinalScores(List<NVRamMapping> values) {
    mappings.put("final_scores", values);
  }

  public NVRamMapping getPlayerCount() {
    return (NVRamMapping) mappings.get("player_count");
  }

  public NVRamMapping getCurrentPlayer() {
    return (NVRamMapping) mappings.get("current_player");
  }

  @SuppressWarnings("unchecked")
  public List<NVRamMapping> getScores() {
    return (List<NVRamMapping>) mappings.get("scores");
  }

  @SuppressWarnings("unchecked")
  public List<NVRamMapping> getMappings() {
    List<NVRamMapping> collector = new ArrayList<>();
    for (String key : mappings.keySet()) {
      Object m = mappings.get(key);
      if (m instanceof NVRamMapping) {
        collector.add((NVRamMapping) m);
      }
      else if (m instanceof List) {
        collector.addAll((List<NVRamMapping>) m);
      }
    }
    return collector;
  }

}

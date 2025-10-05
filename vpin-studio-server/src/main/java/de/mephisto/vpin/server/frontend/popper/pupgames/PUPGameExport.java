package de.mephisto.vpin.server.frontend.popper.pupgames;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PUPGameExport {
  @JsonProperty("GameExport")
  private List<PUPGame> GameExport;

  public List<PUPGame> getGameExport() {
    return GameExport;
  }

  public void setGameExport(List<PUPGame> gameExport) {
    GameExport = gameExport;
  }
}

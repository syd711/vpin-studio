package de.mephisto.vpin.server.rest;

import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;

public class VPinStudioClient {

  public byte[] getDirectB2SImage(@NonNull Game game) {
//    http://localhost:8089/assets/directb2s/7/cropped/RATIO_4x3
    return RestClient.getInstance().readBinary("assets/directb2s/" + game.getId() + "/raw");
  }
}

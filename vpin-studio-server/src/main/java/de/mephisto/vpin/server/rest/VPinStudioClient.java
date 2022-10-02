package de.mephisto.vpin.server.rest;

import de.mephisto.vpin.server.GameInfo;
import edu.umd.cs.findbugs.annotations.NonNull;

public class VPinStudioClient {

  public byte[] getDirectB2SImage(@NonNull GameInfo gameInfo) {
    return RestClient.getInstance().readBinary("assets/directb2s/" + gameInfo.getId() + "/raw");
  }
}

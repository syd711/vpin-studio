package de.mephisto.vpin.server;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface VPinServiceEvent {

  @NonNull
  GameInfo getGameInfo();
}

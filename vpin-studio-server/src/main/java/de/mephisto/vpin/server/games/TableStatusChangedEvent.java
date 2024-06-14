package de.mephisto.vpin.server.games;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface TableStatusChangedEvent {
  @NonNull
  Game getGame();

  TableStatusChangedOrigin getOrigin();
}
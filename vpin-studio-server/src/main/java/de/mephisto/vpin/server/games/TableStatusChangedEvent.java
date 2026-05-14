package de.mephisto.vpin.server.games;

import org.jspecify.annotations.NonNull;

public interface TableStatusChangedEvent {
  @NonNull
  Game getGame();

  TableStatusChangedOrigin getOrigin();

  long getEventAgeMs();
}
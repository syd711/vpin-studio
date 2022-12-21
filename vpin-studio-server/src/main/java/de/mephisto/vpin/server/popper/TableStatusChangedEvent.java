package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;

public interface TableStatusChangedEvent {
  @NonNull
  Game getGame();
}
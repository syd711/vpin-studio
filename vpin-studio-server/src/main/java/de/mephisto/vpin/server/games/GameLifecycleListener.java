package de.mephisto.vpin.server.games;

import org.jspecify.annotations.NonNull;

public interface GameLifecycleListener {
  void gameCreated(int gameId);
  void gameUpdated(int gameId);
  void gameDeleted(int gameId);
}

package de.mephisto.vpin.server.games;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface GameLifecycleListener {
  void gameCreated(int gameId);
  void gameUpdated(int gameId);
  void gameDeleted(int gameId);
}

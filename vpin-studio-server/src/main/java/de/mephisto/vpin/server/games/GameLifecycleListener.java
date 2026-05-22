package de.mephisto.vpin.server.games;

public interface GameLifecycleListener {
  void gameCreated(int gameId);
  void gameUpdated(int gameId);
  void gameDeleted(int gameId);
}

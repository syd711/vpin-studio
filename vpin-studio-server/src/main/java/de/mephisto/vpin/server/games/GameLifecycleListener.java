package de.mephisto.vpin.server.games;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface GameLifecycleListener {
  void gameCreated(@NonNull Game game);
  void gameUpdated(@NonNull Game game);
  void gameDeleted(@NonNull Game game);
}

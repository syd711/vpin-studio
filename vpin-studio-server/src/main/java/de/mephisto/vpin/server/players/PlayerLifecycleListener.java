package de.mephisto.vpin.server.players;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface PlayerLifecycleListener {
  void playerCreated(@NonNull Player player);
  void playerUpdated(@NonNull Player player);
  void playerDeleted(@NonNull Player player);
}

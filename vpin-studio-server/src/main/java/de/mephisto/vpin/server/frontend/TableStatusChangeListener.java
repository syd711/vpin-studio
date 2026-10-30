package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.TableStatusChangedEvent;
import org.jspecify.annotations.NonNull;

public interface TableStatusChangeListener {

  /**
   * Invoked synchronously before the launch event is answered, so the emulator has not been started yet.
   * Use this for preparations that must be finished before the emulator reads its files.
   */
  default void tableLaunching(@NonNull Game game) {
    //ignore
  }

  void tableLaunched(TableStatusChangedEvent event);

  default void tableExited(TableStatusChangedEvent event) {
    //ignore
  }

  default int getPriority() {
    return -1;
  }
}

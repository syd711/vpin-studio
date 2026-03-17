package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.server.games.TableStatusChangedEvent;

public interface TableStatusChangeListener {

  void tableLaunched(TableStatusChangedEvent event);

  default void tableExited(TableStatusChangedEvent event) {
    //ignore
  }

  default int getPriority() {
    return -1;
  }
}

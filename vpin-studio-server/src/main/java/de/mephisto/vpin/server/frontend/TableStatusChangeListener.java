package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.server.games.TableStatusChangedEvent;

public interface TableStatusChangeListener {

  void tableLaunched(TableStatusChangedEvent event);

  void tableExited(TableStatusChangedEvent event);
}

package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.server.games.TableStatusChangedEvent;

public interface FrontendStatusChangeListener {

  void tableLaunched(TableStatusChangedEvent event);

  void tableExited(TableStatusChangedEvent event);

  default void frontendLaunched() {

  }

  default void frontendExited() {

  }

  default void frontendRestarted() {

  }
}

package de.mephisto.vpin.server.popper;

public interface PopperStatusChangeListener {

  void tableLaunched(TableStatusChangedEvent event);

  void tableExited(TableStatusChangedEvent event);

  default void popperLaunched() {

  }

  default void popperExited() {

  }

  default void popperRestarted() {

  }
}

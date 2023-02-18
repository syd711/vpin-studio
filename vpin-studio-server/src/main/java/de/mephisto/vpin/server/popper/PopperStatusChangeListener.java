package de.mephisto.vpin.server.popper;

public interface PopperStatusChangeListener {

  void tableLaunched(TableStatusChangedEvent event);

  void tableExited(TableStatusChangedEvent event);

  void popperLaunched();

  void popperExited();

  void popperRestarted();
}

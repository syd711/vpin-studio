package de.mephisto.vpin.server.popper;

public interface TableStatusChangeListener {

  void tableLaunched(TableStatusChangedEvent event);

  void tableExited(TableStatusChangedEvent event);
}

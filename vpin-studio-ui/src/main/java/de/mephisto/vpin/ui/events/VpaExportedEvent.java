package de.mephisto.vpin.ui.events;

public class VpaExportedEvent implements StudioEvent {

  private final String uuid;

  public VpaExportedEvent(String uuid) {
    this.uuid = uuid;
  }

  public String getUuid() {
    return uuid;
  }
}

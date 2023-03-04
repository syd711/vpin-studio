package de.mephisto.vpin.ui.events;

public class VpaImportedEvent implements StudioEvent {

  private final String uuid;

  public VpaImportedEvent(String uuid) {
    this.uuid = uuid;
  }

  public String getUuid() {
    return uuid;
  }
}

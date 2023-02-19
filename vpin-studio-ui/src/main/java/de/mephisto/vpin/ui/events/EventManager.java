package de.mephisto.vpin.ui.events;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class EventManager {

  private static EventManager instance = new EventManager();

  private List<StudioEventListener> listeners = new ArrayList<>();

  public static EventManager getInstance() {
    return instance;
  }

  public void addListener(@NonNull StudioEventListener listener) {
    this.listeners.add(listener);
  }

  public void notifyVpaExport(String uuid) {
    new Thread(() -> {
      VpaExportedEvent event = new VpaExportedEvent(uuid);
      for (StudioEventListener listener : listeners) {
        listener.onVpaExport(event);
      }
    }).start();
  }
}

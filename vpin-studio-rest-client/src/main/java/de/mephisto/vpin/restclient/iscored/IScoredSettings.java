package de.mephisto.vpin.restclient.iscored;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.webhooks.WebhookSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IScoredSettings extends JsonSettings {
  private List<IScoredGameRoom> gameRooms = new ArrayList<>();
  private boolean enabled = false;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public List<IScoredGameRoom> getGameRooms() {
    return gameRooms;
  }

  public void setGameRooms(List<IScoredGameRoom> gameRooms) {
    this.gameRooms = gameRooms;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.ISCORED_SETTINGS;
  }

  public void remove(IScoredGameRoom selectedItem) {
    Iterator<IScoredGameRoom> iterator = gameRooms.iterator();
    while (iterator.hasNext()) {
      IScoredGameRoom next = iterator.next();
      if (next.getUuid().equals(selectedItem.getUuid())) {
        iterator.remove();
      }
    }
  }
}

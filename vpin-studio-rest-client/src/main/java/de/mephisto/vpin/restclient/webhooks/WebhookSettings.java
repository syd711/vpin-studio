package de.mephisto.vpin.restclient.webhooks;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WebhookSettings extends JsonSettings {
  private List<WebhookSet> sets = new ArrayList<>();

  public List<WebhookSet> getSets() {
    return sets;
  }

  public void setSets(List<WebhookSet> sets) {
    this.sets = sets;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.WEBHOOK_SETTINGS;
  }

  public void remove(WebhookSet selectedItem) {
    Iterator<WebhookSet> iterator = sets.iterator();
    while (iterator.hasNext()) {
      WebhookSet next = iterator.next();
      if (next.getName().equals(selectedItem.getName())) {
        iterator.remove();
      }
    }
  }
}

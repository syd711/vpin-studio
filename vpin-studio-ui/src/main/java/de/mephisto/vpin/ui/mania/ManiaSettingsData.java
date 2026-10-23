package de.mephisto.vpin.ui.mania;

import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.restclient.mania.ManiaSettings;

import java.util.List;

/**
 * Holds the data that is fetched by the {@link ManiaSettingsLoadProgressModel} before a mania preference page is rendered.
 */
public class ManiaSettingsData {
  private boolean registered;
  private ManiaSettings settings;
  private Cabinet cabinet;
  private List<Account> accounts;

  public boolean isRegistered() {
    return registered;
  }

  public void setRegistered(boolean registered) {
    this.registered = registered;
  }

  public ManiaSettings getSettings() {
    return settings;
  }

  public void setSettings(ManiaSettings settings) {
    this.settings = settings;
  }

  public Cabinet getCabinet() {
    return cabinet;
  }

  public void setCabinet(Cabinet cabinet) {
    this.cabinet = cabinet;
  }

  public List<Account> getAccounts() {
    return accounts;
  }

  public void setAccounts(List<Account> accounts) {
    this.accounts = accounts;
  }
}

package de.mephisto.vpin.ui.mania;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
import de.mephisto.vpin.ui.mania.util.ManiaHelper;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

/**
 * Fetches all mandatory data that is required to render a mania preference page.
 */
public class ManiaSettingsLoadProgressModel extends ProgressModel<ManiaSettingsLoadProgressModel.Step> {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaSettingsLoadProgressModel.class);

  public enum Step {
    registration("dialog.mania_settings_loading_registration"),
    cabinet("dialog.mania_settings_loading_cabinet"),
    accounts("dialog.mania_settings_loading_accounts"),
    avatar("dialog.mania_settings_loading_avatar");

    private final String messageKey;

    Step(String messageKey) {
      this.messageKey = messageKey;
    }
  }

  private final ManiaSettingsData data = new ManiaSettingsData();
  private final List<Step> steps = new ArrayList<>();
  private final Iterator<Step> iterator;

  public ManiaSettingsLoadProgressModel(String screen) {
    super(Messages.get("dialog.mania_settings_loading"));
    steps.add(Step.registration);
    steps.add(Step.cabinet);
    if ("mania-privacy-settings.fxml".equals(screen)) {
      steps.add(Step.accounts);
      steps.add(Step.avatar);
    }
    this.iterator = steps.iterator();
  }

  public ManiaSettingsData getData() {
    return data;
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public boolean isCancelable() {
    return false;
  }

  @Override
  public int getMax() {
    return steps.size();
  }

  @Override
  public Step getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(Step step) {
    return Messages.get(step.messageKey);
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, Step step) {
    try {
      switch (step) {
        case registration: {
          data.setRegistered(ManiaHelper.isRegistered());
          data.setSettings(client.getPreferenceService().getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class));
          break;
        }
        case cabinet: {
          if (data.isRegistered()) {
            data.setCabinet(maniaClient.getCabinetClient().getDefaultCabinetCached());
          }
          break;
        }
        case accounts: {
          Cabinet cabinet = data.getCabinet();
          if (cabinet != null) {
            data.setAccounts(maniaClient.getCabinetClient().getAccounts(cabinet.getId(), cabinet.getUuid()));
          }
          break;
        }
        case avatar: {
          Cabinet cabinet = data.getCabinet();
          if (cabinet != null) {
            // warms up the image cache, so that the cabinet panel can render the avatar without blocking
            try (InputStream in = client.getCachedUrlImage(maniaClient.getCabinetClient().getAvatarUrl(cabinet.getUuid()))) {
              // nothing to read
            }
          }
          break;
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to fetch mania settings data for step '{}': {}", step, e.getMessage(), e);
    }
    progressResultModel.addProcessed();
  }
}

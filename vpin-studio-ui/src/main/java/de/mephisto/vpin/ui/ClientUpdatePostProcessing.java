package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.ui.preferences.RegenerateMediaCacheProgressModel;
import de.mephisto.vpin.ui.util.ProgressDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.mephisto.vpin.ui.Studio.client;

/**
 * Update hook if something must be updated or migrated from the client.
 */
public class ClientUpdatePostProcessing {
  private final static Logger LOG = LoggerFactory.getLogger(ClientUpdatePostProcessing.class);

  public static void executePostProcessing() {
    try {
      if (!client.getAssetService().isMediaIndexAvailable()) {
        ProgressDialog.createProgressDialog(new RegenerateMediaCacheProgressModel(client.getGameService().getVpxGamesCached()));
      }
    }
    catch (Exception e) {
      LOG.error("Client update post processing failed: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Client update post processing failed: " + e.getMessage());
    }
  }
}

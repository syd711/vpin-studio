package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class SupportPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(SupportPreferencesController.class);

  @FXML
  private void onZipDownload() {
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle("Select Target Folder");
    File targetFolder = chooser.showDialog(stage);

    ProgressResultModel resultModel = ProgressDialog.createProgressDialog(new LogsDownloadProgressModel("Download Logs", targetFolder));
    if(!resultModel.getResults().isEmpty()) {
      File target = (File) resultModel.getResults().get(0);
      WidgetFactory.showInformation(stage, "Logs Generated", "Downloaded \"" + target.getAbsolutePath() + "\".", "Please attach this file with a description to a github issue.");
    }
  }

  @FXML
  private void onDiscordLink() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(new URI(UIDefaults.DISCORD_INVITE_LINK));
      } catch (Exception e) {
        LOG.error("Failed to open discord link: " + e.getMessage(), e);
      }
    }
  }

  @FXML
  private void onGithubLink() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(new URI("https://github.com/syd711/vpin-studio/issues"));
      } catch (Exception e) {
        LOG.error("Failed to open discord link: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }
}
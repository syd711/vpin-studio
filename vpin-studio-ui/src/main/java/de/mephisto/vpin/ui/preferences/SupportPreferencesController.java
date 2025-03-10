package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.stage.DirectoryChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.stage;

public class SupportPreferencesController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(SupportPreferencesController.class);

  @FXML
  private void onZipDownload() {
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle("Select Target Folder");
    File targetFolder = chooser.showDialog(stage);

    if (targetFolder != null && targetFolder.exists()) {
      ProgressResultModel resultModel = ProgressDialog.createProgressDialog(new LogsDownloadProgressModel("Download Logs", targetFolder));
      if (!resultModel.getResults().isEmpty()) {
        File target = (File) resultModel.getResults().get(0);
        WidgetFactory.showInformation(stage, "Logs Generated", "Downloaded \"" + target.getAbsolutePath() + "\".", "Please attach this file with a description to a github issue.");
      }
    }
  }

  @FXML
  private void onLink(ActionEvent event) {
    Hyperlink link = (Hyperlink) event.getSource();
    String linkText = link.getText();
    Studio.browse(linkText);
  }

  @FXML
  private void onDiscordLink() {
    Studio.browse(UIDefaults.DISCORD_INVITE_LINK);
  }

  @FXML
  private void onGithubLink() {
    Studio.browse("https://github.com/syd711/vpin-studio/issues");
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }
}

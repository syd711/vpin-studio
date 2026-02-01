package de.mephisto.vpin.ui.mania.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.ui.Studio;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static de.mephisto.vpin.ui.Studio.maniaClient;

public class FriendSearchDialogController implements DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(FriendSearchDialogController.class);

  @FXML
  private TextField nameField;

  @FXML
  private Button okButton;

  private Stage stage;

  @Override
  public void onDialogCancel() {

  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onDialogSubmit(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    String text = nameField.getText();
    if (!StringUtils.isEmpty(text)) {
      Cabinet myCabinet = maniaClient.getCabinetClient().getDefaultCabinetCached();
      if (myCabinet == null || myCabinet.getUuid().equalsIgnoreCase(text) || myCabinet.getDisplayName().equalsIgnoreCase(text)) {
        WidgetFactory.showInformation(stage, "Invalid Cabinet Id", "You can not invite yourself.");
        return;
      }

      List<String> search = maniaClient.getCabinetClient().search(text);
      if (search.size() > 1) {
        WidgetFactory.showInformation(stage, "Cabinet Id Not Unique", "Try to search for the full cabinet name or search for your friends Cabinet Id.");
        return;
      }
      if (search.size() == 1) {
        stage.close();
        maniaClient.getContactClient().createInvite(myCabinet.getId(), search.get(0));
        Platform.runLater(() -> {
          WidgetFactory.showInformation(Studio.stage, "Invite Sent", "An invite has been sent.", "The friend will appear in the friends list once the invite has been accepted.");
        });
      }
      else {
        Platform.runLater(() -> {
          WidgetFactory.showInformation(stage, "No Match Found", "No matching cabinet has been found.");
        });
        return;
      }
    }
    stage.close();
  }

  public void setStage(Stage stage) {
    this.stage = stage;
    this.nameField.requestFocus();
  }
}

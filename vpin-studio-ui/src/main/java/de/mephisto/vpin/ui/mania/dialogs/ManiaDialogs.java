package de.mephisto.vpin.ui.mania.dialogs;

import de.mephisto.vpin.restclient.mania.ManiaRegistration;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManiaDialogs {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaDialogs.class);


  public static ManiaRegistration openRegistrationDialog() {
    Stage stage = Dialogs.createStudioDialogStage(ManiaRegistrationDialogController.class, "dialog-mania-registration.fxml", "VPin Mania Registration");
    ManiaRegistrationDialogController controller = (ManiaRegistrationDialogController) stage.getUserData();
    stage.showAndWait();
    return controller.getManiaRegistration();
  }

  public static void openAccountSearchDialog() {
    Stage stage = Dialogs.createStudioDialogStage(FriendSearchDialogController.class, "dialog-friend-search.fxml", "VPin Mania Cabinet Search");
    FriendSearchDialogController controller = (FriendSearchDialogController) stage.getUserData();
    controller.setStage(stage);
    stage.showAndWait();
  }
}

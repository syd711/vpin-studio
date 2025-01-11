package de.mephisto.vpin.ui.friends;

import de.mephisto.vpin.ui.friends.dialogs.FriendSearchDialogController;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.stage.Stage;

public class FriendsDialogs {

  public static void openAccountSearchDialog() {
    Stage stage = Dialogs.createStudioDialogStage(FriendSearchDialogController.class, "dialog-friend-search.fxml", "VPin Mania Cabinet Search");
    FriendSearchDialogController controller = (FriendSearchDialogController) stage.getUserData();
    controller.setStage(stage);
    stage.showAndWait();
  }
}

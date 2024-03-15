package de.mephisto.vpin.ui.cards;

import de.mephisto.vpin.ui.cards.dialogs.TemplateManagerDialogController;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.stage.Stage;

public class CardsDialogs {
  public static boolean openTemplateManager(HighscoreCardsController highscoreCardsController) {
    Stage stage = Dialogs.createStudioDialogStage(TemplateManagerDialogController.class, "dialog-template-admin.fxml", "Card Template Manager");
    TemplateManagerDialogController controller = (TemplateManagerDialogController) stage.getUserData();
    controller.setHighscoreCardsController(highscoreCardsController);
    stage.showAndWait();
    return true;
  }
}

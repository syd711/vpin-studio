package de.mephisto.vpin.ui.cards;

import de.mephisto.vpin.ui.cards.dialogs.TemplateManagerDialogController;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.FXResizeHelper;
import javafx.stage.Stage;

public class CardsDialogs {
  public static boolean openTemplateManager(HighscoreCardsController highscoreCardsController) {
    Stage stage = Dialogs.createStudioDialogStage(TemplateManagerDialogController.class, "dialog-template-admin.fxml", "Template Editor");
    stage.setResizable(true);
    TemplateManagerDialogController controller = (TemplateManagerDialogController) stage.getUserData();
    controller.setHighscoreCardsController(highscoreCardsController);

    FXResizeHelper fxResizeHelper = new FXResizeHelper(stage, 30, 6);
    stage.setUserData(fxResizeHelper);

    stage.showAndWait();
    return true;
  }
}

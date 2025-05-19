package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.restclient.highscores.NVRamList;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

public class CompetitionsDialogHelper {

  public static void refreshResetStatusIcon(GameRepresentation game, NVRamList nvRamList, Label nvramLabel) {
    if (nvRamList.contains(game.getRom()) || nvRamList.contains(game.getTableName())) {
      nvramLabel.setGraphic(WidgetFactory.createCheckIcon());
      nvramLabel.setTooltip(new Tooltip("The highscore can be resetted, resetted NVRam has been found."));
    }
    else if (game.getHighscoreType() == null) {
      nvramLabel.setGraphic(WidgetFactory.createExclamationIcon());
      nvramLabel.setTooltip(new Tooltip("Unknown Highscore Format"));
    }
    else if (game.getHighscoreType().equals(HighscoreType.EM) || game.getHighscoreType().equals(HighscoreType.VPReg) || game.getHighscoreType().equals(HighscoreType.Ini)) {
      nvramLabel.setGraphic(WidgetFactory.createCheckIcon());
      nvramLabel.setTooltip(new Tooltip("The highscore can be resetted."));
    }
    else if (game.getHighscoreType().equals(HighscoreType.NVRam)) {
      nvramLabel.setGraphic(WidgetFactory.createIcon("mdi2a-alert-circle-check-outline"));
      nvramLabel.setTooltip(new Tooltip("No resetted NVRam found, the highscores will be resetted to the ROMs default values."));
    }
  }
}

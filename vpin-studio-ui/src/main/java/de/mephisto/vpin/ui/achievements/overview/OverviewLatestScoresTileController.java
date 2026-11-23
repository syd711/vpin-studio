package de.mephisto.vpin.ui.achievements.overview;

import de.mephisto.vpin.commons.fx.widgets.WidgetLatestScoresController;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Overview tile: wraps the latest scores widget of the commons module.
 */
public class OverviewLatestScoresTileController {

  @FXML
  private BorderPane latestScores;
  @FXML
  private WidgetLatestScoresController latestScoresController;

  @FXML
  private void initialize() {
    // the tile renders the title, the widget only provides the list which fills the full tile height
    latestScores.setTop(null);
    latestScores.setPadding(Insets.EMPTY);
    latestScores.setMaxHeight(Double.MAX_VALUE);
    if (latestScores.getCenter() instanceof Pane listRoot) {
      listRoot.setMaxHeight(Double.MAX_VALUE);
      for (Node child : listRoot.getChildren()) {
        if (child instanceof ScrollPane scrollPane) {
          scrollPane.setMaxHeight(Double.MAX_VALUE);
          VBox.setVgrow(scrollPane, Priority.ALWAYS);
        }
      }
    }
  }

  public void render() {
    latestScoresController.refresh();
  }
}

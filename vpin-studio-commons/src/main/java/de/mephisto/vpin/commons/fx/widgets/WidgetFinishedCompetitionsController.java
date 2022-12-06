package de.mephisto.vpin.commons.fx.widgets;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.restclient.representations.CompetitionRepresentation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class WidgetFinishedCompetitionsController extends WidgetController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(WidgetFinishedCompetitionsController.class);

  @FXML
  private VBox competitionsVBox;

  @FXML
  private BorderPane root;

  // Add a public no-args constructor
  public WidgetFinishedCompetitionsController() {
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    List<CompetitionRepresentation> competitions = OverlayWindowFX.client.getFinishedCompetitions(10);
    try {
      for (CompetitionRepresentation c : competitions) {
        FXMLLoader loader = new FXMLLoader(WidgetCompetitionSummaryController.class.getResource("widget-competition-summary.fxml"));
        BorderPane row = loader.load();
        WidgetCompetitionSummaryController controller = loader.getController();
        row.setMaxWidth(Double.MAX_VALUE);
        competitionsVBox.getChildren().add(row);
        controller.setCompetition(c);
      }
    } catch (IOException e) {
      LOG.error("Failed to create widget: " + e.getMessage(), e);
    }
  }
}
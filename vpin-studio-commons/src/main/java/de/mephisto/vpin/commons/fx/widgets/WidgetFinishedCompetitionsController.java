package de.mephisto.vpin.commons.fx.widgets;

import de.mephisto.vpin.commons.fx.LoadingOverlayController;
import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class WidgetFinishedCompetitionsController extends WidgetController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private VBox competitionsVBox;

  @FXML
  private BorderPane root;

  @FXML
  private StackPane viewStack;

  private Parent loadingOverlay;

  // Add a public no-args constructor
  public WidgetFinishedCompetitionsController() {
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    try {
      FXMLLoader loader = new FXMLLoader(LoadingOverlayController.class.getResource("loading-overlay.fxml"));
      loadingOverlay = loader.load();
      LoadingOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Finished Competitions...");
    } catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }
  }

  public void refresh() {
    viewStack.getChildren().add(loadingOverlay);
    competitionsVBox.getChildren().removeAll(competitionsVBox.getChildren());

    new Thread(() -> {
      List<CompetitionRepresentation> competitions = ServerFX.client.getCompetitionService().getFinishedCompetitions(3);

      Platform.runLater(() -> {
        root.setVisible(!competitions.isEmpty());
        try {
          for (CompetitionRepresentation c : competitions) {
            FXMLLoader loader = new FXMLLoader(WidgetCompetitionSummaryController.class.getResource("widget-competition-summary.fxml"));
            BorderPane row = loader.load();
            WidgetCompetitionSummaryController controller = loader.getController();
            row.setMaxWidth(Double.MAX_VALUE);
            controller.setCompetition(null, c);

            competitionsVBox.getChildren().add(row);
          }
        } catch (IOException e) {
          LOG.error("Failed to create widget: " + e.getMessage(), e);
        }

        viewStack.getChildren().remove(loadingOverlay);
      });
    }).start();
  }
}
package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.widgets.WidgetCompetitionController;
import de.mephisto.vpin.commons.fx.widgets.WidgetLatestScoresController;
import de.mephisto.vpin.commons.fx.widgets.WidgetPlayerRankController;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class DashboardController implements Initializable, StudioFXController, ChangeListener<Number> {
  private final static Logger LOG = LoggerFactory.getLogger(DashboardController.class);

  private final Debouncer debouncer = new Debouncer();
  public static final int DEBOUNCE_MS = 200;

  @FXML
  private BorderPane widgetLatestScore;

  @FXML
  private BorderPane widgetRight;

  @FXML
  private VBox widgetCompetition;

  private WidgetCompetitionController offlineCompetitionWidgetController;
  private WidgetCompetitionController discordCompetitionWidgetController;
  private WidgetLatestScoresController latestScoresController;
  private WidgetPlayerRankController playerRankController;
  private BorderPane discordCompetitionsRoot;

  // Add a public no-args constructor
  public DashboardController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NavigationController.setBreadCrumb(Arrays.asList("Dashboard"));

    try {
      FXMLLoader loader = new FXMLLoader(WidgetLatestScoresController.class.getResource("widget-latest-scores.fxml"));
      BorderPane root = loader.load();
      latestScoresController = loader.getController();
      root.setMaxHeight(Double.MAX_VALUE);
      widgetLatestScore.setLeft(root);
    }
    catch (IOException e) {
      LOG.error("Failed to load score widget: " + e.getMessage(), e);
    }

//    try {
//      FXMLLoader loader = new FXMLLoader(WidgetCompetitionController.class.getResource("widget-competition.fxml"));
//      BorderPane activeCompetitionBorderPane = loader.load();
//      activeCompetitionBorderPane.setMaxWidth(Double.MAX_VALUE);
//      offlineCompetitionWidgetController = loader.getController();
//      widgetCompetition.getChildren().add(activeCompetitionBorderPane);
//      offlineCompetitionWidgetController.setCompetitionType(CompetitionType.OFFLINE);
//      offlineCompetitionWidgetController.setCompact();
//
//    }
//    catch (IOException e) {
//      LOG.error("Failed to load competitions widget: " + e.getMessage(), e);
//    }
//
//    try {
//      FXMLLoader loader = new FXMLLoader(WidgetCompetitionController.class.getResource("widget-competition.fxml"));
//      discordCompetitionsRoot = loader.load();
//      discordCompetitionsRoot.setMaxWidth(Double.MAX_VALUE);
//      discordCompetitionWidgetController = loader.getController();
//      widgetCompetition.getChildren().add(discordCompetitionsRoot);
//      discordCompetitionWidgetController.setCompact();
//      discordCompetitionWidgetController.setCompetitionType(CompetitionType.DISCORD);
//    discordCompetitionsRoot.managedProperty().bindBidirectional(discordCompetitionsRoot.visibleProperty());
//    }
//    catch (IOException e) {
//      LOG.error("Failed to load competitions widget: " + e.getMessage(), e);
//    }

    try {
      FXMLLoader loader = new FXMLLoader(WidgetPlayerRankController.class.getResource("widget-player-rank.fxml"));
      BorderPane playersBorderPane = loader.load();
      playerRankController = loader.getController();
      playersBorderPane.setMaxWidth(Double.MAX_VALUE);
      playersBorderPane.setMaxHeight(Double.MAX_VALUE);
      widgetRight.setCenter(playersBorderPane);
    }
    catch (IOException e) {
      LOG.error("Failed to load finished players widget: " + e.getMessage(), e);
    }

  }

  private void refreshView() {
    Platform.runLater(() -> {
//      discordCompetitionsRoot.setVisible(Studio.stage.getHeight() > 1000);
      latestScoresController.refresh();
      playerRankController.refresh();

//      CompetitionRepresentation c = client.getActiveCompetition(CompetitionType.OFFLINE);
//      offlineCompetitionWidgetController.refresh(c);
//      if (discordCompetitionsRoot.isVisible()) {
//        c = client.getActiveCompetition(CompetitionType.DISCORD);
//        discordCompetitionWidgetController.refresh(c);
//      }
      NavigationController.setBreadCrumb(Arrays.asList("Dashboard"));
    });
  }

  @Override
  public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//    debouncer.debounce("height", () -> {
//      Platform.runLater(() -> {
//        refreshView();
//      });
//    }, DEBOUNCE_MS);
  }

  @Override
  public void onViewActivated(@Nullable NavigationOptions options) {
    Studio.stage.heightProperty().addListener(this);
    refreshView();
  }

  @Override
  public void onViewDeactivated() {
    Studio.stage.heightProperty().removeListener(this);
  }
}
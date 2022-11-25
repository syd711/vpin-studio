package de.mephisto.vpin.ui.players;

import de.mephisto.vpin.restclient.representations.PlayerRepresentation;
import de.mephisto.vpin.restclient.representations.PlayerScoreRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.StudioFXController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class PlayersController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(PlayersController.class);

  @FXML
  private Tab discordTab;

  @FXML
  private Tab buildInUsersTab;

  @FXML
  private TabPane tabPane;

  @FXML
  private Label noScoreLabel;

  @FXML
  private VBox highscoreList;

  @FXML
  private TitledPane highscoresTitledPane;

  private BuiltInPlayersController builtInPlayersController;
  private DiscordPlayersController discordPlayersController;

  // Add a public no-args constructor
  public PlayersController() {
  }

  public void updateSelection(Optional<PlayerRepresentation> player) {
    updateNavBar(player);

    highscoreList.getChildren().removeAll(highscoreList.getChildren());
    noScoreLabel.setVisible(false);
    if (player.isPresent()) {
      PlayerRepresentation p = player.get();
      if (StringUtils.isEmpty(p.getInitials())) {
        noScoreLabel.setVisible(true);
        noScoreLabel.setText("Player has no initials, no highscores could be resolved.");
        return;
      }

      new Thread() {
        @Override
        public void run() {
          List<PlayerScoreRepresentation> playerScores = client.getPlayerScores(p.getInitials());
          Platform.runLater(() -> {
            if (playerScores.isEmpty()) {
              noScoreLabel.setVisible(true);
              noScoreLabel.setText("No scores found for this player.");
            }
            else {
              noScoreLabel.setVisible(true);
              noScoreLabel.setText("Highscores for player '" + p.getName() + "'");
              for (PlayerScoreRepresentation playerScore : playerScores) {
                try {
                  FXMLLoader loader = new FXMLLoader(HighscoreWidgetController.class.getResource("widget-highscore.fxml"));
                  BorderPane row = loader.load();
                  row.setPrefWidth(600 - 48);
                  HighscoreWidgetController controller = loader.getController();
                  controller.setData(playerScore);
                  highscoreList.getChildren().add(row);
                } catch (IOException e) {
                  LOG.error("failed to load score component: " + e.getMessage(), e);
                }
              }
            }
          });
        }
      }.start();
    }
    else {
      noScoreLabel.setText("");
    }
  }

  private void updateNavBar(Optional<PlayerRepresentation> player) {
    int index = tabPane.getSelectionModel().selectedIndexProperty().get();
    if (index == 0) {
      if(player.isPresent()) {
        NavigationController.setBreadCrumb(Arrays.asList("Players", "Build-In Players", player.get().getName()));
      }
      else {
        NavigationController.setBreadCrumb(Arrays.asList("Players", "Build-In Players"));
      }
    }
    else {
      if(player.isPresent()) {
        NavigationController.setBreadCrumb(Arrays.asList("Players", "Discord Players", player.get().getName()));
      }
      else {
        NavigationController.setBreadCrumb(Arrays.asList("Players", "Discord Players"));
      }
    }
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NavigationController.setBreadCrumb(Arrays.asList("Players", "Build-In Players"));

    try {
      FXMLLoader loader = new FXMLLoader(BuiltInPlayersController.class.getResource("tab-builtin-users.fxml"));
      Parent builtInRoot = loader.load();
      builtInPlayersController = loader.getController();
      builtInPlayersController.setPlayersController(this);
      buildInUsersTab.setContent(builtInRoot);
    } catch (IOException e) {
      LOG.error("failed to load buildIn players: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(DiscordPlayersController.class.getResource("tab-discord-users.fxml"));
      Parent builtInRoot = loader.load();
      discordPlayersController = loader.getController();
      discordPlayersController.setPlayersController(this);
      discordTab.setContent(builtInRoot);
    } catch (IOException e) {
      LOG.error("failed to load buildIn players: " + e.getMessage(), e);
    }

    tabPane.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
        if (t1.intValue() == 0) {
          NavigationController.setBreadCrumb(Arrays.asList("Players", "Build-In Players"));
          Optional<PlayerRepresentation> selection = builtInPlayersController.getSelection();
          updateSelection(selection);
        }
        else {
          NavigationController.setBreadCrumb(Arrays.asList("Players", "Discord Players"));
          Optional<PlayerRepresentation> selection = discordPlayersController.getSelection();
          updateSelection(selection);
        }
      }
    });
  }
}
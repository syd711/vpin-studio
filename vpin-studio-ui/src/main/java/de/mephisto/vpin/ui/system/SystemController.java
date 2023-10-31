package de.mephisto.vpin.ui.system;

import de.mephisto.vpin.restclient.highscores.ScoreRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.tables.GameRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.players.BuiltInPlayersController;
import de.mephisto.vpin.ui.players.DiscordPlayersController;
import de.mephisto.vpin.ui.players.WidgetPlayerScoreController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class SystemController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(SystemController.class);

  @FXML
  private Tab discordTab;

  @FXML
  private Tab buildInUsersTab;

  @FXML
  private TabPane tabPane;

  // Add a public no-args constructor
  public SystemController() {
  }

  private void updateForTabSelection(Optional<PlayerRepresentation> player) {
    int index = tabPane.getSelectionModel().selectedIndexProperty().get();
//    if (index == 0) {
//      if (player.isPresent()) {
//        NavigationController.setBreadCrumb(Arrays.asList("Players", "Build-In Players", player.get().getName()));
//      }
//      else {
//        NavigationController.setBreadCrumb(Arrays.asList("Players", "Build-In Players"));
//      }
//
//      playerCountLabel.setText(builtInPlayersController.getCount() + " players");
//    }
//    else {
//      if (player.isPresent()) {
//        NavigationController.setBreadCrumb(Arrays.asList("Players", "Discord Players", player.get().getName()));
//      }
//      else {
//        NavigationController.setBreadCrumb(Arrays.asList("Players", "Discord Players"));
//      }
//
//      playerCountLabel.setText(discordPlayersController.getCount() + " players");
//    }
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NavigationController.setBreadCrumb(Arrays.asList("System Manager"));
//    try {
//      FXMLLoader loader = new FXMLLoader(BuiltInPlayersController.class.getResource("tab-builtin-users.fxml"));
//      Parent builtInRoot = loader.load();
//      builtInPlayersController = loader.getController();
//      builtInPlayersController.setPlayersController(this);
//      buildInUsersTab.setContent(builtInRoot);
//    } catch (IOException e) {
//      LOG.error("failed to load buildIn players: " + e.getMessage(), e);
//    }
//
//    try {
//      FXMLLoader loader = new FXMLLoader(DiscordPlayersController.class.getResource("tab-discord-users.fxml"));
//      Parent builtInRoot = loader.load();
//      discordPlayersController = loader.getController();
//      discordPlayersController.setPlayersController(this);
//      discordTab.setContent(builtInRoot);
//    } catch (IOException e) {
//      LOG.error("failed to load buildIn players: " + e.getMessage(), e);
//    }
//
    tabPane.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> {
      refreshTabSelection(t1);
    });

    updateForTabSelection(Optional.empty());
  }

  private void refreshTabSelection(Number t1) {
      NavigationController.setBreadCrumb(Arrays.asList("System Manager"));
//      updateSelection(selection);
  }

  @Override
  public void onViewActivated() {
    refreshTabSelection(tabPane.getSelectionModel().getSelectedIndex());
  }
}
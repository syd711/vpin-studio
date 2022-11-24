package de.mephisto.vpin.ui.players;

import de.mephisto.vpin.restclient.representations.PlayerRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.StudioFXController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;

public class PlayersController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(PlayersController.class);

  @FXML
  private Tab discordTab;

  @FXML
  private Tab buildInUsersTab;

  @FXML
  private TabPane tabPane;

  private BuiltInPlayersController builtInPlayersController;
  private DiscordPlayersController discordPlayersController;

  // Add a public no-args constructor
  public PlayersController() {
  }

  public void updateSelection(Optional<PlayerRepresentation> player) {

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
        System.out.println("Selected tab " + t1);
      }
    });
  }
}
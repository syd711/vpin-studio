package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.representations.PlayerRepresentation;
import de.mephisto.vpin.restclient.representations.PlaylistRepresentation;
import de.mephisto.vpin.ui.Studio;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class DiscordBotAllowListDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(DiscordBotAllowListDialogController.class);

  @FXML
  private Button cancelBtn;

  @FXML
  private ComboBox<DiscordServer> serverCombo;

  @FXML
  private VBox userList;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    List<DiscordServer> servers = client.getDiscordService().getDiscordServers();
    ObservableList<DiscordServer> discordServers = FXCollections.observableArrayList(servers);
    serverCombo.setItems(FXCollections.observableList(discordServers));

    serverCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      userList.getChildren().removeAll(userList.getChildren());

      if (newValue != null) {
        List<PlayerRepresentation> users = client.getDiscordService().getDiscordUsers(newValue.getId());
        for (PlayerRepresentation user : users) {
          HBox root = new HBox();
          root.setPrefWidth(300);
          root.setStyle("-fx-padding: 3 0 3 0;");
          root.setAlignment(Pos.BASELINE_LEFT);
          root.setSpacing(3);
          CheckBox checkBox = new CheckBox();
          checkBox.setUserData(user);
          checkBox.setText(user.getName());
//          checkBox.setSelected(playlist.getGameIds().contains(game.getId()));
          checkBox.setStyle("-fx-font-size: 14px;-fx-text-fill: white;");

          checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
              try {
//                if (t1) {
//                  PlaylistRepresentation update = client.getPlaylistsService().addToPlaylist(playlist, game);
//                  refreshPlaylist(update);
//                }
//                else {
//                  PlaylistRepresentation update = client.getPlaylistsService().removeFromPlaylist(playlist, game);
//                  refreshPlaylist(update);
//                }
              } catch (Exception e) {
                LOG.error("Failed to update playlists: " + e.getMessage(), e);
                WidgetFactory.showAlert(stage, "Error", "Failed to update playlists: " + e.getMessage());
              }
            }
          });

          root.getChildren().add(checkBox);

          userList.getChildren().add(root);
        }
      }
    });
  }


  @Override
  public void onDialogCancel() {
  }
}

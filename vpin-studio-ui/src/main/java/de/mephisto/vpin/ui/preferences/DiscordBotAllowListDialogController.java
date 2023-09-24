package de.mephisto.vpin.ui.preferences;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
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
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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
  private Label usersLabel;
  private DiscordBotPreferencesController preferencesController;

  @FXML
  private void onCancelClick(ActionEvent e) {
    preferencesController.refreshAllowList();
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    List<DiscordServer> servers = client.getDiscordService().getDiscordServers();
    ObservableList<DiscordServer> discordServers = FXCollections.observableArrayList(servers);
    serverCombo.setItems(FXCollections.observableList(discordServers));

    List<PlayerRepresentation> allowList = new ArrayList<>(client.getDiscordService().getAllowList());

    serverCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      userList.getChildren().removeAll(userList.getChildren());

      if (newValue != null) {
        List<PlayerRepresentation> users = client.getDiscordService().getDiscordUsers(newValue.getId());
        int count = 0;
        for (PlayerRepresentation user : users) {
          if (user.isBot()) {
            continue;
          }
          count++;

          HBox root = new HBox();
          root.setStyle("-fx-padding: 3 0 3 0;");
          root.setAlignment(Pos.BASELINE_LEFT);
          root.setSpacing(3);
          CheckBox checkBox = new CheckBox();
          checkBox.setUserData(user);
          checkBox.setText(user.getDisplayName());
          checkBox.setSelected(allowList.contains(user));
          checkBox.setStyle("-fx-font-size: 14px;-fx-text-fill: white;");

          checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean checked) {
              PlayerRepresentation player = (PlayerRepresentation) checkBox.getUserData();
              try {
                if (checked) {
                  if(!allowList.contains(player)) {
                    allowList.add(player);
                  }
                }
                else {
                  allowList.remove(player);
                }

                List<String> updatedList = allowList.stream().map(playerRepresentation -> String.valueOf(playerRepresentation.getId())).collect(Collectors.toList());
                String pref = String.join(",", updatedList);
                client.getPreferenceService().setPreference(PreferenceNames.DISCORD_BOT_ALLOW_LIST, pref);
                preferencesController.refreshAllowList();
              } catch (Exception e) {
                LOG.error("Failed to update playlists: " + e.getMessage(), e);
                WidgetFactory.showAlert(stage, "Error", "Failed to update playlists: " + e.getMessage());
              }
            }
          });

          root.getChildren().add(checkBox);
          userList.getChildren().add(root);
        }
        usersLabel.setText("Resolved Users (" + count + "):");
      }
    });

    if(!discordServers.isEmpty()) {
      serverCombo.setValue(discordServers.get(0));
    }
  }


  public void setPreferencesController(DiscordBotPreferencesController preferencesController) {
    this.preferencesController = preferencesController;
  }

  @Override
  public void onDialogCancel() {
  }
}

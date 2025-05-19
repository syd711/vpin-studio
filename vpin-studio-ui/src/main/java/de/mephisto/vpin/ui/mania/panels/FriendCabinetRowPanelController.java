package de.mephisto.vpin.ui.mania.panels;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.*;
import de.mephisto.vpin.ui.mania.FriendsListController;
import de.mephisto.vpin.ui.mania.FriendsPendingInvitesController;
import de.mephisto.vpin.ui.mania.ManiaPrivacySettingsController;
import de.mephisto.vpin.ui.mania.ManiaSettingsController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.*;

public class FriendCabinetRowPanelController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(FriendCabinetRowPanelController.class);

  @FXML
  private BorderPane root;

  @FXML
  private Label nameLabel;

  @FXML
  private Label activeGameLabel;

  @FXML
  private Label statusLabel;

  @FXML
  private ImageView avatarView;

  @FXML
  private ToolBar toolbar;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button acceptBtn;

  @FXML
  private VBox playerList;

  private FriendsPendingInvitesController invitesController;
  private ManiaPrivacySettingsController privacySettingsController;
  private FriendsListController friendsListController;

  private String cabinetUuid;
  private String displayName;
  private CabinetStatus status;

  @FXML
  private void onDelete() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Delete friendship to \"" + displayName + "\"?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      maniaClient.getContactClient().deleteContact(cabinetUuid);
      if (invitesController != null) {
        invitesController.reload();
      }
      else if (friendsListController != null) {
        friendsListController.reload();
      }
    }
  }

  @FXML
  private void onAccept() {
    maniaClient.getContactClient().acceptInvite(cabinetUuid);
    invitesController.reload();
    ManiaSettingsController.navigateTo("mania-friends-list");
  }

  public void setData(FriendsPendingInvitesController invitesController, CabinetContact contact) {
    this.cabinetUuid = contact.getUuid();
    this.status = contact.getStatus();
    this.displayName = contact.getDisplayName();

    this.invitesController = invitesController;
    acceptBtn.setVisible(true);
    refresh(contact.getUuid());
  }


  public void setData(ManiaPrivacySettingsController privacySettingsController, Cabinet cabinet) {
    this.cabinetUuid = cabinet.getUuid();
    this.status = cabinet.getStatus();
    this.displayName = cabinet.getDisplayName();
    this.privacySettingsController = privacySettingsController;
    acceptBtn.setVisible(false);
    deleteBtn.setVisible(false);
    toolbar.setVisible(false);
    refresh(cabinet.getUuid());
  }

  public void setData(FriendsListController friendsListController, CabinetContact contact) {
    this.cabinetUuid = contact.getUuid();
    this.status = contact.getStatus();
    this.displayName = contact.getDisplayName();

    this.friendsListController = friendsListController;
    acceptBtn.setVisible(false);
    refresh(contact.getUuid());
  }

  private void refresh(String cUuid) {
    playerList.setVisible(false);
    new Thread(() -> {
      Platform.runLater(() -> {
        InputStream in = client.getCachedUrlImage(maniaClient.getCabinetClient().getAvatarUrl(cUuid));
        if (in == null) {
          in = ServerFX.class.getResourceAsStream("avatar-blank.png");
        }
        Image image = new Image(in);
        avatarView.setImage(image);
        CommonImageUtil.setClippedImage(avatarView, (int) (image.getWidth() / 2));

        if (status.getStatus() != null && status.getStatus().equals(CabinetOnlineStatus.online)) {
          statusLabel.setText("Online");
          FontIcon icon = WidgetFactory.createIcon("mdi2c-checkbox-blank-circle");
          icon.setIconColor(Paint.valueOf(WidgetFactory.OK_COLOR));
          statusLabel.setGraphic(icon);
          statusLabel.setStyle(WidgetFactory.OK_STYLE);
          activeGameLabel.setVisible(true);
          if (status.getActiveGame() != null) {
            activeGameLabel.setText("Playing \"" + status.getActiveGame() + "\"");
          }
          else {
            activeGameLabel.setVisible(false);
          }
        }
        else {
          statusLabel.setText("Offline");
          FontIcon icon = WidgetFactory.createIcon("mdi2c-checkbox-blank-circle-outline");
          icon.setIconColor(Paint.valueOf("#FFFFFF"));
          statusLabel.setGraphic(icon);
          statusLabel.setStyle(WidgetFactory.DEFAULT_TEXT_STYLE);
          activeGameLabel.setVisible(false);
        }

        nameLabel.setText(displayName);
        List<Account> accounts = maniaClient.getCabinetClient().getAccounts(cabinetUuid);
        playerList.setVisible(!accounts.isEmpty());

        for (Account account : accounts) {
          try {
            FXMLLoader loader = new FXMLLoader(FriendAccountRowPanelController.class.getResource("friend-account-row-panel.fxml"));
            Pane node = loader.load();
            FriendAccountRowPanelController friendController = loader.getController();
            friendController.setData(friendsListController, cabinetUuid, account);
            playerList.getChildren().add(node);
          }
          catch (Exception e) {
            LOG.error("Failed to loading friends data: " + e.getMessage(), e);
            Platform.runLater(() -> {
              WidgetFactory.showAlert(stage, "Error", "Error loading friends data: " + e.getMessage());
            });
          }
        }
      });
    }).start();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    acceptBtn.managedProperty().bindBidirectional(acceptBtn.visibleProperty());
    deleteBtn.managedProperty().bindBidirectional(deleteBtn.visibleProperty());
    playerList.managedProperty().bindBidirectional(playerList.visibleProperty());
  }
}

package de.mephisto.vpin.ui.friends.panels;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.ui.FriendsController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.friends.FriendsListController;
import de.mephisto.vpin.ui.friends.FriendsPendingInvitesController;
import de.mephisto.vpin.ui.friends.FriendsPrivacySettingsController;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
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
  private FriendsPrivacySettingsController privacySettingsController;
  private FriendsListController friendsListController;
  private Cabinet contact;

  @FXML
  private void onDelete() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Delete friendship to \"" + contact.getDisplayName() + "\"?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      maniaClient.getContactClient().deleteContact(contact.getUuid());
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
    maniaClient.getContactClient().acceptInvite(contact.getUuid());
    invitesController.reload();
    FriendsController.navigateTo("friend-list");
  }

  public void setData(FriendsPendingInvitesController invitesController, Cabinet contact) {
    this.invitesController = invitesController;
    this.contact = contact;
    acceptBtn.setVisible(true);
    refresh(contact);
  }


  public void setData(FriendsPrivacySettingsController privacySettingsController, Cabinet contact) {
    this.privacySettingsController = privacySettingsController;
    this.contact = contact;
    acceptBtn.setVisible(false);
    deleteBtn.setVisible(false);
    toolbar.setVisible(false);
    refresh(contact);
  }

  public void setData(FriendsListController friendsListController, Cabinet contact) {
    this.friendsListController = friendsListController;
    this.contact = contact;
    acceptBtn.setVisible(false);
    refresh(contact);
  }

  private void refresh(Cabinet contact) {
    playerList.setVisible(false);
    new Thread(() -> {
      Platform.runLater(() -> {
        InputStream in = client.getCachedUrlImage(maniaClient.getCabinetClient().getAvatarUrl(contact.getUuid()));
        if (in == null) {
          in = ServerFX.class.getResourceAsStream("avatar-blank.png");
        }
        Image image = new Image(in);
        avatarView.setImage(image);
        CommonImageUtil.setClippedImage(avatarView, (int) (image.getWidth() / 2));

        nameLabel.setText(contact.getDisplayName());
        List<Account> accounts = maniaClient.getCabinetClient().getAccounts(contact.getUuid());
        playerList.setVisible(!accounts.isEmpty());

        for (Account account : accounts) {
          try {
            FXMLLoader loader = new FXMLLoader(FriendAccountRowPanelController.class.getResource("friend-account-row-panel.fxml"));
            Pane node = loader.load();
            FriendAccountRowPanelController friendController = loader.getController();
            friendController.setData(friendsListController, contact, account);
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

package de.mephisto.vpin.ui.mania.panels;

import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.connectors.mania.model.AccountVisibility;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.mania.FriendsListController;
import de.mephisto.vpin.ui.mania.util.ManiaAvatarCache;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.maniaClient;

public class FriendAccountRowPanelController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(FriendAccountRowPanelController.class);

  @FXML
  private BorderPane root;

  @FXML
  private Label nameLabel;

  @FXML
  private CheckBox visibilityCheckbox;

  @FXML
  private ToolBar toolbar;

  @FXML
  private ImageView avatarView;

  private FriendsListController friendsListController;
  private Account account;

  public void setData(FriendsListController friendsListController, String cabinetUuid, Account account) {
    this.friendsListController = friendsListController;
    this.account = account;
    this.toolbar.setVisible(String.valueOf(cabinetUuid).equals(maniaClient.getCabinetClient().getDefaultCabinetCached().getUuid()));

    visibilityCheckbox.setSelected(AccountVisibility.searchable.equals(account.getVisibility()));
    visibilityCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        try {
          account.setVisibility(newValue ? AccountVisibility.searchable : AccountVisibility.hidden);
          Cabinet defaultCabinetCached = maniaClient.getCabinetClient().getDefaultCabinetCached();
          maniaClient.getAccountClient().update(defaultCabinetCached.getId(), account);
        }
        catch (Exception e) {
          LOG.error("Failed to save mania account: " + e.getMessage(), e);
          Platform.runLater(() -> {
            WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save mania account: " + e.getMessage());
          });
        }
      }
    });


    refresh(account);
  }

  private void refresh(Account account) {
    new Thread(() -> {
      Platform.runLater(() -> {
        Image avatarImage = ManiaAvatarCache.getAvatarImage(account.getUuid());
        avatarView.setImage(avatarImage);
        CommonImageUtil.setClippedImage(avatarView, (int) (avatarImage.getWidth() / 2));
        nameLabel.setText(account.getDisplayName() + " [" + account.getInitials() + "]");
      });
    }).start();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
  }
}

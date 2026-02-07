package de.mephisto.vpin.ui.mania.panels;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.*;
import de.mephisto.vpin.ui.mania.ManiaPrivacySettingsController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
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
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.*;

public class CabinetRowPanelController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(CabinetRowPanelController.class);

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
  private VBox playerList;

  private ManiaPrivacySettingsController privacySettingsController;

  private String cabinetUuid;
  private String displayName;
  private CabinetStatus status;
  private Cabinet cabinet;

  public void setData(CabinetContact contact) {
    this.cabinetUuid = contact.getUuid();
    this.status = contact.getStatus();
    this.displayName = contact.getDisplayName();
    refresh(contact.getUuid());
  }


  public void setData(ManiaPrivacySettingsController privacySettingsController, Cabinet cabinet) {
    this.cabinetUuid = cabinet.getUuid();
    this.status = cabinet.getStatus();
    this.displayName = cabinet.getDisplayName();
    this.privacySettingsController = privacySettingsController;
    refresh(cabinet.getUuid());
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
        List<Account> accounts = maniaClient.getCabinetClient().getAccounts(cabinet.getId(), cabinetUuid);
        playerList.setVisible(!accounts.isEmpty());

        for (Account account : accounts) {
          try {
            FXMLLoader loader = new FXMLLoader(AccountRowPanelController.class.getResource("account-row-panel.fxml"));
            Pane node = loader.load();
            AccountRowPanelController friendController = loader.getController();
            friendController.setData(cabinetUuid, account);
            playerList.getChildren().add(node);
          }
          catch (Exception e) {
            LOG.error("Failed to loading account row data: " + e.getMessage(), e);
            Platform.runLater(() -> {
              WidgetFactory.showAlert(stage, "Error", "Error loading account data: " + e.getMessage());
            });
          }
        }
      });
    }).start();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    playerList.managedProperty().bindBidirectional(playerList.visibleProperty());

    cabinet = maniaClient.getCabinetClient().getDefaultCabinetCached();
  }
}

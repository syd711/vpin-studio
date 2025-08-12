package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class TablesSidebarDefaultBackgroundController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarDefaultBackgroundController.class);

  @FXML
  private ImageView rawDefaultBackgroundImage;

  @FXML
  private Button defaultPictureUploadBtn;

  @FXML
  private Button openDefaultPictureBtn;

  @FXML
  private Button resetBackgroundBtn;

  @FXML
  private Label resolutionLabel;

  private Optional<GameRepresentation> game = Optional.empty();

  //private TablesSidebarController tablesSidebarController;

  // Add a public no-args constructor
  public TablesSidebarDefaultBackgroundController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }

  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.refreshView(game);
  }

  @FXML
  public void onDefaultBackgroundUpload() {
    if (this.game.isPresent()) {
      boolean uploaded = TableDialogs.openDefaultBackgroundUploadDialog(this.game.get());
      if (uploaded) {
        EventManager.getInstance().notifyTableChange(this.game.get().getId(), this.game.get().getRom());
      }
    }
  }

  @FXML
  private void onBackgroundReset() {
    if (this.game.isPresent()) {
      GameRepresentation g = this.game.get();
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Re-generate default background for \"" + g.getGameDisplayName() + "\"?",
          "This will re-generate the existing default background.", null, "Generate Background");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        Studio.client.getAssetService().deleteGameAssets(g.getId());
        this.refreshView(this.game);
      }
    }
  }

  @FXML
  private void onDefaultBackgroundView() {
    if (game.isPresent()) {
      TableDialogs.openMediaDialog(Studio.stage, "Default Picture", Studio.client.getBackglassServiceClient().getDefaultPictureUrl(game.get()));
    }
  }

  public void refreshView(Optional<GameRepresentation> game) {
    openDefaultPictureBtn.setDisable(true);
    openDefaultPictureBtn.setTooltip(new Tooltip("Open default background"));
    rawDefaultBackgroundImage.setVisible(false);
    resetBackgroundBtn.setDisable(true);
    defaultPictureUploadBtn.setDisable(true);

    if (game.isPresent()) {
      GameRepresentation g = game.get();

      defaultPictureUploadBtn.setDisable(StringUtils.isEmpty(g.getRom()));
      resetBackgroundBtn.setDisable(!g.isDefaultBackgroundAvailable());
      openDefaultPictureBtn.setDisable(!g.isDefaultBackgroundAvailable());

      resolutionLabel.setText("Loading...");

      new Thread(() -> {
        try (InputStream input = Studio.client.getBackglassServiceClient().getDefaultPicture(game.get())) {
          Image image = new Image(input);
          Platform.runLater(() -> {
            rawDefaultBackgroundImage.setVisible(true);
            rawDefaultBackgroundImage.setImage(image);

            if (image.getWidth() > 300 && g.isDefaultBackgroundAvailable()) {
              openDefaultPictureBtn.setDisable(false);
              resolutionLabel.setText("Resolution: " + (int) image.getWidth() + " x " + (int) image.getHeight());
            }
            else {
              resolutionLabel.setText("");
            }
          });
        }
        catch (IOException e) {
          LOG.error("Failed to load default background: " + e.getMessage(), e);
        }
      }, "Default Background Loader...").start();

    }
    else {
      resolutionLabel.setText("");
    }
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    //this.tablesSidebarController = tablesSidebarController;
  }
}
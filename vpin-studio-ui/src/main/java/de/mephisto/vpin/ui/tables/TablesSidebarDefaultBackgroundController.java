package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.MediaUtil;
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

import java.io.ByteArrayInputStream;
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

  private VPinStudioClient client;

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;

  // Add a public no-args constructor
  public TablesSidebarDefaultBackgroundController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    client = Studio.client;
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.refreshView(game);
  }

  @FXML
  public void onDefaultBackgroundUpload() {
    if (this.game.isPresent()) {
      boolean uploaded = Dialogs.openDefaultBackgroundUploadDialog(this.game.get());
      if (uploaded) {
        tablesSidebarController.getTablesController().onReload();
      }
    }
  }

  @FXML
  private void onBackgroundReset() {
    if (this.game.isPresent()) {
      GameRepresentation g = this.game.get();
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Re-generate default background for \"" + g.getGameDisplayName() + "\"?",
          "This will re-generate the existing default background.", null, "Yes, generate background");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        client.getAssets().regenerateGameAssets(g.getId());
        this.refreshView(this.game);
      }
    }
  }

  @FXML
  private void onDefaultBackgroundView() {
    if (game.isPresent()) {
      ByteArrayInputStream image = client.getDirectB2S().getDefaultPicture(game.get());
      MediaUtil.openMedia(image);
    }
  }

  public void refreshView(Optional<GameRepresentation> game) {
    try {
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

        InputStream input = client.getDirectB2S().getDefaultPicture(game.get());
        Image image = new Image(input);
        rawDefaultBackgroundImage.setVisible(true);
        rawDefaultBackgroundImage.setImage(image);
        input.close();

        if (image.getWidth() > 300 && g.isDefaultBackgroundAvailable()) {
          openDefaultPictureBtn.setDisable(false);
          resolutionLabel.setText("Resolution: " + (int) image.getWidth() + " x " + (int) image.getHeight());
        }
        else {
          resolutionLabel.setText("");
        }
      }
      else {
        resolutionLabel.setText("");
      }
    } catch (IOException e) {
      LOG.error("Failed to load default background: " + e.getMessage(), e);
    }
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }
}
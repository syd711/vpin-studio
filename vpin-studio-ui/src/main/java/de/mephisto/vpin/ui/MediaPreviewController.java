package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.media.AssetMediaPlayer;
import de.mephisto.vpin.commons.utils.media.ImageViewer;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class MediaPreviewController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(MediaPreviewController.class);


  @FXML
  private BorderPane mediaView;

  private Stage dialogStage;
  private AssetMediaPlayer assetMediaPlayer;
  private FrontendMediaItemRepresentation item;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }

  public void setData(Stage dialogStage, GameRepresentation game, FrontendMediaItemRepresentation item) {
    this.dialogStage = dialogStage;
    assetMediaPlayer = WidgetFactory.addMediaItemToBorderPane(client, item, mediaView);

    this.item = item;

    if (assetMediaPlayer == null) {
      new Thread(() -> {
        Platform.runLater(() -> {
          ImageViewer imageViewer = (ImageViewer) mediaView.getUserData();
          imageViewer.getImageView().setFitWidth(dialogStage.getWidth() * 1 - 80);
          imageViewer.getImageView().setFitHeight(dialogStage.getHeight() * 1 - 80);
        });
      }).start();

    }
  }

  @Override
  public void onDialogCancel() {

  }
}
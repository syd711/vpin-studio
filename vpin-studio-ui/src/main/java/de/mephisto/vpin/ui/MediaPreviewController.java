package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.media.AssetMediaPlayer;
import de.mephisto.vpin.commons.utils.media.MediaOptions;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class MediaPreviewController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(MediaPreviewController.class);
  public static final int MARGIN = 44;

  @FXML
  private BorderPane mediaView;

  private Stage dialogStage;
  private AssetMediaPlayer assetMediaPlayer;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }

  public void setData(Stage dialogStage, String url, String mimeType) {
    this.dialogStage = dialogStage;

    Frontend frontend = client.getFrontendService().getFrontendCached();
    boolean playfieldMediaInverted = frontend.isPlayfieldMediaInverted();
    assetMediaPlayer = WidgetFactory.createAssetMediaPlayer(client, url, null, mimeType, playfieldMediaInverted, false, false);

    mediaView.setCenter(assetMediaPlayer);

    Platform.runLater(() -> resizeAssetMediaPlayer());
    dialogStage.widthProperty().addListener((obs, o, n) -> resizeAssetMediaPlayer());
    dialogStage.heightProperty().addListener((obs, o, n) -> resizeAssetMediaPlayer());
  }

  public void setData(Stage dialogStage, FrontendMediaItemRepresentation item, boolean usePreview) {
    this.dialogStage = dialogStage;

    assetMediaPlayer = WidgetFactory.createAssetMediaPlayer(client, item, false, usePreview);
    MediaOptions mediaOptions = new MediaOptions();
    mediaOptions.setAutoRotate(false);
    assetMediaPlayer.setMediaOptions(mediaOptions);

    mediaView.setCenter(assetMediaPlayer);

    Platform.runLater(() -> resizeAssetMediaPlayer());
    dialogStage.widthProperty().addListener((obs, o, n) -> resizeAssetMediaPlayer());
    dialogStage.heightProperty().addListener((obs, o, n) -> resizeAssetMediaPlayer());
  }

  @Override
  public void onDialogCancel() {
    assetMediaPlayer.disposeMedia();
  }

  private void resizeAssetMediaPlayer() {
    if (assetMediaPlayer != null) {
      assetMediaPlayer.resize(dialogStage.getWidth() - MARGIN, dialogStage.getHeight() - MARGIN);
      assetMediaPlayer.setMediaViewSize(dialogStage.getWidth() - MARGIN, dialogStage.getHeight() - MARGIN);
    }
  }
}

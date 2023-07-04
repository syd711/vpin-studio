package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.tables.TablesSidebarController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TableMediaAdminController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TableMediaAdminController.class);

  @FXML
  private BorderPane mediaPane;

  @FXML
  private Button addToPlaylistBtn;

  @FXML
  private ListView<GameMediaItemRepresentation> assetList;

  private boolean result = false;
  private GameRepresentation game;
  private PopperScreen screen;
  private TablesSidebarController tablesSidebarController;

  @FXML
  private void onPlaylistAdd() {

  }

  @FXML
  private void onDelete() {

  }

  @FXML
  private void onCancel(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.result = false;

    this.assetList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<GameMediaItemRepresentation>() {
      @Override
      public void changed(ObservableValue<? extends GameMediaItemRepresentation> observable, GameMediaItemRepresentation oldValue, GameMediaItemRepresentation mediaItem) {
        mediaPane.setCenter(null);

        String mimeType = mediaItem.getMimeType();
        String baseType = mimeType.split("/")[0];
        String url = client.getURL(mediaItem.getUri());

        if (baseType.equals("image")) {
          ImageView imageView = new ImageView();
          imageView.setFitWidth(400 - 10);
          imageView.setFitHeight(500 - 20);
          imageView.setPreserveRatio(true);

          ByteArrayInputStream gameMediaItem = client.getAssetService().getGameMediaItem(mediaItem.getGameId(), PopperScreen.valueOf(mediaItem.getScreen()));
          Image image = new Image(gameMediaItem);
          imageView.setImage(image);
          imageView.setUserData(mediaItem);

          mediaPane.setCenter(imageView);
        }
        else if (baseType.equals("video")) {
          Media media = new Media(url);
          MediaPlayer mediaPlayer = new MediaPlayer(media);
          mediaPlayer.setAutoPlay(true);
          mediaPlayer.setCycleCount(-1);
          mediaPlayer.setMute(true);
          mediaPlayer.setOnError(() -> {
            LOG.error("Media player error: " + mediaPlayer.getError());
            mediaPlayer.stop();
            mediaPlayer.dispose();

            Label label = new Label("Media Error");
            label.setStyle("-fx-font-size: 14px;-fx-text-fill: #444444;");
            label.setUserData(mediaItem);
            mediaPane.setCenter(label);
          });

          MediaView mediaView = new MediaView(mediaPlayer);
          mediaView.setUserData(mediaItem);
          mediaView.setPreserveRatio(true);
          mediaView.setFitWidth(400 - 10);
          mediaView.setFitHeight(500 - 20);

          mediaPane.setCenter(mediaView);
        }
      }
    });
  }

  @Override
  public void onDialogCancel() {

  }


  public void setGame(GameRepresentation game, PopperScreen screen) {
    this.game = game;
    this.screen = screen;

    List<GameMediaItemRepresentation> items = this.game.getGameMedia().getMediaItems(screen);
    ObservableList<GameMediaItemRepresentation> assets = FXCollections.observableList(items);
    assetList.setItems(assets);

    if (!items.isEmpty()) {
      assetList.getSelectionModel().select(0);
    }
  }

  public void setTableSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }
}

package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameMediaRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.JobFinishedEvent;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.tables.TablesSidebarController;
import de.mephisto.vpin.ui.util.Dialogs;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.restclient.jobs.JobType.POPPER_MEDIA_INSTALL;
import static de.mephisto.vpin.ui.Studio.client;

public class TableMediaAdminController implements Initializable, DialogController, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(TableMediaAdminController.class);

  @FXML
  private BorderPane mediaPane;

  @FXML
  private Button addToPlaylistBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button renameBtn;

  @FXML
  private VBox helpBox;


  @FXML
  private ListView<GameMediaItemRepresentation> assetList;

  private GameRepresentation game;
  private PopperScreen screen;
  private TablesSidebarController tablesSidebarController;

  @FXML
  private void onPlaylistAdd() {
    GameMediaItemRepresentation selectedItem = assetList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      try {
        client.getPinUPPopperService().toFullScreen(game.getId(), screen);
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", "Fullscreen switch failed: " + e.getMessage());
      }
      refresh();
    }
  }

  @FXML
  private void onRename() {
    GameMediaItemRepresentation selectedItem = assetList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      String name = FilenameUtils.getBaseName(selectedItem.getName());
      String newName = WidgetFactory.showInputDialog(Studio.stage, "Rename", "New Media Name",
          "Enter the new name of the media item \"" + selectedItem.getName() + "\".", null, name);
      if (!StringUtils.isEmpty(newName) && !newName.equalsIgnoreCase(selectedItem.getName())) {
        try {
          boolean rename = client.getPinUPPopperService().rename(game.getId(), screen, selectedItem.getName(), newName);
          if (!rename) {
            WidgetFactory.showAlert(Studio.stage, "Error", "Renaming failed, invalid name used?");
          }
        } catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, "Error", "Renaming failed: " + e.getMessage());
        }
        refresh();
      }
    }
  }

  @FXML
  private void onReload() {
    refresh();
  }

  @FXML
  private void onMediaUpload(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    Dialogs.openMediaUploadDialog(stage, tablesSidebarController, game, screen);
    refresh();
  }

  @FXML
  private void onDelete(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    GameMediaItemRepresentation selectedItem = assetList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Delete \"" + selectedItem.getName() + "\"?", "The selected media will be deleted.", "You have to use PinUP Popper to add new media assets.");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        client.getPinUPPopperService().deleteMedia(game.getId(), screen, selectedItem.getName());
        refresh();

        Platform.runLater(() -> {
          EventManager.getInstance().notifyJobFinished(POPPER_MEDIA_INSTALL, this.game.getId());
        });
      }
    }
  }

  @FXML
  private void onHelpLink() {
    if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
      try {
        Desktop.getDesktop().browse(new URI("https://www.nailbuster.com/wikipinup/doku.php?id=loading_video"));
      } catch (Exception ex) {
        LOG.error("Failed to open link: " + ex.getMessage(), ex);
      }
    }
  }

  @FXML
  private void onCancel(ActionEvent e) {
    EventManager.getInstance().removeListener(this);
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.deleteBtn.setDisable(true);
    this.helpBox.setVisible(false);
    this.addToPlaylistBtn.setDisable(true);
    this.addToPlaylistBtn.setVisible(false);
    this.renameBtn.setDisable(true);

    helpBox.managedProperty().bindBidirectional(helpBox.visibleProperty());

    EventManager.getInstance().addListener(this);

    this.assetList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<GameMediaItemRepresentation>() {
      @Override
      public void changed(ObservableValue<? extends GameMediaItemRepresentation> observable, GameMediaItemRepresentation oldValue, GameMediaItemRepresentation mediaItem) {
        disposeAll();

        deleteBtn.setDisable(mediaItem == null);
        renameBtn.setDisable(mediaItem == null);

        if (mediaItem == null) {
          Label label = new Label("No media selected");
          label.setStyle("-fx-font-size: 14px;-fx-text-fill: #444444;");
          label.setUserData(mediaItem);
          mediaPane.setCenter(label);
          return;
        }

        String mimeType = mediaItem.getMimeType();
        String baseType = mimeType.split("/")[0];
        String url = client.getURL(mediaItem.getUri()) + "/" + URLEncoder.encode(mediaItem.getName(), Charset.defaultCharset());
        LOG.info("Loading " + url);

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
        else if (baseType.equals("audio")) {
          VBox vBox = new VBox();
          vBox.setAlignment(Pos.BASELINE_CENTER);

          FontIcon fontIcon = new FontIcon();
          fontIcon.setIconSize(48);
          fontIcon.setIconColor(Paint.valueOf("#FFFFFF"));
          fontIcon.setIconLiteral("bi-play");

          Button playBtn = new Button();
          playBtn.setGraphic(fontIcon);
          vBox.getChildren().add(playBtn);

          Media media = new Media(url);
          MediaPlayer mediaPlayer = new MediaPlayer(media);
          mediaPlayer.setAutoPlay(false);
          mediaPlayer.setCycleCount(-1);
          mediaPlayer.setMute(true);
          mediaPlayer.setOnError(() -> {
            LOG.error("Media player error: " + mediaPlayer.getError());
            mediaPlayer.stop();
            mediaPlayer.dispose();

            Label label = new Label("Media Error");
            label.setStyle("-fx-font-size: 14px;-fx-text-fill: #444444;");
            label.setUserData(mediaItem);
            vBox.getChildren().add(label);
            mediaPane.setCenter(label);
          });


          MediaView mediaView = new MediaView(mediaPlayer);
          vBox.getChildren().add(mediaView);
          mediaPane.setCenter(vBox);

          mediaPlayer.setOnEndOfMedia(() -> {
            fontIcon.setIconLiteral("bi-play");
          });

          playBtn.setOnAction(event -> {
            String iconLiteral = fontIcon.getIconLiteral();
            if (iconLiteral.equals("bi-play")) {
              mediaView.getMediaPlayer().setMute(false);
              mediaView.getMediaPlayer().setCycleCount(1);
              mediaView.getMediaPlayer().play();
              fontIcon.setIconLiteral("bi-stop");
            }
            else {
              mediaView.getMediaPlayer().stop();
              fontIcon.setIconLiteral("bi-play");
            }
          });

        }
        else if (baseType.equals("video")) {
          Media media = new Media(url);
          MediaPlayer mediaPlayer = new MediaPlayer(media);
          mediaPlayer.setAutoPlay(true);
          mediaPlayer.setCycleCount(-1);
          mediaPlayer.setMute(true);
          mediaPlayer.setOnError(() -> {
            LOG.error("Media player error: " + mediaPlayer.getError() + ", URL: " + url);
            mediaPlayer.stop();
            mediaPlayer.dispose();

            Label label = new Label("  Media available\n(but not playable)");
            label.setStyle("-fx-font-color: #33CC00;-fx-text-fill:#33CC00; -fx-font-weight: bold;");
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

  private void disposeAll() {
    Node center = mediaPane.getCenter();
    if (center instanceof MediaView) {
      MediaView mediaView = (MediaView) center;
      mediaView.getMediaPlayer().stop();
      mediaView.getMediaPlayer().dispose();
    }
    else if (center instanceof VBox) {
      MediaView mediaView = (MediaView) ((VBox) center).getChildren().get(1);
      mediaView.getMediaPlayer().stop();
      mediaView.getMediaPlayer().dispose();
    }

    mediaPane.setCenter(null);
  }

  @Override
  public void onDialogCancel() {
    EventManager.getInstance().removeListener(this);
  }


  public void setGame(GameRepresentation game, PopperScreen screen) {
    this.game = game;
    this.screen = screen;

    refresh();
  }

  private void refresh() {
    this.addToPlaylistBtn.setVisible(screen.equals(PopperScreen.Loading));
    this.addToPlaylistBtn.setDisable(true);

    GameMediaRepresentation gameMedia = client.getPinUPPopperService().getGameMedia(this.game.getId());
    List<GameMediaItemRepresentation> items = gameMedia.getMediaItems(screen);
    ObservableList<GameMediaItemRepresentation> assets = FXCollections.observableList(items);
    assetList.getItems().removeAll(assetList.getItems());
    assetList.setItems(assets);
    assetList.refresh();

    if (!items.isEmpty()) {
      assetList.getSelectionModel().select(0);
    }

    boolean convertable = items.size() == 1 && !items.get(0).getName().contains("(SCREEN");
    this.addToPlaylistBtn.setDisable(!convertable);

    helpBox.setVisible(screen != null && screen.equals(PopperScreen.Loading));
  }

  public void setTableSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }

  @Override
  public void jobFinished(@NonNull JobFinishedEvent event) {
    Platform.runLater(() -> {
      refresh();
    });
  }
}

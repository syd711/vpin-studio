package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.assets.EncryptDecrypt;
import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.connectors.assets.TableAssetsService;
import de.mephisto.vpin.restclient.client.TableAssetSearch;
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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.restclient.jobs.JobType.POPPER_MEDIA_INSTALL;
import static de.mephisto.vpin.ui.Studio.client;


public class TablePopperMediaDialogController implements Initializable, DialogController, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(TablePopperMediaDialogController.class);
  public static final int MEDIA_SIZE = 280;

  @FXML
  private BorderPane serverAssetMediaPane;

  @FXML
  private Button previewBtn;

  @FXML
  private Button downloadBtn;

  @FXML
  private TextField searchField;

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

  @FXML
  private ListView<TableAsset> serverAssetsList;

  private GameRepresentation game;
  private PopperScreen screen;
  private TablesSidebarController tablesSidebarController;
  private TableAssetsService tableAssetsService;
  private EncryptDecrypt encryptDecrypt;

  @FXML
  private void onVPSAssets() {
    Dialogs.openVPSAssetsDialog(game);
  }

  @FXML
  private void onPlaylistAdd() {
    GameMediaItemRepresentation selectedItem = assetList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      try {
        client.getPinUPPopperService().toFullScreen(game.getId(), screen);
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", "Fullscreen switch failed: " + e.getMessage());
      }
      refreshTableMediaView();
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
          if (!newName.trim().equalsIgnoreCase(name)) {
            boolean rename = client.getPinUPPopperService().rename(game.getId(), screen, selectedItem.getName(), newName);
            if (!rename) {
              WidgetFactory.showAlert(Studio.stage, "Error", "Renaming failed, invalid name used?");
            }
          }
        } catch (Exception e) {
          WidgetFactory.showAlert(Studio.stage, "Error", "Renaming failed: " + e.getMessage());
        }
        refreshTableMediaView();
      }
    }
  }

  @FXML
  private void onReload() {
    refreshTableMediaView();
  }

  @FXML
  private void onMediaUpload(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    Dialogs.openMediaUploadDialog(stage, tablesSidebarController, game, screen);
    refreshTableMediaView();
  }

  @FXML
  private void onDelete(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    GameMediaItemRepresentation selectedItem = assetList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Delete \"" + selectedItem.getName() + "\"?", "The selected media will be deleted.", null, "Delete");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        client.getPinUPPopperService().deleteMedia(game.getId(), screen, selectedItem.getName());
        refreshTableMediaView();

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
  private void onSearch() {
    String term = searchField.getText().trim();
    if (!StringUtils.isEmpty(term)) {
      try {
        TableAssetSearch result = client.getPinUPPopperService().searchTableAsset(screen, term);
        ObservableList<TableAsset> assets = FXCollections.observableList(result.getResult());
        serverAssetsList.getItems().removeAll(serverAssetsList.getItems());
        serverAssetsList.setItems(assets);
        serverAssetsList.refresh();
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", "Search failed: " + e.getMessage());
      }
    }
    else {
      serverAssetsList.getItems().removeAll(serverAssetsList.getItems());
      serverAssetsList.setItems(FXCollections.observableList(new ArrayList<>()));
      serverAssetsList.refresh();
    }
  }

  @FXML
  private void onPreview() {
    TableAsset tableAsset = serverAssetsList.getSelectionModel().getSelectedItem();
    if (tableAsset == null) {
      return;
    }

    downloadBtn.setVisible(true);
    serverAssetMediaPane.setCenter(new ProgressIndicator());

    Platform.runLater(() -> {
      String mimeType = tableAsset.getMimeType();
      String baseType = mimeType.split("/")[0];
      String assetUrl = null;
      try {
        assetUrl = this.encryptDecrypt.decrypt(tableAsset.getUrl());
      } catch (InvalidAlgorithmParameterException e) {
        throw new RuntimeException(e);
      } catch (InvalidKeyException e) {
        throw new RuntimeException(e);
      } catch (BadPaddingException e) {
        throw new RuntimeException(e);
      } catch (IllegalBlockSizeException e) {
        throw new RuntimeException(e);
      }

      try {
        if (baseType.equals("image")) {
          ImageView imageView = new ImageView();
          imageView.setFitWidth(MEDIA_SIZE);
          imageView.setFitHeight(MEDIA_SIZE);
          imageView.setPreserveRatio(true);

          Image image = new Image(assetUrl);
          imageView.setImage(image);
          imageView.setUserData(tableAsset);

          serverAssetMediaPane.setCenter(imageView);
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

          Media media = new Media(assetUrl);
          MediaPlayer mediaPlayer = new MediaPlayer(media);
          mediaPlayer.setAutoPlay(false);
          mediaPlayer.setCycleCount(-1);
          mediaPlayer.setMute(false);
          mediaPlayer.setOnError(() -> {
            LOG.error("Media player error: " + mediaPlayer.getError());
            mediaPlayer.stop();
            mediaPlayer.dispose();

            Label label = new Label("Media Error");
            label.setStyle("-fx-font-size: 14px;-fx-text-fill: #444444;");
            label.setUserData(tableAsset);
            vBox.getChildren().add(label);
            serverAssetMediaPane.setCenter(label);
          });


          MediaView mediaView = new MediaView(mediaPlayer);
          vBox.getChildren().add(mediaView);
          serverAssetMediaPane.setCenter(vBox);

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
          Media media = new Media(assetUrl);
          MediaPlayer mediaPlayer = new MediaPlayer(media);
          mediaPlayer.setAutoPlay(true);
          mediaPlayer.setStopTime(Duration.seconds(5));
          mediaPlayer.setCycleCount(-1);
          mediaPlayer.setMute(true);
          mediaPlayer.setOnError(() -> {
            LOG.error("Media player error: " + mediaPlayer.getError());
            mediaPlayer.stop();
            mediaPlayer.dispose();

            Label label = new Label("  Media available\n(but not playable)");
            label.setStyle("-fx-font-color: #33CC00;-fx-text-fill:#33CC00; -fx-font-weight: bold;");
            label.setUserData(tableAsset);
            serverAssetMediaPane.setCenter(label);
          });

          MediaView mediaView = new MediaView(mediaPlayer);
          mediaView.setUserData(tableAsset);
          mediaView.setPreserveRatio(true);
          mediaView.setFitWidth(MEDIA_SIZE);
          mediaView.setFitHeight(MEDIA_SIZE);

          serverAssetMediaPane.setCenter(mediaView);
        }
      } catch (Exception e) {
        LOG.error("Preview failed for " + tableAsset);
      }
    });


  }

  @FXML
  private void onDownload(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
    TableAsset tableAsset = this.serverAssetsList.getSelectionModel().getSelectedItem();
    boolean append = false;

    ObservableList<GameMediaItemRepresentation> items = assetList.getItems();
    boolean alreadyExists = items.stream().anyMatch(i -> i.getName().equalsIgnoreCase(tableAsset.toAssetName(game.getGameDisplayName())));
    if (alreadyExists) {
      Optional<ButtonType> buttonType = WidgetFactory.showConfirmationWithOption(Studio.stage, "Asset Exists", "An asset with the same name already exists.",
          "Overwrite existing asset or append new asset?", "Overwrite", "Append");
      if (buttonType.isPresent() && buttonType.get().equals(ButtonType.OK)) {

      }
      else if (buttonType.isPresent() && buttonType.get().equals(ButtonType.APPLY)) {
        append = true;
      }
      else {
        return;
      }
    }

    if (tableAsset != null) {
      Dialogs.createProgressDialog(stage, new TableAssetDownloadProgressModel(screen, game, tableAsset, append));
      refreshTableMediaView();
      EventManager.getInstance().notifyTableChange(game.getId(), null);
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
    try {
      encryptDecrypt = new EncryptDecrypt(EncryptDecrypt.KEY);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    } catch (NoSuchPaddingException e) {
      throw new RuntimeException(e);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }

    previewBtn.setDisable(true);
    downloadBtn.setVisible(false);

    this.deleteBtn.setDisable(true);
    this.helpBox.setVisible(false);
    this.addToPlaylistBtn.setDisable(true);
    this.addToPlaylistBtn.setVisible(false);
    this.renameBtn.setDisable(true);

    searchField.setOnKeyPressed(ke -> {
      if (ke.getCode().equals(KeyCode.ENTER)) {
        onSearch();
      }
    });

    serverAssetsList.setPlaceholder(new Label("No assets found."));
    assetList.setPlaceholder(new Label("No assets found."));

    helpBox.managedProperty().bindBidirectional(helpBox.visibleProperty());

    EventManager.getInstance().addListener(this);

    Label label = new Label("No asset preview activated.");
    label.setStyle("-fx-font-size: 14px;-fx-text-fill: #444444;");
    serverAssetMediaPane.setCenter(label);

    tableAssetsService = new TableAssetsService();

    try {
      Class<?> aClass = Class.forName("de.mephisto.vpin.popper.PopperAssetAdapter");
      TableAssetsAdapter adapter = (TableAssetsAdapter) aClass.getDeclaredConstructor().newInstance();
      tableAssetsService.registerAdapter(adapter);
    } catch (Exception e) {
      LOG.error("Unable to find PopperAssetAdapter: " + e.getMessage());
    }

    this.serverAssetsList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TableAsset>() {
      @Override
      public void changed(ObservableValue<? extends TableAsset> observable, TableAsset oldValue, TableAsset tableAsset) {
        disposeServerAssetPreview();
        previewBtn.setDisable(tableAsset == null);
        downloadBtn.setVisible(false);

        Label label = new Label("No asset preview activated.");
        label.setStyle("-fx-font-size: 14px;-fx-text-fill: #444444;");
        serverAssetMediaPane.setCenter(label);
      }
    });

    this.serverAssetsList.setOnMouseClicked(click -> {
      if (click.getClickCount() == 2) {
        onPreview();
      }
    });


    this.assetList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<GameMediaItemRepresentation>() {
      @Override
      public void changed(ObservableValue<? extends GameMediaItemRepresentation> observable, GameMediaItemRepresentation oldValue, GameMediaItemRepresentation mediaItem) {
        if(screen.equals(PopperScreen.Wheel)) {
          client.getImageCache().clearWheelCache();
        }

        disposeTableMediaPreview();

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
          imageView.setFitWidth(MEDIA_SIZE);
          imageView.setFitHeight(MEDIA_SIZE);
          imageView.setPreserveRatio(true);

          Image image = new Image(url);
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
          mediaView.setFitWidth(MEDIA_SIZE);
          mediaView.setFitHeight(MEDIA_SIZE);

          mediaPane.setCenter(mediaView);
        }
      }
    });
  }

  private void disposeServerAssetPreview() {
    Node center = serverAssetMediaPane.getCenter();
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

    serverAssetMediaPane.setCenter(null);
  }


  private void disposeTableMediaPreview() {
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

    String term = game.getGameDisplayName();
    term = term.replaceAll("the", "");
    term = term.replaceAll("The", "");
    term = term.replaceAll(", ", "");
    term = term.replaceAll("-", "");
    term = term.replaceAll("'", "");
    term = term.replaceAll("\\(", "");
    term = term.replaceAll("\\)", "");
    term = term.replaceAll("\\[", "");
    term = term.replaceAll("\\]", "");
    term = term.replaceAll("MOD", "");
    term = term.replaceAll("VOW", "");
    term = term.replaceAll("VR ", "");
    term = term.replaceAll("Room ", "");

    String[] terms = term.split(" ");

    List<String> sanitizedTerms = new ArrayList<>();
    for (String s : terms) {
      if (!StringUtils.isEmpty(s)) {
        String value = s.trim();
        try {
          if (value.length() == 4) {
            Integer.parseInt(value);
            continue;
          }
        } catch (NumberFormatException e) {
        }

        sanitizedTerms.add(s.trim());
      }

      if (sanitizedTerms.size() == 2) {
        break;
      }
    }

    if (sanitizedTerms.isEmpty()) {
      this.searchField.setText(game.getGameDisplayName());
    }
    else {
      this.searchField.setText(String.join(" ", sanitizedTerms));
    }

    refreshTableMediaView();
    onSearch();
  }


  private void refreshTableMediaView() {
    if(screen.equals(PopperScreen.Wheel)) {
      client.getImageCache().clearWheelCache();
    }

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
      refreshTableMediaView();
    });
  }
}

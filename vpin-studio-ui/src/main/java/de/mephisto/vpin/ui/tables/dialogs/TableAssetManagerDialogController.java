package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.media.AssetMediaPlayer;
import de.mephisto.vpin.commons.utils.media.AudioMediaPlayer;
import de.mephisto.vpin.commons.utils.media.VideoMediaPlayer;
import de.mephisto.vpin.connectors.assets.EncryptDecrypt;
import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.connectors.assets.TableAssetsService;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.GameMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.DownloadJobDescriptor;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.frontend.TableAssetSearch;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.JobFinishedEvent;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.jobs.JobPoller;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tables.TableOverviewController;
import de.mephisto.vpin.ui.tables.drophandler.TableMediaFileDropEventHandler;
import de.mephisto.vpin.ui.util.*;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.NoSuchPaddingException;
import java.awt.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.*;

import static de.mephisto.vpin.restclient.jobs.JobType.POPPER_MEDIA_INSTALL;
import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;


public class TableAssetManagerDialogController implements Initializable, DialogController, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(TableAssetManagerDialogController.class);
//  public static final int MEDIA_SIZE = 280;

  @FXML
  private BorderPane root;

  @FXML
  private ComboBox<GameRepresentation> tablesCombo;

  @FXML
  private BorderPane serverAssetMediaPane;

  @FXML
  private Button downloadBtn;

  @FXML
  private Button helpBtn;

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
  private Button downloadAssetBtn;

  @FXML
  private Button folderBtn;

  @FXML
  private Separator folderSeparator;

  @FXML
  private ListView<GameMediaItemRepresentation> assetList;

  @FXML
  private ListView<TableAsset> serverAssetsList;


  @FXML
  private BorderPane screenTopper;

  @FXML
  private BorderPane screenBackGlass;

  @FXML
  private BorderPane screenDMD;

  @FXML
  private BorderPane screenPlayField;

  @FXML
  private BorderPane screenMenu;

  @FXML
  private BorderPane screenOther2;

  @FXML
  private BorderPane screenWheel;

  @FXML
  private BorderPane screenGameInfo;

  @FXML
  private BorderPane screenGameHelp;

  @FXML
  private BorderPane screenLoading;

  @FXML
  private BorderPane screenAudio;

  @FXML
  private BorderPane screenAudioLaunch;

  @FXML
  private Button openDataManager;

  @FXML
  private Button addAudioBlank;

  @FXML
  private Label previewTitleLabel;

  private TableOverviewController overviewController;
  private GameRepresentation game;
  private VPinScreen screen = VPinScreen.Wheel;
  private TableAssetsService tableAssetsService;
  private EncryptDecrypt encryptDecrypt;
  private Node lastSelected;
  private GameMediaRepresentation gameMedia;


  @FXML
  private void onNext(ActionEvent e) {
    overviewController.selectNext();
    GameRepresentation selection = overviewController.getSelection();
    if (selection != null && !selection.equals(this.game)) {
      this.tablesCombo.setValue(selection);
    }
  }

  @FXML
  private void onPrevious(ActionEvent e) {
    overviewController.selectPrevious();
    GameRepresentation selection = overviewController.getSelection();
    if (selection != null && !selection.equals(this.game)) {
      this.tablesCombo.setValue(selection);
    }
  }

  @FXML
  private void onAudioBlank() {
    try {
      client.getGameMediaService().addBlank(game.getId(), screen);
      EventManager.getInstance().notifyTableChange(game.getId(), null, game.getGameName());
    }
    catch (Exception e) {
      WidgetFactory.showAlert(Studio.stage, "Error", "Adding blank media failed: " + e.getMessage());
    }
    refreshTableMediaView();
  }

  @FXML
  private void onDataManager(ActionEvent e) {
    this.onCancel(e);
    Platform.runLater(() -> {
      TableDialogs.openTableDataDialog(overviewController, this.game);
    });
  }

  @FXML
  private void onVPSAssets() {
    TableDialogs.openVPSAssetsDialog(game);
  }

  @FXML
  private void onFolderBtn() {
    if (this.gameMedia != null) {
      int emulatorId = this.game.getEmulatorId();
      GameEmulatorRepresentation gameEmulator = client.getFrontendService().getGameEmulator(emulatorId);
      String mediaDir = gameEmulator.getMediaDirectory();
      File screenDir = new File(mediaDir, screen.name());
      SystemUtil.openFolder(screenDir);
    }
  }

  @FXML
  private void onPlaylistAdd() {
    GameMediaItemRepresentation selectedItem = assetList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      try {
        client.getGameMediaService().toFullScreen(game.getId(), screen);
      }
      catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, "Error", "Fullscreen switch failed: " + e.getMessage());
      }
      refreshTableMediaView();
    }
  }

  @FXML
  private void onHelp() {
    String loadingHelp = "https://www.nailbuster.com/wikipinup/doku.php?id=loading_video";
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(new URI(loadingHelp));
      }
      catch (Exception e) {
        LOG.error("Failed to open help link: " + e.getMessage(), e);
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
    TableDialogs.directAssetUpload(stage, game, screen);
    refreshTableMediaView();
  }

  @FXML
  private void onAssetDownload(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    GameMediaItemRepresentation selectedItem = this.assetList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      StudioFolderChooser chooser = new StudioFolderChooser();
      chooser.setTitle("Select Download Folder");
      File targetFolder = chooser.showOpenDialog(stage);
      if (targetFolder != null && targetFolder.exists() && targetFolder.isDirectory()) {
        File asset = new File(targetFolder, selectedItem.getName());
        File uniqueTarget = FileUtils.uniqueFile(asset);

        Platform.runLater(() -> {
          DownloadJobDescriptor job = new DownloadJobDescriptor(selectedItem.getUri() + "/" + URLEncoder.encode(selectedItem.getName(), Charset.defaultCharset()), uniqueTarget);
          job.setTitle("Download of \"" + uniqueTarget.getName() + "\"");
          job.setDescription("Downloading targetFolder \"" + uniqueTarget.getName() + "\"");
          JobPoller.getInstance().queueJob(job);
        });
      }
    }
  }

  @FXML
  private void onDelete(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    GameMediaItemRepresentation selectedItem = assetList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Delete \"" + selectedItem.getName() + "\"?", "The selected media will be deleted.", null, "Delete");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        client.getGameMediaService().deleteMedia(game.getId(), screen, selectedItem.getName());

        Platform.runLater(() -> {
          EventManager.getInstance().notifyJobFinished(POPPER_MEDIA_INSTALL, this.game.getId());
          refreshTableMediaView();
        });
      }
    }
  }

  @FXML
  private void onHelpLink() {
    if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
      try {
        Desktop.getDesktop().browse(new URI("https://www.nailbuster.com/wikipinup/doku.php?id=loading_video"));
      }
      catch (Exception ex) {
        LOG.error("Failed to open link: " + ex.getMessage(), ex);
      }
    }
  }

  @FXML
  private void onRename() {
    GameMediaItemRepresentation selectedItem = this.assetList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      String name = selectedItem.getName();
      String baseName = FilenameUtils.getBaseName(name);
      String suffix = FilenameUtils.getExtension(name);
      String s = WidgetFactory.showInputDialog(Studio.stage, "Rename", "Renaming of Table Asset \"" + selectedItem.getName() + "\"", "Enter a new name:", null, baseName);
      if (!StringUtils.isEmpty(s) && FileUtils.isValidFilename(s)) {
        if (s.equalsIgnoreCase(baseName)) {
          return;
        }

        if (!s.endsWith(baseName)) {
          s = s + "." + suffix;
        }

        if (!s.startsWith(game.getGameName())) {
          WidgetFactory.showAlert(Studio.stage, "Error", "The asset name must start with \"" + game.getGameName() + "\".");
          onRename();
          return;
        }

        try {
          client.getGameMediaService().renameMedia(game.getId(), screen, selectedItem.getName(), s);
          EventManager.getInstance().notifyTableChange(game.getId(), null, game.getGameName());
          onReload();
        }
        catch (Exception e) {
          LOG.error("Renaming table asset failed: " + e.getMessage(), e);
          WidgetFactory.showAlert(Studio.stage, "Error", "Renaming failed: " + e.getMessage());
        }
      }
      else if (!StringUtils.isEmpty(s) && !FileUtils.isValidFilename(s)) {
        WidgetFactory.showAlert(Studio.stage, "Error", "Renaming cancelled, invalid character found.");
        onRename();
      }
    }
  }

  @FXML
  private void onSearch() {
    Platform.runLater(() -> {
      String term = searchField.getText().trim();
      if (!StringUtils.isEmpty(term)) {
        TableAssetSearch assetSearch = searchPopper(screen, term);
        ObservableList<TableAsset> assets = FXCollections.observableList(new ArrayList<>(assetSearch.getResult()));
        serverAssetsList.getItems().removeAll(serverAssetsList.getItems());
        serverAssetsList.setItems(assets);
        serverAssetsList.refresh();
        return;
      }

      serverAssetsList.getItems().removeAll(serverAssetsList.getItems());
      serverAssetsList.setItems(FXCollections.observableList(new ArrayList<>()));
      serverAssetsList.refresh();
    });
  }

  private TableAssetSearch searchPopper(VPinScreen screen, String term) {
    TableAssetSearch cached = client.getGameMediaService().getCached(screen, term);
    if (cached != null) {
      return cached;
    }

    ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(stage, new PopperAssetSearchProgressModel("Popper Asset Search", screen, term));
    List<Object> results = progressDialog.getResults();
    if (!results.isEmpty()) {
      return (TableAssetSearch) results.get(0);
    }

    TableAssetSearch empty = new TableAssetSearch();
    empty.setResult(Collections.emptyList());
    empty.setScreen(screen);
    return empty;
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
      if (mimeType == null) {
        LOG.info("No mimetype found for asset " + tableAsset);
        return;
      }

      String baseType = mimeType.split("/")[0];

      String assetUrl = null;
      try {
        assetUrl = this.encryptDecrypt.decrypt(tableAsset.getUrl());
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }

      try {
        if (baseType.equals("image")) {
          ImageView imageView = new ImageView();
          imageView.setFitWidth(getServerAssetPreviewWidth());
          imageView.setFitHeight(getServerAssetPreviewHeight());
          imageView.setPreserveRatio(true);

          Image image = new Image(assetUrl);
          imageView.setImage(image);
          imageView.setUserData(tableAsset);

          serverAssetMediaPane.setCenter(imageView);
        }
        else if (baseType.equals("audio")) {
          new AudioMediaPlayer(serverAssetMediaPane, assetUrl);
        }
        else if (baseType.equals("video")) {
          new VideoMediaPlayer(serverAssetMediaPane, assetUrl, tableAsset.getScreen(), mimeType);
        }
      }
      catch (Exception e) {
        LOG.error("Preview failed for " + tableAsset);
      }
    });
  }

  private double getServerAssetPreviewWidth() {
    return serverAssetMediaPane.getPrefWidth() - 10;
  }

  private double getServerAssetPreviewHeight() {
    return serverAssetMediaPane.getPrefHeight() - 10;
  }

  private double getLocalAssetPreviewWidth() {
    return mediaPane.getPrefWidth() - 10;
  }

  private double getLocalAssetPreviewHeight() {
    return mediaPane.getPrefHeight() - 10;
  }

  @FXML
  private void onDownload(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
    TableAsset tableAsset = this.serverAssetsList.getSelectionModel().getSelectedItem();
    boolean append = false;

    ObservableList<GameMediaItemRepresentation> items = assetList.getItems();
    String targetName = game.getGameName() + "." + FilenameUtils.getExtension(tableAsset.getName());
    boolean alreadyExists = items.stream().anyMatch(i -> i.getName().equalsIgnoreCase(targetName));
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
      ProgressDialog.createProgressDialog(stage, new TableAssetDownloadProgressModel(screen, game, tableAsset, append));
      refreshTableMediaView();
      EventManager.getInstance().notifyTableChange(game.getId(), null, game.getGameName());
    }
  }

  @FXML
  private void onCancel(ActionEvent e) {
    EventManager.getInstance().removeListener(this);
    EventManager.getInstance().notifyTableChange(game.getId(), null);
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.folderSeparator.managedProperty().bindBidirectional(this.folderSeparator.visibleProperty());
    this.folderBtn.managedProperty().bindBidirectional(this.folderBtn.visibleProperty());
    this.addToPlaylistBtn.managedProperty().bindBidirectional(this.addToPlaylistBtn.visibleProperty());
    this.addAudioBlank.managedProperty().bindBidirectional(this.addAudioBlank.visibleProperty());

    this.folderBtn.setVisible(SystemUtil.isFolderActionSupported());
    this.folderSeparator.setVisible(SystemUtil.isFolderActionSupported());

    try {
      encryptDecrypt = new EncryptDecrypt(EncryptDecrypt.KEY);
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    catch (NoSuchPaddingException e) {
      throw new RuntimeException(e);
    }
    catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }

    screenAudio.hoverProperty().addListener((observableValue, aBoolean, t1) -> updateState(VPinScreen.Audio, screenAudio, t1, false));
    screenAudio.setOnMouseClicked(mouseEvent -> updateState(VPinScreen.Audio, screenAudio, true, true));
    screenAudioLaunch.hoverProperty().addListener((observableValue, aBoolean, t1) -> updateState(VPinScreen.AudioLaunch, screenAudioLaunch, t1, false));
    screenAudioLaunch.setOnMouseClicked(mouseEvent -> updateState(VPinScreen.AudioLaunch, screenAudioLaunch, true, true));
    screenDMD.hoverProperty().addListener((observableValue, aBoolean, t1) -> updateState(VPinScreen.DMD, screenDMD, t1, false));
    screenDMD.setOnMouseClicked(mouseEvent -> updateState(VPinScreen.DMD, screenDMD, true, true));
    screenBackGlass.hoverProperty().addListener((observableValue, aBoolean, t1) -> updateState(VPinScreen.BackGlass, screenBackGlass, t1, false));
    screenBackGlass.setOnMouseClicked(mouseEvent -> updateState(VPinScreen.BackGlass, screenBackGlass, true, true));
    screenMenu.hoverProperty().addListener((observableValue, aBoolean, t1) -> updateState(VPinScreen.Menu, screenMenu, t1, false));
    screenMenu.setOnMouseClicked(mouseEvent -> updateState(VPinScreen.Menu, screenMenu, true, true));
    screenGameInfo.hoverProperty().addListener((observableValue, aBoolean, t1) -> updateState(VPinScreen.GameInfo, screenGameInfo, t1, false));
    screenGameInfo.setOnMouseClicked(mouseEvent -> updateState(VPinScreen.GameInfo, screenGameInfo, true, true));
    screenGameHelp.hoverProperty().addListener((observableValue, aBoolean, t1) -> updateState(VPinScreen.GameHelp, screenGameHelp, t1, false));
    screenGameHelp.setOnMouseClicked(mouseEvent -> updateState(VPinScreen.GameHelp, screenGameHelp, true, true));
    screenLoading.hoverProperty().addListener((observableValue, aBoolean, t1) -> updateState(VPinScreen.Loading, screenLoading, t1, false));
    screenLoading.setOnMouseClicked(mouseEvent -> updateState(VPinScreen.Loading, screenLoading, true, true));
    screenBackGlass.hoverProperty().addListener((observableValue, aBoolean, t1) -> updateState(VPinScreen.BackGlass, screenBackGlass, t1, false));
    screenBackGlass.setOnMouseClicked(mouseEvent -> updateState(VPinScreen.BackGlass, screenBackGlass, true, true));
    screenPlayField.hoverProperty().addListener((observableValue, aBoolean, t1) -> updateState(VPinScreen.PlayField, screenPlayField, t1, false));
    screenPlayField.setOnMouseClicked(mouseEvent -> updateState(VPinScreen.PlayField, screenPlayField, true, true));
    screenTopper.hoverProperty().addListener((observableValue, aBoolean, t1) -> updateState(VPinScreen.Topper, screenTopper, t1, false));
    screenTopper.setOnMouseClicked(mouseEvent -> updateState(VPinScreen.Topper, screenTopper, true, true));
    screenOther2.hoverProperty().addListener((observableValue, aBoolean, t1) -> updateState(VPinScreen.Other2, screenOther2, t1, false));
    screenOther2.setOnMouseClicked(mouseEvent -> updateState(VPinScreen.Other2, screenOther2, true, true));
    screenWheel.hoverProperty().addListener((observableValue, aBoolean, t1) -> updateState(VPinScreen.Wheel, screenWheel, t1, false));
    screenWheel.setOnMouseClicked(mouseEvent -> updateState(VPinScreen.Wheel, screenWheel, true, true));

    downloadBtn.setVisible(false);

    this.deleteBtn.setDisable(true);
    this.renameBtn.setDisable(true);
    this.downloadAssetBtn.setDisable(true);
    this.addAudioBlank.setVisible(false);
    this.addToPlaylistBtn.setVisible(false);

    searchField.setOnKeyPressed(ke -> {
      if (ke.getCode().equals(KeyCode.ENTER)) {
        onSearch();
      }
    });

    serverAssetsList.setPlaceholder(new Label("No Popper assets found for this screen and table."));
    assetList.setPlaceholder(new Label("No assets found for this screen and table."));

    if (!isEmbeddedMode()) {
      EventManager.getInstance().addListener(this);
    }

    Label label = new Label("No asset preview activated.");
    label.setStyle("-fx-font-size: 14px;-fx-text-fill: #444444;");
    serverAssetMediaPane.setCenter(label);

    tableAssetsService = new TableAssetsService();

    try {
      Class<?> aClass = Class.forName("de.mephisto.vpin.popper.PopperAssetAdapter");
      TableAssetsAdapter adapter = (TableAssetsAdapter) aClass.getDeclaredConstructor().newInstance();
      tableAssetsService.registerAdapter(adapter);
    }
    catch (Exception e) {
      LOG.error("Unable to find PopperAssetAdapter: " + e.getMessage());
    }

    this.serverAssetsList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TableAsset>() {
      @Override
      public void changed(ObservableValue<? extends TableAsset> observable, TableAsset oldValue, TableAsset tableAsset) {
        disposeServerAssetPreview();
        downloadBtn.setVisible(false);

        Label label = new Label("No asset preview activated.");
        label.setStyle("-fx-font-size: 14px;-fx-text-fill: #444444;");
        serverAssetMediaPane.setCenter(label);
      }
    });

    this.serverAssetsList.setOnMouseClicked(click -> {
      onPreview();
    });


    this.assetList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<GameMediaItemRepresentation>() {
      @Override
      public void changed(ObservableValue<? extends GameMediaItemRepresentation> observable, GameMediaItemRepresentation oldValue, GameMediaItemRepresentation mediaItem) {
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        if (screen.equals(VPinScreen.Wheel)) {
          client.getImageCache().clearWheelCache();
        }

        disposeTableMediaPreview();

        deleteBtn.setDisable(mediaItem == null);
        renameBtn.setDisable(mediaItem == null);
        downloadAssetBtn.setDisable(mediaItem == null);

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
          imageView.setFitWidth(getLocalAssetPreviewHeight());
          imageView.setFitHeight(getLocalAssetPreviewWidth());
          imageView.setPreserveRatio(true);

          Image image = new Image(url);
          imageView.setImage(image);
          imageView.setUserData(mediaItem);

          mediaPane.setCenter(imageView);
        }
        else if (baseType.equals("audio")) {
          new AudioMediaPlayer(mediaPane, mediaItem, url);
        }
        else if (baseType.equals("video")) {
          new VideoMediaPlayer(mediaPane, mediaItem, url, mimeType, true);
        }
      }
    });

    if (isEmbeddedMode()) {
      assetList.prefHeightProperty().bind(root.prefHeightProperty());
    }
  }

  private boolean isEmbeddedMode() {
    return this.tablesCombo == null;
  }

  private void updateState(VPinScreen s, BorderPane borderPane, Boolean hovered, Boolean clicked) {
    List<GameMediaItemRepresentation> mediaItems = new ArrayList<>();
    if (gameMedia != null) {
      mediaItems = gameMedia.getMediaItems(s);
    }

    if (mediaItems.isEmpty()) {
      borderPane.getStyleClass().removeAll("green");
    }
    else {
      borderPane.getStyleClass().add("green");
    }

    if (clicked) {
      if (this.lastSelected != null) {
        this.lastSelected.setStyle(null);
      }

      if (this.screen.equals(s)) {
        borderPane.setStyle("-fx-cursor: hand;-fx-background-color: #6666FF");
        this.lastSelected = borderPane;
        return;
      }
      borderPane.setStyle("-fx-cursor: hand;-fx-background-color: #6666FF");
      this.lastSelected = borderPane;

      this.screen = s;
      refreshTableMediaView();
      onSearch();
      return;
    }

    if (!borderPane.equals(lastSelected)) {
      if (hovered) {
        borderPane.setStyle("-fx-cursor: hand;-fx-background-color: #6666FF");
      }
      else {
        borderPane.setStyle(null);
      }
    }
  }

  private void disposeServerAssetPreview() {
    Node center = serverAssetMediaPane.getCenter();
    if (center instanceof AssetMediaPlayer) {
      ((AssetMediaPlayer) center).disposeMedia();
    }
    serverAssetMediaPane.setCenter(null);
  }


  private void disposeTableMediaPreview() {
    Node center = mediaPane.getCenter();
    if (center instanceof AssetMediaPlayer) {
      ((AssetMediaPlayer) center).disposeMedia();
    }
    mediaPane.setCenter(null);
  }

  @Override
  public void onDialogCancel() {
    EventManager.getInstance().removeListener(this);
    EventManager.getInstance().notifyTableChange(this.game.getId(), null, this.game.getGameName());
  }


  public void setGame(TableOverviewController overviewController, GameRepresentation game, VPinScreen screen) {
    this.overviewController = overviewController;
    this.game = game;
    this.searchField.setText("");
    if (screen != null) {
      this.screen = screen;
    }

    if (!isEmbeddedMode()) {
      this.tablesCombo.setValue(game);
      this.helpBtn.setDisable(!VPinScreen.Loading.equals(screen));
      List<GameRepresentation> games = client.getGameService().getGamesCached(this.game.getEmulatorId());
      ObservableList<GameRepresentation> gameRepresentations = FXCollections.observableArrayList(games);
      tablesCombo.getItems().addAll(gameRepresentations);
      tablesCombo.valueProperty().addListener((observableValue, gameRepresentation, t1) -> {
        this.setGame(this.overviewController, t1, this.screen != null ? this.screen : VPinScreen.Wheel);
      });
    }

    if (game == null) {
      searchField.setText("");
    }
    else {
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
          }
          catch (NumberFormatException e) {
          }

          sanitizedTerms.add(s.trim());
        }

        if (sanitizedTerms.size() == 2) {
          break;
        }
      }

      if (StringUtils.isEmpty(this.searchField.getText())) {
        if (sanitizedTerms.isEmpty()) {
          this.searchField.setText(game.getGameDisplayName());
        }
        else {
          this.searchField.setText(String.join(" ", sanitizedTerms));
        }
      }
    }


    refreshTableMediaView();
    onSearch();


    screenAudio.setOnDragOver(new FileDragEventHandler(screenAudio, false, "mp3"));
    screenAudio.setOnDragDropped(new TableMediaFileDropEventHandler(this, VPinScreen.Audio, "mp3"));

    screenAudioLaunch.setOnDragOver(new FileDragEventHandler(screenAudioLaunch, false, "mp3"));
    screenAudioLaunch.setOnDragDropped(new TableMediaFileDropEventHandler(this, VPinScreen.AudioLaunch, "mp3"));

    screenTopper.setOnDragOver(new FileDragEventHandler(screenTopper, false, "mp4", "png", "jpg"));
    screenTopper.setOnDragDropped(new TableMediaFileDropEventHandler(this, VPinScreen.Topper, "mp4", "png", "jpg"));

    screenLoading.setOnDragOver(new FileDragEventHandler(screenLoading, false, "mp4"));
    screenLoading.setOnDragDropped(new TableMediaFileDropEventHandler(this, VPinScreen.Loading, "mp4"));

    screenPlayField.setOnDragOver(new FileDragEventHandler(screenPlayField, false, "mp4"));
    screenPlayField.setOnDragDropped(new TableMediaFileDropEventHandler(this, VPinScreen.PlayField, "mp4"));

    screenBackGlass.setOnDragOver(new FileDragEventHandler(screenBackGlass, false, "mp4", "png", "jpg"));
    screenBackGlass.setOnDragDropped(new TableMediaFileDropEventHandler(this, VPinScreen.BackGlass, "mp4", "png", "jpg"));

    screenGameInfo.setOnDragOver(new FileDragEventHandler(screenGameInfo, false, "mp4", "png", "jpg"));
    screenGameInfo.setOnDragDropped(new TableMediaFileDropEventHandler(this, VPinScreen.GameInfo, "mp4", "png", "jpg"));

    screenGameHelp.setOnDragOver(new FileDragEventHandler(screenGameHelp, false, "mp4", "png", "jpg"));
    screenGameHelp.setOnDragDropped(new TableMediaFileDropEventHandler(this, VPinScreen.GameHelp, "mp4", "png", "jpg"));

    screenMenu.setOnDragOver(new FileDragEventHandler(screenMenu, false, "mp4", "png", "jpg"));
    screenMenu.setOnDragDropped(new TableMediaFileDropEventHandler(this, VPinScreen.Menu, "mp4", "png", "jpg"));

    screenDMD.setOnDragOver(new FileDragEventHandler(screenDMD, false, "mp4", "png", "jpg"));
    screenDMD.setOnDragDropped(new TableMediaFileDropEventHandler(this, VPinScreen.DMD, "mp4", "png", "jpg"));

    screenOther2.setOnDragOver(new FileDragEventHandler(screenOther2, false, "mp4", "png", "jpg"));
    screenOther2.setOnDragDropped(new TableMediaFileDropEventHandler(this, VPinScreen.Other2, "mp4", "png", "jpg"));

    screenWheel.setOnDragOver(new FileDragEventHandler(screenWheel, false, "apng", "png", "jpg"));
    screenWheel.setOnDragDropped(new TableMediaFileDropEventHandler(this, VPinScreen.Wheel, "apng", "png", "apng"));

  }

  private void refreshTableView() {
    updateState(VPinScreen.Audio, screenAudio, false, this.screen.equals(VPinScreen.Audio));
    updateState(VPinScreen.AudioLaunch, screenAudioLaunch, false, this.screen.equals(VPinScreen.AudioLaunch));
    updateState(VPinScreen.DMD, screenDMD, false, this.screen.equals(VPinScreen.DMD));
    updateState(VPinScreen.BackGlass, screenBackGlass, false, this.screen.equals(VPinScreen.BackGlass));
    updateState(VPinScreen.Menu, screenMenu, false, this.screen.equals(VPinScreen.Menu));
    updateState(VPinScreen.GameInfo, screenGameInfo, false, this.screen.equals(VPinScreen.GameInfo));
    updateState(VPinScreen.GameHelp, screenGameHelp, false, this.screen.equals(VPinScreen.GameHelp));
    updateState(VPinScreen.Loading, screenLoading, false, this.screen.equals(VPinScreen.Loading));
    updateState(VPinScreen.BackGlass, screenBackGlass, false, this.screen.equals(VPinScreen.BackGlass));
    updateState(VPinScreen.PlayField, screenPlayField, false, this.screen.equals(VPinScreen.PlayField));
    updateState(VPinScreen.Topper, screenTopper, false, this.screen.equals(VPinScreen.Topper));
    updateState(VPinScreen.Other2, screenOther2, false, this.screen.equals(VPinScreen.Other2));
    updateState(VPinScreen.Wheel, screenWheel, false, this.screen.equals(VPinScreen.Wheel));
  }


  public void refreshTableMediaView() {
    if (!isEmbeddedMode()) {
      this.helpBtn.setDisable(!VPinScreen.Loading.equals(screen));
    }
    if (screen.equals(VPinScreen.Wheel)) {
      client.getImageCache().clearWheelCache();
    }

    this.addToPlaylistBtn.setVisible(screen.equals(VPinScreen.Loading));
    this.addAudioBlank.setVisible(screen.equals(VPinScreen.AudioLaunch));

    if (game != null) {
      gameMedia = client.getGameMediaService().getGameMedia(this.game.getId());
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
    }
    else {
      assetList.setItems(FXCollections.emptyObservableList());
      assetList.refresh();
      addToPlaylistBtn.setDisable(true);

      serverAssetsList.setItems(FXCollections.emptyObservableList());
      serverAssetsList.refresh();
    }

    refreshTableView();
    if (previewTitleLabel != null) {
      Node node = this.lastSelected;
      if (node == null) {
        node = screenWheel;
      }
      Label nameLabel = (Label) ((BorderPane) node).getBottom();
      previewTitleLabel.setText(nameLabel.getText());
    }
  }

  @Override
  public void jobFinished(@NonNull JobFinishedEvent event) {
    Platform.runLater(() -> {
      refreshTableMediaView();
    });
  }

  public GameRepresentation getGame() {
    return game;
  }
}

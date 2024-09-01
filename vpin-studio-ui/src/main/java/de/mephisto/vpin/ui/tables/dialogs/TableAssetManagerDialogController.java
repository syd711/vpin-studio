package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.media.AudioMediaPlayer;
import de.mephisto.vpin.commons.utils.media.ImageViewer;
import de.mephisto.vpin.commons.utils.media.VideoMediaPlayer;
import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.frontend.TableAssetSearch;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.PlaylistRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.DownloadJobDescriptor;
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
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;


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
  private Button webPreviewBtn;

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
  private Button nextButton;

  @FXML
  private Button prevButton;

  @FXML
  private Button renameBtn;

  @FXML
  private Button downloadAssetBtn;

  @FXML
  private Button folderBtn;

  @FXML
  private Button clearCacheBtn;

  @FXML
  private Separator folderSeparator;

  @FXML
  private ImageView frontendImage;

  @FXML
  private ListView<FrontendMediaItemRepresentation> assetList;

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
  private Node assetsBox;

  @FXML
  private Node assetSearchBox;

  @FXML
  private Label assetSearchLabel;

  @FXML
  private BorderPane assetSearchList;

  @FXML
  private Label previewTitleLabel;

  @FXML
  private RadioButton playlistsRadio;

  @FXML
  private RadioButton tablesRadio;

  @FXML
  private Pane tableSelection;

  @FXML
  private Pane playlistSelection;

  @FXML
  private ComboBox<PlaylistRepresentation> playlistCombo;

  private Stage localStage;
  private TableOverviewController overviewController;
  private GameRepresentation game;
  private PlaylistRepresentation playlist;
  private VPinScreen screen = VPinScreen.Wheel;
  private FrontendMediaRepresentation frontendMedia;
  private Node lastSelected;


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
  private void onWebPreview() {
    TableAsset tableAsset = serverAssetsList.getSelectionModel().getSelectedItem();
    String mimeType = tableAsset.getMimeType();
    if (mimeType == null) {
      LOG.info("No mimetype found for asset " + tableAsset);
      return;
    }

    String baseType = mimeType.split("/")[0];
    String assetUrl = client.getGameMediaService().getUrl(tableAsset, this.game.getId());
    Studio.browse(assetUrl);
  }

  @FXML
  private void onClearCache() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(localStage, "Rebuild Index", "The rebuilding of the index can take a few minutes.", "Please wait until the indexing is finished.", "Build Index");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      ProgressDialog.createProgressDialog(new MediaCacheProgressModel());
    }
  }

  @FXML
  private void onAudioBlank() {
    try {
      client.getGameMediaService().addBlank(game.getId(), screen);
      EventManager.getInstance().notifyTableChange(game.getId(), null, game.getGameName());
    }
    catch (Exception e) {
      WidgetFactory.showAlert(localStage, "Error", "Adding blank media failed: " + e.getMessage());
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
    if (this.playlist != null && this.playlistsRadio != null && this.playlistsRadio.isSelected()) {
      File screenDir = client.getFrontendService().getPlaylistMediaDirectory(this.playlist.getId(), screen.name());
      SystemUtil.openFolder(screenDir);
    }
    else if (this.game != null) {
      File screenDir = client.getFrontendService().getMediaDirectory(this.game.getId(), screen.name());
      SystemUtil.openFolder(screenDir);
    }
  }

  @FXML
  private void onPlaylistAdd() {
    FrontendMediaItemRepresentation selectedItem = assetList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      try {
        client.getGameMediaService().toFullScreen(game.getId(), screen);
      }
      catch (Exception e) {
        WidgetFactory.showAlert(localStage, "Error", "Fullscreen switch failed: " + e.getMessage());
      }
      refreshTableMediaView();
    }
  }

  @FXML
  private void onHelp() {
    String loadingHelp = "https://www.nailbuster.com/wikipinup/doku.php?id=loading_video";
    Studio.browse(loadingHelp);
  }

  @FXML
  private void onReload() {
    refreshTableMediaView();
  }

  @FXML
  private void onMediaUpload(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    if (isPlaylistMode()) {
      TableDialogs.directAssetUpload(stage, playlist, screen);
    }
    else {
      TableDialogs.directAssetUpload(stage, game, screen);
    }
    refreshTableMediaView();
  }

  @FXML
  private void onAssetDownload(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    FrontendMediaItemRepresentation selectedItem = this.assetList.getSelectionModel().getSelectedItem();
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
    FrontendMediaItemRepresentation selectedItem = assetList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Delete \"" + selectedItem.getName() + "\"?", "The selected media will be deleted.", null, "Delete");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {

        if (isPlaylistMode()) {
          client.getPlaylistMediaService().deleteMedia(playlist.getId(), screen, selectedItem.getName());
        }
        else {
          client.getGameMediaService().deleteMedia(game.getId(), screen, selectedItem.getName());
        }

        Platform.runLater(() -> {
          if (game != null) {
            EventManager.getInstance().notifyTableChange(game.getId(), null);
          }
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
    FrontendMediaItemRepresentation selectedItem = this.assetList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      String name = selectedItem.getName();
      String baseName = FilenameUtils.getBaseName(name);
      String suffix = FilenameUtils.getExtension(name);
      String s = WidgetFactory.showInputDialog(localStage, "Rename", "Renaming of Table Asset \"" + selectedItem.getName() + "\"", "Enter a new name:", null, baseName);
      if (!StringUtils.isEmpty(s) && FileUtils.isValidFilename(s)) {
        if (s.equalsIgnoreCase(baseName)) {
          return;
        }

        if (!s.endsWith(baseName)) {
          s = s + "." + suffix;
        }

        if (!s.startsWith(game.getGameName())) {
          WidgetFactory.showAlert(localStage, "Error", "The asset name must start with \"" + game.getGameName() + "\".");
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
          WidgetFactory.showAlert(localStage, "Error", "Renaming failed: " + e.getMessage());
        }
      }
      else if (!StringUtils.isEmpty(s) && !FileUtils.isValidFilename(s)) {
        WidgetFactory.showAlert(localStage, "Error", "Renaming cancelled, invalid character found.");
        onRename();
      }
    }
  }

  @FXML
  private void onSearch() {
    Platform.runLater(() -> {
      String term = searchField.getText().trim();
      if (!StringUtils.isEmpty(term)) {
        TableAssetSearch assetSearch = searchMedia(screen, term);
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

  private TableAssetSearch searchMedia(VPinScreen screen, String term) {
    ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(localStage,
        new TableAssetSearchProgressModel("Media Asset Search", game.getId(), screen, term));
    List<Object> results = progressDialog.getResults();
    if (!results.isEmpty()) {
      return (TableAssetSearch) results.get(0);
    }

    TableAssetSearch empty = new TableAssetSearch();
    empty.setResult(Collections.emptyList());
    empty.setGameId(game.getId());
    empty.setTerm(term);
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
    webPreviewBtn.setVisible(true);
    serverAssetMediaPane.setCenter(new ProgressIndicator());

    Platform.runLater(() -> {
      String mimeType = tableAsset.getMimeType();
      if (mimeType == null) {
        LOG.info("No mimetype found for asset " + tableAsset);
        return;
      }

      String baseType = mimeType.split("/")[0];
      String assetUrl = client.getGameMediaService().getUrl(tableAsset, this.game.getId());
      LOG.info("Loading asset: " + assetUrl);

      try {
        Frontend frontend = client.getFrontendService().getFrontendCached();

        if (baseType.equals("image")) {
          new ImageViewer(serverAssetMediaPane, assetUrl, tableAsset, tableAsset.getScreen(), frontend.isPlayfieldMediaInverted());
        }
        else if (baseType.equals("audio")) {
          AudioMediaPlayer audioMediaPlayer = new AudioMediaPlayer(serverAssetMediaPane, assetUrl);
          audioMediaPlayer.render();
        }
        else if (baseType.equals("video")) {
          VideoMediaPlayer videoMediaPlayer = new VideoMediaPlayer(serverAssetMediaPane, assetUrl, tableAsset.getScreen(), mimeType, frontend.isPlayfieldMediaInverted());
          videoMediaPlayer.render();
        }
      }
      catch (Exception e) {
        LOG.error("Preview failed for " + tableAsset, e);
      }
    });
  }

  @FXML
  private void onDownload(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
    TableAsset tableAsset = this.serverAssetsList.getSelectionModel().getSelectedItem();
    boolean append = false;

    ObservableList<FrontendMediaItemRepresentation> items = assetList.getItems();
    String targetName = game.getGameName() + "." + FilenameUtils.getExtension(tableAsset.getName());
    boolean alreadyExists = items.stream().anyMatch(i -> i.getName().equalsIgnoreCase(targetName));
    if (alreadyExists) {
      Optional<ButtonType> buttonType = WidgetFactory.showConfirmationWithOption(localStage, "Asset Exists",
          "An asset with the same name already exists.",
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
      ProgressDialog.createProgressDialog(stage, new TableAssetDownloadProgressModel(stage, screen, game, tableAsset, append));
      refreshTableMediaView();
      EventManager.getInstance().notifyTableChange(game.getId(), null, game.getGameName());
    }
  }

  @FXML
  private void onCancel(ActionEvent e) {
    EventManager.getInstance().removeListener(this);
    if (this.game != null) {
      EventManager.getInstance().notifyTableChange(game.getId(), null);
    }

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    assetSearchBox.managedProperty().bindBidirectional(assetSearchBox.visibleProperty());
    renameBtn.managedProperty().bindBidirectional(renameBtn.visibleProperty());

    Frontend frontend = client.getFrontendService().getFrontendCached();
    FrontendType frontendType = frontend.getFrontendType();

    if (!isEmbeddedMode()) {
      helpBtn.managedProperty().bindBidirectional(helpBtn.visibleProperty());
      playlistSelection.managedProperty().bindBidirectional(playlistSelection.visibleProperty());
      tableSelection.managedProperty().bindBidirectional(tableSelection.visibleProperty());
      playlistSelection.setVisible(false);

      ToggleGroup toggleGroup = new ToggleGroup();
      playlistsRadio.setToggleGroup(toggleGroup);
      tablesRadio.setToggleGroup(toggleGroup);
      tablesRadio.setSelected(true);

      playlistsRadio.setDisable(!frontendType.supportPlaylists());
      if (frontendType.supportPlaylists()) {
        List<PlaylistRepresentation> playlists = client.getPlaylistsService().getPlaylists();
        playlistCombo.setItems(FXCollections.observableList(playlists));
        if (!playlists.isEmpty()) {
          playlistCombo.getSelectionModel().select(0);
        }
      }

      tablesRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          if (game != null) {
            setGame(localStage, overviewController, game, screen);
          }
          else {
            setGame(localStage, overviewController, tablesCombo.getValue(), screen);
          }
        }
      });
      playlistsRadio.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          if (playlist != null) {
            setPlaylist(localStage, overviewController, playlist, screen);
          }
          else {
            setPlaylist(localStage, overviewController, playlistCombo.getValue(), screen);
          }
        }
      });
      playlistCombo.valueProperty().addListener(new ChangeListener<PlaylistRepresentation>() {
        @Override
        public void changed(ObservableValue<? extends PlaylistRepresentation> observable, PlaylistRepresentation oldValue, PlaylistRepresentation newValue) {
          if (newValue != null) {
            setPlaylist(localStage, overviewController, newValue, screen);
          }
        }
      });
    }

    List<VPinScreen> supportedScreens = frontend.getSupportedScreens();
    assetSearchBox.setVisible(frontend.isAssetSearchEnabled());

    this.folderSeparator.managedProperty().bindBidirectional(this.folderSeparator.visibleProperty());
    this.folderBtn.managedProperty().bindBidirectional(this.folderBtn.visibleProperty());
    this.addToPlaylistBtn.managedProperty().bindBidirectional(this.addToPlaylistBtn.visibleProperty());
    this.addAudioBlank.managedProperty().bindBidirectional(this.addAudioBlank.visibleProperty());

    this.folderBtn.setVisible(SystemUtil.isFolderActionSupported());
    this.folderSeparator.setVisible(SystemUtil.isFolderActionSupported());

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

    screenAudio.setVisible(supportedScreens.contains(VPinScreen.Audio));
    screenAudioLaunch.setVisible(supportedScreens.contains(VPinScreen.AudioLaunch));
    screenDMD.setVisible(supportedScreens.contains(VPinScreen.DMD));
    screenBackGlass.setVisible(supportedScreens.contains(VPinScreen.BackGlass));
    screenMenu.setVisible(supportedScreens.contains(VPinScreen.Menu));
    screenGameInfo.setVisible(supportedScreens.contains(VPinScreen.GameInfo));
    screenGameHelp.setVisible(supportedScreens.contains(VPinScreen.GameHelp));
    screenLoading.setVisible(supportedScreens.contains(VPinScreen.Loading));
    screenBackGlass.setVisible(supportedScreens.contains(VPinScreen.BackGlass));
    screenPlayField.setVisible(supportedScreens.contains(VPinScreen.PlayField));
    screenTopper.setVisible(supportedScreens.contains(VPinScreen.Topper));
    screenOther2.setVisible(supportedScreens.contains(VPinScreen.Other2));
    screenWheel.setVisible(supportedScreens.contains(VPinScreen.Wheel));

    downloadBtn.setVisible(false);
    webPreviewBtn.setVisible(false);

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

    serverAssetsList.setPlaceholder(new Label("No assets found for this screen and table."));
    assetList.setPlaceholder(new Label("No assets found for this screen and table."));

    if (!isEmbeddedMode()) {
      EventManager.getInstance().addListener(this);
    }

    Label label = new Label("No asset preview activated.");
    label.setStyle("-fx-font-size: 14px;-fx-text-fill: #444444;");
    serverAssetMediaPane.setCenter(label);

    this.serverAssetsList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TableAsset>() {
      @Override
      public void changed(ObservableValue<? extends TableAsset> observable, TableAsset oldValue, TableAsset tableAsset) {
        WidgetFactory.disposeMediaPane(serverAssetMediaPane);
        downloadBtn.setVisible(false);
        webPreviewBtn.setVisible(false);

        Label label = new Label("No asset preview activated.");
        label.setStyle("-fx-font-size: 14px;-fx-text-fill: #444444;");
        serverAssetMediaPane.setCenter(label);

        onPreview();
      }
    });

    this.assetList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<FrontendMediaItemRepresentation>() {
      @Override
      public void changed(ObservableValue<? extends FrontendMediaItemRepresentation> observable, FrontendMediaItemRepresentation oldValue, FrontendMediaItemRepresentation mediaItem) {
        if (screen.equals(VPinScreen.Wheel)) {
          client.getImageCache().clearWheelCache();
        }

        WidgetFactory.disposeMediaPane(mediaPane);

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
          new ImageViewer(mediaPane, url, mediaItem, mediaItem.getScreen(), frontend.isPlayfieldMediaInverted());
        }
        else if (baseType.equals("audio")) {
          AudioMediaPlayer audioMediaPlayer = new AudioMediaPlayer(mediaPane, mediaItem, url);
          audioMediaPlayer.render();
        }
        else if (baseType.equals("video")) {
          VideoMediaPlayer videoMediaPlayer = new VideoMediaPlayer(mediaPane, mediaItem, url, mimeType, frontend.isPlayfieldMediaInverted(), true);
          videoMediaPlayer.render();
        }
      }
    });

    if (isEmbeddedMode()) {
      assetList.prefHeightProperty().bind(root.prefHeightProperty());
    }
    else {
      clearCacheBtn.setVisible(frontend.getFrontendType().isSupportMediaCache());

      if (frontend.getFrontendType().equals(FrontendType.Popper)) {
        frontendImage.setImage(new Image(Studio.class.getResourceAsStream("pinup-system.png")));
      }
      if (frontend.getFrontendType().equals(FrontendType.PinballX)) {
        frontendImage.setImage(new Image(Studio.class.getResourceAsStream("gameex.png")));
      }
    }
  }

  private boolean isEmbeddedMode() {
    return this.tablesCombo == null;
  }

  private void updateState(VPinScreen s, BorderPane borderPane, Boolean hovered, Boolean clicked) {
    List<FrontendMediaItemRepresentation> mediaItems = new ArrayList<>();
    if (frontendMedia != null) {
      mediaItems = frontendMedia.getMediaItems(s);
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

  public boolean isPlaylistMode() {
    return playlistsRadio != null && playlistsRadio.isSelected() && this.playlist != null;
  }

  @Override
  public void onDialogCancel() {
    EventManager.getInstance().removeListener(this);
    if ((this.game != null)) {
      EventManager.getInstance().notifyTableChange(this.game.getId(), null, this.game.getGameName());
    }
  }

  public void loadAllTables(int emulatorId) {
    if (!isEmbeddedMode()) {
      new Thread(() -> {
        // as get of games may takes some time, run in a dedicated Thread
        List<GameRepresentation> games = client.getGameService().getGamesByEmulator(emulatorId);
        ObservableList<GameRepresentation> gameRepresentations = FXCollections.observableArrayList(games);
        tablesCombo.getItems().addAll(gameRepresentations);
        tablesCombo.valueProperty().addListener((observableValue, gameRepresentation, t1) -> {
          this.setGame(localStage, this.overviewController, t1, this.screen != null ? this.screen : VPinScreen.Wheel);
        });
      }).start();
    }
  }

  public void setPlaylist(Stage stage, @NonNull TableOverviewController overviewController, @NonNull PlaylistRepresentation playlist, @Nullable VPinScreen screen) {
    localStage = stage;
    this.overviewController = overviewController;
    this.playlist = playlist;
    this.searchField.setText("");
    if (screen != null) {
      this.screen = screen;
    }

    this.renameBtn.setVisible(false);

    if (!isEmbeddedMode()) {
      nextButton.setDisable(true);
      prevButton.setDisable(true);
      openDataManager.setVisible(false);
      tableSelection.setVisible(false);
      playlistSelection.setVisible(true);

      this.playlistCombo.setValue(playlist);
      this.helpBtn.setVisible(false);
    }

    refreshTableMediaView();
    onSearch();
    initDragAndDrop();
  }

  public void setGame(Stage stage, @NonNull TableOverviewController overviewController, @NonNull GameRepresentation game, @Nullable VPinScreen screen) {
    localStage = stage;
    this.overviewController = overviewController;

    // detection of change in game  
    if (this.game != null && (game == null || this.game.getId() != game.getId())) {
      searchField.setText("");
    }

    this.game = game;
    if (screen != null) {
      this.screen = screen;
    }

    this.renameBtn.setVisible(true);

    if (!isEmbeddedMode()) {
      nextButton.setDisable(false);
      prevButton.setDisable(false);
      openDataManager.setVisible(true);
      tableSelection.setVisible(true);
      playlistSelection.setVisible(false);

      this.tablesCombo.setValue(game);
      this.helpBtn.setDisable(!VPinScreen.Loading.equals(screen));
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
    initDragAndDrop();
  }

  private void initDragAndDrop() {
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
    if (this.game == null && this.playlist == null) {
      return;
    }

    if (isPlaylistMode()) {
      this.frontendMedia = client.getPlaylistMediaService().getPlaylistMedia(this.playlist.getId());
    }
    else {
      if (this.game == null) {
        this.game = tablesCombo.getItems().get(0);
      }
      this.frontendMedia = client.getGameMediaService().getGameMedia(this.game.getId());
    }

    if (!isEmbeddedMode()) {
      this.helpBtn.setDisable(!VPinScreen.Loading.equals(screen));
    }
    if (screen.equals(VPinScreen.Wheel)) {
      client.getImageCache().clearWheelCache();
    }

    this.addToPlaylistBtn.setVisible(screen.equals(VPinScreen.Loading));
    this.addAudioBlank.setVisible(screen.equals(VPinScreen.AudioLaunch));


    if (frontendMedia != null) {
      List<FrontendMediaItemRepresentation> items = frontendMedia.getMediaItems(screen);
      ObservableList<FrontendMediaItemRepresentation> assets = FXCollections.observableList(items);
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

  public PlaylistRepresentation getPlaylist() {
    return playlist;
  }

  public GameRepresentation getGame() {
    return game;
  }

  public FrontendMediaRepresentation getFrontendMedia() {
    return frontendMedia;
  }

  public void setPlaylistMode() {
    this.playlistsRadio.setSelected(true);
  }
}

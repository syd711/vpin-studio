package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.localsettings.LocalUISettings;
import de.mephisto.vpin.commons.utils.media.AssetMediaPlayer;
import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.restclient.assets.AssetRequest;
import de.mephisto.vpin.restclient.converter.MediaConversionCommand;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.frontend.TableAssetSearch;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.DownloadJobDescriptor;
import de.mephisto.vpin.restclient.playlists.PlaylistRepresentation;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.JobFinishedEvent;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.jobs.JobPoller;
import de.mephisto.vpin.ui.playlistmanager.PlaylistDialogs;
import de.mephisto.vpin.ui.preferences.dialogs.PreferencesDialogs;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tables.TableOverviewController;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import de.mephisto.vpin.ui.util.StudioFolderChooser;
import de.mephisto.vpin.ui.util.SystemUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
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
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.*;


public class TableAssetManagerDialogController implements Initializable, DialogController, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(TableAssetManagerDialogController.class);
  public static final String MODAL_STATE_ID = "tableAssetManagerDialog";
  public static Stage INSTANCE = null;

  @FXML
  private BorderPane root;

  @FXML
  private ComboBox<GameRepresentation> tablesCombo;

  @FXML
  private BorderPane serverAssetMediaPane;

  @FXML
  private Button downloadBtn;

  @FXML
  private Button openPlaylistManagerBtn;

  @FXML
  private Button webPreviewBtn;

  @FXML
  private Button helpBtn;

  @FXML
  private TextField searchField;

  @FXML
  private BorderPane mediaPane;

  @FXML
  private TableAssetManagerPane mediaRootPane;

  @FXML
  private ToolBar installedAssetsToolbar;

  @FXML
  private Button addToPlaylistBtn;

  @FXML
  private Button viewBtn;

  @FXML
  private SplitMenuButton deleteBtn;

  @FXML
  private Button nextButton;

  @FXML
  private Button prevButton;

  @FXML
  private Button setDefaultBtn;

  @FXML
  private Button renameBtn;

  @FXML
  private Button downloadAssetBtn;

  @FXML
  private Button folderBtn;

  @FXML
  private Button reloadBtn;

  @FXML
  private Button uploadBtn;

  @FXML
  private Button clearCacheBtn;

  @FXML
  private Button infoBtn;

  @FXML
  private MenuButton conversionMenu;

  @FXML
  private Separator folderSeparator;

  @FXML
  private ImageView frontendImage;

  @FXML
  private ListView<FrontendMediaItemRepresentation> assetList;

  @FXML
  private ListView<TableAsset> serverAssetsList;

  @FXML
  private ComboBox<AssetSourceModel> assetSourceComboBox;

  @FXML
  private Button openDataManager;

  @FXML
  private Button assetSourceBtn;

  @FXML
  private Button addAudioBlank;

  @FXML
  private Node assetsBox;

  @FXML
  private Node assetSearchBox;

  @FXML
  private Label assetSearchLabel;

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
  private MenuItem gameDeleteBtn;

  @FXML
  private MenuItem screenDeleteBtn;

  @FXML
  private HBox playlistHint;

  @FXML
  private ComboBox<PlaylistRepresentation> playlistCombo;

  private Stage localStage;
  private TableOverviewController overviewController;
  private GameRepresentation game;
  private PlaylistRepresentation playlist;
  private VPinScreen screen = VPinScreen.Wheel;
  private FrontendMediaRepresentation frontendMedia;
  private Node lastSelected;
  private boolean embedded = false;
  private AssetMediaPlayer serverMediaPlayer;
  private AssetMediaPlayer assetMediaPlayer;

  public static void close() {
    if (INSTANCE != null) {
      INSTANCE.close();
      INSTANCE = null;
    }
  }

  @FXML
  private void onNext(ActionEvent e) {
    overviewController.selectNextModel();
    GameRepresentation selection = overviewController.getSelection();
    if (selection != null && !selection.equals(this.game)) {
      this.tablesCombo.setValue(selection);
    }
  }

  @FXML
  private void onPrevious(ActionEvent e) {
    overviewController.selectPreviousModel();
    GameRepresentation selection = overviewController.getSelection();
    if (selection != null && !selection.equals(this.game)) {
      this.tablesCombo.setValue(selection);
    }
  }

  @FXML
  private void onPlaylistManager(ActionEvent e) {
    this.onCancel(e);
    PlaylistRepresentation playlist = getPlaylist();
    Platform.runLater(() -> {
      PlaylistDialogs.openPlaylistManager(this.overviewController, playlist);
    });
  }

  @FXML
  private void onAssetSourceEdit(ActionEvent e) {
    AssetSourceModel selectedItem = assetSourceComboBox.getSelectionModel().getSelectedItem();
    if (selectedItem != null && selectedItem.getSource() != null) {
      PreferencesDialogs.openMediaSource(selectedItem.getSource());
    }
  }

  @FXML
  private void onScreenDelete(ActionEvent e) {
    if (!this.assetList.getItems().isEmpty()) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Delete Screen Assets", "Delete all media for screen \"" + screen.name() + "\"?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        for (FrontendMediaItemRepresentation item : assetList.getItems()) {
          if (isPlaylistMode()) {

          }
          else {
            client.getGameMediaService().deleteMedia(game.getId(), screen, item.getName());
          }
        }
      }
      Platform.runLater(() -> {
        if (game != null) {
          EventManager.getInstance().notifyTableChange(game.getId(), null);
        }
        refreshTableMediaView();
      });
    }
  }


  @FXML
  private void onGameDelete(ActionEvent e) {
    String msg = "Delete all media of game \"" + game.getGameDisplayName() + "\"?";
    if (isPlaylistMode()) {
      msg = "Delete all media of playlist \"" + playlist.getName() + "\"?";
    }
    Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Delete All Assets", msg);
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      if (isPlaylistMode()) {

      }
      else {
        client.getGameMediaService().deleteMedia(game.getId());
      }
    }
    Platform.runLater(() -> {
      if (game != null) {
        EventManager.getInstance().notifyTableChange(game.getId(), null);
      }
      refreshTableMediaView();
    });
  }

  @FXML
  private void onWebPreview() {
    TableAsset tableAsset = serverAssetsList.getSelectionModel().getSelectedItem();
    String mimeType = tableAsset.getMimeType();
    if (mimeType == null) {
      LOG.info("No mimetype found for asset " + tableAsset);
      return;
    }
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
  private void onInfo() {
    FrontendMediaItemRepresentation selectedItem = assetList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      String name = selectedItem.getName();
      AssetRequest metadata = client.getAssetService().getMetadata(game.getId(), screen, name);
      TableDialogs.openMetadataDialog(metadata);
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
    FrontendMediaItemRepresentation selectedItem = assetList.getSelectionModel().getSelectedItem();
    if (this.playlist != null && this.playlistsRadio != null && this.playlistsRadio.isSelected()) {
      File screenDir = client.getFrontendService().getPlaylistMediaDirectory(this.playlist.getId(), screen);
      if (selectedItem != null) {
        screenDir = new File(screenDir, selectedItem.getName());
        SystemUtil.openFile(screenDir);
        return;
      }
      SystemUtil.openFolder(screenDir);
    }
    else if (this.game != null) {
      File screenDir = client.getFrontendService().getMediaDirectory(this.game.getId(), screen);
      if (selectedItem != null) {
        screenDir = new File(screenDir, selectedItem.getName());
        SystemUtil.openFile(screenDir);
        return;
      }
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
          JobPoller.getInstance().queueJob(job);
        });
      }
    }
  }

  @FXML
  private void onView(ActionEvent e) {
    FrontendMediaItemRepresentation selectedItem = assetList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      TableDialogs.openMediaDialog(stage, selectedItem);
    }
  }

  @FXML
  private void onDelete(ActionEvent e) {
    Stage stage = (Stage) ((Labeled) e.getSource()).getScene().getWindow();
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
  private void onSetDefault() {
    FrontendMediaItemRepresentation selectedItem = this.assetList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      String name = selectedItem.getName();
      String baseName = FilenameUtils.getBaseName(name);
      String uniqueAssetName = null; //FileUtils.baseUniqueAsset(name);
      if (StringUtils.equals(baseName, uniqueAssetName)) {
      }

      Optional<ButtonType> buttonType = WidgetFactory.showConfirmation(localStage, "Set As Default Asset",
          "Do you want to set this file as your default asset ?",
          "Current default asset file will be automatically renamed.");
      if (buttonType.isPresent() && buttonType.get().equals(ButtonType.OK)) {
        try {
          boolean status = client.getGameMediaService().setDefaultMedia(game.getId(), screen, selectedItem.getName());
          if (!status) {
            WidgetFactory.showAlert(localStage, "Warning",
                "Coundl't set default asset for game " + game.getGameName() + "\".",
                "Please check the asset files as they may be in an inconsistent state.");

          }
          EventManager.getInstance().notifyTableChange(game.getId(), null, game.getGameName());
          onReload();
        }
        catch (Exception e) {
          WidgetFactory.showAlert(localStage, "Error",
              "An error occurred while setting default asset for game " + game.getGameName() + "\".",
              e.getMessage());
        }
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
        TableAssetSource source = null;
        if (assetSourceComboBox.isVisible()) {
          source = assetSourceComboBox.getValue().getSource();
        }
        TableAssetSearch assetSearch = searchMedia(source, screen, term);
        ObservableList<TableAsset> assets = FXCollections.observableList(new ArrayList<>(assetSearch.getResult()));
        serverAssetsList.getItems().removeAll(serverAssetsList.getItems());
        serverAssetsList.setItems(assets);
        serverAssetsList.refresh();

        if (assets.isEmpty()) {
          serverAssetsList.setPlaceholder(new Label("No matching assets found."));
        }
        return;
      }

      serverAssetsList.getItems().removeAll(serverAssetsList.getItems());
      serverAssetsList.setItems(FXCollections.observableList(new ArrayList<>()));
      serverAssetsList.setPlaceholder(new Label("Enter a search term to find assets for this screen and table."));
      serverAssetsList.refresh();
    });
  }

  private TableAssetSearch searchMedia(TableAssetSource source, VPinScreen screen, String term) {
    ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(localStage,
        new TableAssetSearchProgressModel("Asset Search", game == null ? -1 : game.getId(), source, screen, term));
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

      String assetUrl = client.getGameMediaService().getUrl(tableAsset, this.game == null ? -1 : this.game.getId());
      LOG.info("Loading asset: " + assetUrl);

      try {
        // if a previous media player was on, dispose it and free resources
        if (serverMediaPlayer != null) {
          serverMediaPlayer.disposeMedia();
        }

        VPinScreen screen = VPinScreen.valueOfSegment(tableAsset.getScreen());
        this.serverMediaPlayer = WidgetFactory.createAssetMediaPlayer(client, assetUrl, screen, mimeType, false, false);
        serverAssetMediaPane.setCenter(serverMediaPlayer);
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

    String name = null;
    if (isPlaylistMode()) {
      name = playlist.getName();
    }
    else {
      name = game.getGameName();
    }

    ObservableList<FrontendMediaItemRepresentation> items = assetList.getItems();
    String targetName = name + "." + FilenameUtils.getExtension(tableAsset.getName());
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

    if (isPlaylistMode()) {
      ProgressDialog.createProgressDialog(stage, new TableAssetDownloadProgressModel(stage, screen, playlist, tableAsset, append));
    }
    else {
      ProgressDialog.createProgressDialog(stage, new TableAssetDownloadProgressModel(stage, screen, game, tableAsset, append));
    }

    if (game != null) {
      EventManager.getInstance().notifyTableChange(game.getId(), null);
    }
    refreshTableMediaView();
  }

  @FXML
  private void onCancel(ActionEvent e) {
    onDialogCancel();
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    if (playlistHint != null) {
      playlistHint.managedProperty().bindBidirectional(playlistHint.visibleProperty());
      playlistHint.setVisible(false);
    }

    assetSearchBox.managedProperty().bindBidirectional(assetSearchBox.visibleProperty());
    assetSourceComboBox.managedProperty().bindBidirectional(assetSourceComboBox.visibleProperty());
    setDefaultBtn.managedProperty().bindBidirectional(setDefaultBtn.visibleProperty());
    renameBtn.managedProperty().bindBidirectional(renameBtn.visibleProperty());

    if (openPlaylistManagerBtn != null) {
      openPlaylistManagerBtn.managedProperty().bindBidirectional(openPlaylistManagerBtn.visibleProperty());
      openPlaylistManagerBtn.setVisible(false);
    }

    List<TableAssetSource> assetSources = new ArrayList<>(client.getAssetSourcesService().getAssetSources());
    assetSourceComboBox.setVisible(!assetSources.isEmpty());
    if (!assetSources.isEmpty()) {
      assetSources.add(0, null);

      TableAssetSource defaultAssetSource = client.getAssetSourcesService().getDefaultAssetSource();
      if (defaultAssetSource != null) {
        assetSources.add(1, client.getAssetSourcesService().getDefaultAssetSource());
      }
    }
    List<AssetSourceModel> assetSourceModels = assetSources.stream().map(AssetSourceModel::new).collect(Collectors.toList());
    assetSourceComboBox.setItems(FXCollections.observableList(assetSourceModels));
    if (!assetSourceComboBox.getItems().isEmpty()) {
      assetSourceComboBox.getSelectionModel().select(0);
    }

    assetSourceBtn.setDisable(true);
    assetSourceComboBox.valueProperty().addListener(new ChangeListener<AssetSourceModel>() {
      @Override
      public void changed(ObservableValue<? extends AssetSourceModel> observable, AssetSourceModel oldValue, AssetSourceModel newValue) {
        assetSourceBtn.setDisable(newValue == null || newValue.getSource() == null || newValue.getSource().isSystemSource());
        onSearch();
      }
    });

    if (openDataManager != null) {
      openDataManager.managedProperty().bindBidirectional(openDataManager.visibleProperty());
    }

    TableAssetSource tableAssetSource = client.getGameMediaService().getTableAssetsConf();

    if (!isEmbeddedMode()) {
      helpBtn.managedProperty().bindBidirectional(helpBtn.visibleProperty());
      playlistSelection.managedProperty().bindBidirectional(playlistSelection.visibleProperty());
      tableSelection.managedProperty().bindBidirectional(tableSelection.visibleProperty());
      playlistSelection.setVisible(false);

      ToggleGroup toggleGroup = new ToggleGroup();
      playlistsRadio.setToggleGroup(toggleGroup);
      tablesRadio.setToggleGroup(toggleGroup);
      tablesRadio.setSelected(true);

      playlistsRadio.setDisable(!Features.PLAYLIST_ENABLED);
      if (Features.PLAYLIST_ENABLED) {
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
            setGame(localStage, overviewController, game, screen, embedded);
          }
          else {
            setGame(localStage, overviewController, tablesCombo.getValue(), screen, embedded);
          }

          screenDeleteBtn.setDisable(isPlaylistMode());
          gameDeleteBtn.setDisable(isPlaylistMode());
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

          screenDeleteBtn.setDisable(isPlaylistMode());
          gameDeleteBtn.setDisable(isPlaylistMode());
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

      installedAssetsToolbar.widthProperty().addListener((obs, o, n) -> {
        reshapeToolbar(n);
      });
    }

    Frontend frontend = client.getFrontendService().getFrontendCached();
    List<VPinScreen> supportedScreens = frontend.getSupportedScreens();
    assetSearchBox.setVisible(tableAssetSource != null);

    this.folderSeparator.managedProperty().bindBidirectional(this.folderSeparator.visibleProperty());
    this.folderBtn.managedProperty().bindBidirectional(this.folderBtn.visibleProperty());
    this.addToPlaylistBtn.managedProperty().bindBidirectional(this.addToPlaylistBtn.visibleProperty());
    this.addAudioBlank.managedProperty().bindBidirectional(this.addAudioBlank.visibleProperty());

    this.folderBtn.setVisible(SystemUtil.isFolderActionSupported());
    this.folderSeparator.setVisible(SystemUtil.isFolderActionSupported());

    mediaRootPane.addListeners(this);
    mediaRootPane.setPanesVisibility(supportedScreens);
    if (isEmbeddedMode()) {
      mediaRootPane.setEmbeddedMode();
    }

    downloadBtn.setVisible(false);
    webPreviewBtn.setVisible(false);

    this.setDefaultBtn.setDisable(true);
    this.renameBtn.setDisable(true);
    this.downloadAssetBtn.setDisable(true);
    this.addAudioBlank.setVisible(false);
    this.addToPlaylistBtn.setVisible(false);

    searchField.setOnKeyPressed(ke -> {
      if (ke.getCode().equals(KeyCode.ENTER)) {
        onSearch();
      }
    });

    serverAssetsList.setPlaceholder(new Label("          Press the search button\nto find assets for this screen and table."));
    assetList.setPlaceholder(new Label("No assets found for this screen and table."));
    assetList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    assetList.setOnDragDetected(event -> {
      FrontendMediaItemRepresentation selectedItem = assetList.getSelectionModel().getSelectedItem();
      if (selectedItem != null) {
        Dragboard db = assetList.startDragAndDrop(TransferMode.COPY);
        Map<DataFormat, Object> data = new HashMap<>();
        data.put(DataFormat.URL, selectedItem);
        db.setContent(data);
        event.consume();
      }
    });
    assetList.setOnDragOver(event -> {
      if (event.getGestureSource() != assetList && event.getDragboard().hasContent(DataFormat.URL)) {
        event.acceptTransferModes(TransferMode.COPY);
      }
      event.consume();
    });


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

    this.assetList.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<FrontendMediaItemRepresentation>() {
      @Override
      public void onChanged(Change<? extends FrontendMediaItemRepresentation> c) {
        List<? extends FrontendMediaItemRepresentation> list = c.getList();
        if (screen.equals(VPinScreen.Wheel)) {
          client.getImageCache().clearWheelCache();
        }
        if (game != null) {
          client.getFrontendService().clearCache(game.getId());
        }

        WidgetFactory.disposeMediaPane(mediaPane);
        infoBtn.setDisable(list.size() != 1);
        setDefaultBtn.setDisable(list.size() != 1);
        renameBtn.setDisable(list.size() != 1);
        downloadAssetBtn.setDisable(list.size() != 1);

        if (list.isEmpty()) {
          conversionMenu.setDisable(true);

          Label label = new Label("No media selected");
          label.setStyle("-fx-font-size: 14px;-fx-text-fill: #444444;");
          label.setUserData(null);
          mediaPane.setCenter(label);
          return;
        }
        // else
        FrontendMediaItemRepresentation mediaItem = list.get(0);
        String mimeType = mediaItem.getMimeType();
        String baseType = mimeType.split("/")[0];

        // add a condition, asset should not be the default asset already
        setDefaultBtn.setDisable(setDefaultBtn.isDisable() || FileUtils.isDefaultAsset(mediaItem.getName()));

        boolean atleastone = false;
        for (MenuItem item : conversionMenu.getItems()) {
          MediaConversionCommand cmd = (MediaConversionCommand) item.getUserData();
          boolean visible = cmd.isActiveForType(baseType);
          atleastone |= visible;
          item.setVisible(visible);
        }
        conversionMenu.setDisable(!atleastone);

        Tooltip.uninstall(mediaPane, null);

        assetMediaPlayer = WidgetFactory.createAssetMediaPlayer(client, mediaItem, false, false);
        mediaPane.setCenter(assetMediaPlayer);

        Tooltip.install(mediaPane, WidgetFactory.createMediaItemTooltip(mediaItem));
      }
    });

    if (isEmbeddedMode()) {
      assetList.prefHeightProperty().bind(root.prefHeightProperty());
    }
    else {
      clearCacheBtn.setVisible(Features.MEDIA_CACHE || !client.getAssetSourcesService().getAssetSources().isEmpty());

      if (tableAssetSource != null && tableAssetSource.getAssetSearchIcon() != null) {
        frontendImage.setImage(new Image(Studio.class.getResourceAsStream(tableAssetSource.getAssetSearchIcon())));
      }
      //if (tableAssetConf != null && tableAssetConf.getAssetSearchLabel() != null) {
      //    assetSearchLabel.setText(tableAssetConf.getAssetSearchLabel());
      //}
    }

    infoBtn.setDisable(true);

    conversionMenu.managedProperty().bindBidirectional(conversionMenu.visibleProperty());
    conversionMenu.setDisable(true);
    List<MediaConversionCommand> commandList = client.getMediaConversionService().getCommandList();
    conversionMenu.setVisible(!commandList.isEmpty());
    for (MediaConversionCommand command : commandList) {
      MenuItem item = new MenuItem(command.getName());
      item.setUserData(command);
      conversionMenu.getItems().add(item);
      item.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
          Platform.runLater(() -> {
            List<FrontendMediaItemRepresentation> selectedItems = assetList.getSelectionModel().getSelectedItems();
            if (selectedItems.isEmpty()) {
              return;
            }

            String name = "Video Conversion of " + selectedItems.size() + " media items";
            if (selectedItems.size() == 1) {
              name = "Video Conversion " + "\"" + selectedItems.get(0).getName() + "\"";
            }
            ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(new MediaConversionProgressModel(name, game.getId(), screen, selectedItems, command));
            List<Object> results = progressDialog.getResults();

            Platform.runLater(() -> {
              if (!results.isEmpty()) {
                WidgetFactory.showAlert(stage, "Error", "Error converting video: " + results.get(0));
              }
              else {
                refreshTableMediaView();
              }
            });
          });
        }
      });
    }
  }

  /**
   * Called when the toolbar is resized
   */
  private void reshapeToolbar(Number w) {
    boolean small = w.intValue() < 725;
    uploadBtn.setText(small ? "" : "Upload");
    downloadAssetBtn.setText(small ? "" : "Download");
    setDefaultBtn.setText(small ? "" : "Set As Default");
    renameBtn.setText(small ? "" : "Rename");
    reloadBtn.setText(small ? "" : "Reload");
  }

  private boolean isEmbeddedMode() {
    return this.tablesCombo == null;
  }

  public void updateState(VPinScreen s, BorderPane borderPane, Boolean hovered, Boolean clicked) {
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
      if (isAutoSearchEnabled()) {
        onSearch();
      }
      else {
        serverAssetsList.setItems(FXCollections.emptyObservableList());
        serverAssetsList.refresh();
      }
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
    try {
      if (this.serverMediaPlayer != null) {
        this.serverMediaPlayer.stopAndDispose();
      }
    }
    catch (Exception e) {
      LOG.error("Media disposal failed: {}", e.getMessage());
    }

    try {
      if (this.assetMediaPlayer != null) {
        this.assetMediaPlayer.stopAndDispose();
      }
    }
    catch (Exception e) {
      LOG.error("Media disposal failed: {}", e.getMessage());
    }

    EventManager.getInstance().removeListener(this);
    if ((this.game != null)) {
      client.getGameService().reload(this.game.getId());
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
          this.setGame(localStage, this.overviewController, t1, this.screen != null ? this.screen : VPinScreen.Wheel, false);
        });
      }).start();
    }
  }

  public void setPlaylist(Stage stage, @Nullable TableOverviewController overviewController, @NonNull PlaylistRepresentation playlist, @Nullable VPinScreen screen) {
    if (this.playlistHint != null) {
      this.playlistHint.setVisible(client.getFrontendService().getFrontendType().equals(FrontendType.Popper));
    }
    localStage = stage;
    this.overviewController = overviewController;
    this.playlist = playlist;
    this.searchField.setText("");
    if (screen != null) {
      this.screen = screen;
    }

    this.nextButton.setVisible(overviewController != null);
    this.prevButton.setVisible(overviewController != null);

    this.setDefaultBtn.setVisible(false);
    this.renameBtn.setVisible(false);

    if (!isEmbeddedMode()) {
      nextButton.setDisable(true);
      prevButton.setDisable(true);
      openDataManager.setVisible(false);
      openPlaylistManagerBtn.setVisible(overviewController != null);
      tableSelection.setVisible(false);
      playlistSelection.setVisible(true);

      this.playlistCombo.setValue(playlist);
      this.helpBtn.setVisible(false);
    }

    refreshTableMediaView();
    if (isAutoSearchEnabled()) {
      onSearch();
    }
    initDragAndDrop();
  }

  public void setGame(Stage stage, @Nullable TableOverviewController overviewController, @Nullable GameRepresentation game, @Nullable VPinScreen screen, boolean embedded) {
    serverAssetsList.setItems(FXCollections.emptyObservableList());
    if (this.playlistHint != null) {
      this.playlistHint.setVisible(false);
    }
    this.localStage = stage;
    this.embedded = embedded;
    if (!embedded) {
      TableAssetManagerDialogController.INSTANCE = localStage;
      localStage.setOnHiding(new EventHandler<WindowEvent>() {
        @Override
        public void handle(WindowEvent event) {
          TableAssetManagerDialogController.INSTANCE = null;
        }
      });
    }
    this.overviewController = overviewController;

    if (this.nextButton != null) {
      this.nextButton.setVisible(overviewController != null);
    }

    if (this.prevButton != null) {
      this.prevButton.setVisible(overviewController != null);
    }

    setGame(game, screen);
    initDragAndDrop();
  }

  private void setGame(@Nullable GameRepresentation game, @Nullable VPinScreen screen) {
    serverAssetsList.setItems(FXCollections.emptyObservableList());

    // detection of change in game
    if (this.game != null && (game == null || this.game.getId() != game.getId())) {
      searchField.setText("");
    }

    this.game = game;
    if (screen != null) {
      this.screen = screen;
    }

    this.setDefaultBtn.setVisible(true);
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
      term = term.replaceAll("", "");
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
    if (isAutoSearchEnabled()) {
      onSearch();
    }
  }

  private void initDragAndDrop() {
    mediaRootPane.installFileDragEventHandlers(this, isEmbeddedMode());
  }

  private boolean isAutoSearchEnabled() {
    if (client.getFrontendService().getFrontendType().equals(FrontendType.Popper)) {
      return false;
    }
    return true;
  }

  private void refreshTableView() {
    mediaRootPane.refreshPanes(this, this.screen);
  }

  public void refreshTableMediaView() {
    if (this.game == null && this.playlist == null) {
      return;
    }

    viewBtn.setDisable(true);
    deleteBtn.setDisable(true);

    JFXFuture.supplyAsync(() -> {
      if (isPlaylistMode()) {
        return client.getPlaylistMediaService().getPlaylistMedia(this.playlist.getId());
      }
      else {
        if (this.game == null) {
          this.game = tablesCombo.getItems().get(0);
        }
        return client.getGameMediaService().getGameMedia(this.game.getId());
      }
    }).thenAcceptLater(media -> {
      this.frontendMedia = media;

      if (!isEmbeddedMode()) {
        this.helpBtn.setDisable(!VPinScreen.Loading.equals(screen));
      }
      if (screen.equals(VPinScreen.Wheel)) {
        client.getImageCache().clearWheelCache();
        if (this.game != null) {
          client.getFrontendService().clearCache(this.game.getId());
        }
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
          viewBtn.setDisable(false);
          deleteBtn.setDisable(false);
        }

        boolean convertable = items.size() == 1 && !items.get(0).getName().contains("(SCREEN");
        this.addToPlaylistBtn.setDisable(!convertable);
      }
      else {
        assetList.setItems(FXCollections.emptyObservableList());
        assetList.refresh();
        addToPlaylistBtn.setDisable(true);

        serverAssetsList.setItems(FXCollections.emptyObservableList());
        serverAssetsList.setPlaceholder(new Label("Press the search button to search to find assets for this screen and table."));
        serverAssetsList.refresh();
      }

      refreshTableView();
      if (previewTitleLabel != null) {
        Node node = this.lastSelected;
        if (node == null) {
          node = mediaRootPane.wheel;
        }
        Label nameLabel = (Label) ((BorderPane) node).getBottom();
        previewTitleLabel.setText(nameLabel.getText());
      }
    });
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

  @Override
  public void setModality(boolean modal) {
    LocalUISettings.setModal(MODAL_STATE_ID, modal);
    Platform.runLater(() -> {
      localStage.close();
    });
    Platform.runLater(() -> {
      if (playlistsRadio.isSelected()) {
        TableDialogs.openTableAssetsDialog(overviewController, game, playlist, screen);
      }
      else {
        TableDialogs.openTableAssetsDialog(overviewController, game, screen);
      }
    });
  }

  @Override
  public void tablesSelected(List<GameRepresentation> games) {
    if (!games.isEmpty()) {
      boolean modal = LocalUISettings.isModal(MODAL_STATE_ID);
      if (!modal) {
        Platform.runLater(() -> {
          GameRepresentation gameRepresentation = games.get(0);
          if (this.game == null || this.game.getId() != gameRepresentation.getId()) {
            tablesRadio.setSelected(true);
            setGame(gameRepresentation, screen);
          }
        });
      }
    }
  }

  public void setStage(Stage stage) {
    this.localStage = stage;
  }

  class AssetSourceModel {
    private final TableAssetSource source;

    public AssetSourceModel(TableAssetSource source) {
      this.source = source;
    }

    @Override
    public String toString() {
      if (source == null) {
        return "All Asset Sources";
      }
      return source.getName();
    }

    public TableAssetSource getSource() {
      return source;
    }
  }
}

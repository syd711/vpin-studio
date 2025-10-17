package de.mephisto.vpin.ui.tables.panels;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tables.TablesController;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.FrontendUtil;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

public class UploadsButtonController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(UploadsButtonController.class);

  @FXML
  private HBox root;

  @FXML
  private SplitMenuButton uploadTableBtn;

  @FXML
  private MenuItem backglassUploadItem;

  @FXML
  private MenuItem romsUploadItem;

  @FXML
  private MenuItem iniUploadMenuItem;

  @FXML
  private MenuItem nvUploadMenuItem;

  @FXML
  private MenuItem altSoundUploadItem;

  @FXML
  private MenuItem cfgUploadItem;

  @FXML
  private MenuItem patchItem;

  @FXML
  private MenuItem altColorUploadItem;

  @FXML
  private MenuItem dmdUploadItem;

  @FXML
  private MenuItem mediaUploadItem;

  @FXML
  private MenuItem musicUploadItem;

  @FXML
  private MenuItem pupPackUploadItem;

  @FXML
  private MenuItem bamCfgUploadItem;

  @FXML
  private MenuItem povItem;

  @FXML
  private MenuItem resItem;

  private List<GameRepresentation> games = new ArrayList<>();
  private GameEmulatorRepresentation gameEmulator;
  private TablesController tablesController;

  @FXML
  public void onAltSoundUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    GameRepresentation game = null;
    if (selectedItems != null && !selectedItems.isEmpty()) {
      game = selectedItems.get(0);
    }
    TableDialogs.openAltSoundUploadDialog(game, null, null, null);
  }

  @FXML
  public void onAltColorUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      TableDialogs.openAltColorUploadDialog(selectedItems.get(0), null, null, () -> Platform.runLater(() -> {
        tablesController.getTablesSideBarController().getTitledPaneAltColor().setExpanded(true);
      }));
    }
  }

  @FXML
  public void onRomsUpload() {
    TableDialogs.onRomUploads(null, null);
  }

  @FXML
  public void onCfgUpload() {
    TableDialogs.openCfgUploads(null, null);
  }

  @FXML
  public void onPatchUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      TableDialogs.openPatchUpload(selectedItems.get(0), null, null, null);
    }
  }

  @FXML
  public void onNvRamUpload() {
    TableDialogs.openNvRamUploads(null, null);
  }


  @FXML
  public void onMusicUpload() {
    TableDialogs.onMusicUploads(null, null, null);
  }


  @FXML
  public void onPupPackUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      TableDialogs.openPupPackUploadDialog(selectedItems.get(0), null, null, () -> Platform.runLater(() -> {
        tablesController.getTablesSideBarController().getTitledPaneDirectB2s().setExpanded(true);
      }));
    }
  }

  @FXML
  public void onBackglassUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      GameRepresentation gameRepresentation = selectedItems.get(0);
      TableDialogs.openBackglassUpload(tablesController, stage, gameRepresentation, null, null);
    }
  }

  @FXML
  public void onIniUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      boolean b = TableDialogs.directUpload(stage, AssetType.INI, selectedItems.get(0), null);
      if (b) {
        tablesController.getTablesSideBarController().getTitledPaneTableData().setExpanded(true);
      }
    }
  }

  @FXML
  public void onMediaUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      TableDialogs.openMediaUploadDialog(Studio.stage, selectedItems.get(0), null, null, null, -1);
    }
  }

  @FXML
  public void onDMDUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      TableDialogs.openDMDUploadDialog(selectedItems.get(0), null, null, null);
    }
  }

  @FXML
  public void onPOVUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      boolean b = TableDialogs.directUpload(stage, AssetType.POV, selectedItems.get(0), null);
      if (b) {
        tablesController.getTablesSideBarController().getTitledPanePov().setExpanded(true);
      }
    }
  }

  @FXML
  public void onBamCfgUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      boolean b = TableDialogs.directUpload(stage, AssetType.BAM_CFG, selectedItems.get(0), null);
      if (b) {
        tablesController.getTablesSideBarController().getTitledPanePov().setExpanded(true);
      }
    }
  }

  @FXML
  public void onResUpload() {
    List<GameRepresentation> selectedItems = getSelections();
    if (selectedItems != null && !selectedItems.isEmpty()) {
      TableDialogs.directUpload(stage, AssetType.RES, selectedItems.get(0), null);
    }
  }

  @FXML
  public void onTableUpload() {
    openUploadDialogWithCheck(null);
  }

  public void openUploadDialogWithCheck(@Nullable UploadType uploadType) {
    if (client.getFrontendService().isFrontendRunning()) {
      if (Dialogs.openFrontendRunningWarning(Studio.stage)) {
        openUploadDialog(uploadType);
      }
      return;
    }

    openUploadDialog(uploadType);
  }

  private void openUploadDialog(@Nullable UploadType uploadType) {
    GameRepresentation game = getSelection();
    if (game != null) {
      GameEmulatorRepresentation emu = client.getEmulatorService().getGameEmulator(game.getEmulatorId());
      TableDialogs.openTableUploadDialog(game, emu.getType(), uploadType, null);
    }
    else {
      if (gameEmulator != null) {
        TableDialogs.openTableUploadDialog(null, gameEmulator.getType(), uploadType, null);
      }
      else {
        TableDialogs.openTableUploadDialog(null, null, uploadType, null);
      }
    }
  }

  public void setDisable(boolean b) {
    this.uploadTableBtn.setDisable(b);
  }

  private GameRepresentation getSelection() {
    if (this.games != null && !this.games.isEmpty()) {
      return this.games.get(0);
    }
    return null;
  }

  private List<GameRepresentation> getSelections() {
    return this.games;
  }

  public void setVisible(boolean b) {
    this.root.setVisible(b);
  }

  public void updateVisibility(boolean vpxOrFpEmulator, boolean vpxEmulator, boolean fpEmulator) {
    this.uploadTableBtn.setVisible(vpxOrFpEmulator);
    altSoundUploadItem.setVisible(vpxEmulator);
    altColorUploadItem.setVisible(vpxEmulator);
    dmdUploadItem.setVisible(vpxEmulator);
    patchItem.setVisible(vpxEmulator);
    iniUploadMenuItem.setVisible(vpxEmulator);
    povItem.setVisible(vpxEmulator);
    nvUploadMenuItem.setVisible(vpxEmulator);
    resItem.setVisible(vpxEmulator);
    mediaUploadItem.setVisible(vpxOrFpEmulator);
    musicUploadItem.setVisible(vpxEmulator);
    cfgUploadItem.setVisible(vpxEmulator);
    romsUploadItem.setVisible(vpxEmulator);
    pupPackUploadItem.setVisible(vpxOrFpEmulator);
    bamCfgUploadItem.setVisible(fpEmulator);
  }

  public void setTablesController(TablesController tablesController) {
    this.tablesController = tablesController;
  }

  public void setData(List<GameRepresentation> games, GameEmulatorRepresentation gameEmulator) {
    this.games = games;
    this.gameEmulator = gameEmulator;

    boolean disable = games.size() != 1;
    altSoundUploadItem.setDisable(disable);
    altColorUploadItem.setDisable(disable);
    mediaUploadItem.setDisable(disable);
    povItem.setDisable(disable);
    resItem.setDisable(disable);
    backglassUploadItem.setDisable(disable);
    iniUploadMenuItem.setDisable(disable);
  }

  public void setCompact(boolean b) {
    if (!b) {
      uploadTableBtn.setText("Uploads");
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    this.root.managedProperty().bindBidirectional(this.root.visibleProperty());
    this.uploadTableBtn.managedProperty().bindBidirectional(this.uploadTableBtn.visibleProperty());

    Frontend frontend = client.getFrontendService().getFrontendCached();
    FrontendUtil.replaceName(uploadTableBtn.getTooltip(), frontend);

    if (!Features.PUPPACKS_ENABLED) {
      uploadTableBtn.getItems().remove(pupPackUploadItem);
    }
    if (!Features.MEDIA_ENABLED) {
      uploadTableBtn.getItems().remove(mediaUploadItem);
    }
  }
}

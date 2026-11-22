package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.localsettings.LocalUISettings;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.games.GameFileType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.representations.POVRepresentation;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.system.FileInfo;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.panels.BaseSideBarController;
import de.mephisto.vpin.ui.tables.vbsedit.VBSManager;
import de.mephisto.vpin.ui.util.SystemUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;
import de.mephisto.vpin.commons.utils.i18n.Messages;

public class TablesSidebarController extends BaseSideBarController<GameRepresentation> implements Initializable, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String[] VPX_EXES = {"VPinballX64.exe", "VPinballX_BGFX64.exe", "VPinballX_GL64.exe", "VPinballX.exe", "VPinballX_BGFX.exe", "VPinballX_GL.exe"};

  @FXML
  private Accordion tableAccordion;

  @FXML
  private TitledPane titledPaneMedia;
//
//  @FXML
//  private TitledPane titledPaneDefaultBackground;

  @FXML
  private TitledPane titledPaneHighscores;

  @FXML
  private TitledPane titledPanePlaylists;

  @FXML
  private TitledPane titledPanePov;

  @FXML
  private TitledPane titledPaneAltSound;

  @FXML
  private TitledPane titledPaneScriptDetails;

  @FXML
  private TitledPane titledPaneDirectB2s;

  @FXML
  private TitledPane titledPanePUPPack;

  @FXML
  private TitledPane titledPaneDMD;

  @FXML
  private TitledPane titledPaneTableData;

  @FXML
  private TitledPane titledPaneMame;

  @FXML
  private TitledPane titledPaneVps;

  @FXML
  private TitledPane titledPaneAltColor;

  @FXML
  private CheckBox mediaPreviewCheckbox;

  @FXML
  private Button altSoundExplorerBtn;

  @FXML
  private Button directb2sBtn;

  @FXML
  private Button nvramExplorerBtn;

  @FXML
  private Button altColorExplorerBtn;

  @FXML
  private Button scriptBtn;

  @FXML
  private Button tablesBtn;

  @FXML
  private Button povBtn;

  @FXML
  private Button pupBackBtn;

  @FXML
  private Button dmdBtn;

  @FXML
  private HBox frontendTitleButtonArea;

  @FXML
  private TablesSidebarAltSoundController tablesSidebarAudioController; //fxml magic! Not unused

//  @FXML
//  private TablesSidebarDefaultBackgroundController tablesSidebarDefaultBackgroundController; //fxml magic! Not unused

  @FXML
  private TablesSidebarHighscoresController tablesSidebarHighscoresController; //fxml magic! Not unused

  @FXML
  private TablesSidebarMediaController tablesSidebarMediaController; //fxml magic! Not unused

  @FXML
  private TablesSidebarScriptDataController tablesSidebarMetadataController; //fxml magic! Not unused

  @FXML
  private TablesSidebarPovController tablesSidebarPovController; //fxml magic! Not unused

  @FXML
  private TablesSidebarDirectB2SController tablesSidebarDirectB2SController; //fxml magic! Not unused

  @FXML
  private TablesSidebarPUPPackController tablesSidebarPUPPackController; //fxml magic! Not unused

  @FXML
  private TablesSidebarDMDController tablesSidebarDMDController; //fxml magic! Not unused

  @FXML
  private TablesSidebarTableDetailsController tablesSidebarTableDetailsController; //fxml magic! Not unused

  @FXML
  private TablesSidebarPlaylistsController tablesSidebarPlaylistsController; //fxml magic! Not unused

  @FXML
  private TablesSidebarVpsController tablesSidebarVpsController; //fxml magic! Not unused

  @FXML
  private TablesSidebarMameController tablesSidebarMameController; //fxml magic! Not unused

  @FXML
  private TablesSidebarAltColorController tablesSidebarAltColorController; //fxml magic! Not unused

  private Optional<GameRepresentation> game = Optional.empty();
  private List<GameRepresentation> games = Collections.emptyList();

  private POVRepresentation pov;

  // Add a public no-args constructor
  public TablesSidebarController() {
  }

  @FXML
  private void onVpsBtn() {
    String url = VPS.getVpsBaseUrl();
    GameRepresentation selection = getTableOverviewController().getSelection();
    if (selection != null && !StringUtils.isEmpty(selection.getExtTableId())) {
      url = VPS.getVpsTableUrl(selection.getExtTableId());
    }
    Studio.browse(url);
  }

  @FXML
  private void onScriptEdit() {
    VBSManager.getInstance().edit(this.game);
  }

  @FXML
  private void onHighscores() {
    openFileInfo(GameFileType.HIGHSCORE, "msg.no_highscore_file_found_for");
  }

  @FXML
  private void onTables() {
    openFileInfo(GameFileType.TABLE, null);
  }

  @FXML
  private void onPupPack() {
    openFileInfo(GameFileType.PUPPACK, null);
  }

  @FXML
  private void onAltSound() {
    try {
      if (this.game.isPresent()) {
        FileInfo altSoundFolder = client.getAltSoundService().getAltSoundFolderInfo(game.get().getId());
        if (altSoundFolder != null) {
          SystemUtil.open(altSoundFolder);
        }
        else {
          WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("msg.no_valid_alt_sound_folder_found_for", game.get().getGameDisplayName()));
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to open Explorer: " + e.getMessage(), e);
    }
  }

  @FXML
  private void onAltColor() {
    try {
      if (this.game.isPresent()) {
        FileInfo altColorFolder = client.getAltColorService().getAltColorFolderInfo(game.get().getId());
        if (altColorFolder != null) {
          SystemUtil.open(altColorFolder);
        }
        else {
          WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("msg.no_valid_alt_color_folder_found_for", game.get().getGameDisplayName()));
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to open Explorer: " + e.getMessage(), e);
    }
  }

  @FXML
  private void onDirectB2S() {
    openFileInfo(GameFileType.DIRECTB2S, null);
  }

  @FXML
  private void onPov() {
    openFileInfo(GameFileType.POV, null);
  }

  @FXML
  private void onDMD() {
    openFileInfo(GameFileType.DMD, "dialog.no_dmd_found");
  }

  /**
   * Opens the file or folder of the selected table in the file explorer. The server resolves it, as the location
   * depends on the file layout of the table, e.g. VPX 10.8.1 keeps most files in the table's own subfolder.
   *
   * @param notFoundMessageKey the message shown when the table has no such file, called with the table name
   */
  private void openFileInfo(GameFileType type, String notFoundMessageKey) {
    try {
      if (this.game.isPresent()) {
        GameRepresentation g = this.game.get();
        FileInfo fileInfo = client.getGameService().getFileInfo(g.getId(), type);
        if (fileInfo != null) {
          SystemUtil.open(fileInfo);
        }
        else if (notFoundMessageKey != null) {
          WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get(notFoundMessageKey, g.getGameDisplayName()));
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to open Explorer for {}: {}", type, e.getMessage(), e);
    }
  }

  @FXML
  private void onScript() {
    try {
      if (this.game.isPresent()) {
        GameRepresentation game = this.game.get();
        GameEmulatorRepresentation emulatorRepresentation = client.getEmulatorService().getGameEmulator(game.getEmulatorId());

        String vpxFilePath = "\"" + new File(emulatorRepresentation.getGamesDirectory(), game.getGameFileName()).getAbsolutePath() + "\"";
        String vpxExePath = getVpxExe(emulatorRepresentation).getAbsolutePath();
        ProcessBuilder builder = new ProcessBuilder(vpxExePath, "-Edit", vpxFilePath);
        builder.directory(new File(emulatorRepresentation.getInstallationDirectory()));
        builder.start();
      }
    }
    catch (Exception e) {
      LOG.error("Failed to open VPX: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("msg.failed_to_open_vpx", e.getMessage()));
    }
  }

  /**
   * The VPX executable of the emulator: the configured one, else the first existing one of the known builds,
   * as VPX 10.8.1 may only ship the BGFX or GL build.
   */
  private static File getVpxExe(GameEmulatorRepresentation emulator) {
    File installationFolder = new File(emulator.getInstallationDirectory());
    String exeName = emulator.getExeName();
    if (!StringUtils.isEmpty(exeName) && exeName.toLowerCase().endsWith(".exe")) {
      File exe = new File(installationFolder, exeName);
      if (exe.exists()) {
        return exe;
      }
    }
    for (String name : VPX_EXES) {
      File exe = new File(installationFolder, name);
      if (exe.exists()) {
        return exe;
      }
    }
    return new File(installationFolder, VPX_EXES[0]);
  }

  @FXML
  private void onPrefsMame() {
    PreferencesController.open("mame");
  }

  @FXML
  private void onVpsPrefs() {
    PreferencesController.open("vps");
  }

  @FXML
  private void onPrefsScreenValidators() {
    PreferencesController.open("validators_screens");
  }

  @FXML
  private void onPrefsBackglass() {
    PreferencesController.open("backglass");
  }

  @FXML
  private void onPrefsHighscore() {
    PreferencesController.open("highscores");
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tableAccordion.managedProperty().bindBidirectional(tableAccordion.visibleProperty());
    frontendTitleButtonArea.managedProperty().bindBidirectional(frontendTitleButtonArea.visibleProperty());
    frontendTitleButtonArea.setVisible(SystemUtil.isFolderActionSupported());
    altSoundExplorerBtn.setVisible(SystemUtil.isFolderActionSupported());
    altColorExplorerBtn.setVisible(SystemUtil.isFolderActionSupported());
    directb2sBtn.setVisible(SystemUtil.isFolderActionSupported());
    scriptBtn.setVisible(SystemUtil.isFolderActionSupported());
    nvramExplorerBtn.setVisible(SystemUtil.isFolderActionSupported());
    tablesBtn.setVisible(SystemUtil.isFolderActionSupported());
    povBtn.setVisible(SystemUtil.isFolderActionSupported());
    pupBackBtn.setVisible(SystemUtil.isFolderActionSupported());
    dmdBtn.setVisible(SystemUtil.isFolderActionSupported());

    client.getPreferenceService().addListener(this);
  }

  public void loadSidePanels() {
    Frontend frontend = client.getFrontendService().getFrontendCached();

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarAltSoundController.class.getResource("scene-tables-sidebar-altsound.fxml"));
      loader.setResources(Messages.getBundle());
      Parent tablesRoot = loader.load();
      tablesSidebarAudioController = loader.getController();
      tablesSidebarAudioController.setSidebarController(this);
      titledPaneAltSound.setContent(tablesRoot);
    }
    catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarAltSoundController.class.getResource("scene-tables-sidebar-altcolor.fxml"));
      loader.setResources(Messages.getBundle());
      Parent tablesRoot = loader.load();
      tablesSidebarAltColorController = loader.getController();
      tablesSidebarAltColorController.setSidebarController(this);
      titledPaneAltColor.setContent(tablesRoot);
    }
    catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarAltSoundController.class.getResource("scene-tables-sidebar-vps.fxml"));
      loader.setResources(Messages.getBundle());
      Parent tablesRoot = loader.load();
      tablesSidebarVpsController = loader.getController();
      tablesSidebarVpsController.setSidebarController(this);
      titledPaneVps.setContent(tablesRoot);
    }
    catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarHighscoresController.class.getResource("scene-tables-sidebar-highscores.fxml"));
      loader.setResources(Messages.getBundle());
      Parent tablesRoot = loader.load();
      tablesSidebarHighscoresController = loader.getController();
      tablesSidebarHighscoresController.setSidebarController(this);
      titledPaneHighscores.setContent(tablesRoot);
    }
    catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    if (!frontend.getSupportedScreens().isEmpty()) {
      try {
        FXMLLoader loader = new FXMLLoader(TablesSidebarMediaController.class.getResource("scene-tables-sidebar-media.fxml"));
        loader.setResources(Messages.getBundle());
        Parent tablesRoot = loader.load();
        tablesSidebarMediaController = loader.getController();
        tablesSidebarMediaController.setSidebarController(this);
        titledPaneMedia.setContent(tablesRoot);
      }
      catch (IOException e) {
        LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
      }
    }
    else {
      tableAccordion.getPanes().remove(titledPaneMedia);
    }

    if (Features.PLAYLIST_ENABLED) {
      try {
        FXMLLoader loader = new FXMLLoader(TablesSidebarTableDetailsController.class.getResource("scene-tables-sidebar-playlists.fxml"));
        loader.setResources(Messages.getBundle());
        Parent tablesRoot = loader.load();
        tablesSidebarPlaylistsController = loader.getController();
        tablesSidebarPlaylistsController.setTableOverviewController(this.getTableOverviewController());
        titledPanePlaylists.setContent(tablesRoot);
      }
      catch (IOException e) {
        LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
      }
    }
    else {
      tableAccordion.getPanes().remove(titledPanePlaylists);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarScriptDataController.class.getResource("scene-tables-sidebar-scriptdata.fxml"));
      loader.setResources(Messages.getBundle());
      Parent tablesRoot = loader.load();
      tablesSidebarMetadataController = loader.getController();
      tablesSidebarMetadataController.setSidebarController(this);
      titledPaneScriptDetails.setContent(tablesRoot);
    }
    catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarPovController.class.getResource("scene-tables-sidebar-pov.fxml"));
      loader.setResources(Messages.getBundle());
      Parent tablesRoot = loader.load();
      tablesSidebarPovController = loader.getController();
      tablesSidebarPovController.setSidebarController(this);
      titledPanePov.setContent(tablesRoot);
    }
    catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarDirectB2SController.class.getResource("scene-tables-sidebar-directb2s.fxml"));
      loader.setResources(Messages.getBundle());
      Parent tablesRoot = loader.load();
      tablesSidebarDirectB2SController = loader.getController();
      tablesSidebarDirectB2SController.setRootController(tablesController);
      titledPaneDirectB2s.setContent(tablesRoot);
    }
    catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    if (Features.PUPPACKS_ENABLED) {
      try {
        FXMLLoader loader = new FXMLLoader(TablesSidebarPUPPackController.class.getResource("scene-tables-sidebar-pup-pack.fxml"));
        loader.setResources(Messages.getBundle());
        Parent tablesRoot = loader.load();
        tablesSidebarPUPPackController = loader.getController();
        tablesSidebarPUPPackController.setSidebarController(this);
        titledPanePUPPack.setContent(tablesRoot);
      }
      catch (IOException e) {
        LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
      }
    }
    else {
      tableAccordion.getPanes().remove(titledPanePUPPack);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarDMDController.class.getResource("scene-tables-sidebar-dmd.fxml"));
      loader.setResources(Messages.getBundle());
      Parent tablesRoot = loader.load();
      tablesSidebarDMDController = loader.getController();
      tablesSidebarDMDController.setSidebarController(this);
      titledPaneDMD.setContent(tablesRoot);
    }
    catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarTableDetailsController.class.getResource("scene-tables-sidebar-tabledata.fxml"));
      loader.setResources(Messages.getBundle());
      Parent tablesRoot = loader.load();
      tablesSidebarTableDetailsController = loader.getController();
      tablesSidebarTableDetailsController.setSidebarController(this);
      titledPaneTableData.setContent(tablesRoot);
    }
    catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }


    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarMameController.class.getResource("scene-tables-sidebar-mame.fxml"));
      loader.setResources(Messages.getBundle());
      Parent tablesRoot = loader.load();
      tablesSidebarMameController = loader.getController();
      tablesSidebarMameController.setSidebarController(this);
      titledPaneMame.setContent(tablesRoot);
    }
    catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    PreferenceEntryRepresentation preference = client.getPreferenceService().getPreference(PreferenceNames.PREVIEW_ENABLED);
    mediaPreviewCheckbox.setSelected(preference.getBooleanValue());

    Platform.runLater(() -> {
      this.tableAccordion.setExpandedPane(titledPaneMedia);
      titledPaneMedia.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
        if (expanded) {
          refreshView(game);
        }

      });
    });

    titledPanePov.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
      if (expanded) {
        refreshView(game);
      }
    });
    titledPaneHighscores.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
      if (expanded) {
        refreshView(game);
      }
    });
    titledPanePlaylists.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
      if (expanded) {
        refreshView(game);
      }
    });
    titledPaneAltSound.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
      if (expanded) {
        refreshView(game);
      }
    });

    if (tablesSidebarPUPPackController != null) {
      titledPanePUPPack.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
        if (expanded) {
          refreshView(game);
        }
      });
    }
    titledPaneDirectB2s.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
      if (expanded) {
        refreshView(game);
      }
    });
    titledPaneDMD.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
      if (expanded) {
        refreshView(game);
      }
    });
    titledPaneScriptDetails.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
      if (expanded) {
        refreshView(game);
      }
    });
//    titledPaneDefaultBackground.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
//      if (expanded) {
//        refreshView(game);
//      }
//    });
    titledPaneTableData.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
      if (expanded) {
        refreshView(game);
      }
    });
    titledPaneVps.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
      if (expanded) {
        refreshView(game);
      }
    });
    titledPaneAltColor.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
      if (expanded) {
        refreshView(game);
      }
    });
    titledPaneMame.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
      if (expanded) {
        refreshView(game);
      }
    });


    mediaPreviewCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      client.getPreferenceService().setPreference(PreferenceNames.PREVIEW_ENABLED, newValue);
      refreshView(game);
    });

    refreshSidebarSections();
  }

  public TableOverviewController getTableOverviewController() {
    return tablesController.getTableOverviewController();
  }


  public void setGames(Optional<GameRepresentation> game, List<GameRepresentation> games) {
    this.pov = null;
    this.game = game;
    this.games = games;
    this.refreshView(game, games);
  }

  private void refreshView(Optional<GameRepresentation> g) {
    refreshView(g, this.games);
  }

  private void refreshView(Optional<GameRepresentation> g, List<GameRepresentation> games) {
    Platform.runLater(() -> {
      if (titledPaneMedia != null && tablesSidebarMediaController != null) {
        if (titledPaneMedia.isExpanded() && titledPaneMedia.isVisible()) {
          boolean previewDisabled = LocalUISettings.getBoolean("preview.disabled");
          if (previewDisabled) {
            mediaPreviewCheckbox.setDisable(true);
            mediaPreviewCheckbox.setSelected(false);
            this.tablesSidebarMediaController.setGame(g, false);
          }
          else {
            this.tablesSidebarMediaController.setGame(g, mediaPreviewCheckbox.isSelected());
          }
        }
        else {
          tablesSidebarMediaController.resetMedia();
        }
      }

      if (titledPaneScriptDetails.isExpanded() && titledPaneScriptDetails.isVisible()) {
        this.tablesSidebarMetadataController.setGame(g);
      }
      if (titledPanePUPPack != null && tablesSidebarPUPPackController != null) {
        if (titledPanePUPPack.isExpanded() && titledPanePUPPack.isVisible()) {
          this.tablesSidebarPUPPackController.setGame(g);
        }
      }
      if (titledPaneDMD.isExpanded() && titledPaneDMD.isVisible()) {
        this.tablesSidebarDMDController.setGame(g);
      }
      if (titledPaneDirectB2s.isExpanded() && titledPaneDirectB2s.isVisible()) {
        this.tablesSidebarDirectB2SController.setGame(g);
      }
      if (titledPaneAltSound.isExpanded() && titledPaneAltSound.isVisible()) {
        this.tablesSidebarAudioController.setGame(g);
      }
      if (titledPaneHighscores.isExpanded() && titledPaneHighscores.isVisible()) {
        this.tablesSidebarHighscoresController.setGames(games);
      }
      if (titledPanePov.isExpanded() && titledPanePov.isVisible()) {
        this.tablesSidebarPovController.setGame(g);
      }
      if (titledPaneTableData.isExpanded() && titledPaneTableData.isVisible()) {
        this.tablesSidebarTableDetailsController.setGame(g);
      }
      if (titledPaneVps.isExpanded() && titledPaneVps.isVisible()) {
        this.tablesSidebarVpsController.setGames(games);
      }
      if (titledPaneAltColor.isExpanded() && titledPaneAltColor.isVisible()) {
        this.tablesSidebarAltColorController.setGame(g);
      }
      if (titledPaneMame.isExpanded() && titledPaneMame.isVisible()) {
        this.tablesSidebarMameController.setGame(g);
      }
      if (titledPanePlaylists.isExpanded() && titledPanePlaylists.isVisible()) {
        this.tablesSidebarPlaylistsController.setGames(games);
      }
    });
  }

  public TablesSidebarPlaylistsController getTablesSidebarPlaylistController() {
    return tablesSidebarPlaylistsController;
  }

  public TablesSidebarHighscoresController getTablesSidebarHighscoresController() {
    return tablesSidebarHighscoresController;
  }

  @Override
  public void setVisible(boolean b) {
    tableAccordion.setVisible(b);
  }

  public void refreshViewForEmulator(GameEmulatorRepresentation newValue) {
    boolean vpxMode = newValue == null || newValue.isVpxEmulator();
    boolean fx1Mode = newValue == null || newValue.getType().equals(EmulatorType.ZenFX);
    boolean fx3Mode = newValue == null || newValue.getType().equals(EmulatorType.ZenFX3);
    boolean mMode = newValue == null || newValue.getType().equals(EmulatorType.PinballM);
    boolean fpMode = newValue == null || newValue.getType().equals(EmulatorType.FuturePinball);

    titledPaneHighscores.setVisible(vpxMode || (fpMode && Features.FP_HIGHSCORES_ENABLED));
    titledPanePov.setVisible(vpxMode);
    titledPaneAltSound.setVisible(vpxMode);
    titledPaneDirectB2s.setVisible(newValue == null || newValue.isVpxEmulator() || newValue.isFpEmulator() || fx3Mode || fx1Mode || mMode);
    titledPanePUPPack.setVisible(newValue == null || newValue.isVpxEmulator() || newValue.isFpEmulator());
    titledPaneDMD.setVisible(vpxMode);
    titledPaneMame.setVisible(vpxMode);
    titledPaneVps.setVisible(vpxMode || mMode || fx1Mode || fx3Mode || fpMode);
    titledPaneAltColor.setVisible(vpxMode || fx1Mode || fx3Mode);
    titledPaneScriptDetails.setVisible(vpxMode);

    if (!getTableOverviewController().isAssetManagerMode()) {
      refreshSidebarSections();
    }
  }

  public void refreshSidebarSections() {
    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);

    int index = 0;
    index = refreshSection(titledPaneMedia, uiSettings.isSectionAssets() && Features.MEDIA_ENABLED, index);
    index = refreshSection(titledPaneTableData, uiSettings.isSectionTableData(), index);
    index = refreshSection(titledPaneDirectB2s, uiSettings.isSectionBackglass(), index);
    index = refreshSection(titledPaneDMD, uiSettings.isSectionDMD(), index);
    index = refreshSection(titledPanePlaylists, uiSettings.isSectionPlaylists() && Features.PLAYLIST_ENABLED, index);
    index = refreshSection(titledPaneAltSound, uiSettings.isSectionAltSound(), index);
    index = refreshSection(titledPaneAltColor, uiSettings.isSectionAltColor(), index);
    index = refreshSection(titledPanePov, uiSettings.isSectionPov(), index);
    index = refreshSection(titledPaneHighscores, uiSettings.isSectionHighscore(), index);
    index = refreshSection(titledPaneMame, uiSettings.isSectionVPinMAME(), index);
    index = refreshSection(titledPaneVps, uiSettings.isSectionVps(), index);
    index = refreshSection(titledPaneScriptDetails, uiSettings.isSectionScriptDetails(), index);
    index = refreshSection(titledPanePUPPack, uiSettings.isSectionPupPack() && Features.PUPPACKS_ENABLED, index);

    tablesController.setSidebarVisible(!tableAccordion.getPanes().isEmpty() && uiSettings.isTablesSidebarVisible());
  }

  private int refreshSection(TitledPane section, boolean sectionAssets, int index) {
    if (sectionAssets && section.isVisible()) {
      if (!tableAccordion.getPanes().contains(section)) {
        tableAccordion.getPanes().add(index, section);
      }
      index++;
    }
    else {
      tableAccordion.getPanes().remove(section);
    }
    return index;
  }

  public TitledPane getTitledPanePov() {
    return titledPanePov;
  }

  public TitledPane getTitledPaneDirectB2s() {
    return titledPaneDirectB2s;
  }

  public TitledPane getTitledPaneMedia() {
    return titledPaneMedia;
  }

  public TitledPane getTitledPaneTableData() {
    return titledPaneTableData;
  }

  public TitledPane getTitledPaneAltColor() {
    return titledPaneAltColor;
  }

  public boolean isEmpty() {
    return tableAccordion.getPanes().isEmpty();
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (key.equals(PreferenceNames.UI_SETTINGS)) {
      refreshSidebarSections();
    }
  }
}
package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.dmd.DMDPackage;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.restclient.representations.POVRepresentation;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.FrontendUtil;
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

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TablesSidebarController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarController.class);

  @FXML
  private Accordion tableAccordion;

  @FXML
  private TitledPane titledPaneMedia;

  @FXML
  private TitledPane titledPaneDefaultBackground;

  @FXML
  private TitledPane titledPaneHighscores;

  @FXML
  private TitledPane titledPanePlaylists;

  @FXML
  private TitledPane titledPanePov;

  @FXML
  private TitledPane titledPaneAudio;

  @FXML
  private TitledPane titledPaneMetadata;

  @FXML
  private TitledPane titledPaneDirectB2s;

  @FXML
  private TitledPane titledPanePUPPack;

  @FXML
  private TitledPane titledPaneDMD;

  @FXML
  private TitledPane titledPaneTableDetails;

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
  private Button frontendConfigBtn;

  @FXML
  private HBox frontendTitleButtonArea;

  @FXML
  private TablesSidebarAltSoundController tablesSidebarAudioController; //fxml magic! Not unused

  @FXML
  private TablesSidebarDefaultBackgroundController tablesSidebarDefaultBackgroundController; //fxml magic! Not unused

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

  private TableOverviewController tablesController;
  private POVRepresentation pov;

  // Add a public no-args constructor
  public TablesSidebarController() {
  }

  @FXML
  private void onVpsBtn() {
    if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
      try {
        String url = VPS.getVpsBaseUrl();
        GameRepresentation selection = this.tablesController.getSelection();
        if (selection != null && !StringUtils.isEmpty(selection.getExtTableId())) {
          url = VPS.getVpsTableUrl(selection.getExtTableId());
        }
        Desktop.getDesktop().browse(new URI(url));
      }
      catch (Exception ex) {
        LOG.error("Failed to open link: " + ex.getMessage(), ex);
      }
    }
  }

  @FXML
  private void onHighscores() {
    try {
      if (this.game.isPresent()) {
        GameRepresentation gameRepresentation = this.game.get();
        if (gameRepresentation.getHighscoreType() != null) {
          HighscoreType hsType = HighscoreType.valueOf(gameRepresentation.getHighscoreType());
          if (hsType.equals(HighscoreType.VPReg) || hsType.equals(HighscoreType.EM)) {
            GameEmulatorRepresentation emulatorRepresentation = client.getFrontendService().getGameEmulator(this.game.get().getEmulatorId());
            SystemUtil.openFolder(new File(emulatorRepresentation.getUserDirectory()));
            return;
          }
        }

        GameEmulatorRepresentation emulatorRepresentation = client.getFrontendService().getGameEmulator(this.game.get().getEmulatorId());
        SystemUtil.openFolder(new File(emulatorRepresentation.getNvramDirectory()));
      }
    }
    catch (Exception e) {
      LOG.error("Failed to open Explorer: " + e.getMessage(), e);
    }
  }

  @FXML
  private void onTables() {
    try {
      if (this.game.isPresent()) {
        GameEmulatorRepresentation emulatorRepresentation = client.getFrontendService().getGameEmulator(this.game.get().getEmulatorId());
        SystemUtil.openFolder(new File(emulatorRepresentation.getTablesDirectory()));
      }
    }
    catch (Exception e) {
      LOG.error("Failed to open Explorer: " + e.getMessage(), e);
    }
  }

  @FXML
  private void onPupPack() {
    try {
      if (this.game.isPresent()) {
        Frontend frontend = client.getFrontendService().getFrontend();
        File pupFolder = new File(frontend.getInstallationDirectory(), "PUPVideos");
        File gamePupFolder = new File(pupFolder, game.get().getRom());
        SystemUtil.openFolder(gamePupFolder, new File(frontend.getInstallationDirectory(), "PUPVideos"));
      }
    }
    catch (Exception e) {
      LOG.error("Failed to open Explorer: " + e.getMessage(), e);
    }
  }

  @FXML
  private void onAltSound() {
    try {
      if (this.game.isPresent()) {
        GameEmulatorRepresentation emulatorRepresentation = client.getFrontendService().getGameEmulator(this.game.get().getEmulatorId());
        File altSoundFolder = new File(emulatorRepresentation.getAltSoundDirectory(), game.get().getRom());
        SystemUtil.openFolder(altSoundFolder, new File(emulatorRepresentation.getAltSoundDirectory()));
      }
    }
    catch (
        Exception e) {
      LOG.error("Failed to open Explorer: " + e.getMessage(), e);
    }

  }

  @FXML
  private void onAltColor() {
    try {
      if (this.game.isPresent()) {
        GameEmulatorRepresentation emulatorRepresentation = client.getFrontendService().getGameEmulator(this.game.get().getEmulatorId());
        File folder = new File(emulatorRepresentation.getAltColorDirectory(), game.get().getRom());
        SystemUtil.openFolder(folder, new File(emulatorRepresentation.getAltColorDirectory()));
      }
    }
    catch (Exception e) {
      LOG.error("Failed to open Explorer: " + e.getMessage(), e);
    }
  }

  @FXML
  private void onDirectB2S() {
    try {
      if (this.game.isPresent()) {
        GameEmulatorRepresentation emulatorRepresentation = client.getFrontendService().getGameEmulator(this.game.get().getEmulatorId());
        SystemUtil.openFolder(new File(emulatorRepresentation.getTablesDirectory()));
      }
    }
    catch (Exception e) {
      LOG.error("Failed to open Explorer: " + e.getMessage(), e);
    }
  }

  @FXML
  private void onDMD() {
    try {
      if (this.game.isPresent()) {
        DMDPackage dmdPackage = client.getDmdService().getDMDPackage(this.game.get().getId());
        if (dmdPackage != null) {
          GameEmulatorRepresentation emulatorRepresentation = client.getFrontendService().getGameEmulator(this.game.get().getEmulatorId());
          File tablesFolder = new File(emulatorRepresentation.getTablesDirectory());
          File dmdFolder = new File(tablesFolder, dmdPackage.getName());
          SystemUtil.openFolder(dmdFolder);
          return;
        }
      }

      GameEmulatorRepresentation emulatorRepresentation = client.getFrontendService().getGameEmulator(this.game.get().getEmulatorId());
      SystemUtil.openFolder(new File(emulatorRepresentation.getTablesDirectory()));
    }
    catch (Exception e) {
      LOG.error("Failed to open Explorer: " + e.getMessage(), e);
    }
  }

  @FXML
  private void onScript() {
    try {
      if (this.game.isPresent()) {
        GameRepresentation game = this.game.get();
        GameEmulatorRepresentation emulatorRepresentation = client.getFrontendService().getGameEmulator(this.game.get().getEmulatorId());

        String vpxFilePath = "\"" + new File(emulatorRepresentation.getTablesDirectory(), game.getGameFileName()).getAbsolutePath() + "\"";
        String vpxExePath = new File(emulatorRepresentation.getInstallationDirectory(), "VPinballX64.exe").getAbsolutePath();
        ProcessBuilder builder = new ProcessBuilder(vpxExePath, "-Edit", vpxFilePath);
        builder.directory(new File(emulatorRepresentation.getInstallationDirectory()));
        builder.start();
      }
    }
    catch (Exception e) {
      LOG.error("Failed to open VPX: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open VPX: " + e.getMessage());
    }
  }


  @FXML
  private void onFrontendAdminOpen() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
      try {
        Frontend frontend = client.getFrontendService().getFrontend();
        if (frontend.getAdminExe() != null) {
          File file = new File(frontend.getInstallationDirectory(), frontend.getAdminExe());
          if (!file.exists()) {
            WidgetFactory.showAlert(Studio.stage, "Did not find admin exe", "The exe file " + file.getAbsolutePath() + " was not found.");
          }
          else {
            desktop.open(file);
          }
        }

      }
      catch (Exception e) {
        LOG.error("Failed to open frontend administration: " + e.getMessage(), e);
      }
    }
  }

  @FXML
  private void onPrefsMame() {
    PreferencesController.open("mame");
  }

  @FXML
  private void onPrefsScreenValidators() {
    PreferencesController.open("validators-screens");
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

    titledPaneDefaultBackground.managedProperty().bindBidirectional(titledPaneDefaultBackground.visibleProperty());
    titledPaneHighscores.managedProperty().bindBidirectional(titledPaneDefaultBackground.visibleProperty());
    titledPanePov.managedProperty().bindBidirectional(titledPaneDefaultBackground.visibleProperty());
    titledPaneAudio.managedProperty().bindBidirectional(titledPaneDefaultBackground.visibleProperty());
    titledPaneDirectB2s.managedProperty().bindBidirectional(titledPaneDefaultBackground.visibleProperty());
    titledPanePUPPack.managedProperty().bindBidirectional(titledPaneDefaultBackground.visibleProperty());
    titledPaneDMD.managedProperty().bindBidirectional(titledPaneDefaultBackground.visibleProperty());
    titledPaneMame.managedProperty().bindBidirectional(titledPaneDefaultBackground.visibleProperty());
    titledPaneVps.managedProperty().bindBidirectional(titledPaneDefaultBackground.visibleProperty());
    titledPaneAltColor.managedProperty().bindBidirectional(titledPaneDefaultBackground.visibleProperty());
    titledPaneMetadata.managedProperty().bindBidirectional(titledPaneDefaultBackground.visibleProperty());
  }

  private void loadSidePanels() {
    Frontend frontend = client.getFrontendService().getFrontend();
    FrontendType frontendType = frontend.getFrontendType();

    FrontendUtil.replaceName(frontendConfigBtn.getTooltip(), frontend);

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarAltSoundController.class.getResource("scene-tables-sidebar-altsound.fxml"));
      Parent tablesRoot = loader.load();
      tablesSidebarAudioController = loader.getController();
      tablesSidebarAudioController.setSidebarController(this);
      titledPaneAudio.setContent(tablesRoot);
    }
    catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarAltSoundController.class.getResource("scene-tables-sidebar-altcolor.fxml"));
      Parent tablesRoot = loader.load();
      tablesSidebarAltColorController = loader.getController();
      titledPaneAltColor.setContent(tablesRoot);
    }
    catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarAltSoundController.class.getResource("scene-tables-sidebar-vps.fxml"));
      Parent tablesRoot = loader.load();
      tablesSidebarVpsController = loader.getController();
      tablesSidebarVpsController.setSidebarController(this);
      titledPaneVps.setContent(tablesRoot);
    }
    catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarDefaultBackgroundController.class.getResource("scene-tables-sidebar-default-background.fxml"));
      Parent tablesRoot = loader.load();
      tablesSidebarDefaultBackgroundController = loader.getController();
      tablesSidebarDefaultBackgroundController.setSidebarController(this);
      titledPaneDefaultBackground.setContent(tablesRoot);
    }
    catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarHighscoresController.class.getResource("scene-tables-sidebar-highscores.fxml"));
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


    if (frontendType.supportPlaylists()) {
      try {
        FXMLLoader loader = new FXMLLoader(TablesSidebarTableDetailsController.class.getResource("scene-tables-sidebar-playlists.fxml"));
        Parent tablesRoot = loader.load();
        tablesSidebarPlaylistsController = loader.getController();
        tablesSidebarPlaylistsController.setSidebarController(this);
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
      Parent tablesRoot = loader.load();
      tablesSidebarMetadataController = loader.getController();
      tablesSidebarMetadataController.setSidebarController(this);
      titledPaneMetadata.setContent(tablesRoot);
    }
    catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarPovController.class.getResource("scene-tables-sidebar-pov.fxml"));
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
      Parent tablesRoot = loader.load();
      tablesSidebarDirectB2SController = loader.getController();
      tablesSidebarDirectB2SController.setSidebarController(this);
      titledPaneDirectB2s.setContent(tablesRoot);
    }
    catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    if (frontendType.supportPupPacks()) {
      try {
        FXMLLoader loader = new FXMLLoader(TablesSidebarPUPPackController.class.getResource("scene-tables-sidebar-pup-pack.fxml"));
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
      Parent tablesRoot = loader.load();
      tablesSidebarDMDController = loader.getController();
      tablesSidebarDMDController.setSidebarController(this);
      titledPaneDMD.setContent(tablesRoot);
    }
    catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    if (frontendType.supportStandardFields()) {
      try {
        FXMLLoader loader = new FXMLLoader(TablesSidebarTableDetailsController.class.getResource("scene-tables-sidebar-tabledetails.fxml"));
        Parent tablesRoot = loader.load();
        tablesSidebarTableDetailsController = loader.getController();
        tablesSidebarTableDetailsController.setSidebarController(this);
        titledPaneTableDetails.setContent(tablesRoot);
      }
      catch (IOException e) {
        LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
      }
    }
    else {
      tableAccordion.getPanes().remove(titledPaneTableDetails);
    }


    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarMameController.class.getResource("scene-tables-sidebar-mame.fxml"));
      Parent tablesRoot = loader.load();
      tablesSidebarMameController = loader.getController();
      tablesSidebarMameController.setSidebarController(this);
      titledPaneMame.setContent(tablesRoot);
    }
    catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    PreferenceEntryRepresentation preference = client.getPreference(PreferenceNames.PREVIEW_ENABLED);
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
    titledPaneAudio.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
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
    titledPaneMetadata.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
      if (expanded) {
        refreshView(game);
      }
    });
    titledPaneDefaultBackground.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
      if (expanded) {
        refreshView(game);
      }
    });
    titledPaneTableDetails.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
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
  }

  public void setTablesController(TableOverviewController tablesController) {
    this.tablesController = tablesController;
    loadSidePanels();
  }

  public TableOverviewController getTablesController() {
    return tablesController;
  }

  public void setGame(Optional<GameRepresentation> g) {
    this.pov = null;
    this.game = g;
    this.refreshView(g);
  }

  private void refreshView(Optional<GameRepresentation> g) {
    Platform.runLater(() -> {
      if (titledPaneMedia != null && tablesSidebarMediaController != null) {
        if (titledPaneMedia.isExpanded() && titledPaneMedia.isVisible()) {
          this.tablesSidebarMediaController.setGame(g, mediaPreviewCheckbox.isSelected());
        }
        else {
          tablesSidebarMediaController.resetMedia();
        }
      }

      if (titledPaneMetadata.isExpanded()) {
        this.tablesSidebarMetadataController.setGame(g);
      }

      if (titledPanePUPPack != null && tablesSidebarPUPPackController != null) {
        if (titledPanePUPPack.isExpanded()) {
          this.tablesSidebarPUPPackController.setGame(g);
        }
      }
      if (titledPaneDMD.isExpanded()) {
        this.tablesSidebarDMDController.setGame(g);
      }
      if (titledPaneDirectB2s.isExpanded()) {
        this.tablesSidebarDirectB2SController.setGame(g);
      }
      if (titledPaneAudio.isExpanded()) {
        this.tablesSidebarAudioController.setGame(g);
      }
      if (titledPaneHighscores.isExpanded()) {
        this.tablesSidebarHighscoresController.setGame(g);
      }
      if (titledPaneDefaultBackground.isExpanded()) {
        this.tablesSidebarDefaultBackgroundController.setGame(g);
      }
      if (titledPanePov.isExpanded()) {
        this.tablesSidebarPovController.setGame(g);
      }
      if (titledPaneTableDetails.isExpanded()) {
        this.tablesSidebarTableDetailsController.setGame(g);
      }
      if (titledPaneVps.isExpanded()) {
        this.tablesSidebarVpsController.setGame(g);
      }
      if (titledPaneAltColor.isExpanded()) {
        this.tablesSidebarAltColorController.setGame(g);
      }
      if (titledPaneMame.isExpanded()) {
        this.tablesSidebarMameController.setGame(g);
      }
      if (titledPanePlaylists.isExpanded()) {
        this.tablesSidebarPlaylistsController.setGame(g);
      }
    });
  }

  public TablesSidebarHighscoresController getTablesSidebarHighscoresController() {
    return tablesSidebarHighscoresController;
  }

  public void setVisible(boolean b) {
    tableAccordion.setVisible(b);
  }

  public void refreshViewForEmulator(GameEmulatorRepresentation newValue) {
    boolean vpxMode = newValue == null || newValue.isVpxEmulator();
    FrontendType frontendType = client.getFrontendService().getFrontendType();

    if (!vpxMode) {
      tableAccordion.getPanes().remove(titledPaneDefaultBackground);
      tableAccordion.getPanes().remove(titledPaneHighscores);
      tableAccordion.getPanes().remove(titledPanePov);
      tableAccordion.getPanes().remove(titledPaneAudio);
      tableAccordion.getPanes().remove(titledPaneDirectB2s);
      tableAccordion.getPanes().remove(titledPanePUPPack);
      tableAccordion.getPanes().remove(titledPaneDMD);
      tableAccordion.getPanes().remove(titledPaneMame);
      tableAccordion.getPanes().remove(titledPaneVps);
      tableAccordion.getPanes().remove(titledPaneAltColor);
      tableAccordion.getPanes().remove(titledPaneMetadata);
    }
    else {
      if (tableAccordion.getPanes().size() == 3) {
        tableAccordion.getPanes().add(titledPaneDirectB2s);
        tableAccordion.getPanes().add(titledPaneDMD);
        tableAccordion.getPanes().add(titledPaneAudio);
        tableAccordion.getPanes().add(titledPaneAltColor);
        tableAccordion.getPanes().add(titledPanePov);
        tableAccordion.getPanes().add(titledPaneHighscores);
        tableAccordion.getPanes().add(titledPaneMame);
        tableAccordion.getPanes().add(titledPaneVps);
        tableAccordion.getPanes().add(titledPaneMetadata);
        tableAccordion.getPanes().add(titledPaneDefaultBackground);

        if (tablesSidebarPUPPackController != null) {
          tableAccordion.getPanes().add(titledPanePUPPack);
        }
      }
    }
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

  public TitledPane getTitledPaneAltColor() {
    return titledPaneAltColor;
  }
}
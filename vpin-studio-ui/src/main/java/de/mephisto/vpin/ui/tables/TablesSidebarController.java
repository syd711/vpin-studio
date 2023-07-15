package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.SystemSummary;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.POVRepresentation;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.Studio;
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
  private TitledPane titledPanePopper;

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
  private Button altColorExplorerBtn;

  @FXML
  private Button scriptBtn;

  @FXML
  private HBox popperTitleButtonArea;

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
  private TablesSidebarPopperController tablesSidebarPopperController; //fxml magic! Not unused

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
        Desktop.getDesktop().browse(new URI("https://virtual-pinball-spreadsheet.web.app/"));
      } catch (Exception ex) {
        LOG.error("Failed to open link: " + ex.getMessage(), ex);
      }
    }
  }

  @FXML
  private void onAltSound() {
    try {
      SystemSummary systemSummary = Studio.client.getSystemService().getSystemSummary();
      new ProcessBuilder("explorer.exe", new File(systemSummary.getVpinMameDirectory(), "altsound").getAbsolutePath()).start();
    } catch (Exception e) {
      LOG.error("Failed to open Explorer: " + e.getMessage(), e);
    }
  }

  @FXML
  private void onAltColor() {
    try {
      SystemSummary systemSummary = Studio.client.getSystemService().getSystemSummary();
      new ProcessBuilder("explorer.exe", new File(systemSummary.getVpinMameDirectory(), "altcolor").getAbsolutePath()).start();
    } catch (Exception e) {
      LOG.error("Failed to open Explorer: " + e.getMessage(), e);
    }
  }

  @FXML
  private void onScript() {
    try {
      if (this.game.isPresent()) {
        GameRepresentation game = this.game.get();
        SystemSummary systemSummary = Studio.client.getSystemService().getSystemSummary();
        String vpxFilePath = "\"" + new File(systemSummary.getVisualPinballDirectory(), "Tables/" + game.getGameFileName()).getAbsolutePath() + "\"";
        String vpxExePath = new File(systemSummary.getVisualPinballDirectory(), "VPinballX.exe").getAbsolutePath();
        ProcessBuilder builder = new ProcessBuilder(vpxExePath, "-Edit", vpxFilePath);
        builder.directory(new File(systemSummary.getVisualPinballDirectory()));
        builder.start();
      }
    } catch (Exception e) {
      LOG.error("Failed to open VPX: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open VPX: " + e.getMessage());
    }
  }


  @FXML
  private void onPopperBtn() {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
      try {
        SystemSummary systemSummary = Studio.client.getSystemService().getSystemSummary();
        File file = new File(systemSummary.getPinupSystemDirectory(), "PinUpMenuSetup.exe");
        if (!file.exists()) {
          WidgetFactory.showAlert(Studio.stage, "Did not find PinUpMenuSetup.exe", "The exe file " + file.getAbsolutePath() + " was not found.");
        }
        else {
          desktop.open(file);
        }
      } catch (Exception e) {
        LOG.error("Failed to open PinUpMenuSetup: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tableAccordion.managedProperty().bindBidirectional(tableAccordion.visibleProperty());
    popperTitleButtonArea.managedProperty().bindBidirectional(popperTitleButtonArea.visibleProperty());
    popperTitleButtonArea.setVisible(client.getSystemService().isLocal());
    altSoundExplorerBtn.setVisible(client.getSystemService().isLocal());
    altColorExplorerBtn.setVisible(client.getSystemService().isLocal());
    scriptBtn.setVisible(client.getSystemService().isLocal());

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarAltSoundController.class.getResource("scene-tables-sidebar-altsound.fxml"));
      Parent tablesRoot = loader.load();
      tablesSidebarAudioController = loader.getController();
      tablesSidebarAudioController.setSidebarController(this);
      titledPaneAudio.setContent(tablesRoot);
    } catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarAltSoundController.class.getResource("scene-tables-sidebar-altcolor.fxml"));
      Parent tablesRoot = loader.load();
      tablesSidebarAltColorController = loader.getController();
      tablesSidebarAltColorController.setSidebarController(this);
      titledPaneAltColor.setContent(tablesRoot);
    } catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarAltSoundController.class.getResource("scene-tables-sidebar-vps.fxml"));
      Parent tablesRoot = loader.load();
      tablesSidebarVpsController = loader.getController();
      tablesSidebarVpsController.setSidebarController(this);
      titledPaneVps.setContent(tablesRoot);
    } catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarDefaultBackgroundController.class.getResource("scene-tables-sidebar-default-background.fxml"));
      Parent tablesRoot = loader.load();
      tablesSidebarDefaultBackgroundController = loader.getController();
      tablesSidebarDefaultBackgroundController.setSidebarController(this);
      titledPaneDefaultBackground.setContent(tablesRoot);
    } catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarHighscoresController.class.getResource("scene-tables-sidebar-highscores.fxml"));
      Parent tablesRoot = loader.load();
      tablesSidebarHighscoresController = loader.getController();
      tablesSidebarHighscoresController.setSidebarController(this);
      titledPaneHighscores.setContent(tablesRoot);
    } catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarMediaController.class.getResource("scene-tables-sidebar-media.fxml"));
      Parent tablesRoot = loader.load();
      tablesSidebarMediaController = loader.getController();
      tablesSidebarMediaController.setSidebarController(this);
      titledPaneMedia.setContent(tablesRoot);
    } catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarScriptDataController.class.getResource("scene-tables-sidebar-scriptdata.fxml"));
      Parent tablesRoot = loader.load();
      tablesSidebarMetadataController = loader.getController();
      tablesSidebarMetadataController.setSidebarController(this);
      titledPaneMetadata.setContent(tablesRoot);
    } catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarPovController.class.getResource("scene-tables-sidebar-pov.fxml"));
      Parent tablesRoot = loader.load();
      tablesSidebarPovController = loader.getController();
      tablesSidebarPovController.setSidebarController(this);
      titledPanePov.setContent(tablesRoot);
    } catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarDirectB2SController.class.getResource("scene-tables-sidebar-directb2s.fxml"));
      Parent tablesRoot = loader.load();
      tablesSidebarDirectB2SController = loader.getController();
      tablesSidebarDirectB2SController.setSidebarController(this);
      titledPaneDirectB2s.setContent(tablesRoot);
    } catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarPUPPackController.class.getResource("scene-tables-sidebar-pup-pack.fxml"));
      Parent tablesRoot = loader.load();
      tablesSidebarPUPPackController = loader.getController();
      tablesSidebarPUPPackController.setSidebarController(this);
      titledPanePUPPack.setContent(tablesRoot);
    } catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarPopperController.class.getResource("scene-tables-sidebar-popper.fxml"));
      Parent tablesRoot = loader.load();
      tablesSidebarPopperController = loader.getController();
      tablesSidebarPopperController.setSidebarController(this);
      titledPanePopper.setContent(tablesRoot);
    } catch (IOException e) {
      LOG.error("Failed loading sidebar controller: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarMameController.class.getResource("scene-tables-sidebar-mame.fxml"));
      Parent tablesRoot = loader.load();
      tablesSidebarMameController = loader.getController();
      tablesSidebarMameController.setSidebarController(this);
      titledPaneMame.setContent(tablesRoot);
    } catch (IOException e) {
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
    titledPaneAudio.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
      if (expanded) {
        refreshView(game);
      }
    });
    titledPanePUPPack.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
      if (expanded) {
        refreshView(game);
      }
    });
    titledPaneDirectB2s.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
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
    titledPanePopper.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
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
      if (titledPaneMedia.isExpanded()) {
        this.tablesSidebarMediaController.setGame(g, mediaPreviewCheckbox.isSelected());
      }
      else {
        tablesSidebarMediaController.resetMedia();
      }

      if (titledPaneMetadata.isExpanded()) {
        this.tablesSidebarMetadataController.setGame(g);
      }
      if (titledPanePUPPack.isExpanded()) {
        this.tablesSidebarPUPPackController.setGame(g);
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
      if (titledPanePopper.isExpanded()) {
        this.tablesSidebarPopperController.setGame(g);
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
    });

  }

  public TablesSidebarAltSoundController getTablesSidebarAudioController() {
    return tablesSidebarAudioController;
  }

  public TablesSidebarDefaultBackgroundController getTablesSidebarDefaultBackgroundController() {
    return tablesSidebarDefaultBackgroundController;
  }

  public TablesSidebarHighscoresController getTablesSidebarHighscoresController() {
    return tablesSidebarHighscoresController;
  }

  public TablesSidebarMediaController getTablesSidebarMediaController() {
    return tablesSidebarMediaController;
  }

  public TablesSidebarScriptDataController getTablesSidebarMetadataController() {
    return tablesSidebarMetadataController;
  }

  public TablesSidebarPovController getTablesSidebarPovController() {
    return tablesSidebarPovController;
  }

  public TitledPane getTitledPaneMedia() {
    return titledPaneMedia;
  }

  public void setVisible(boolean b) {
    tableAccordion.setVisible(b);
  }
}
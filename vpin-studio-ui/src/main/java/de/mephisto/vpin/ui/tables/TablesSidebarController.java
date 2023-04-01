package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.POVRepresentation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Accordion;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TitledPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

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
  private CheckBox mediaPreviewCheckbox;

  @FXML
  private TablesSidebarAudioController tablesSidebarAudioController; //fxml magic! Not unused

  @FXML
  private TablesSidebarDefaultBackgroundController tablesSidebarDefaultBackgroundController; //fxml magic! Not unused

  @FXML
  private TablesSidebarHighscoresController tablesSidebarHighscoresController; //fxml magic! Not unused

  @FXML
  private TablesSidebarMediaController tablesSidebarMediaController; //fxml magic! Not unused

  @FXML
  private TablesSidebarMetadataController tablesSidebarMetadataController; //fxml magic! Not unused

  @FXML
  private TablesSidebarPovController tablesSidebarPovController; //fxml magic! Not unused

  @FXML
  private TablesSidebarDirectB2SController tablesSidebarDirectB2SController; //fxml magic! Not unused

  @FXML
  private TablesSidebarPUPPackController tablesSidebarPUPPackController; //fxml magic! Not unused

  private Optional<GameRepresentation> game = Optional.empty();

  private TableOverviewController tablesController;
  private POVRepresentation pov;

  // Add a public no-args constructor
  public TablesSidebarController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tableAccordion.managedProperty().bindBidirectional(tableAccordion.visibleProperty());

    try {
      FXMLLoader loader = new FXMLLoader(TablesSidebarAudioController.class.getResource("scene-tables-sidebar-audio.fxml"));
      Parent tablesRoot = loader.load();
      tablesSidebarAudioController = loader.getController();
      tablesSidebarAudioController.setSidebarController(this);
      titledPaneAudio.setContent(tablesRoot);
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
      FXMLLoader loader = new FXMLLoader(TablesSidebarMetadataController.class.getResource("scene-tables-sidebar-metadata.fxml"));
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

    titledPaneMedia.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
      refreshView(game);
    });
    titledPanePov.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
      refreshView(game);
    });
    titledPaneHighscores.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
      refreshView(game);
    });
    titledPaneAudio.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
      refreshView(game);
    });
    titledPanePUPPack.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
      refreshView(game);
    });
    titledPaneDirectB2s.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
      refreshView(game);
    });
    titledPaneMetadata.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
      refreshView(game);
    });
    titledPaneDefaultBackground.expandedProperty().addListener((observableValue, aBoolean, expanded) -> {
      refreshView(game);
    });

    mediaPreviewCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> refreshView(game));
    Platform.runLater(() -> {
      this.tableAccordion.setExpandedPane(titledPaneMedia);
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
  }

  public TablesSidebarAudioController getTablesSidebarAudioController() {
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

  public TablesSidebarMetadataController getTablesSidebarMetadataController() {
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
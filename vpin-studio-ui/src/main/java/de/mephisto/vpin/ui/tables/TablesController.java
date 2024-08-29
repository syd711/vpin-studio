package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.fx.pausemenu.UIDefaults;
import de.mephisto.vpin.commons.utils.TransitionUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.archiving.RepositoryController;
import de.mephisto.vpin.ui.archiving.RepositorySidebarController;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.JobFinishedEvent;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.preferences.PreferenceType;
import de.mephisto.vpin.ui.tables.alx.AlxController;
import de.mephisto.vpin.ui.backglassmanager.BackglassManagerController;
import de.mephisto.vpin.ui.vps.VpsTablesController;
import de.mephisto.vpin.ui.vps.VpsTablesSidebarController;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TablesController implements Initializable, StudioFXController, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(TablesController.class);

  private TableOverviewController tableOverviewController;
  private BackglassManagerController backglassManagerController;
  private VpsTablesController vpsTablesController;
  private AlxController alxController;
  private RepositoryController repositoryController;

  @FXML
  private BorderPane root;

  @FXML
  private TabPane tabPane;

  @FXML
  private Tab tablesTab;

  @FXML
  private Tab backglassManagerTab;

  @FXML
  private Tab tablesStatisticsTab;

  @FXML
  private Tab tableRepositoryTab;

  @FXML
  private Tab vpsTablesTab;

  @FXML
  private TablesSidebarController tablesSideBarController; //fxml magic! Not unused

  @FXML
  private RepositorySidebarController repositorySideBarController; //fxml magic! Not unused

  @FXML
  private VpsTablesSidebarController vpsTablesSidebarController; //fxml magic! Not unused

  @FXML
  private TablesAssetViewSidebarController assetViewSideBarController; //fxml magic! Not unused

  @FXML
  private StackPane editorRootStack;

  @FXML
  private Button toggleSidebarBtn;

  @FXML
  private Button tableSettingsBtn;

  private Node sidePanelRoot;
  private boolean sidebarVisible = true;

  @Override
  public void onViewActivated(NavigationOptions options) {
    refreshTabSelection(tabPane.getSelectionModel().getSelectedIndex());
    if (options != null) {
      tabPane.getSelectionModel().select(0);
      tableOverviewController.selectGameInModel(options.getGameId());
    }
  }


  @FXML
  private void onTableSettings() {
    int selectedIndex = tabPane.getSelectionModel().getSelectedIndex();
    switch (selectedIndex) {
      case 0: {
        PreferencesController.open("settings_client");
        break;
      }
      case 1: {
        PreferencesController.open("backglass");
        break;
      }
      case 2: {
        PreferencesController.open("settings_client");
        break;
      }
      case 3: {
        PreferencesController.open("settings_client");
        break;
      }
      case 4: {
        PreferencesController.open("vpbm");
        break;
      }
      default: {
        PreferencesController.open("settings_client");
      }
    }
  }

  @FXML
  private void toggleSidebar() {
    sidebarVisible = !sidebarVisible;

    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
    uiSettings.setSidebarVisible(sidebarVisible);
    client.getPreferenceService().setJsonPreference(PreferenceNames.UI_SETTINGS, uiSettings, true);

    setSidebarVisible(sidebarVisible);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NavigationController.setInitialController(NavigationItem.Tables, this, root);
    EventManager.getInstance().addListener(this);
    sidePanelRoot = root.getRight();


    FrontendType frontendType = client.getFrontendService().getFrontendType();
    if (!frontendType.supportStatistics()) {
      tabPane.getTabs().remove(tablesStatisticsTab);
    }
    if (!frontendType.supportArchive()) {
      tabPane.getTabs().remove(tableRepositoryTab);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TableOverviewController.class.getResource("scene-tables-overview.fxml"));
      Parent tablesRoot = loader.load();
      tableOverviewController = loader.getController();
      tableOverviewController.setRootController(this);
      tablesSideBarController.setTableOverviewController(tableOverviewController);
      tablesTab.setContent(tablesRoot);
    }
    catch (IOException e) {
      LOG.error("failed to load table overview: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(BackglassManagerController.class.getResource("scene-directb2s-admin.fxml"));
      Parent directb2sRoot = loader.load();
      backglassManagerController = loader.getController();
      backglassManagerController.setTableSidebarController(tablesSideBarController);
      backglassManagerTab.setContent(directb2sRoot);

      Image image = new Image(Studio.class.getResourceAsStream("b2s.png"));
      ImageView view = new ImageView(image);
      view.setFitWidth(18);
      view.setFitHeight(18);
      backglassManagerTab.setGraphic(view);
    }
    catch (IOException e) {
      LOG.error("failed to load table overview: " + e.getMessage(), e);
    }


    try {
      FXMLLoader loader = new FXMLLoader(AlxController.class.getResource("scene-alx.fxml"));
      Parent repositoryRoot = loader.load();
      alxController = loader.getController();
      tablesStatisticsTab.setContent(repositoryRoot);
    }
    catch (IOException e) {
      LOG.error("failed to load statistic tab: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(RepositoryController.class.getResource("scene-repository.fxml"));
      Parent repositoryRoot = loader.load();
      repositoryController = loader.getController();
      repositoryController.setRootController(this);
      tableRepositoryTab.setContent(repositoryRoot);
    }
    catch (IOException e) {
      LOG.error("failed to load repositoy tab: " + e.getMessage(), e);
    }


    try {
      FXMLLoader loader = new FXMLLoader(VpsTablesController.class.getResource("scene-vps-tables.fxml"));
      Parent repositoryRoot = loader.load();
      vpsTablesController = loader.getController();
      vpsTablesController.setRootController(this);
      vpsTablesTab.setContent(repositoryRoot);

      Image image = new Image(Studio.class.getResourceAsStream("vps.png"));
      ImageView view = new ImageView(image);
      view.setFitWidth(18);
      view.setFitHeight(18);
      vpsTablesTab.setGraphic(view);
    }
    catch (IOException e) {
      LOG.error("failed to load VPS table tab: " + e.getMessage(), e);
    }


    tabPane.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, t1) -> {
      refreshTabSelection(t1);
    });

    tablesSideBarController.setVisible(true);
    repositorySideBarController.setVisible(false);
    vpsTablesSidebarController.setVisible(false);

    Platform.runLater(() -> {
      Studio.stage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
        public void handle(KeyEvent ke) {
          if (ke.getCode() == KeyCode.F3) {
            toggleSidebar();
          }
        }
      });
    });

    sidePanelRoot.managedProperty().bindBidirectional(sidePanelRoot.visibleProperty());
  }

  public void setSidebarVisible(boolean b) {
    toggleSidebarBtn.setDisable(getTablesSideBarController().isEmpty());

    if (b && sidePanelRoot.isVisible()) {
      return;
    }
    if (!b && !sidePanelRoot.isVisible()) {
      return;
    }

    sidebarVisible = b;
    if (!sidebarVisible) {
      TranslateTransition t = TransitionUtil.createTranslateByXTransition(sidePanelRoot, UIDefaults.SCROLL_OFFSET, 612);
      t.onFinishedProperty().set(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
          sidePanelRoot.setVisible(false);
          FontIcon icon = WidgetFactory.createIcon("mdi2a-arrow-expand-left");
          toggleSidebarBtn.setGraphic(icon);
        }
      });
      t.play();
    }
    else {
      TranslateTransition t = TransitionUtil.createTranslateByXTransition(sidePanelRoot, UIDefaults.SCROLL_OFFSET, -612);
      t.onFinishedProperty().set(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
          sidePanelRoot.setVisible(true);
          FontIcon icon = WidgetFactory.createIcon("mdi2a-arrow-expand-right");
          toggleSidebarBtn.setGraphic(icon);
        }
      });
      t.play();
    }
  }


  private void refreshTabSelection(Number t1) {
    Platform.runLater(() -> {
      if (t1.intValue() == 0) {
        tableOverviewController.setVisible(true);
        repositorySideBarController.setVisible(false);
        vpsTablesSidebarController.setVisible(false);
        tableOverviewController.onViewActivated(null);
        root.setRight(sidePanelRoot);
        toggleSidebarBtn.setDisable(false);
      }
      else if (t1.intValue() == 1) {
        tableOverviewController.setVisible(false);
        repositorySideBarController.setVisible(false);
        vpsTablesSidebarController.setVisible(false);
        backglassManagerController.onViewActivated(null);
        root.setRight(null);
        toggleSidebarBtn.setDisable(true);
      }
      else if (t1.intValue() == 2) {
        tableOverviewController.setVisible(false);
        repositorySideBarController.setVisible(false);
        vpsTablesSidebarController.setVisible(true);
        vpsTablesController.onViewActivated(null);
        root.setRight(sidePanelRoot);
        toggleSidebarBtn.setDisable(false);
      }
      else if (t1.intValue() == 3) {
        tableOverviewController.setVisible(false);
        repositorySideBarController.setVisible(false);
        vpsTablesSidebarController.setVisible(false);
        alxController.onViewActivated(null);
        root.setRight(null);
        toggleSidebarBtn.setDisable(true);
      }
      else {
        tableOverviewController.setVisible(false);
        repositorySideBarController.setVisible(true);
        vpsTablesSidebarController.setVisible(false);
        repositoryController.onViewActivated(null);
        root.setRight(sidePanelRoot);
        toggleSidebarBtn.setDisable(false);
      }
    });
  }

  public TablesSidebarController getTablesSideBarController() {
    return tablesSideBarController;
  }

  public RepositorySidebarController getRepositorySideBarController() {
    return repositorySideBarController;
  }

  public TableOverviewController getTableOverviewController() {
    return tableOverviewController;
  }

  public TablesAssetViewSidebarController getAssetViewSideBarController() {
    return assetViewSideBarController;
  }

  public VpsTablesSidebarController getVpsTablesSidebarController() {
    return vpsTablesSidebarController;
  }

  public void switchToBackglassManagerTab(GameRepresentation game) {
    backglassManagerController.selectGame(game);
    tabPane.getSelectionModel().select(backglassManagerTab);
  }

  @Override
  public void jobFinished(@NonNull JobFinishedEvent event) {
    JobType jobType = event.getJobType();
    if (jobType.equals(JobType.TABLE_BACKUP) || jobType.equals(JobType.ARCHIVE_INSTALL)) {
      Platform.runLater(() -> {
        repositoryController.doReload();
      });
    }
    else if (jobType.equals(JobType.PUP_INSTALL) || jobType.equals(JobType.ALTSOUND_INSTALL) || jobType.equals(JobType.ALTCOLOR_INSTALL)) {
      Platform.runLater(() -> {
        if (event.getGameId() > 0) {
          GameRepresentation game = Studio.client.getGameService().getGame(event.getGameId());
          String rom = null;
          if (game != null) {
            rom = game.getRom();
          }
          EventManager.getInstance().notifyTableChange(event.getGameId(), rom);
        }
        else {
          this.tableOverviewController.onReload();
        }
      });
    }
    else if (jobType.equals(JobType.TABLE_IMPORT)) {
      Platform.runLater(() -> {
        tableOverviewController.refreshFilterId();
        tableOverviewController.onReload();
      });
    }
    else if (jobType.equals(JobType.POV_INSTALL)
        || jobType.equals(JobType.POPPER_MEDIA_INSTALL)
        || jobType.equals(JobType.DIRECTB2S_INSTALL)
    ) {
      Platform.runLater(() -> {
        if (event.getGameId() > 0) {
          EventManager.getInstance().notifyTableChange(event.getGameId(), null);
        }
        else {
          this.tableOverviewController.onReload();
        }
      });
    }
  }

  @Override
  public void tableChanged(int id, String rom, String gameName) {
    if (!StringUtils.isEmpty(rom)) {
      List<GameRepresentation> gamesByRom = client.getGameService().getGamesByRom(rom);
      for (GameRepresentation g : gamesByRom) {
        GameRepresentation game = client.getGameService().getGame(g.getId());
        this.tableOverviewController.reload(game);
      }
    }

    if (!StringUtils.isEmpty(gameName)) {
      List<GameRepresentation> gamesByRom = client.getGameService().getGamesByGameName(gameName);
      for (GameRepresentation g : gamesByRom) {
        GameRepresentation game = client.getGameService().getGame(g.getId());
        this.tableOverviewController.reload(game);
      }
    }

    if (id > 0) {
      GameRepresentation refreshedGame = client.getGameService().getGame(id);
      this.tableOverviewController.reload(refreshedGame);
    }
    else {
      GameRepresentation selection = this.tableOverviewController.getSelection();
      if (selection != null) {
        this.tableOverviewController.reload(selection);
      }
    }
  }

  public StackPane getEditorRootStack() {
    return editorRootStack;
  }

  @Override
  public void preferencesChanged(PreferenceType preferenceType) {
    if (preferenceType.equals(PreferenceType.serverSettings) || preferenceType.equals(PreferenceType.uiSettings) || preferenceType.equals(PreferenceType.validationSettings)) {
      Platform.runLater(() -> {
        this.tableOverviewController.onReload();
      });
    }
  }

  public TabPane getTabPane() {
    return tabPane;
  }

  @Override
  public void tablesChanged() {
    preferencesChanged(PreferenceType.uiSettings);
  }

  @Override
  public void tableUploaded(UploadDescriptor uploadeDescription) {
    Platform.runLater(() -> {
        tableOverviewController.refreshUploadResult(uploadeDescription);
    });
  }
}

package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.fx.pausemenu.PauseMenuUIDefaults;
import de.mephisto.vpin.commons.utils.TransitionUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.backups.BackupsController;
import de.mephisto.vpin.ui.backups.BackupsSidebarController;
import de.mephisto.vpin.ui.backglassmanager.BackglassManagerController;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.JobFinishedEvent;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.preferences.PreferenceType;
import de.mephisto.vpin.ui.recorder.RecorderController;
import de.mephisto.vpin.ui.tables.alx.AlxController;
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;

public class TablesController implements Initializable, StudioFXController, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(TablesController.class);

  private static final int TAB_TABLE = 0;
  private static final int TAB_BACKGLASS = 1;
  private static final int TAB_VPS = 2;
  private static final int TAB_STATISTICS = 3;
  private static final int TAB_RECORDER = 4;
  private static final int TAB_REPOSITORY = 5;

  private TableOverviewController tableOverviewController;
  private BackglassManagerController backglassManagerController;
  private VpsTablesController vpsTablesController;
  private AlxController alxController;
  private BackupsController backupsController;

  public static TablesController INSTANCE;

  public boolean isTablesSelected() {
    return tabPane.getSelectionModel().getSelectedIndex() == 0;
  }

  public boolean isBackglassManagerSelected() {
    return tabPane.getSelectionModel().getSelectedIndex() == 1;
  }

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
  private Tab recorderTab;

  @FXML
  private TablesSidebarController tablesSideBarController; //fxml magic! Not unused

  @FXML
  private BackupsSidebarController backupsSideBarController; //fxml magic! Not unused

  @FXML
  private VpsTablesSidebarController vpsTablesSidebarController; //fxml magic! Not unused

  @FXML
  private RecorderController recorderController; //fxml magic! Not unused

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
    refreshTabSelection(tabPane.getSelectionModel().getSelectedIndex(), tabPane.getSelectionModel().getSelectedIndex());
    if (options != null) {
      tabPane.getSelectionModel().select(0);
      tableOverviewController.selectGameInModel(options.getGameId());
    }
  }


  @FXML
  private void onTableSettings() {
    int selectedTab = getSelectedTab();
    switch (selectedTab) {
      case TAB_TABLE: {
        PreferencesController.open("settings_client");
        break;
      }
      case TAB_BACKGLASS: {
        PreferencesController.open("backglass");
        break;
      }
      case TAB_VPS: {
        PreferencesController.open("vps");
        break;
      }
      case TAB_STATISTICS: {
        PreferencesController.open("settings_client");
        break;
      }
      case TAB_REPOSITORY: {
        PreferencesController.open("repositories");
        break;
      }
      case TAB_RECORDER: {
        PreferencesController.open("validators_screens");
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
    uiSettings.setTablesSidebarVisible(sidebarVisible);
    client.getPreferenceService().setJsonPreference(uiSettings, true);

    setSidebarVisible(sidebarVisible);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    INSTANCE = this;

    NavigationController.setInitialController(NavigationItem.Tables, this, root);
    EventManager.getInstance().addListener(this);
    sidePanelRoot = root.getRight();

    if (!Features.STATISTICS_ENABLED) {
      tabPane.getTabs().remove(tablesStatisticsTab);
    }
    if (!Features.BACKUPS_ENABLED) {
      tabPane.getTabs().remove(tableRepositoryTab);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TableOverviewController.class.getResource("scene-tables-overview.fxml"));
      Parent tablesRoot = loader.load();
      tableOverviewController = loader.getController();
      tableOverviewController.setRootController(this);
      tablesSideBarController.setRootController(this);
      tablesSideBarController.loadSidePanels();
      tablesTab.setContent(tablesRoot);
    }
    catch (IOException e) {
      LOG.error("failed to load table overview: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(BackglassManagerController.class.getResource("scene-directb2s-admin.fxml"));
      Parent directb2sRoot = loader.load();
      backglassManagerController = loader.getController();
      backglassManagerController.setRootController(this);
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
      alxController.setTablesController(this);
      tablesStatisticsTab.setContent(repositoryRoot);
    }
    catch (IOException e) {
      LOG.error("failed to load statistic tab: " + e.getMessage(), e);
    }

    try {
      if (Features.BACKUPS_ENABLED) {
        FXMLLoader loader = new FXMLLoader(BackupsController.class.getResource("scene-backups.fxml"));
        Parent repositoryRoot = loader.load();
        backupsController = loader.getController();
        backupsController.setRootController(this);
        tableRepositoryTab.setContent(repositoryRoot);
      }
      else {
        tabPane.getTabs().remove(tableRepositoryTab);
      }
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

    if (Features.RECORDER && !client.getRecorderService().getRecordingScreens().isEmpty()) {
      try {
        FXMLLoader loader = new FXMLLoader(RecorderController.class.getResource("scene-recorder.fxml"));
        Parent repositoryRoot = loader.load();
        recorderController = loader.getController();
        recorderController.setRootController(this);
        recorderTab.setContent(repositoryRoot);
      }
      catch (IOException e) {
        LOG.error("failed to load VPS table tab: " + e.getMessage(), e);
      }
    }
    else {
      tabPane.getTabs().remove(recorderTab);
    }


    tabPane.getSelectionModel().selectedIndexProperty().addListener((observableValue, oldValue, newValue) -> {
      refreshTabSelection(oldValue, newValue);
    });

    tablesSideBarController.setVisible(true);
    backupsSideBarController.setVisible(false);
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
      TranslateTransition t = TransitionUtil.createTranslateByXTransition(sidePanelRoot, PauseMenuUIDefaults.SCROLL_OFFSET, 612);
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
      sidePanelRoot.setVisible(true);
      TranslateTransition t = TransitionUtil.createTranslateByXTransition(sidePanelRoot, PauseMenuUIDefaults.SCROLL_OFFSET, -612);
      t.onFinishedProperty().set(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
          FontIcon icon = WidgetFactory.createIcon("mdi2a-arrow-expand-right");
          toggleSidebarBtn.setGraphic(icon);
        }
      });
      t.play();
    }
  }


  private void refreshTabSelection(Number oldValue, Number newValue) {
    int oldTab = getSelectedTab(oldValue.intValue());
    StudioFXController controller = getController(oldTab);
    if (controller != null) {
      controller.onViewDeactivated();
    }

    int selectedTab = getSelectedTab(newValue.intValue());
    Platform.runLater(() -> {
      tableOverviewController.setVisible(selectedTab == TAB_TABLE);
      backupsSideBarController.setVisible(selectedTab == TAB_REPOSITORY);
      vpsTablesSidebarController.setVisible(selectedTab == TAB_VPS);

      if (selectedTab == TAB_TABLE) {
        tableOverviewController.onViewActivated(null);
        root.setRight(sidePanelRoot);
        toggleSidebarBtn.setDisable(false);
      }
      else if (selectedTab == TAB_BACKGLASS) {
        backglassManagerController.onViewActivated(null);
        root.setRight(null);
        toggleSidebarBtn.setDisable(true);
      }
      else if (selectedTab == TAB_VPS) {
        vpsTablesController.onViewActivated(null);
        root.setRight(sidePanelRoot);
        toggleSidebarBtn.setDisable(false);
      }
      else if (selectedTab == TAB_STATISTICS) {
        alxController.onViewActivated(null);
        root.setRight(null);
        toggleSidebarBtn.setDisable(true);
      }
      else if (selectedTab == TAB_REPOSITORY) {
        backupsController.onViewActivated(null);
        root.setRight(sidePanelRoot);
        toggleSidebarBtn.setDisable(false);
      }
      else if (selectedTab == TAB_RECORDER) {
        recorderController.onViewActivated(null);
        root.setRight(null);
        toggleSidebarBtn.setDisable(true);
      }
    });
  }

  private int getSelectedTab() {
    int selectedIndex = tabPane.getSelectionModel().getSelectedIndex();
    return getSelectedTab(selectedIndex);
  }

  /**
   * Convert the selected tab index into TAB id, managing invisible tabs
   */
  private int getSelectedTab(int index) {
    int cnt = 0;
    if (tabPane.getTabs().contains(tablesTab) && cnt++ == index) {
      return TAB_TABLE;
    }
    if (tabPane.getTabs().contains(backglassManagerTab) && cnt++ == index) {
      return TAB_BACKGLASS;
    }
    if (tabPane.getTabs().contains(vpsTablesTab) && cnt++ == index) {
      return TAB_VPS;
    }
    if (tabPane.getTabs().contains(tablesStatisticsTab) && cnt++ == index) {
      return TAB_STATISTICS;
    }
    if (tabPane.getTabs().contains(tableRepositoryTab) && cnt++ == index) {
      return TAB_REPOSITORY;
    }
    if (tabPane.getTabs().contains(recorderTab) && cnt++ == index) {
      return TAB_RECORDER;
    }
    // should not happen
    return -1;
  }

  public TablesSidebarController getTablesSideBarController() {
    return tablesSideBarController;
  }

  public BackupsSidebarController getRepositorySideBarController() {
    return backupsSideBarController;
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
    if (jobType.equals(JobType.PUP_INSTALL) || jobType.equals(JobType.ALTSOUND_INSTALL) || jobType.equals(JobType.ALTCOLOR_INSTALL)) {
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
          this.tableOverviewController.doReload();
        }
      });
    }
    else if (jobType.equals(JobType.TABLE_IMPORT)) {
      Platform.runLater(() -> {
        tableOverviewController.refreshFilters();
        tableOverviewController.doReload();
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
          this.tableOverviewController.doReload();
        }
      });
    }
  }

  @Override
  public void tableChanged(int id, String rom, String gameName) {
    List<Integer> refreshList = new ArrayList<>();
    if (!StringUtils.isEmpty(rom)) {
      List<GameRepresentation> gamesByRom = client.getGameService().getGamesByRom(rom);
      for (GameRepresentation g : gamesByRom) {
        refreshList.add(g.getId());
      }
    }

    if (!StringUtils.isEmpty(gameName)) {
      List<GameRepresentation> gamesByRom = client.getGameService().getGamesByGameName(gameName);
      for (GameRepresentation g : gamesByRom) {
        if (!refreshList.contains(g.getId())) {
          refreshList.add(g.getId());
        }
      }
    }

    if (id > 0) {
      if (!refreshList.contains(id)) {
        refreshList.add(id);
      }
    }
    else {
      GameRepresentation selection = this.tableOverviewController.getSelection();
      if (selection != null && !refreshList.contains(selection)) {
        refreshList.add(selection.getId());
      }
    }

    for (Integer gameId : refreshList) {
      GameRepresentation game = client.getGameService().getGame(gameId);
      if (game != null) {
        this.tableOverviewController.reloadItem(game);
        if (recorderController != null) {
          this.recorderController.reloadItem(game);
        }
      }
    }

    // also refresh playlists as the addition/modification of tables may impact them
    refreshPlaylists();
  }

  public void refreshPlaylists() {
    this.tableOverviewController.refreshPlaylists();
    this.backglassManagerController.refreshPlaylists();
  }

  public StackPane getEditorRootStack() {
    return editorRootStack;
  }

  public boolean isTabStatisticsSelected() {
    return isTabSelected(TAB_STATISTICS);
  }

  private boolean isTabSelected(int tab) {
    return getSelectedTab() == tab;
  }

  @Override
  public void preferencesChanged(PreferenceType preferenceType) {
    if (preferenceType.equals(PreferenceType.serverSettings)
        || preferenceType.equals(PreferenceType.uiSettings)
        || preferenceType.equals(PreferenceType.validationSettings)
        || preferenceType.equals(PreferenceType.vpsSettings)
        || preferenceType.equals(PreferenceType.competitionSettings)) {
      Platform.runLater(() -> {
        this.tableOverviewController.onReload();
      });
    }
  }

  @Override
  public void tablesChanged() {
    //ignore
  }

  @Override
  public void tableUploaded(UploadDescriptor uploadeDescription) {
    Platform.runLater(() -> {
      tableOverviewController.refreshUploadResult(uploadeDescription);
    });
  }

  @Override
  public void onKeyEvent(KeyEvent ke) {
    if (ke.getCode() == KeyCode.F2) {
      tabPane.getSelectionModel().select(tablesTab);
    }
    else if (ke.getCode() == KeyCode.F3) {
      tabPane.getSelectionModel().select(backglassManagerTab);
    }
    else if (ke.getCode() == KeyCode.F4) {
      tabPane.getSelectionModel().select(vpsTablesTab);
    }
    else if (ke.getCode() == KeyCode.F5) {
      tabPane.getSelectionModel().select(tablesStatisticsTab);
    }
    else if (ke.getCode() == KeyCode.F6) {
      tabPane.getSelectionModel().select(tableRepositoryTab);
    }
    else if (ke.getCode() == KeyCode.F7 && Features.RECORDER) {
      tabPane.getSelectionModel().select(recorderTab);
    }

    if (ke.isConsumed()) {
      return;
    }

    int selectedTab = getSelectedTab();
    StudioFXController activeController = getController(selectedTab);
    if (activeController != null) {
      activeController.onKeyEvent(ke);
    }
  }

  private StudioFXController getController(int tab) {
    switch (tab) {
      case TAB_TABLE: {
        return tableOverviewController;
      }
      case TAB_BACKGLASS: {
        return backglassManagerController;
      }
      case TAB_VPS: {
        return vpsTablesController;
      }
      case TAB_STATISTICS: {
        return alxController;
      }
      case TAB_REPOSITORY: {
        return backupsController;
      }
      case TAB_RECORDER: {
        return recorderController;
      }
    }
    return null;
  }
}

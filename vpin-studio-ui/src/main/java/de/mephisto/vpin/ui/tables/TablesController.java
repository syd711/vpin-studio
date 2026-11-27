package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.fx.pausemenu.PauseMenuUIDefaults;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.TransitionUtil;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.backups.BackupDescriptorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.preferences.VPXZSettings;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.backglassmanager.BackglassManagerController;
import de.mephisto.vpin.ui.backups.BackupsController;
import de.mephisto.vpin.ui.backups.BackupsSidebarController;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.JobFinishedEvent;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.preferences.PreferenceType;
import de.mephisto.vpin.ui.tables.alx.AlxController;
import de.mephisto.vpin.ui.vps.VpsTablesController;
import de.mephisto.vpin.ui.vps.VpsTablesSidebarController;
import de.mephisto.vpin.ui.vpxz.VPXZController;
import de.mephisto.vpin.ui.vpxz.VPXZSidebarController;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;
import de.mephisto.vpin.commons.utils.i18n.Messages;

public class TablesController implements Initializable, StudioFXController, StudioEventListener, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final int TAB_TABLE = 0;
  private static final int TAB_BACKGLASS = 1;
  private static final int TAB_VPS = 2;
  private static final int TAB_STATISTICS = 3;
  private static final int TAB_BACKUPS = 4;
  private static final int TAB_VPXZ = 5;

  private TableOverviewController tableOverviewController;
  private BackglassManagerController backglassManagerController;
  private VpsTablesController vpsTablesController;
  private AlxController alxController;
  private BackupsController backupsController;
  private VPXZController vpxzController;

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
  private Tab tableBackupsTab;

  @FXML
  private Tab tablesStatisticsTab;

  @FXML
  private Tab vpsTablesTab;

  @FXML
  private Tab vpxMobileTab;

  @FXML
  private TablesSidebarController tablesSideBarController; //fxml magic! Not unused

  @FXML
  private BackupsSidebarController backupsSideBarController; //fxml magic! Not unused

  @FXML
  private VPXZSidebarController vpxzSidebarController; //fxml magic! Not unused

  @FXML
  private VpsTablesSidebarController vpsTablesSidebarController; //fxml magic! Not unused

  @FXML
  private TablesAssetViewSidebarController assetViewSideBarController; //fxml magic! Not unused

  @FXML
  private StackPane editorRootStack;

  /**
   * Separator, preferences and "show details panel" button, appended to the first toolbar row of the active page.
   */
  @FXML
  private HBox viewActions;

  @FXML
  private Button showSidebarBtn;

  @FXML
  private HBox sidebarHeader;

  @FXML
  private Label sidebarTitleLabel;

  private Node sidePanelRoot;
  private boolean sidebarVisible = true;

  private final BooleanProperty sidebarHidden = new SimpleBooleanProperty(false);
  private final BooleanProperty sidebarAvailable = new SimpleBooleanProperty(true);

  /**
   * The toolbar row that currently holds the view actions.
   */
  private ObservableList<Node> viewActionsOwner;

  /**
   * The name of the item selected in each sidebar, keyed by the sidebar controller.
   */
  private final Map<Object, String> sidebarTitles = new HashMap<>();

  /**
   * The tabs are the pages of this view, they are selected through the navigation.
   */
  public void showPage(NavigationItem item, @Nullable NavigationOptions options) {
    Tab tab = options != null ? tablesTab : getTab(item);
    if (tabPane.getTabs().contains(tab) && tabPane.getSelectionModel().getSelectedItem() != tab) {
      // the selection listener activates the page
      tabPane.getSelectionModel().select(tab);
    }
    else {
      int selectedIndex = tabPane.getSelectionModel().getSelectedIndex();
      refreshTabSelection(selectedIndex, selectedIndex);
    }

    if (options != null) {
      tableOverviewController.selectGameInModel(options.getGameId());
    }
  }

  private Tab getTab(NavigationItem item) {
    return switch (item) {
      case Backglasses -> backglassManagerTab;
      case VpsTables -> vpsTablesTab;
      case TableStatistics -> tablesStatisticsTab;
      case TableBackups -> tableBackupsTab;
      case VpxMobile -> vpxMobileTab;
      default -> tablesTab;
    };
  }

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
      case TAB_BACKUPS: {
        PreferencesController.open("backups");
        break;
      }
      case TAB_VPXZ: {
        PreferencesController.open("vpxz");
        break;
      }
      default: {
        PreferencesController.open("settings_client");
      }
    }
  }

  @FXML
  public void toggleSidebar() {
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
      tabPane.getTabs().remove(tableBackupsTab);
    }

    VPXZSettings vpxzSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.VPXZ_SETTINGS, VPXZSettings.class);
    if (!Features.VPXZ_ENABLED || !vpxzSettings.isEnabled()) {
      tabPane.getTabs().remove(vpxMobileTab);
    }

    try {
      FXMLLoader loader = new FXMLLoader(TableOverviewController.class.getResource("scene-tables-overview.fxml"));
      loader.setResources(Messages.getBundle());
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
      loader.setResources(Messages.getBundle());
      Parent directb2sRoot = loader.load();
      backglassManagerController = loader.getController();
      backglassManagerController.setRootController(this);
      backglassManagerTab.setContent(directb2sRoot);
    }
    catch (IOException e) {
      LOG.error("failed to load table overview: " + e.getMessage(), e);
    }


    try {
      FXMLLoader loader = new FXMLLoader(AlxController.class.getResource("scene-alx.fxml"));
      loader.setResources(Messages.getBundle());
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
        loader.setResources(Messages.getBundle());
        Parent repositoryRoot = loader.load();
        backupsController = loader.getController();
        backupsController.setRootController(this);
        tableBackupsTab.setContent(repositoryRoot);
      }
      else {
        tabPane.getTabs().remove(tableBackupsTab);
      }
    }
    catch (IOException e) {
      LOG.error("failed to load repository tab: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(VPXZController.class.getResource("scene-vpxz.fxml"));
      loader.setResources(Messages.getBundle());
      Parent vpxMobileRoot = loader.load();
      vpxzController = loader.getController();
      vpxzController.setRootController(this);
      vpxMobileTab.setContent(vpxMobileRoot);
    }
    catch (IOException e) {
      LOG.error("failed to load VPX Mobile tab: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(VpsTablesController.class.getResource("scene-vps-tables.fxml"));
      loader.setResources(Messages.getBundle());
      Parent repositoryRoot = loader.load();
      vpsTablesController = loader.getController();
      vpsTablesController.setRootController(this);
      vpsTablesTab.setContent(repositoryRoot);
    }
    catch (IOException e) {
      LOG.error("failed to load VPS table tab: " + e.getMessage(), e);
    }

    tabPane.getSelectionModel().selectedIndexProperty().addListener((observableValue, oldValue, newValue) -> {
      refreshTabSelection(oldValue, newValue);
    });

    tablesSideBarController.setVisible(true);
    backupsSideBarController.setVisible(false);
    vpxzSidebarController.setVisible(false);
    vpsTablesSidebarController.setVisible(false);

    Platform.runLater(() -> {
      if (Studio.stage != null && Studio.stage.getScene() != null) {
        Studio.stage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
          public void handle(KeyEvent ke) {
            if (ke.getCode() == KeyCode.F3) {
              toggleSidebar();
            }
          }
        });
      }
    });

    sidePanelRoot.managedProperty().bindBidirectional(sidePanelRoot.visibleProperty());

    showSidebarBtn.visibleProperty().bind(sidebarHidden.and(sidebarAvailable));
    showSidebarBtn.managedProperty().bind(showSidebarBtn.visibleProperty());
    sidebarHeader.managedProperty().bind(sidebarHeader.visibleProperty());

    client.getPreferenceService().addListener(this);
  }

  public void setSidebarVisible(boolean b) {
    refreshSidebarAvailable();

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
          sidebarHidden.set(true);
        }
      });
      t.play();
    }
    else {
      sidebarHidden.set(false);
      sidePanelRoot.setVisible(true);
      TranslateTransition t = TransitionUtil.createTranslateByXTransition(sidePanelRoot, PauseMenuUIDefaults.SCROLL_OFFSET, -612);
      t.play();
    }
  }

  /**
   * The "show details panel" button is only offered on pages with a (non-empty) sidebar.
   */
  private void refreshSidebarAvailable() {
    int selectedTab = getSelectedTab();
    boolean hasSidebar = selectedTab == TAB_VPS || selectedTab == TAB_BACKUPS || selectedTab == TAB_VPXZ
        || (selectedTab == TAB_TABLE && !getTablesSideBarController().isEmpty());
    sidebarAvailable.set(hasSidebar);
  }

  /**
   * Moves the view actions (preferences, show details panel) to the end of the first toolbar row of the given page.
   */
  private void attachViewActions(int tab) {
    ObservableList<Node> items = getToolbarItems(tab);
    if (items == null || items == viewActionsOwner) {
      return;
    }
    if (viewActionsOwner != null) {
      viewActionsOwner.remove(viewActions);
    }
    // in a ToolBar the actions are pushed to the right end, in the HBox row of the backglass manager
    // the search field already takes the free space
    HBox.setHgrow(viewActions, tab == TAB_BACKGLASS ? Priority.NEVER : Priority.ALWAYS);
    items.add(viewActions);
    viewActionsOwner = items;
  }

  private ObservableList<Node> getToolbarItems(int tab) {
    return switch (tab) {
      case TAB_TABLE -> tableOverviewController.getToolbarItems();
      case TAB_BACKGLASS -> backglassManagerController.getToolbarItems();
      case TAB_VPS -> vpsTablesController.getToolbarItems();
      case TAB_STATISTICS -> alxController.getToolbarItems();
      case TAB_BACKUPS -> backupsController != null ? backupsController.getToolbarItems() : null;
      case TAB_VPXZ -> vpxzController.getToolbarItems();
      default -> null;
    };
  }

  public HBox getViewActions() {
    return viewActions;
  }

  /**
   * Sets the name of the item selected in the given sidebar, shown in the header of the details panel.
   */
  public void setSidebarTitle(Object sidebarController, @Nullable String title) {
    sidebarTitles.put(sidebarController, title);
    refreshSidebarTitle();
  }

  private void refreshSidebarTitle() {
    Object sidebarController = switch (getSelectedTab()) {
      case TAB_TABLE -> tablesSideBarController;
      case TAB_BACKUPS -> backupsSideBarController;
      case TAB_VPXZ -> vpxzSidebarController;
      default -> null;
    };
    String title = sidebarController != null ? sidebarTitles.get(sidebarController) : null;
    sidebarTitleLabel.setText(StringUtils.defaultIfBlank(title, "-"));
  }

  private void refreshTabSelection(Number oldValue, Number newValue) {
    int oldTab = getSelectedTab(oldValue.intValue());
    StudioFXController controller = getController(oldTab);
    if (controller != null) {
      controller.onViewDeactivated();
    }

    int selectedTab = getSelectedTab(newValue.intValue());
    Platform.runLater(() -> {
      attachViewActions(selectedTab);
      // the VPS sidebar comes with its own header
      sidebarHeader.setVisible(selectedTab != TAB_VPS);
      refreshSidebarTitle();
      refreshSidebarAvailable();

      tableOverviewController.setVisible(selectedTab == TAB_TABLE);
      backupsSideBarController.setVisible(selectedTab == TAB_BACKUPS);
      vpxzSidebarController.setVisible(selectedTab == TAB_VPXZ);
      vpsTablesSidebarController.setVisible(selectedTab == TAB_VPS);

      if (selectedTab == TAB_TABLE) {
        tableOverviewController.onViewActivated(null);
        root.setRight(sidePanelRoot);
      }
      else if (selectedTab == TAB_BACKGLASS) {
        backglassManagerController.onViewActivated(null);
        root.setRight(null);
      }
      else if (selectedTab == TAB_VPS) {
        vpsTablesController.onViewActivated(null);
        root.setRight(sidePanelRoot);
      }
      else if (selectedTab == TAB_STATISTICS) {
        alxController.onViewActivated(null);
        root.setRight(null);
      }
      else if (selectedTab == TAB_BACKUPS) {
        backupsController.onViewActivated(null);
        root.setRight(sidePanelRoot);
      }
      else if (selectedTab == TAB_VPXZ) {
        vpxzController.onViewActivated(null);
        root.setRight(sidePanelRoot);
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
    if (tabPane.getTabs().contains(tableBackupsTab) && cnt++ == index) {
      return TAB_BACKUPS;
    }
    if (tabPane.getTabs().contains(vpxMobileTab) && cnt++ == index) {
      return TAB_VPXZ;
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

  public VPXZSidebarController getVpxzSidebarController() {
    return vpxzSidebarController;
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
    NavigationController.navigateTo(NavigationItem.Backglasses);
    backglassManagerController.selectGame(game);
  }

  public void switchToBackupsTab(BackupDescriptorRepresentation backup) {
    NavigationController.navigateTo(NavigationItem.TableBackups);
    backupsController.selectBackup(backup);
  }

  @Override
  public void jobFinished(@NonNull JobFinishedEvent event) {
    JobType jobType = event.getJobType();
    if (jobType.equals(JobType.PUP_INSTALL) || jobType.equals(JobType.ALTSOUND_INSTALL) || jobType.equals(JobType.ALTSOUND_GENERATE) || jobType.equals(JobType.ALTCOLOR_INSTALL)) {
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
          // force cache refresh
          client.getFrontendService().clearCache(event.getGameId());
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
      if (selection != null && !refreshList.contains(selection.getId())) {
        refreshList.add(selection.getId());
      }
    }

    for (Integer gameId : refreshList) {
      JFXFuture.supplyAsync(() -> client.getGameService().getGame(gameId))
          .thenAcceptLater(game -> {
            if (game != null) {
              this.tableOverviewController.reloadItem(game);
            }
            else {
              Optional<GameRepresentationModel> first = this.tableOverviewController.getTableView().getItems().stream().filter(t -> t.getGameId() == gameId).findFirst();
              if (first.isPresent()) {
                this.tableOverviewController.getTableView().getItems().remove(first.get());
                this.tableOverviewController.getTableView().refresh();
              }
            }
          });
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
    // F2 - F8 are navigation shortcuts, see NavigationController.handleShortcut
    int selectedTab = getSelectedTab();
    StudioFXController activeController = getController(selectedTab);
    if (activeController != null) {
      activeController.onKeyEvent(ke);
    }
  }

  public BackupsController getTableBackupsController() {
    return backupsController;
  }

  public VPXZController getVPXMobileController() {
    return vpxzController;
  }

  private StudioFXController getController(int tab) {
    return switch (tab) {
      case TAB_TABLE -> tableOverviewController;
      case TAB_BACKGLASS -> backglassManagerController;
      case TAB_VPS -> vpsTablesController;
      case TAB_STATISTICS -> alxController;
      case TAB_BACKUPS -> backupsController;
      case TAB_VPXZ -> vpxzController;
      default -> null;
    };
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (PreferenceNames.VPXZ_SETTINGS.equalsIgnoreCase(key)) {
      VPXZSettings vpxzSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.VPXZ_SETTINGS, VPXZSettings.class);
      if (vpxzSettings.isEnabled()) {
        // always the last page
        if (!tabPane.getTabs().contains(vpxMobileTab)) {
          tabPane.getTabs().add(vpxMobileTab);
        }
      }
      else {
        tabPane.getTabs().remove(vpxMobileTab);
      }
    }
  }
}

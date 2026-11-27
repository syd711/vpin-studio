package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.commons.utils.localsettings.LocalUISettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.preferences.VPXZSettings;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.achievements.AchievementsController;
import de.mephisto.vpin.ui.cards.HighscoreCardsController;
import de.mephisto.vpin.ui.competitions.CompetitionsController;
import de.mephisto.vpin.ui.components.ComponentsController;
import de.mephisto.vpin.ui.components.doftester.DOFTesterController;
import de.mephisto.vpin.ui.components.emulators.EmulatorsController;
import de.mephisto.vpin.ui.components.screens.ScreensController;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.players.PlayersController;
import de.mephisto.vpin.ui.recorder.RecorderController;
import de.mephisto.vpin.ui.tables.TablesController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;

public class NavigationController implements Initializable, StudioEventListener, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(NavigationController.class);

  private static final String COLLAPSED_KEY = "navigationCollapsed";
  private static final String SELECTED_CLASS = "nav-item-selected";
  private static final String CAPTION_KEY = "caption";
  private static final String TOOLTIP_KEY = "navTooltip";

  @FXML
  private VBox sidebar;

  @FXML
  private HBox headerBox;

  @FXML
  private VBox headerText;

  @FXML
  private ImageView avatarView;

  @FXML
  private Label systemLabel;

  @FXML
  private VBox itemList;

  @FXML
  private VBox footer;

  @FXML
  private Pane tablesBtn;

  @FXML
  private Pane emulatorsBtn;

  @FXML
  private Pane backglassesBtn;

  @FXML
  private Pane vpsTablesBtn;

  @FXML
  private Pane statisticsBtn;

  @FXML
  private Pane backupsBtn;

  @FXML
  private Pane vpxMobileBtn;

  @FXML
  private Label studioCaption;

  @FXML
  private Pane dashboardBtn;

  @FXML
  private Pane playersBtn;

  @FXML
  private Pane competitionsBtn;

  @FXML
  private Pane screensBtn;

  @FXML
  private Pane dofTesterBtn;

  @FXML
  private Pane updatesBtn;

  @FXML
  private Pane cardsBtn;

  @FXML
  private Pane recorderBtn;

  @FXML
  private Pane collapseBtn;

  @FXML
  private FontIcon collapseIcon;

  @FXML
  private FontIcon updatesIcon;

  @FXML
  private Label updateBadge;

  private static ImageView staticAvatarView;
  private static Label staticSystemLabel;

  private static NavigationView activeNavigation;

  private final static Map<NavigationItem, NavigationView> navigationItemMap = new HashMap<>();

  private NavigationView highscoreCardsNavigationView;

  private boolean collapsed = false;
  private int updateCount = 0;

  // Add a public no-args constructor
  public NavigationController() {
  }

  public static NavigationItem getActiveNavigation() {
    if (activeNavigation != null) {
      return activeNavigation.getItem();
    }
    return null;
  }

  public static void refreshControllerCache() {
    navigateTo(activeNavigation.getItem());
    refreshAvatar();
  }

  public static void refreshViewCache() {
    Collection<NavigationView> values = navigationItemMap.values();
    for (NavigationView value : values) {
      value.setController(null);
    }
  }

  public static void setInitialController(NavigationItem navigationItem, StudioFXController controller, BorderPane root) {
    NavigationView navigationView = navigationItemMap.get(navigationItem);
    navigationView.setRoot(root);
    navigationView.setController(controller);

    activeNavigation = navigationView;
  }

  @FXML
  private void onDashboardClick() {
    navigateTo(NavigationItem.Achievements);
  }

  @FXML
  private void onHighscoreCardsClick() {
    navigateTo(NavigationItem.HighscoreCards);
  }

  @FXML
  private void onRecorderClick() {
    navigateTo(NavigationItem.MediaRecorder);
  }

  @FXML
  private void onTablesClick() {
    navigateTo(NavigationItem.Tables);
  }

  @FXML
  private void onEmulatorsClick() {
    navigateTo(NavigationItem.Emulators);
  }

  @FXML
  private void onBackglassesClick() {
    navigateTo(NavigationItem.Backglasses);
  }

  @FXML
  private void onVpsTablesClick() {
    navigateTo(NavigationItem.VpsTables);
  }

  @FXML
  private void onStatisticsClick() {
    navigateTo(NavigationItem.TableStatistics);
  }

  @FXML
  private void onBackupsClick() {
    navigateTo(NavigationItem.TableBackups);
  }

  @FXML
  private void onVpxMobileClick() {
    navigateTo(NavigationItem.VpxMobile);
  }

  @FXML
  private void onCompetitionsClick() {
    navigateTo(NavigationItem.Competitions);
  }

  @FXML
  private void onPlayersClick() {
    navigateTo(NavigationItem.Players);
  }

  @FXML
  private void onScreensClick() {
    navigateTo(NavigationItem.Screens);
  }

  @FXML
  private void onDofTesterClick() {
    navigateTo(NavigationItem.DofTester);
  }

  @FXML
  private void onUpdatesClick() {
    navigateTo(NavigationItem.Updates);
  }

  @FXML
  private void onCollapseClick() {
    setCollapsed(!collapsed);
    LocalUISettings.saveProperty(COLLAPSED_KEY, String.valueOf(collapsed));
  }

  /**
   * The collapsed sidebar only shows the icons, the labels are available as tooltips.
   */
  private void setCollapsed(boolean collapsed) {
    this.collapsed = collapsed;
    sidebar.getStyleClass().remove("nav-sidebar-collapsed");
    if (collapsed) {
      sidebar.getStyleClass().add("nav-sidebar-collapsed");
    }

    headerText.setVisible(!collapsed);
    headerText.setManaged(!collapsed);
    headerBox.setAlignment(collapsed ? Pos.CENTER : Pos.CENTER_LEFT);

    for (Node node : getEntries()) {
      if (node instanceof Label) {
        // section captions turn into small gaps between the groups
        ((Label) node).setText(collapsed ? "" : (String) node.getProperties().get(CAPTION_KEY));
      }
      else if (node instanceof HBox) {
        HBox item = (HBox) node;
        item.setAlignment(collapsed ? Pos.CENTER : Pos.CENTER_LEFT);
        Tooltip tooltip = (Tooltip) item.getProperties().get(TOOLTIP_KEY);
        if (tooltip != null) {
          Tooltip.uninstall(item, tooltip);
          if (collapsed) {
            Tooltip.install(item, tooltip);
          }
        }
        for (Node child : item.getChildren()) {
          if (child instanceof FontIcon || child == updateBadge) {
            continue;
          }
          child.setVisible(!collapsed);
          child.setManaged(!collapsed);
        }
      }
    }

    collapseIcon.setIconLiteral(collapsed ? "mdi2c-chevron-double-right" : "mdi2c-chevron-double-left");
    getItemLabel(collapseBtn).ifPresent(l -> l.setText(Messages.get(collapsed ? "navigation.expand" : "navigation.collapse")));
    refreshUpdateIndicator();
  }

  private List<Node> getEntries() {
    List<Node> nodes = new ArrayList<>(itemList.getChildren());
    nodes.addAll(footer.getChildren());
    return nodes;
  }

  private Optional<Label> getItemLabel(Pane item) {
    return item.getChildren().stream().filter(c -> c instanceof Label && c != updateBadge).map(c -> (Label) c).findFirst();
  }

  private void refreshUpdateIndicator() {
    updateBadge.setText(String.valueOf(updateCount));
    boolean showBadge = updateCount > 0 && !collapsed;
    updateBadge.setVisible(showBadge);
    updateBadge.setManaged(showBadge);

    // no room for the badge in the collapsed mode, the icon turns orange instead
    updatesIcon.getStyleClass().remove("nav-icon-update");
    if (updateCount > 0 && collapsed) {
      updatesIcon.getStyleClass().add("nav-icon-update");
    }
  }

  public static void navigateTo(NavigationItem item) {
    navigateTo(item, null);
  }

  public static StudioFXController getActiveNavigationController() {
    return getContentView(activeNavigation).getController();
  }

  /**
   * The view that holds root and controller: the host for the pages of the tables view, the view itself otherwise.
   */
  private static NavigationView getContentView(NavigationView view) {
    if (view != null && view.getHost() != null) {
      return navigationItemMap.get(view.getHost());
    }
    return view;
  }

  public static void navigateTo(NavigationItem item, NavigationOptions options) {
    NavigationView navigationView = navigationItemMap.get(item);
    NavigationView contentView = getContentView(navigationView);
    if (activeNavigation != null) {
      activeNavigation.getNavigationButton().getStyleClass().remove(SELECTED_CLASS);
      // switching between the pages of the tables view is handled by the tables controller
      NavigationView previousContentView = getContentView(activeNavigation);
      if (previousContentView != contentView && previousContentView.getController() != null) {
        previousContentView.getController().onViewDeactivated();
      }
    }

    activeNavigation = navigationView;

    if (!activeNavigation.getNavigationButton().getStyleClass().contains(SELECTED_CLASS)) {
      activeNavigation.getNavigationButton().getStyleClass().add(SELECTED_CLASS);
    }

    if (Studio.stage != null && Studio.stage.getScene() != null) {
      Node lookup = Studio.stage.getScene().lookup("#main");
      if (lookup instanceof BorderPane) {
        BorderPane main = (BorderPane) lookup;

        if (contentView.getController() == null) {
          try {
            FXMLLoader loader = new FXMLLoader(contentView.getControllerClass().getResource(contentView.getFxml()));
            loader.setResources(Messages.getBundle());
            Parent root = loader.load();
            StudioFXController controller = loader.<StudioFXController>getController();
            contentView.setController(controller);
            contentView.setRoot(root);
            // the tables controller registers itself as the active navigation while loading
            activeNavigation = navigationView;
          }
          catch (IOException e) {
            LOG.info("Failed to load main view: {}", e.getMessage(), e);
            return;
          }
        }

        if (main.getCenter() != contentView.getRoot()) {
          main.setCenter(contentView.getRoot());
        }

        StudioFXController controller = contentView.getController();
        if (controller instanceof TablesController) {
          ((TablesController) controller).showPage(item, options);
        }
        else {
          controller.onViewActivated(options);
        }
      }
    }
  }

  /**
   * F2 - F8 open the pages of the tables view and the media recorder.
   */
  public static void handleShortcut(KeyEvent ke) {
    if (ke.isShiftDown() || ke.isControlDown() || ke.isAltDown()) {
      return;
    }

    NavigationItem item = switch (ke.getCode()) {
      case F2 -> NavigationItem.Tables;
      case F3 -> NavigationItem.Backglasses;
      case F4 -> NavigationItem.VpsTables;
      case F5 -> NavigationItem.TableStatistics;
      case F6 -> NavigationItem.TableBackups;
      case F7 -> NavigationItem.VpxMobile;
      case F8 -> NavigationItem.MediaRecorder;
      default -> null;
    };
    if (item == null) {
      return;
    }
    // F3 toggles the sidebar of the competitions view
    if (item == NavigationItem.Backglasses && getActiveNavigation() == NavigationItem.Competitions) {
      return;
    }

    NavigationView view = navigationItemMap.get(item);
    if (view != null && view.getNavigationButton().isVisible()) {
      navigateTo(item);
      ke.consume();
    }
  }

  public static void refreshAvatar() {
    InputStream av = client.getAssetService().getAvatar(false);
    if (av == null) {
      av = ServerFX.class.getResourceAsStream("avatar-default.png");
    }
    Image image = new Image(av);

    PreferenceEntryRepresentation systemNameEntry = client.getPreferenceService().getPreference(PreferenceNames.SYSTEM_NAME);
    String name = UIDefaults.VPIN_NAME;
    if (systemNameEntry != null && !StringUtils.isEmpty(systemNameEntry.getValue())) {
      name = systemNameEntry.getValue();
    }

    try {
      if (staticAvatarView != null) {
        staticAvatarView.setImage(image);
      }
      if (staticSystemLabel != null) {
        staticSystemLabel.setText(name);
      }
    }
    catch (Exception e) {
      LOG.error("Failed to refresh avatar tile: {}", e.getMessage());
    }

    if (Studio.stage != null) {
      Studio.stage.setTitle("VPin Studio (" + Studio.getVersion() + ") - " + name);

      if (Studio.stage.getScene() != null) {
        Node header = Studio.stage.getScene().lookup("#header");
        if (header != null) {
          HeaderResizeableController dialogHeaderController = (HeaderResizeableController) header.getUserData();
          if (dialogHeaderController != null) {
            dialogHeaderController.setTitle(Studio.stage.getTitle());
          }
        }
      }
    }
  }

  public static void setBreadCrumb(List<String> crumbs) {
    Platform.runLater(() -> {
      if (Studio.stage == null || Studio.stage.getScene() == null) {
        return;
      }
      Node lookup = Studio.stage.getScene().lookup("#breadcrumb");
      if (lookup instanceof Label) {
        Label breadCrumb = (Label) lookup;
        String join = StringUtils.join(crumbs, " / ");
        breadCrumb.setText("/ " + join);
      }
    });
  }

  @Override
  public void thirdPartyVersionUpdated(@NonNull ComponentType type) {
    loadUpdateCount();
  }

  private void loadUpdateCount() {
    JFXFuture.supplyAsync(() -> client.getComponentService().getComponents()).thenAcceptLater(components -> {
      int count = 0;
      for (ComponentRepresentation component : components) {
        if (component.isVersionDiff()) {
          count++;
        }
      }
      updateCount = count;
      refreshUpdateIndicator();
    });
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (PreferenceNames.VPXZ_SETTINGS.equalsIgnoreCase(key)) {
      Platform.runLater(() -> {
        refreshVpxMobileVisibility();
        if (!vpxMobileBtn.isVisible() && getActiveNavigation() == NavigationItem.VpxMobile) {
          navigateTo(NavigationItem.Tables);
        }
      });
    }
    else if (PreferenceNames.HIGHSCORE_CARD_SETTINGS.equals(key)) {
      highscoreCardsNavigationView.setController(null);

      if (activeNavigation != null && activeNavigation.getItem().equals(NavigationItem.HighscoreCards)) {
        navigateTo(NavigationItem.HighscoreCards);
      }
    }
  }

  private void refreshVpxMobileVisibility() {
    VPXZSettings vpxzSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.VPXZ_SETTINGS, VPXZSettings.class);
    vpxMobileBtn.setVisible(Features.VPXZ_ENABLED && vpxzSettings.isEnabled());
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    for (Node node : List.of(emulatorsBtn, cardsBtn, recorderBtn, studioCaption, statisticsBtn, backupsBtn, vpxMobileBtn, screensBtn, dofTesterBtn)) {
      node.managedProperty().bindBidirectional(node.visibleProperty());
    }

    avatarView.setClip(new Circle(20, 20, 20));
    staticAvatarView = this.avatarView;
    staticSystemLabel = this.systemLabel;
    refreshAvatar();

    tablesBtn.getStyleClass().add(SELECTED_CLASS);

    EventManager.getInstance().addListener(this);
    client.getPreferenceService().addListener(this);

    cardsBtn.setVisible(Features.MEDIA_ENABLED);
    recorderBtn.setVisible(Features.RECORDER && !client.getRecorderService().getRecordingScreens().isEmpty());
    studioCaption.setVisible(cardsBtn.isVisible() || recorderBtn.isVisible());
    statisticsBtn.setVisible(Features.STATISTICS_ENABLED);
    backupsBtn.setVisible(Features.BACKUPS_ENABLED);
    emulatorsBtn.setVisible(Features.EMULATORS_ENABLED);
    screensBtn.setVisible(Features.SCREEN_MANAGER_ENABLED && !Features.IS_STANDALONE);
    dofTesterBtn.setVisible(Features.DOF_TESTER_ENABLED || LocalUISettings.getBoolean("doftester.enabled"));
    refreshVpxMobileVisibility();

    for (Node node : getEntries()) {
      if (node instanceof Label) {
        node.getProperties().put(CAPTION_KEY, ((Label) node).getText());
      }
      else if (node instanceof HBox) {
        HBox item = (HBox) node;
        getItemLabel(item).ifPresent(label -> {
          Tooltip tooltip = new Tooltip();
          tooltip.textProperty().bind(label.textProperty());
          item.getProperties().put(TOOLTIP_KEY, tooltip);
        });
      }
    }

    navigationItemMap.put(NavigationItem.Tables, new NavigationView(NavigationItem.Tables, TablesController.class, tablesBtn, "scene-tables.fxml"));
    navigationItemMap.put(NavigationItem.Emulators, new NavigationView(NavigationItem.Emulators, EmulatorsController.class, emulatorsBtn, "tab-emulators.fxml"));
    navigationItemMap.put(NavigationItem.Backglasses, new NavigationView(NavigationItem.Backglasses, backglassesBtn, NavigationItem.Tables));
    navigationItemMap.put(NavigationItem.VpsTables, new NavigationView(NavigationItem.VpsTables, vpsTablesBtn, NavigationItem.Tables));
    navigationItemMap.put(NavigationItem.TableStatistics, new NavigationView(NavigationItem.TableStatistics, statisticsBtn, NavigationItem.Tables));
    navigationItemMap.put(NavigationItem.TableBackups, new NavigationView(NavigationItem.TableBackups, backupsBtn, NavigationItem.Tables));
    navigationItemMap.put(NavigationItem.VpxMobile, new NavigationView(NavigationItem.VpxMobile, vpxMobileBtn, NavigationItem.Tables));
    navigationItemMap.put(NavigationItem.Achievements, new NavigationView(NavigationItem.Achievements, AchievementsController.class, dashboardBtn, "scene-achievements.fxml"));
    navigationItemMap.put(NavigationItem.Players, new NavigationView(NavigationItem.Players, PlayersController.class, playersBtn, "scene-players.fxml"));

    navigationItemMap.put(NavigationItem.Competitions, new NavigationView(NavigationItem.Competitions, CompetitionsController.class, competitionsBtn, "scene-competitions.fxml"));

    this.highscoreCardsNavigationView = new NavigationView(NavigationItem.HighscoreCards, HighscoreCardsController.class, cardsBtn, "scene-highscore-cards.fxml");
    navigationItemMap.put(NavigationItem.HighscoreCards, highscoreCardsNavigationView);

    navigationItemMap.put(NavigationItem.MediaRecorder, new NavigationView(NavigationItem.MediaRecorder, RecorderController.class, recorderBtn, "scene-recorder.fxml"));

    navigationItemMap.put(NavigationItem.Screens, new NavigationView(NavigationItem.Screens, ScreensController.class, screensBtn, "tab-screens.fxml"));
    navigationItemMap.put(NavigationItem.DofTester, new NavigationView(NavigationItem.DofTester, DOFTesterController.class, dofTesterBtn, "tab-dof-tester.fxml"));
    navigationItemMap.put(NavigationItem.Updates, new NavigationView(NavigationItem.Updates, ComponentsController.class, updatesBtn, "scene-components.fxml"));

    setCollapsed(LocalUISettings.getBoolean(COLLAPSED_KEY));
    loadUpdateCount();
  }
}

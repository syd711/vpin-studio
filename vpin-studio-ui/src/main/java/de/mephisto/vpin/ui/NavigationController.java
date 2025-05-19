package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.cards.HighscoreCardsController;
import de.mephisto.vpin.ui.competitions.CompetitionsController;
import de.mephisto.vpin.ui.components.ComponentsController;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.players.PlayersController;
import de.mephisto.vpin.ui.tables.TablesController;
import de.mephisto.vpin.ui.tournaments.TournamentsController;
import de.mephisto.vpin.ui.util.Dialogs;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class NavigationController implements Initializable, StudioEventListener, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(NavigationController.class);
  private final Debouncer debouncer = new Debouncer();
  public static final int DEBOUNCE_MS = 200;

  private final static FontIcon updateIcon = WidgetFactory.createUpdateStar();

  @FXML
  private BorderPane avatarPane;

  @FXML
  private Pane tablesBtn;

  @FXML
  private Pane dashboardBtn;

  @FXML
  private Pane systemManagerOverlay;

  @FXML
  private Pane tournamentsBtn;

  @FXML
  private Pane playersBtn;

  @FXML
  private Pane competitionsBtn;

  @FXML
  private Pane systemManagerBtn;

  @FXML
  private Pane cardsBtn;

  @FXML
  private StackPane systemManagerStack;

  private static BorderPane staticAvatarPane;

  private static NavigationView activeNavigation;

  private final static Map<NavigationItem, NavigationView> navigationItemMap = new HashMap<>();

  private final List<Pane> buttons = new ArrayList();

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
    navigateTo(NavigationItem.Dashboard);
  }

  @FXML
  private void onTournamentsClick() {
    if (ToolbarController.newVersion != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Update " + ToolbarController.newVersion, "You need the latest VPin Studio version to use these services.", null, "Update");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        Dialogs.openUpdateDialog();
      }
    }
    else {
      navigateTo(NavigationItem.Tournaments);
    }
  }

  @FXML
  private void onHighscoreCardsClick() {
    navigateTo(NavigationItem.HighscoreCards);
  }

  @FXML
  private void onTablesClick() {
    navigateTo(NavigationItem.Tables);
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
  private void onSystemClick() {
    navigateTo(NavigationItem.SystemManager);
  }

  public static void navigateTo(NavigationItem item) {
    navigateTo(item, null);
  }

  public static StudioFXController getActiveNavigationController() {
    return activeNavigation.getController();
  }

  public static void navigateTo(NavigationItem item, NavigationOptions options) {
    NavigationView navigationView = navigationItemMap.get(item);
    if (activeNavigation != null) {
      activeNavigation.getNavigationButton().getStyleClass().remove("navigation-button-selected");
      if (activeNavigation.getController() != null) {
        activeNavigation.getController().onViewDeactivated();
      }
    }

    activeNavigation = navigationView;

    if (!activeNavigation.getNavigationButton().getStyleClass().contains("navigation-button-selected")) {
      activeNavigation.getNavigationButton().getStyleClass().add("navigation-button-selected");
    }

    Node lookup = Studio.stage.getScene().lookup("#main");
    BorderPane main = (BorderPane) lookup;

    if (activeNavigation.getController() != null) {
      Parent root = activeNavigation.getRoot();
      main.setCenter(root);
      activeNavigation.getController().onViewActivated(options);
    }
    else {
      try {
        FXMLLoader loader = new FXMLLoader(activeNavigation.getControllerClass().getResource(activeNavigation.getFxml()));
        Parent root = loader.load();
        StudioFXController controller = loader.<StudioFXController>getController();
        activeNavigation.setController(controller);
        activeNavigation.setRoot(root);
        main.setCenter(root);
        activeNavigation.getController().onViewActivated(options);
      }
      catch (IOException e) {
        LOG.info("Failed to load main view: " + e.getMessage(), e);
      }
    }
  }

  public static void refreshAvatar() {
    InputStream av = client.getAssetService().getAvatar(false);
    if (av == null) {
      av = ServerFX.class.getResourceAsStream("avatar-default.png");
    }
    Image image = new Image(av);
    ImageView avatar = new ImageView(image);
    avatar.setFitWidth(100);
    avatar.setFitHeight(100);

    PreferenceEntryRepresentation systemNameEntry = client.getPreference(PreferenceNames.SYSTEM_NAME);
    String name = UIDefaults.VPIN_NAME;
    if (systemNameEntry != null && !StringUtils.isEmpty(systemNameEntry.getValue())) {
      name = systemNameEntry.getValue();
    }

    try {
      if (staticAvatarPane.isVisible()) {
        staticAvatarPane.setCenter(avatar);
      }
    }
    catch (Exception e) {
      LOG.error("Failed to refresh avatar tile: " + e.getMessage());
    }

    Studio.stage.setTitle("VPin Studio (" + Studio.getVersion() + ") - " + name);

    if (Studio.stage != null && Studio.stage.getScene() != null) {
      Node header = Studio.stage.getScene().lookup("#header");
      HeaderResizeableController dialogHeaderController = (HeaderResizeableController) header.getUserData();
      dialogHeaderController.setTitle(Studio.stage.getTitle());
    }
  }

  public static void setBreadCrumb(List<String> crumbs) {
    Platform.runLater(() -> {
      Label breadCrumb = (Label) Studio.stage.getScene().lookup("#breadcrumb");
      String join = StringUtils.join(crumbs, " / ");
      if (breadCrumb != null) {
        breadCrumb.setText("/ " + join);
      }
    });
  }

  @Override
  public void thirdPartyVersionUpdated(@NonNull ComponentType type) {
    JFXFuture.supplyAsync(() -> Studio.client.getComponentService().getComponents())
        .thenAcceptLater(components -> {
          systemManagerOverlay.getChildren().remove(updateIcon);
          for (ComponentRepresentation component : components) {
            if (component.isVersionDiff()) {
              systemManagerOverlay.getChildren().add(updateIcon);
              break;
            }
          }
        });
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    if (PreferenceNames.MANIA_SETTINGS.equals(key)) {
      ManiaSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
      tournamentsBtn.setVisible(settings.isEnabled() && settings.isTournamentsEnabled());
      if (!tournamentsBtn.isVisible()) {
        navigateTo(NavigationItem.Tables);
      }
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tournamentsBtn.managedProperty().bindBidirectional(tournamentsBtn.visibleProperty());
    cardsBtn.managedProperty().bindBidirectional(cardsBtn.visibleProperty());

    staticAvatarPane = this.avatarPane;
    refreshAvatar();

    tablesBtn.getStyleClass().add("navigation-button-selected");

    EventManager.getInstance().addListener(this);
    client.getPreferenceService().addListener(this);

    FrontendType frontendType = client.getFrontendService().getFrontendType();
    cardsBtn.setVisible(frontendType.supportMedias());

    tournamentsBtn.setVisible(false);
    try {
      ManiaSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
      if (Features.MANIA_ENABLED && Studio.maniaClient != null && Studio.maniaClient.getCabinetClient().getCabinet() != null && settings.isTournamentsEnabled()) {
        tournamentsBtn.setVisible(settings.isEnabled());
      }
    }
    catch (Exception e) {
      LOG.error("Mania initialization failed: {}", e.getMessage(), e);
    }

    this.buttons.add(tablesBtn);
    this.buttons.add(dashboardBtn);
    this.buttons.add(cardsBtn);

    this.buttons.add(competitionsBtn);
    this.buttons.add(playersBtn);
    this.buttons.add(tournamentsBtn);

    this.buttons.add(systemManagerBtn);

    navigationItemMap.put(NavigationItem.Tables, new NavigationView(NavigationItem.Tables, TablesController.class, tablesBtn, "scene-tables.fxml"));
    navigationItemMap.put(NavigationItem.Dashboard, new NavigationView(NavigationItem.Dashboard, DashboardController.class, dashboardBtn, "scene-dashboard.fxml"));
    navigationItemMap.put(NavigationItem.Players, new NavigationView(NavigationItem.Players, PlayersController.class, playersBtn, "scene-players.fxml"));

    navigationItemMap.put(NavigationItem.Competitions, new NavigationView(NavigationItem.Competitions, CompetitionsController.class, competitionsBtn, "scene-competitions.fxml"));
    navigationItemMap.put(NavigationItem.HighscoreCards, new NavigationView(NavigationItem.HighscoreCards, HighscoreCardsController.class, cardsBtn, "scene-highscore-cards.fxml"));
    navigationItemMap.put(NavigationItem.Tournaments, new NavigationView(NavigationItem.Tournaments, TournamentsController.class, tournamentsBtn, "scene-tournaments.fxml"));

    navigationItemMap.put(NavigationItem.SystemManager, new NavigationView(NavigationItem.SystemManager, ComponentsController.class, systemManagerBtn, "scene-components.fxml"));

    Studio.stage.heightProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        debouncer.debounce("height", () -> {
          Platform.runLater(() -> {
            boolean scaleDown = newValue.intValue() < 900;
            String style = scaleDown ? "navigation-button-small" : "navigation-button";
            systemManagerStack.getStyleClass().removeAll();
            systemManagerStack.getStyleClass().add(style);
            for (Pane button : buttons) {
              setLabelStyle(button, scaleDown ? "-fx-font-size: 12px" : "-fx-font-size: 14px");
              setFontIconSize(button, scaleDown ? 20 : 28);
              button.getStyleClass().remove("navigation-button-small");
              button.getStyleClass().remove("navigation-button");
              button.getStyleClass().add(style);
            }
          });
        }, DEBOUNCE_MS);
      }

      private void setLabelStyle(Pane button, String style) {
        button.getChildren().stream().filter(c -> c instanceof Label).forEach(c -> c.setStyle(style));
        button.getChildren().stream().filter(c -> c instanceof Pane).forEach(c -> setLabelStyle((Pane) c, style));
      }

      private void setFontIconSize(Pane button, int size) {
        button.getChildren().stream().filter(c -> c instanceof FontIcon).forEach(c -> ((FontIcon) c).setIconSize(size));
        button.getChildren().stream().filter(c -> c instanceof ImageView).forEach(c -> ((ImageView) c).setFitHeight(size));
        button.getChildren().stream().filter(c -> c instanceof Pane).forEach(c -> setFontIconSize((Pane) c, size));
      }
    });
  }
}
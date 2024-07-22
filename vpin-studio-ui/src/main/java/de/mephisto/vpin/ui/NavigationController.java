package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.tournaments.TournamentSettings;
import de.mephisto.vpin.ui.cards.HighscoreCardsController;
import de.mephisto.vpin.ui.competitions.CompetitionsController;
import de.mephisto.vpin.ui.components.ComponentsController;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.mania.ManiaController;
import de.mephisto.vpin.ui.players.PlayersController;
import de.mephisto.vpin.ui.tables.TablesController;
import de.mephisto.vpin.ui.tournaments.TournamentsController;
import edu.umd.cs.findbugs.annotations.NonNull;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class NavigationController implements Initializable, StudioEventListener, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(NavigationController.class);

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
  private Pane maniaBtn;

  @FXML
  private Pane cardsBtn;

  private static BorderPane staticAvatarPane;

  private static NavigationView activeNavigation;

  private final static Map<NavigationItem, NavigationView> navigationItemMap = new HashMap<>();

  // Add a public no-args constructor
  public NavigationController() {
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
  private void onManiaClick() {
    navigateTo(NavigationItem.Mania);
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
  private void onTournamentsClick() {
    navigateTo(NavigationItem.Tournaments);
  }

  @FXML
  private void onSystemClick() {
    navigateTo(NavigationItem.SystemManager);
  }

  public static void navigateTo(NavigationItem item) {
    navigateTo(item, null);
  }

  public static void navigateTo(NavigationItem item, NavigationOptions options) {
    NavigationView navigationView = navigationItemMap.get(item);
    if (activeNavigation != null) {
      activeNavigation.getNavigationButton().getStyleClass().remove("navigation-button-selected");
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
      }
      catch (IOException e) {
        LOG.info("Failed to load main view: " + e.getMessage(), e);
      }
    }
  }

  public static void refreshAvatar() {
    PreferenceEntryRepresentation avatarEntry = client.getPreference(PreferenceNames.AVATAR);
    Image image = new Image(DashboardController.class.getResourceAsStream("avatar-default.png"));
    if (avatarEntry != null && !StringUtils.isEmpty(avatarEntry.getValue())) {
      image = new Image(client.getAsset(AssetType.VPIN_AVATAR, avatarEntry.getValue()));
    }

    PreferenceEntryRepresentation systemNameEntry = client.getPreference(PreferenceNames.SYSTEM_NAME);
    String name = UIDefaults.VPIN_NAME;
    if (systemNameEntry != null && !StringUtils.isEmpty(systemNameEntry.getValue())) {
      name = systemNameEntry.getValue();
    }

    Tile avatar = TileBuilder.create()
        .skinType(Tile.SkinType.IMAGE)
        .prefSize(300, 300)
        .backgroundColor(Color.TRANSPARENT)
        .image(image)
        .imageMask(Tile.ImageMask.ROUND)
        .text("")
        .textSize(Tile.TextSize.BIGGER)
        .textAlignment(TextAlignment.CENTER)
        .build();

    try {
      if (staticAvatarPane.isVisible()) {
        staticAvatarPane.setCenter(avatar);
      }
    }
    catch (Exception e) {
      LOG.error("Failed to refresh avatar tile: " + e.getMessage());
    }

    Studio.stage.setTitle("VPin Studio - " + name);

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
    Platform.runLater(() -> {
      systemManagerOverlay.getChildren().remove(updateIcon);
      List<ComponentRepresentation> components = Studio.client.getComponentService().getComponents();
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
    if (PreferenceNames.TOURNAMENTS_SETTINGS.equals(key)) {
      TournamentSettings settings = client.getTournamentsService().getSettings();
      tournamentsBtn.setVisible(settings.isEnabled());
      if (!tournamentsBtn.isVisible()) {
        navigateTo(NavigationItem.Tables);
      }
      maniaBtn.setVisible(settings.isEnabled());
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
    maniaBtn.setVisible(false);
    if (Features.TOURNAMENTS_ENABLED && Studio.maniaClient != null && Studio.maniaClient.getCabinetClient().getCabinet() != null) {
      TournamentSettings settings = client.getTournamentsService().getSettings();
      tournamentsBtn.setVisible(settings.isEnabled());
    }
    if (Features.MANIA && Studio.maniaClient != null && Studio.maniaClient.getCabinetClient().getCabinet() != null) {
      maniaBtn.setVisible(true);
    }

    navigationItemMap.put(NavigationItem.Tables, new NavigationView(NavigationItem.Tables, TablesController.class, tablesBtn, "scene-tables.fxml"));
    navigationItemMap.put(NavigationItem.Dashboard, new NavigationView(NavigationItem.Dashboard, DashboardController.class, dashboardBtn, "scene-dashboard.fxml"));
    navigationItemMap.put(NavigationItem.Players, new NavigationView(NavigationItem.Players, PlayersController.class, playersBtn, "scene-players.fxml"));
    navigationItemMap.put(NavigationItem.Competitions, new NavigationView(NavigationItem.Competitions, CompetitionsController.class, competitionsBtn, "scene-competitions.fxml"));
    navigationItemMap.put(NavigationItem.HighscoreCards, new NavigationView(NavigationItem.HighscoreCards, HighscoreCardsController.class, cardsBtn, "scene-highscore-cards.fxml"));
    navigationItemMap.put(NavigationItem.Tournaments, new NavigationView(NavigationItem.Tournaments, TournamentsController.class, tournamentsBtn, "scene-tournaments.fxml"));
    navigationItemMap.put(NavigationItem.SystemManager, new NavigationView(NavigationItem.SystemManager, ComponentsController.class, systemManagerBtn, "scene-components.fxml"));
    navigationItemMap.put(NavigationItem.Mania, new NavigationView(NavigationItem.Mania, ManiaController.class, maniaBtn, "scene-mania.fxml"));
  }
}
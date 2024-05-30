package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.tournaments.TournamentSettings;
import de.mephisto.vpin.ui.cards.HighscoreCardsController;
import de.mephisto.vpin.ui.competitions.CompetitionsController;
import de.mephisto.vpin.ui.components.ComponentsController;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
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
import javafx.scene.input.MouseEvent;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class NavigationController implements Initializable, StudioEventListener, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(NavigationController.class);

  private final static FontIcon updateIcon = WidgetFactory.createUpdateStar();

  @FXML
  private BorderPane avatarPane;

  @FXML
  private Pane tablesBtn;

  @FXML
  private Pane buttonList;

  private static Pane staticButtonList;

  @FXML
  private Pane systemManagerOverlay;

  public static StudioFXController activeController;

  private static BorderPane staticAvatarPane;
  private static String activeScreenId = "scene-tables.fxml";

  private static Map<String, Parent> viewCache = new HashMap<>();
  private static Map<String, StudioFXController> controllerCache = new HashMap<>();

  @FXML
  private Pane tournamentsBtn;


  // Add a public no-args constructor
  public NavigationController() {
  }

  public static void refreshControllerCache() {
    try {
      StudioFXController studioFXController = controllerCache.get(activeScreenId);
      loadScreen(null, studioFXController.getClass(), activeScreenId);
      refreshAvatar();
    } catch (IOException e) {
      LOG.error("Refresh of navigation components cache failed: " + e.getMessage(), e);
    }
  }

  public static void refreshViewCache() {
    viewCache.clear();
  }

  @FXML
  private void onDashboardClick(MouseEvent event) throws IOException {
    loadScreen(event, DashboardController.class, "scene-dashboard.fxml");
  }

  @FXML
  private void onHighscoreCardsClick(MouseEvent event) throws IOException {
    loadScreen(event, HighscoreCardsController.class, "scene-highscore-cards.fxml");
  }

  @FXML
  private void onTablesClick(MouseEvent event) throws IOException {
    loadScreen(event, TablesController.class, "scene-tables.fxml");
  }

  @FXML
  private void onCompetitionsClick(MouseEvent event) throws IOException {
    loadScreen(event, CompetitionsController.class, "scene-competitions.fxml");
  }

  @FXML
  private void onPlayersClick(MouseEvent event) throws IOException {
    loadScreen(event, PlayersController.class, "scene-players.fxml");
  }

  @FXML
  private void onTournamentsClick(MouseEvent event) throws IOException {
    loadScreen(event, TournamentsController.class, "scene-tournaments.fxml");
  }

  @FXML
  private void onSystemClick(MouseEvent event) throws IOException {
    loadScreen(event, ComponentsController.class, "scene-components.fxml");
  }

  public static void load(String fxml) throws IOException {
    StudioFXController studioFXController = controllerCache.get(fxml);
    loadScreen(null, studioFXController.getClass(), activeScreenId);
  }

  public static void setInitialController(String key, StudioFXController controller, Parent root) {
    viewCache.put(key, root);
    controllerCache.put(key, controller);
  }

  public static void loadScreen(MouseEvent event, Class<?> controller, String name) throws IOException {
    if (event != null) {
      Pane b = (Pane) event.getSource();
      for (Node node : staticButtonList.getChildren()) {
        Pane child = (Pane) node;
        node.getStyleClass().remove("navigation-button-selected");
        child.getChildren().stream().forEach(c -> c.getStyleClass().remove("navigation-button-selected"));
      }
      b.getStyleClass().add("navigation-button-selected");
    }
    else {

    }

    activeScreenId = name;
    Node lookup = Studio.stage.getScene().lookup("#main");
    BorderPane main = (BorderPane) lookup;

    Parent root;
    if (viewCache.containsKey(name)) {
      root = viewCache.get(name);
      activeController = controllerCache.get(name);
      activeController.onViewActivated();
    }
    else {
      FXMLLoader loader = new FXMLLoader(controller.getResource(name));
      root = loader.load();
      activeController = loader.<StudioFXController>getController();

      viewCache.put(name, root);
      controllerCache.put(name, activeController);
    }

    main.setCenter(root);
  }

  public static void refreshAvatar() {
    PreferenceEntryRepresentation avatarEntry = client.getPreference(PreferenceNames.AVATAR);
    Image image = new Image(DashboardController.class.getResourceAsStream("avatar-default.png"));
    if (avatarEntry!=null && !StringUtils.isEmpty(avatarEntry.getValue())) {
      image = new Image(client.getAsset(AssetType.VPIN_AVATAR, avatarEntry.getValue()));
    }

    PreferenceEntryRepresentation systemNameEntry = client.getPreference(PreferenceNames.SYSTEM_NAME);
    String name = UIDefaults.VPIN_NAME;
    if (systemNameEntry!=null && !StringUtils.isEmpty(systemNameEntry.getValue())) {
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
    } catch (Exception e) {
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
        try {
          onTablesClick(null);
        } catch (IOException e) {
          //ignore
        }
      }
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tournamentsBtn.managedProperty().bindBidirectional(tournamentsBtn.visibleProperty());

    staticAvatarPane = this.avatarPane;
    refreshAvatar();

    tablesBtn.getStyleClass().add("navigation-button-selected");

    staticButtonList = this.buttonList;
    EventManager.getInstance().addListener(this);
    client.getPreferenceService().addListener(this);

    tournamentsBtn.setVisible(false);
    if (Features.TOURNAMENTS_ENABLED && Studio.maniaClient != null) {
      TournamentSettings settings = client.getTournamentsService().getSettings();
      tournamentsBtn.setVisible(settings.isEnabled());
    }
  }
}
package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.restclient.AssetType;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.cards.HighscoreCardsController;
import de.mephisto.vpin.ui.competitions.CompetitionsController;
import de.mephisto.vpin.ui.players.PlayersController;
import de.mephisto.vpin.ui.tables.TableOverviewController;
import de.mephisto.vpin.ui.tables.TablesController;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.application.Platform;
import javafx.collections.ObservableList;
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

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class NavigationController implements Initializable {

  @FXML
  private BorderPane avatarPane;

  @FXML
  private Pane dashboardBtn;

  public static StudioFXController activeController;

  private static BorderPane staticAvatarPane;
  private static String activeScreenId = "scene-dashboard.fxml";

  private static Map<String, Parent> viewCache = new HashMap<>();
  private static Map<String, StudioFXController> controllerCache = new HashMap<>();

  // Add a public no-args constructor
  public NavigationController() {
  }

  public static void refresh() throws IOException {
    StudioFXController studioFXController = controllerCache.get(activeScreenId);
    loadScreen(null, studioFXController.getClass(), activeScreenId);
    refreshAvatar();
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

  public static void load(String fxml) throws IOException {
    StudioFXController studioFXController = controllerCache.get(fxml);
    loadScreen(null, studioFXController.getClass(), activeScreenId);
  }

  public static void setInitialController(String key, StudioFXController controller) {
    controllerCache.put(key, controller);
  }

  public static void loadScreen(MouseEvent event, Class<?> controller, String name) throws IOException {
    if (event != null) {
      Pane b = (Pane) event.getSource();
      ObservableList<Node> childrenUnmodifiable = b.getParent().getChildrenUnmodifiable();
      for (Node node : childrenUnmodifiable) {
        node.getStyleClass().remove("navigation-button-selected");
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

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    staticAvatarPane = this.avatarPane;
    refreshAvatar();

    dashboardBtn.getStyleClass().add("navigation-button-selected");
  }

  private static void refreshAvatar() {
    PreferenceEntryRepresentation avatarEntry = client.getPreference(PreferenceNames.AVATAR);
    Image image = new Image(DashboardController.class.getResourceAsStream("avatar-default.png"));
    if (!StringUtils.isEmpty(avatarEntry.getValue())) {
      image = new Image(client.getAsset(AssetType.VPIN_AVATAR, avatarEntry.getValue()));
    }

    PreferenceEntryRepresentation systemNameEntry = client.getPreference(PreferenceNames.SYSTEM_NAME);
    String name = UIDefaults.VPIN_NAME;
    if (!StringUtils.isEmpty(systemNameEntry.getValue())) {
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
    staticAvatarPane.setCenter(avatar);

    Studio.stage.setTitle("VPin Studio - " + name);

    if(Studio.stage != null && Studio.stage.getScene() != null) {
      Node header = Studio.stage.getScene().lookup("#header");
      HeaderResizeableController dialogHeaderController = (HeaderResizeableController) header.getUserData();
      dialogHeaderController.setTitle(Studio.stage.getTitle());
    }
  }

  public static void setBreadCrumb(List<String> crumbs) {
    Platform.runLater(() -> {
      Label breadCrumb = (Label) Studio.stage.getScene().lookup("#breadcrumb");
      breadCrumb.setText("/ " + StringUtils.join(crumbs, " / "));
    });
  }
}
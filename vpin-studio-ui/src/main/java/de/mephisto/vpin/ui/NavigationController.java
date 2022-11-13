package de.mephisto.vpin.ui;

import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.preferences.PreferenceNames;
import de.mephisto.vpin.ui.util.UIDefaults;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class NavigationController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(NavigationController.class);
  private static Tile avatar;

  @FXML
  private BorderPane avatarPane;

  public static StudioFXController activeController;
  public static StudioFXController navigationController;

  private static Parent root;
  private static BorderPane staticAvatarPane;
  private static String activeScreenId = "scene-dashboard.fxml";

  private static Map<String, Parent> viewCache = new HashMap<>();
  private static Map<String, StudioFXController> controllerCache = new HashMap<>();
  private Node preferencesRoot;

  // Add a public no-args constructor
  public NavigationController() {
  }

  public static void refresh() throws IOException {
    loadScreen(null, activeScreenId);
    refreshAvatar();
  }

  @FXML
  private void onDashboardClick(ActionEvent event) throws IOException {
    loadScreen(event, "scene-dashboard.fxml");
  }

  @FXML
  private void onHighscoreCardsClick(ActionEvent event) throws IOException {
    loadScreen(event, "scene-highscoreCards.fxml");
  }

  @FXML
  private void onTablesClick(ActionEvent event) throws IOException {
    loadScreen(event, "scene-tables.fxml");
  }

  @FXML
  private void onCompetitionsClick(ActionEvent event) throws IOException {
    loadScreen(event, "scene-competitions.fxml");
  }

  @FXML
  private void onOverlayClick(ActionEvent event) throws IOException {
    loadScreen(event, "scene-overlay.fxml");
  }

  @FXML
  private void onPlayersClick(ActionEvent event) throws IOException {
    loadScreen(event, "scene-players.fxml");
  }

  public static void load(String fxml) throws IOException {
    loadScreen(null, fxml);
  }

  @FXML
  private void onPreferencesClicked(ActionEvent event) throws IOException {
    Node lookup = Studio.stage.getScene().lookup("#root");
    BorderPane main = (BorderPane) lookup;
    StackPane stack = (StackPane) main.getCenter();
    stack.getChildren().add(preferencesRoot);
  }

  public static void loadScreen(ActionEvent event, String name) throws IOException {
    activeScreenId = name;
    Node lookup = Studio.stage.getScene().lookup("#main");
    BorderPane main = (BorderPane) lookup;

    if (viewCache.containsKey(name)) {
      root = viewCache.get(name);
      activeController = controllerCache.get(name);
    }
    else {
      FXMLLoader loader = new FXMLLoader(NavigationController.class.getResource(name));
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
    if (preferencesRoot == null) {
      try {
        FXMLLoader loader = new FXMLLoader(NavigationController.class.getResource("scene-preferences.fxml"));
        preferencesRoot = loader.load();
      } catch (IOException e) {
        LOG.error("Failed to load preferences: " + e.getMessage(), e);
      }
    }
    refreshAvatar();
  }

  private static void refreshAvatar() {
    PreferenceEntryRepresentation avatarEntry = client.getPreference(PreferenceNames.AVATAR);
    Image image = new Image(DashboardController.class.getResourceAsStream("avatar-default.png"));
    if (!StringUtils.isEmpty(avatarEntry.getValue())) {
      image = new Image(client.getAsset(avatarEntry.getValue()));
    }

    PreferenceEntryRepresentation systemNameEntry = client.getPreference(PreferenceNames.SYSTEM_NAME);
    String name = UIDefaults.VPIN_NAME;
    if (!StringUtils.isEmpty(systemNameEntry.getValue())) {
      name = systemNameEntry.getValue();
    }

    avatar = TileBuilder.create()
        .skinType(Tile.SkinType.IMAGE)
        .prefSize(300, 300)
        .backgroundColor(Color.TRANSPARENT)
        .image(image)
        .imageMask(Tile.ImageMask.ROUND)
        .text(name)
        .textSize(Tile.TextSize.BIGGER)
        .textAlignment(TextAlignment.CENTER)
        .build();
    staticAvatarPane.setCenter(avatar);
  }

  public static void setBreadCrumb(List<String> crumbs) {
    Platform.runLater(() -> {
      Label breadCrumb = (Label) Studio.stage.getScene().lookup("#breadcrumb");
      breadCrumb.setText("/ " + StringUtils.join(crumbs, " / "));
    });
  }
}
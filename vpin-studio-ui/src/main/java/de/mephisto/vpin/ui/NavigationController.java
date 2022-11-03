package de.mephisto.vpin.ui;

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
import java.util.List;
import java.util.ResourceBundle;

public class NavigationController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(NavigationController.class);

  @FXML
  private BorderPane avatarPane;

  public static StudioFXController activeController;
  public static StudioFXController navigationController;

  private static Parent root;

  // Add a public no-args constructor
  public NavigationController() {
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
  private void onPlayersClick(ActionEvent event) throws IOException {
    loadScreen(event, "scene-players.fxml");
  }

  public static void load(String fxml) throws IOException {
    loadScreen(null, fxml);
  }

  @FXML
  private void onPreferencesClicked(ActionEvent event) throws IOException {
    FXMLLoader loader = new FXMLLoader(NavigationController.class.getResource("scene-preferences.fxml"));
    Node preferencesRoot = loader.load();

    Node lookup = VPinStudioApplication.stage.getScene().lookup("#root");
    BorderPane main = (BorderPane) lookup;
    StackPane stack = (StackPane) main.getCenter();
    stack.getChildren().add(preferencesRoot);
  }

  public static void loadScreen(ActionEvent event, String name) throws IOException {
    Node lookup = VPinStudioApplication.stage.getScene().lookup("#main");
    BorderPane main = (BorderPane) lookup;
    if (activeController != null) {
      activeController.dispose();
    }

    FXMLLoader loader = new FXMLLoader(NavigationController.class.getResource(name));
    root = loader.load();
    activeController = loader.<StudioFXController>getController();
    main.setCenter(root);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Tile avatar = TileBuilder.create()
        .skinType(Tile.SkinType.IMAGE)
        .prefSize(300, 300)
        .backgroundColor(Color.TRANSPARENT)
        .image(new Image(DashboardController.class.getResourceAsStream("dashboard.png")))
        .imageMask(Tile.ImageMask.ROUND)
        .text("Whatever text")
        .textSize(Tile.TextSize.BIGGER)
        .textAlignment(TextAlignment.CENTER)
        .build();
    avatarPane.setCenter(avatar);
  }

  public static void setBreadCrumb(List<String> crumbs) {
    Platform.runLater(() -> {
      Label breadCrumb = (Label) VPinStudioApplication.stage.getScene().lookup("#breadcrumb");
      breadCrumb.setText("/ " + StringUtils.join(crumbs, " / "));
    });
  }
}
package de.mephisto.vpin.ui;

import de.mephisto.vpin.ui.util.TransitionUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.TextAlignment;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class NavigationController implements Initializable {
  @FXML
  private BorderPane avatarPane;

  private static StudioFXController activeController;
  private static Parent root;

  // Add a public no-args constructor
  public NavigationController() {
  }

  @FXML
  private void onDashboardClick(ActionEvent event) throws IOException {
    this.loadScene(event, "scene-dashboard.fxml");
  }

  @FXML
  private void onHighscoreCardsClick(ActionEvent event) throws IOException {
    this.loadScene(event, "scene-highscoreCards.fxml");
  }

  @FXML
  private void onSettingsClicked(ActionEvent event) throws IOException {
    TransitionUtil.createOutFader(root, 300).play();
  }

  @FXML
  private void onTablesClick(ActionEvent event) throws IOException {
    this.loadScene(event, "scene-tables.fxml");
  }

  private void loadScene(@NonNull ActionEvent event, @NonNull String name) throws IOException {
    if(activeController != null) {
      activeController.dispose();
    }

    FXMLLoader loader = new FXMLLoader(getClass().getResource(name));
    root = loader.load();
    activeController = loader.<StudioFXController>getController();
    Scene scene = ((Node) event.getSource()).getScene();
    scene.setFill(Paint.valueOf("#212529"));
    scene.setRoot(root);
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
}
package de.mephisto.vpin.ui;

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
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class NavigationController implements Initializable {
  @FXML
  private BorderPane avatarPane;

  // Add a public no-args constructor
  public NavigationController() {
  }

  @FXML
  private void onDashboardClick(ActionEvent event) throws IOException {
    Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
    Scene scene = ((Node) event.getSource()).getScene();
    scene.setRoot(root);
  }

  @FXML
  private void onHighscoreCardsClick(ActionEvent event) throws IOException {
    Parent root = FXMLLoader.load(getClass().getResource("highscoreCards.fxml"));
    Scene scene = ((Node) event.getSource()).getScene();
    scene.setRoot(root);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Tile avatar = TileBuilder.create()
        .skinType(Tile.SkinType.IMAGE)
        .prefSize(300, 300)
        .backgroundColor(Color.TRANSPARENT)
        .image(new Image(MainController.class.getResourceAsStream("dashboard.png")))
        .imageMask(Tile.ImageMask.ROUND)
        .text("Whatever text")
        .textSize(Tile.TextSize.BIGGER)
        .textAlignment(TextAlignment.CENTER)
        .build();
    avatarPane.setCenter(avatar);

  }
}
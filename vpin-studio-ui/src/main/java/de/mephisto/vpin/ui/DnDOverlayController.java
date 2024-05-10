package de.mephisto.vpin.ui;

import de.mephisto.vpin.restclient.games.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.GameMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class DnDOverlayController implements Initializable {

  @FXML
  private Label messageLabel;

  @FXML
  private Label tableTitleLabel;

  @FXML
  private Label tableLabel;

  @FXML
  private ImageView tableWheelImage;


  @FXML
  private BorderPane root;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tableWheelImage.managedProperty().bindBidirectional(tableWheelImage.visibleProperty());
  }

  public void setViewParams(double width, double height) {
    root.setPrefWidth(width);
    root.setPrefHeight(height);
  }

  public void setGame(@Nullable GameRepresentation game) {
    if (game != null) {
      tableTitleLabel.setVisible(true);
      GameMediaRepresentation gameMedia = game.getGameMedia();
      GameMediaItemRepresentation item = gameMedia.getDefaultMediaItem(PopperScreen.Wheel);
      if (item != null) {
        ByteArrayInputStream gameMediaItem = client.getGameMediaItem(game.getId(), PopperScreen.Wheel);
        tableWheelImage.setImage(new Image(gameMediaItem));
      }
      else {
        tableWheelImage.setImage(new Image(Studio.class.getResourceAsStream("avatar-blank.png")));
      }
      tableLabel.setText("\"" + game.getGameDisplayName() + "\"");
    }
    else {
      tableTitleLabel.setVisible(false);
    }
  }
}

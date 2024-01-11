package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.commons.fx.widgets.WidgetLatestScoreItemController;
import de.mephisto.vpin.restclient.alx.AlxTileEntry;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MenuCustomViewController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MenuCustomViewController.class);

  @FXML
  private ImageView wheelImage;

  @FXML
  private Label nameLabel;

  @FXML
  private VBox stats1Col;

  @FXML
  private VBox stats2Col;

  @FXML
  private VBox stats3Col;

  private MenuCustomTileEntryController tile1Controller;
  private MenuCustomTileEntryController tile2Controller;
  private MenuCustomTileEntryController tile3Controller;
  private MenuCustomTileEntryController tile4Controller;

  public void setGame(GameRepresentation game) {
    this.nameLabel.setText(game.getGameDisplayName());

    InputStream imageStream = PauseMenu.client.getGameMediaItem(game.getId(), PopperScreen.Wheel);
    if (imageStream == null) {
      imageStream = OverlayWindowFX.class.getResourceAsStream("avatar-blank.png");
    }
    Image image = new Image(imageStream);
    wheelImage.setImage(image);

    ScoreSummaryRepresentation recentlyPlayedGames = PauseMenu.client.getRecentlyPlayedGames(3);
    List<ScoreRepresentation> scores = recentlyPlayedGames.getScores();
    for (ScoreRepresentation score : scores) {
      try {
        FXMLLoader loader = new FXMLLoader(WidgetLatestScoreItemController.class.getResource("widget-latest-score-item.fxml"));
        Pane row = loader.load();
        row.setPrefWidth(stats3Col.getPrefWidth() - 24);
        WidgetLatestScoreItemController controller = loader.getController();
        controller.setData(game, score);

        stats3Col.getChildren().add(row);
      } catch (IOException e) {
        LOG.error("Failed to load tile: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    try {
      FXMLLoader loader = new FXMLLoader(MenuCustomTileEntryController.class.getResource("menu-custom-tile.fxml"));
      Parent builtInRoot = loader.load();
      tile1Controller = loader.getController();
      tile1Controller.refresh(new AlxTileEntry("Total Time Played", "(The total emulation time of all tables)", "2323"));
      stats1Col.getChildren().add(builtInRoot);

      loader = new FXMLLoader(MenuCustomTileEntryController.class.getResource("menu-custom-tile.fxml"));
      builtInRoot = loader.load();
      tile2Controller = loader.getController();
      tile2Controller.refresh(new AlxTileEntry("Total Time Played", "(The total emulation time of all tables)", "2323"));
      stats1Col.getChildren().add(builtInRoot);

      loader = new FXMLLoader(MenuCustomTileEntryController.class.getResource("menu-custom-tile.fxml"));
      builtInRoot = loader.load();
      tile3Controller = loader.getController();
      tile3Controller.refresh(new AlxTileEntry("Total Time Played", "(The total emulation time of all tables)", "2323"));
      stats2Col.getChildren().add(builtInRoot);

      loader = new FXMLLoader(MenuCustomTileEntryController.class.getResource("menu-custom-tile.fxml"));
      builtInRoot = loader.load();
      tile4Controller = loader.getController();
      tile4Controller.refresh(new AlxTileEntry("Total Time Played", "(The total emulation time of all tables)", "2323"));
      stats2Col.getChildren().add(builtInRoot);
    } catch (IOException e) {
      LOG.error("Failed to load tile: " + e.getMessage(), e);
    }
  }
}

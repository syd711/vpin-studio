package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.commons.fx.widgets.WidgetLatestScoreItemController;
import de.mephisto.vpin.restclient.alx.AlxSummary;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import java.util.Collections;
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

    AlxSummary alxSummary = PauseMenu.client.getAlxService().getAlxSummary();
    List<TableAlxEntry> entries = alxSummary.getEntries();
//    tile1Controller.refresh(entries);

    ScoreSummaryRepresentation recentlyPlayedGames = PauseMenu.client.getGameService().getRecentScoresByGame(3, game.getId());
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
        LOG.error("Failed to load paused scores: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    tile1Controller = TileFactory.createTotalTimeTile(stats1Col, Collections.emptyList());
    tile2Controller = TileFactory.createTotalGamesPlayedTile(stats1Col, Collections.emptyList());
    tile3Controller = TileFactory.createTotalScoresTile(stats2Col, Collections.emptyList());
    tile4Controller = TileFactory.createTotalHighScoresTile(stats2Col, Collections.emptyList());
  }
}

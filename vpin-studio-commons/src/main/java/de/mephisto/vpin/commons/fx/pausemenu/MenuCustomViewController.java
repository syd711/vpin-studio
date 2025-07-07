package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.widgets.WidgetLatestScoreItemController;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.alx.AlxSummary;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.GameScoreValidation;
import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.restclient.highscores.ScoreRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
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
  private Label versionLabel;

  @FXML
  private Label authorsLabel;

  @FXML
  private Label scoreInfoLabel;

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

  public void setGame(GameRepresentation game, FrontendMediaRepresentation frontendMedia, GameStatus status, VpsTable tableById) {
    this.nameLabel.setText(game.getGameDisplayName());
    this.versionLabel.setText("");
    this.authorsLabel.setText("");
    this.scoreInfoLabel.setText("");

    // when game is mapped to VPS Table
    if (tableById != null) {
      String extVersion = game.getExtTableVersionId();
      VpsTableVersion version = tableById.getTableVersionById(extVersion);
      if (version != null) {
        this.versionLabel.setText(version.getComment());
        List<String> authors = version.getAuthors();
        if (authors != null && !authors.isEmpty()) {
          this.authorsLabel.setText(String.join(", ", authors));
        }
      }
      else {
        this.versionLabel.setText(tableById.getManufacturer() + " (" + tableById.getYear() + ")");
        List<String> designers = tableById.getDesigners();
        if (designers != null && !designers.isEmpty()) {
          this.authorsLabel.setText(String.join(", ", designers));
        }
      }
    }

    GameScoreValidation scoreValidation = ServerFX.client.getGameScoreValidation(game.getId());
   //boolean valid = scoreValidation.isValidScoreConfiguration();
    if (!StringUtils.isEmpty(game.getRom())) {
      if (scoreValidation.getRomStatus() == null & scoreValidation.getHighscoreFilenameStatus() == null) {
        scoreInfoLabel.setText("ROM: \"" + game.getRom() + "\" (supported)");
      }
      else {
        if(scoreValidation.getHighscoreFilenameStatus() != null) {
          scoreInfoLabel.setText("ROM: \"" + game.getRom() + "\" (" + scoreValidation.getHighscoreFilenameStatus() + ")");
        }
        else {
          scoreInfoLabel.setText("ROM: \"" + game.getRom() + "\" (" + scoreValidation.getRomStatus() + ")");
        }
      }
    }

    InputStream imageStream = ServerFX.client.getGameMediaItem(game.getId(), VPinScreen.Wheel);
    if (imageStream == null) {
      imageStream = ServerFX.class.getResourceAsStream("avatar-blank.png");
    }
    Image image = new Image(imageStream);
    wheelImage.setImage(image);

    AlxSummary alxSummary = ServerFX.client.getAlxSummary(game.getId());
    List<TableAlxEntry> entries = alxSummary.getEntries();
    tile1Controller.refresh(TileFactory.toTotalGamesPlayedEntry(entries));
    tile2Controller.refresh(TileFactory.toTotalScoresEntry(entries));
    tile3Controller.refresh(TileFactory.toTotalTimeEntry(entries));
    tile4Controller.refresh(TileFactory.toSessionDurationTile(status.getStarted()));

    ScoreSummaryRepresentation recentlyPlayedGames = ServerFX.client.getRecentScoresByGame(3, game.getId());
    List<ScoreRepresentation> scores = recentlyPlayedGames.getScores();
    stats3Col.getChildren().removeAll(stats3Col.getChildren());

    stats3Col.setAlignment(Pos.CENTER);

    if (scores.isEmpty()) {
      Label noScoresLabel = new Label("No scores found.");
      noScoresLabel.setStyle("-fx-font-size: 20px;-fx-text-fill: #FFFFFF;");
      noScoresLabel.setPadding(new Insets(50, 0, 0, 0));
      stats3Col.getChildren().add(noScoresLabel);

      Label info = new Label("(Note that this list may be filtered.)");
      info.setStyle("-fx-font-size:16px;-fx-text-fill: #FFFFFF;");
      stats3Col.getChildren().add(info);
    }

    for (ScoreRepresentation score : scores) {
      try {
        FXMLLoader loader = new FXMLLoader(WidgetLatestScoreItemController.class.getResource("widget-latest-score-item.fxml"));
        Pane row = loader.load();
        row.setPrefWidth(stats3Col.getPrefWidth() - 24);
        WidgetLatestScoreItemController controller = loader.getController();
        controller.setData(game, frontendMedia, score);

        stats3Col.getChildren().add(row);
      }
      catch (IOException e) {
        LOG.error("Failed to load paused scores: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    tile1Controller = TileFactory.createCustomTile(stats1Col);
    tile2Controller = TileFactory.createCustomTile(stats1Col);
    tile3Controller = TileFactory.createCustomTile(stats2Col);
    tile4Controller = TileFactory.createCustomTile(stats2Col);
  }
}

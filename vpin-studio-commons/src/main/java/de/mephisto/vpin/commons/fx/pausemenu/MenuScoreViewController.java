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

public class MenuScoreViewController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MenuScoreViewController.class);

  @FXML
  private ImageView wheelImage;

  @FXML
  private ImageView sectionIcon;

  @FXML
  private Label nameLabel;

  @FXML
  private Label versionLabel;

  @FXML
  private Label authorsLabel;

  @FXML
  private Label scoreInfoLabel;

  @FXML
  private VBox mainColumn;

  public void setGame(GameRepresentation game, GameStatus status, VpsTable tableById, Image sectionImage) {
    this.nameLabel.setText(game.getGameDisplayName());
    this.versionLabel.setText("");
    this.authorsLabel.setText("");
    this.scoreInfoLabel.setText("");

    this.sectionIcon.setImage(sectionImage);

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

//    GameScoreValidation scoreValidation = PauseMenu.client.getGameService().getGameScoreValidation(game.getId());
//    boolean valid = scoreValidation.isValidScoreConfiguration();
//    if (!StringUtils.isEmpty(game.getRom())) {
//      if (scoreValidation.getRomStatus() == null & scoreValidation.getHighscoreFilenameStatus() == null) {
//        scoreInfoLabel.setText("ROM: \"" + game.getRom() + "\" (supported)");
//      }
//      else {
//        if(scoreValidation.getHighscoreFilenameStatus() != null) {
//          scoreInfoLabel.setText("ROM: \"" + game.getRom() + "\" (" + scoreValidation.getHighscoreFilenameStatus() + ")");
//        }
//        else {
//          scoreInfoLabel.setText("ROM: \"" + game.getRom() + "\" (" + scoreValidation.getRomStatus() + ")");
//        }
//      }
//    }

    InputStream imageStream = PauseMenu.client.getGameMediaItem(game.getId(), VPinScreen.Wheel);
    if (imageStream == null) {
      imageStream = ServerFX.class.getResourceAsStream("avatar-blank.png");
    }
    Image image = new Image(imageStream);
    wheelImage.setImage(image);



//    for (ScoreRepresentation score : scores) {
//      try {
//        FXMLLoader loader = new FXMLLoader(WidgetLatestScoreItemController.class.getResource("widget-latest-score-item.fxml"));
//        Pane row = loader.load();
//        row.setPrefWidth(500);
//        WidgetLatestScoreItemController controller = loader.getController();
//        controller.setData(game, frontendMedia, score);
//        mainColumn.getChildren().add(row);
//      }
//      catch (IOException e) {
//        LOG.error("Failed to load paused scores: " + e.getMessage(), e);
//      }
//    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
  }
}

package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.fx.widgets.WidgetController;
import de.mephisto.vpin.commons.utils.ScoreGraphUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.descriptors.ResetHighscoreDescriptor;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.*;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.MediaUtil;
import eu.hansolo.tilesfx.Tile;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class TablesSidebarHighscoresController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarHighscoresController.class);

  @FXML
  private Label hsTypeLabel;

  @FXML
  private Label hsFileLabel;

  @FXML
  private Label hsLastModifiedLabel;

  @FXML
  private Label hsStatusLabel;

  @FXML
  private Label hsLastScannedLabel;

  @FXML
  private Label hsRecordLabel;

  @FXML
  private VBox formattedScoreWrapper;

  @FXML
  private VBox rawScoreWrapper;

  @FXML
  private VBox scoreGraphWrapper;

  @FXML
  private Button resetBtn;

  @FXML
  private Button scanHighscoreBtn;

  @FXML
  private Button cardBtn;

  @FXML
  private Label rawScoreLabel;

  @FXML
  private Label formattedScoreLabel;

  @FXML
  private Label rawTitleLabel;

  @FXML
  private Label formattedTitleLabel;

  @FXML
  private BorderPane scoreGraph;

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;

  // Add a public no-args constructor
  public TablesSidebarHighscoresController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }

  @FXML
  private void onCard() {
    if (this.game.isPresent()) {
      GameRepresentation g = this.game.get();
      boolean b = Studio.client.getHighscoreCardsService().generateHighscoreCardSample(g);
      if (b) {
        ByteArrayInputStream s = Studio.client.getHighscoreCardsService().getHighscoreCard(g);
        MediaUtil.openMedia(s);
      }
      else {
        ScoreSummaryRepresentation summary = Studio.client.getGameService().getGameScores(g.getId());
        String status = summary.getMetadata().getStatus();
        WidgetFactory.showAlert(Studio.stage, "Card Generation Failed.", "The card generation failed: " + status);
      }
    }
  }

  @FXML
  private void onScan() {
    refreshView(game, true);
  }

  @FXML
  private void onScoreReset() {
    if (this.game.isPresent()) {
      GameRepresentation g = this.game.get();
      ResetHighscoreDescriptor reset = Dialogs.openHighscoreResetDialog(g);
      if (reset != null) {
        try {
          Studio.client.getGameService().resetHighscore(reset);
        } catch (Exception e) {
          LOG.error("Failed to reset highscore: " + e.getMessage(), e);
        } finally {
          this.refreshView(this.game, true);
        }
      }
    }
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.refreshView(game, false);
  }

  public void refreshView(Optional<GameRepresentation> g, boolean forceRescan) {
    rawScoreLabel.setText("");
    formattedScoreLabel.setText("");

    this.hsFileLabel.setText("-");
    this.hsStatusLabel.setText("-");
    this.hsTypeLabel.setText("-");
    this.hsLastModifiedLabel.setText("-");
    this.hsLastScannedLabel.setText("-");
    this.hsRecordLabel.setText("-");

    rawTitleLabel.setVisible(false);
    formattedTitleLabel.setVisible(false);

    rawScoreWrapper.setVisible(false);
    formattedScoreWrapper.setVisible(false);
    scoreGraphWrapper.setVisible(false);

    scanHighscoreBtn.setDisable(true);
    cardBtn.setDisable(true);
    resetBtn.setDisable(true);

    if (g.isPresent()) {
      GameRepresentation game = g.get();
      scanHighscoreBtn.setDisable(false);

      ScoreSummaryRepresentation summary = Studio.client.getGameService().getGameScores(game.getId());
      HighscoreMetadataRepresentation metadata = summary.getMetadata();
      if (forceRescan) {
        metadata = Studio.client.getGameService().scanGameScore(game.getId());
        EventManager.getInstance().notifyTableChange(game.getId(), game.getRom());
      }

      ScoreListRepresentation scoreHistory = Studio.client.getGameService().getScoreHistory(game.getId());
      hsRecordLabel.setText(String.valueOf(scoreHistory.getScores().size()));
      if (!scoreHistory.getScores().isEmpty()) {
        Tile highscoresGraphTile = ScoreGraphUtil.createGraph(scoreHistory);
        scoreGraph.setCenter(highscoresGraphTile);
      }

      if (metadata != null) {
        if (metadata.getFilename() != null) {
          this.hsFileLabel.setText(metadata.getFilename());
        }

        if (metadata.getStatus() != null) {
          this.hsStatusLabel.setText(metadata.getStatus());
        }

        if (metadata.getType() != null) {
          this.hsTypeLabel.setText(metadata.getType());
        }

        if (metadata.getModified() != null) {
          this.hsLastModifiedLabel.setText(SimpleDateFormat.getDateTimeInstance().format(metadata.getModified()));
        }

        if (metadata.getScanned() != null) {
          this.hsLastScannedLabel.setText(SimpleDateFormat.getDateTimeInstance().format(metadata.getScanned()));
        }

        if (!summary.getScores().isEmpty()) {
          cardBtn.setDisable(false);
          resetBtn.setDisable(false);

          rawTitleLabel.setVisible(true);
          rawScoreWrapper.setVisible(true);
          scoreGraphWrapper.setVisible(true);

          rawScoreLabel.setFont(WidgetController.getScoreFontText());
          rawScoreLabel.setText(summary.getRaw());

          List<ScoreRepresentation> scores = summary.getScores();
          StringBuilder builder = new StringBuilder();
          for (ScoreRepresentation score : scores) {
            builder.append("#");
            builder.append(score.getPosition());
            builder.append(" ");
            builder.append(score.getPlayerInitials());
            builder.append("   ");
            builder.append(score.getScore());
            builder.append("\n");
          }

          formattedTitleLabel.setVisible(true);
          formattedScoreWrapper.setVisible(true);

          formattedScoreLabel.setFont(WidgetController.getScoreFontText());
          formattedScoreLabel.setText(builder.toString());
        }
      }
    }
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }
}
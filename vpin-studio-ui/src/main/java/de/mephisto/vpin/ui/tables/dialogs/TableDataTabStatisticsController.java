package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.alx.AlxSummary;
import de.mephisto.vpin.restclient.alx.AlxTileEntry;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.tables.alx.AlxDialogs;
import de.mephisto.vpin.ui.tables.alx.AlxTileEntryController;
import de.mephisto.vpin.ui.util.FrontendUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TableDataTabStatisticsController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TableDataTabStatisticsController.class);

  @FXML
  private VBox col1;
  @FXML
  private VBox col2;

  private AlxTileEntryController timesPlayedTile;
  private AlxTileEntryController timePlayedTile;
  private AlxTileEntryController scoresCountTile;

  private Stage stage;
  private GameRepresentation game;
  private TableDetails tableDetails;


  @FXML
  private void onDelete(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    AlxDialogs.openDeleteAlxDialog(stage, game);
    refreshView();
  }

  @FXML
  private void onStatsEdit(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    AlxDialogs.openUpdateTableAlxDialog(stage, game);
    refreshView();
  }


  public void setGame(Stage stage, GameRepresentation game, TableDetails tableDetails) {
    this.stage = stage;
    this.game = game;
    this.tableDetails = tableDetails;
    Platform.runLater(() -> refreshView());
  }

  private void refreshView() {
    AlxSummary alxSummary = client.getAlxService().getAlxSummary(game.getId());
    Frontend frontend = client.getFrontendService().getFrontendCached();

    int played = 0;

    if (!alxSummary.getEntries().isEmpty()) {
      TableAlxEntry entry = alxSummary.getEntries().get(0);

      // may be overriden by TableDetail below
      played = entry.getNumberOfPlays();

      String totalTimeFormatted = null;
      try {
        if (entry.getTimePlayedSecs() < 60 * 60) {
          totalTimeFormatted = DurationFormatUtils.formatDuration(entry.getTimePlayedSecs() * 1000, "mm 'mins'", false);
        }
        else {
          totalTimeFormatted = DurationFormatUtils.formatDuration(entry.getTimePlayedSecs() * 1000, "HH 'hrs'", false);
        }
      }
      catch (Exception e) {
        LOG.error("Error calculating total play time: " + e.getMessage());
      }
      timePlayedTile.refresh(stage, new AlxTileEntry("Total Time Played", "(The total emulation time of this table)", totalTimeFormatted));
    }
    else {
      timePlayedTile.refresh(stage, new AlxTileEntry("Total Time Played", "(The total emulation time of this table)", "-"));
    }

    if(client.getFrontendService().getFrontendType().equals(FrontendType.PinballX)) {
      // Override statistics by TableDetails when numberPlays is set
      // TODO why?
      if (tableDetails != null && tableDetails.getNumberPlays() != null) {
        played = tableDetails.getNumberPlays();
      }
    }

    int scores = client.getGameService().getGameScores(game.getId()).getScores().size();
    AlxTileEntry entry = new AlxTileEntry("Recorded Scores", "(Total number of scores recorded by the VPin Studio)", String.valueOf(scores));
    scoresCountTile.refresh(stage, entry);

    timesPlayedTile.refresh(stage, new AlxTileEntry("Total Times Played",
        FrontendUtil.replaceName("(The total number of table launches from [Frontend])", frontend),
        String.valueOf(played)));
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    try {
      FXMLLoader loader = new FXMLLoader(AlxTileEntryController.class.getResource("alx-tile-entry.fxml"));
      Parent builtInRoot = loader.load();
      timesPlayedTile = loader.getController();
      col1.getChildren().add(builtInRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load tile: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(AlxTileEntryController.class.getResource("alx-tile-entry.fxml"));
      Parent builtInRoot = loader.load();
      timePlayedTile = loader.getController();
      col1.getChildren().add(builtInRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load tile: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(AlxTileEntryController.class.getResource("alx-tile-entry.fxml"));
      Parent builtInRoot = loader.load();
      scoresCountTile = loader.getController();
      col2.getChildren().add(builtInRoot);
    }
    catch (IOException e) {
      LOG.error("Failed to load tile: " + e.getMessage(), e);
    }
  }
}

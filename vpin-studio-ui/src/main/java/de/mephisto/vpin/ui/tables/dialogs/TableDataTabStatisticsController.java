package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.alx.AlxSummary;
import de.mephisto.vpin.restclient.alx.AlxTileEntry;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.ui.tables.alx.AlxTileEntryController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
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
  private VBox root;
  private AlxTileEntryController timesPlayedTile;
  private AlxTileEntryController timePlayedTile;


  public void setGame(GameRepresentation game, TableDetails tableDetails) {
    AlxSummary alxSummary = client.getAlxService().getAlxSummary(game.getId());

    int played = 0;
    if (tableDetails.getNumberPlays() != null) {
      played = tableDetails.getNumberPlays();
    }
    timesPlayedTile.refresh(new AlxTileEntry("Total Times Played", "(The total number of table launches from Popper)", String.valueOf(played)));

    if (!alxSummary.getEntries().isEmpty()) {
      TableAlxEntry entry = alxSummary.getEntries().get(0);
      String totalTimeFormatted = null;
      try {
        totalTimeFormatted = DurationFormatUtils.formatDuration(entry.getTimePlayedSecs() * 1000, "HH 'hrs'", false);
      } catch (Exception e) {
        LOG.error("Error calculating total play time: " + e.getMessage());
      }
      timePlayedTile.refresh(new AlxTileEntry("Total Time Played", "(The total emulation time of this table)", totalTimeFormatted));
    }
    else {
      timePlayedTile.refresh(new AlxTileEntry("Total Time Played", "(The total emulation time of this table)", "-"));
    }

  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    try {
      FXMLLoader loader = new FXMLLoader(AlxTileEntryController.class.getResource("alx-tile-entry.fxml"));
      Parent builtInRoot = loader.load();
      timesPlayedTile = loader.getController();
      root.getChildren().add(builtInRoot);
    } catch (IOException e) {
      LOG.error("Failed to load tile: " + e.getMessage(), e);
    }

    try {
      FXMLLoader loader = new FXMLLoader(AlxTileEntryController.class.getResource("alx-tile-entry.fxml"));
      Parent builtInRoot = loader.load();
      timePlayedTile = loader.getController();
      root.getChildren().add(builtInRoot);
    } catch (IOException e) {
      LOG.error("Failed to load tile: " + e.getMessage(), e);
    }
  }
}

package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.restclient.alx.AlxTileEntry;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.util.DateUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.List;

public class TileFactory {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static MenuCustomTileEntryController createCustomTile(Pane root) {
    try {
      FXMLLoader loader = new FXMLLoader(MenuCustomTileEntryController.class.getResource("menu-custom-tile.fxml"));
      Parent builtInRoot = loader.load();
      MenuCustomTileEntryController controller = loader.getController();
      root.getChildren().add(builtInRoot);
      return controller;
    } catch (IOException e) {
      LOG.error("Failed to load tile: " + e.getMessage(), e);
    }
    return null;
  }

  public static AlxTileEntry toTotalTimeEntry(List<TableAlxEntry> entries) {
    int total = 0;
    for (TableAlxEntry entry : entries) {
      total += entry.getTimePlayedSecs();
    }


    String totalTimeFormatted = null;
    try {
      totalTimeFormatted = DurationFormatUtils.formatDuration(total * 1000, "HH 'hrs'", false);
    } catch (Exception e) {
      LOG.error("Error calculating total play time: " + e.getMessage());
    }

    return new AlxTileEntry("Total Time Played", "(The total emulation time of this table)", totalTimeFormatted);
  }
  //--------------------------------------------------------------------------------------------------------------------


  public static AlxTileEntry toTotalScoresEntry(List<TableAlxEntry> entries) {
    int total = 0;
    for (TableAlxEntry entry : entries) {
      total += entry.getScores();
    }
    return new AlxTileEntry("Total Scores Created", "(The total amount of recorded scores)", String.valueOf(total));
  }
  //--------------------------------------------------------------------------------------------------------------------

  public static AlxTileEntry toSessionDurationTile(Date startDate) {
    long durationMs = System.currentTimeMillis() - startDate.getTime();
    long durationMin = durationMs/1000/60;
    if(durationMin == 0) {
      durationMin = 1;
    }
    return new AlxTileEntry("Play Time", "(Current playtime of this table)", durationMin + " min");
  }

  //--------------------------------------------------------------------------------------------------------------------

  public static AlxTileEntry toTotalGamesPlayedEntry(List<TableAlxEntry> entries) {
    int total = 0;
    for (TableAlxEntry entry : entries) {
      total += entry.getNumberOfPlays();
    }
    return new AlxTileEntry("Total Games Played", "(Numer of launches)", String.valueOf(total));
  }
}

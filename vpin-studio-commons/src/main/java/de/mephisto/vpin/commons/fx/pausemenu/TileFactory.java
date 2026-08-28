package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.restclient.alx.AlxTileEntry;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import de.mephisto.vpin.commons.utils.i18n.Messages;

public class TileFactory {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static MenuCustomTileEntryController createCustomTile(Pane root) {
    try {
      FXMLLoader loader = new FXMLLoader(MenuCustomTileEntryController.class.getResource("menu-custom-tile.fxml"));
      loader.setResources(Messages.getBundle());
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
      totalTimeFormatted = DurationFormatUtils.formatDuration(total * 1000L, "HH 'hrs'", false);
    } catch (Exception e) {
      LOG.error("Error calculating total play time: " + e.getMessage());
    }

    return new AlxTileEntry(Messages.get("pausemenu.tile_factory.total_time_played"), Messages.get("pausemenu.tile_factory.total_emulation_time_of_this_table"), totalTimeFormatted);
  }
  //--------------------------------------------------------------------------------------------------------------------


  public static AlxTileEntry toTotalScoresEntry(List<TableAlxEntry> entries) {
    int total = 0;
    for (TableAlxEntry entry : entries) {
      total += entry.getScores();
    }
    return new AlxTileEntry(Messages.get("pausemenu.tile_factory.total_scores_created"), Messages.get("pausemenu.tile_factory.total_amount_of_recorded_scores"), String.valueOf(total));
  }
  //--------------------------------------------------------------------------------------------------------------------

  public static AlxTileEntry toSessionDurationTile(OffsetDateTime startDate) {
    if (startDate == null) {
      return new AlxTileEntry(Messages.get("pausemenu.tile_factory.play_time"), Messages.get("pausemenu.tile_factory.current_playtime_of_this_table"), "-");
    }
    long durationMin = Duration.between(startDate, OffsetDateTime.now()).toMinutes();
    if (durationMin == 0) {
      durationMin = 1;
    }
    return new AlxTileEntry(Messages.get("pausemenu.tile_factory.play_time"), Messages.get("pausemenu.tile_factory.current_playtime_of_this_table"), durationMin + " min");
  }

  //--------------------------------------------------------------------------------------------------------------------

  public static AlxTileEntry toTotalGamesPlayedEntry(List<TableAlxEntry> entries) {
    int total = 0;
    for (TableAlxEntry entry : entries) {
      total += entry.getNumberOfPlays();
    }
    return new AlxTileEntry(Messages.get("pausemenu.tile_factory.total_games_played"), Messages.get("pausemenu.tile_factory.number_of_launches"), String.valueOf(total));
  }
}

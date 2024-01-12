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
import java.text.DateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

public class TileFactory {
  private final static Logger LOG = LoggerFactory.getLogger(TileFactory.class);

  public static MenuCustomTileEntryController createTotalTimeTile(Pane root, List<TableAlxEntry> entries) {
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

    try {
      FXMLLoader loader = new FXMLLoader(MenuCustomTileEntryController.class.getResource("menu-custom-tile.fxml"));
      Parent builtInRoot = loader.load();
      MenuCustomTileEntryController controller = loader.getController();
      controller.refresh(new AlxTileEntry("Total Time Played", "(The total emulation time of this table)", totalTimeFormatted));
      root.getChildren().add(builtInRoot);
      return  controller;
    } catch (IOException e) {
      LOG.error("Failed to load tile: " + e.getMessage(), e);
    }
    return null;
  }

  public static MenuCustomTileEntryController createTotalScoresTile(Pane root, List<TableAlxEntry> entries) {
    int total = 0;
    for (TableAlxEntry entry : entries) {
      total += entry.getScores();
    }

    try {
      FXMLLoader loader = new FXMLLoader(MenuCustomTileEntryController.class.getResource("menu-custom-tile.fxml"));
      Parent builtInRoot = loader.load();
      MenuCustomTileEntryController controller = loader.getController();
      controller.refresh(new AlxTileEntry("Total Scores Created", "(The total amount of recorded scores)", String.valueOf(total)));
      root.getChildren().add(builtInRoot);
      return  controller;
    } catch (IOException e) {
      LOG.error("Failed to load tile: " + e.getMessage(), e);
    }
    return null;
  }


  public static MenuCustomTileEntryController createTotalHighScoresTile(Pane root, List<TableAlxEntry> entries) {
    int total = 0;
    for (TableAlxEntry entry : entries) {
      total += entry.getHighscores();
    }

    try {
      FXMLLoader loader = new FXMLLoader(MenuCustomTileEntryController.class.getResource("menu-custom-tile.fxml"));
      Parent builtInRoot = loader.load();
      MenuCustomTileEntryController controller = loader.getController();
      controller.refresh(new AlxTileEntry("Total Highscores Created", "(The total amount of times a #1 score has been created)", String.valueOf(total)));
      root.getChildren().add(builtInRoot);
      return  controller;
    } catch (IOException e) {
      LOG.error("Failed to load tile: " + e.getMessage(), e);
    }
    return null;
  }

  public static MenuCustomTileEntryController createAvgWeekTimeTile(Pane root, List<TableAlxEntry> entries, Date start) {
    int total = 0;
    for (TableAlxEntry entry : entries) {
      total += entry.getTimePlayedSecs();
    }


    Instant d1i = Instant.ofEpochMilli(start.getTime());
    Instant d2i = Instant.ofEpochMilli(new Date().getTime());

    LocalDateTime startDate = LocalDateTime.ofInstant(d1i, ZoneId.systemDefault());
    LocalDateTime endDate = LocalDateTime.ofInstant(d2i, ZoneId.systemDefault());

    long weeks = ChronoUnit.WEEKS.between(startDate, endDate);

    long avgSeksPerWeek = total / weeks;
    String time = (avgSeksPerWeek / 60) + " min";
    if (avgSeksPerWeek / 60 / 60 > 0) {
      time = (avgSeksPerWeek / 60 / 60) + " hrs";
    }

    try {
      FXMLLoader loader = new FXMLLoader(MenuCustomTileEntryController.class.getResource("menu-custom-tile.fxml"));
      Parent builtInRoot = loader.load();
      MenuCustomTileEntryController controller = loader.getController();
      controller.refresh(new AlxTileEntry("Avg. Playtime / Week", "(The average time played every week, starting " + DateFormat.getDateInstance().format(start) + ")", time));
      root.getChildren().add(builtInRoot);
      return  controller;
    } catch (IOException e) {
      LOG.error("Failed to load tile: " + e.getMessage(), e);
    }
    return null;
  }

  public static MenuCustomTileEntryController createTotalGamesPlayedTile(Pane root, List<TableAlxEntry> entries) {
    int total = 0;
    for (TableAlxEntry entry : entries) {
      total += entry.getNumberOfPlays();
    }

    try {
      FXMLLoader loader = new FXMLLoader(MenuCustomTileEntryController.class.getResource("menu-custom-tile.fxml"));
      Parent builtInRoot = loader.load();
      MenuCustomTileEntryController controller = loader.getController();
      controller.refresh(new AlxTileEntry("Total Games Played", "(The total number of table launches from Popper)", String.valueOf(total)));
      root.getChildren().add(builtInRoot);
      return controller;
    } catch (IOException e) {
      LOG.error("Failed to load tile: " + e.getMessage(), e);
    }
    return null;
  }

}

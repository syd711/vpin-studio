package de.mephisto.vpin.ui.tables.alx;

import de.mephisto.vpin.restclient.alx.AlxBarEntry;
import de.mephisto.vpin.restclient.alx.AlxTileEntry;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.ui.util.PreferenceBindingUtil;
import eu.hansolo.tilesfx.Tile;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class AlxFactory {
  private final static Logger LOG = LoggerFactory.getLogger(AlxFactory.class);

  private static final List<Color> colors = Arrays.asList(Tile.BLUE, Tile.RED, Tile.ORANGE, Tile.GREEN, Tile.MAGENTA, Tile.PINK.grayscale(), Tile.YELLOW, Tile.DARK_BLUE);


  public static void createTotalTimeTile(Pane root, List<TableAlxEntry> entries) {
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
      FXMLLoader loader = new FXMLLoader(AlxTileEntryController.class.getResource("alx-tile-entry.fxml"));
      Parent builtInRoot = loader.load();
      AlxTileEntryController controller = loader.getController();
      controller.refresh(new AlxTileEntry("Total Time Played", "(The total emulation time of all tables)", totalTimeFormatted));
      root.getChildren().add(builtInRoot);
    } catch (IOException e) {
      LOG.error("Failed to load tile: " + e.getMessage(), e);
    }
  }

  public static void createTotalScoresTile(Pane root, List<TableAlxEntry> entries) {
    int total = 0;
    for (TableAlxEntry entry : entries) {
      total += entry.getScores();
    }

    try {
      FXMLLoader loader = new FXMLLoader(AlxTileEntryController.class.getResource("alx-tile-entry.fxml"));
      Parent builtInRoot = loader.load();
      AlxTileEntryController controller = loader.getController();
      controller.refresh(new AlxTileEntry("Total Scores Created", "(The total amount of recorded scores)", String.valueOf(total)));
      root.getChildren().add(builtInRoot);
    } catch (IOException e) {
      LOG.error("Failed to load tile: " + e.getMessage(), e);
    }
  }


  public static void createTotalHighScoresTile(Pane root, List<TableAlxEntry> entries) {
    int total = 0;
    for (TableAlxEntry entry : entries) {
      total += entry.getHighscores();
    }

    try {
      FXMLLoader loader = new FXMLLoader(AlxTileEntryController.class.getResource("alx-tile-entry.fxml"));
      Parent builtInRoot = loader.load();
      AlxTileEntryController controller = loader.getController();
      controller.refresh(new AlxTileEntry("Total Highscores Created", "(The total amount of times a #1 score has been created)", String.valueOf(total)));
      root.getChildren().add(builtInRoot);
    } catch (IOException e) {
      LOG.error("Failed to load tile: " + e.getMessage(), e);
    }
  }

  public static void createAvgWeekTimeTile(Pane root, List<TableAlxEntry> entries, Date start) {
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
      FXMLLoader loader = new FXMLLoader(AlxTileEntryController.class.getResource("alx-tile-entry.fxml"));
      Parent builtInRoot = loader.load();
      AlxTileEntryController controller = loader.getController();
      controller.refresh(new AlxTileEntry("Avg. Playtime / Week", "(The average time played every week, starting " + DateFormat.getDateInstance().format(start) + ")", time));
      root.getChildren().add(builtInRoot);
    } catch (IOException e) {
      LOG.error("Failed to load tile: " + e.getMessage(), e);
    }
  }

  public static void createTotalGamesPlayedTile(Pane root, List<TableAlxEntry> entries) {
    int total = 0;
    for (TableAlxEntry entry : entries) {
      total += entry.getNumberOfPlays();
    }

    try {
      FXMLLoader loader = new FXMLLoader(AlxTileEntryController.class.getResource("alx-tile-entry.fxml"));
      Parent builtInRoot = loader.load();
      AlxTileEntryController controller = loader.getController();
      controller.refresh(new AlxTileEntry("Total Games Played", "(The total number of table launches from Popper)", String.valueOf(total)));
      root.getChildren().add(builtInRoot);
    } catch (IOException e) {
      LOG.error("Failed to load tile: " + e.getMessage(), e);
    }
  }


  public static void createRecordedScores(Pane root, List<TableAlxEntry> entries) {
    List<TableAlxEntry> statEntries = new ArrayList<>(entries);
    Collections.sort(statEntries, Comparator.comparingInt(TableAlxEntry::getScores));
    Collections.reverse(statEntries);

    int maxValue = 0;
    for (TableAlxEntry entry : statEntries) {
      if (entry.getScores() > maxValue) {
        maxValue = entry.getScores();
      }
    }

    int counter = 0;
    for (TableAlxEntry alxEntry : statEntries) {
      if (alxEntry.getScores() == 0) {
        continue;
      }

      int percentage = alxEntry.getScores() * 100 / maxValue;
      AlxBarEntry entry = new AlxBarEntry(alxEntry.getDisplayName(), String.valueOf(alxEntry.getScores()), percentage, PreferenceBindingUtil.toHexString(colors.get(counter)));
      try {
        FXMLLoader loader = new FXMLLoader(AlxBarEntryController.class.getResource("alx-bar-entry.fxml"));
        Parent builtInRoot = loader.load();
        AlxBarEntryController controller = loader.getController();
        controller.refresh(entry);
        root.getChildren().add(builtInRoot);
      } catch (IOException e) {
        LOG.error("Failed to load bar: " + e.getMessage(), e);
      }
      counter++;

      if (counter >= colors.size()) {
        counter = 0;
      }
    }
  }

  public static void createLongestPlayed(Pane root, List<TableAlxEntry> entries) {
    List<TableAlxEntry> statEntries = new ArrayList<>(entries);
    Collections.sort(statEntries, Comparator.comparingInt(TableAlxEntry::getTimePlayedSecs));
    Collections.reverse(statEntries);

    int maxValue = 0;
    for (TableAlxEntry entry : statEntries) {
      if (entry.getTimePlayedSecs() > maxValue) {
        maxValue = entry.getTimePlayedSecs();
      }
    }

    int counter = 0;
    for (TableAlxEntry alxEntry : statEntries) {
      if (alxEntry.getTimePlayedSecs() <= 0) {
        continue;
      }

      int percentage = alxEntry.getTimePlayedSecs() * 100 / maxValue;
      String durationText = DurationFormatUtils.formatDuration(alxEntry.getTimePlayedSecs() * 1000, "HH 'hours', mm 'minutes'", false);
      AlxBarEntry entry = new AlxBarEntry(alxEntry.getDisplayName(), durationText, percentage, PreferenceBindingUtil.toHexString(colors.get(counter)));
      try {
        FXMLLoader loader = new FXMLLoader(AlxBarEntryController.class.getResource("alx-bar-entry.fxml"));
        Parent builtInRoot = loader.load();
        AlxBarEntryController controller = loader.getController();
        controller.refresh(entry);
        root.getChildren().add(builtInRoot);
      } catch (IOException e) {
        LOG.error("Failed to load bar: " + e.getMessage(), e);
      }
      counter++;

      if (counter >= colors.size()) {
        counter = 0;
      }
    }
  }

  public static void createMostPlayed(Pane root, List<TableAlxEntry> entries) {
    List<TableAlxEntry> mostPlayedEntries = new ArrayList<>(entries);
    Collections.sort(mostPlayedEntries, Comparator.comparingInt(TableAlxEntry::getNumberOfPlays));
    Collections.reverse(mostPlayedEntries);

    int maxValue = 0;
    for (TableAlxEntry mostPlayedEntry : mostPlayedEntries) {
      if (mostPlayedEntry.getNumberOfPlays() > maxValue) {
        maxValue = mostPlayedEntry.getNumberOfPlays();
      }
    }

    int counter = 0;
    for (TableAlxEntry alxEntry : mostPlayedEntries) {
      if (alxEntry.getNumberOfPlays() <= 0) {
        continue;
      }

      int percentage = alxEntry.getNumberOfPlays() * 100 / maxValue;
      AlxBarEntry entry = new AlxBarEntry(alxEntry.getDisplayName(), String.valueOf(alxEntry.getNumberOfPlays()), percentage, PreferenceBindingUtil.toHexString(colors.get(counter)));
      try {
        FXMLLoader loader = new FXMLLoader(AlxBarEntryController.class.getResource("alx-bar-entry.fxml"));
        Parent builtInRoot = loader.load();
        AlxBarEntryController controller = loader.getController();
        controller.refresh(entry);
        root.getChildren().add(builtInRoot);
      } catch (IOException e) {
        LOG.error("Failed to load bar: " + e.getMessage(), e);
      }
      counter++;

      if (counter >= colors.size()) {
        counter = 0;
      }
    }
  }
}

package de.mephisto.vpin.ui.alx;

import de.mephisto.vpin.restclient.alx.AlxBarEntry;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.ui.util.BindingUtil;
import eu.hansolo.tilesfx.Tile;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class AlxFactory {
  private final static Logger LOG = LoggerFactory.getLogger(AlxFactory.class);

  private static final List<Color> colors = Arrays.asList(Tile.BLUE, Tile.RED, Tile.ORANGE, Tile.GREEN, Tile.MAGENTA, Tile.PINK.grayscale(), Tile.YELLOW, Tile.DARK_BLUE);

//    Tile totalGamesPlayedTile = TileBuilder.create().skinType(Tile.SkinType.CHARACTER)
//      .backgroundColor(Color.web("#2a2a2a"))
//      .autoScale(false)
//      .borderColor(Color.TRANSPARENT)
//      .borderWidth(0)
//      .title("Total Games Played")
//      .customFontEnabled(true)
//      .description(String.valueOf(totalGamesPlayed))
//      .decimals(0)
//      .build();
//
//    root.getChildren().add(totalGamesPlayedTile);
//
//    String totalTimeFormatted = DurationFormatUtils.formatDuration(totalTimePlayed * 1000, "HH 'hr'", false);
//    Tile totalTimePlayedTile = TileBuilder.create().skinType(Tile.SkinType.CHARACTER)
//      .backgroundColor(Color.web("#2a2a2a"))
//      .autoScale(false)
//      .borderColor(Color.TRANSPARENT)
//      .borderWidth(0)
//      .title("Total Time Played")
//      .customFontEnabled(true)
//      .prefWidth(400)
//      .description(totalTimeFormatted)
//      .decimals(0)
//      .build();
//
//    root.getChildren().add(totalTimePlayedTile);


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
      int percentage = alxEntry.getScores() * 100 / maxValue;
      AlxBarEntry entry = new AlxBarEntry(alxEntry.getDisplayName(), String.valueOf(alxEntry.getScores()), percentage, BindingUtil.toHexString(colors.get(counter)));
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
      int percentage = alxEntry.getTimePlayedSecs() * 100 / maxValue;
      String durationText = DurationFormatUtils.formatDuration(alxEntry.getTimePlayedSecs() * 1000, "HH 'hours', mm 'minutes'", false);
      AlxBarEntry entry = new AlxBarEntry(alxEntry.getDisplayName(), durationText, percentage, BindingUtil.toHexString(colors.get(counter)));
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
      int percentage = alxEntry.getNumberOfPlays() * 100 / maxValue;
      AlxBarEntry entry = new AlxBarEntry(alxEntry.getDisplayName(), String.valueOf(alxEntry.getNumberOfPlays()), percentage, BindingUtil.toHexString(colors.get(counter)));
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

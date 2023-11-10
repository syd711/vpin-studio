package de.mephisto.vpin.ui.system;

import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.skins.BarChartItem;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class TileFactory {
  private static final List<Color> colors = Arrays.asList(Tile.BLUE, Tile.RED, Tile.ORANGE, Tile.GREEN, Tile.MAGENTA, Tile.PINK.grayscale(), Tile.YELLOW, Tile.DARK_BLUE);

  public static void createAlxTiles(Pane root) {
    List<TableAlxEntry> entries = client.getAlxService().getAlxEntries();

    createMostPlayedTile(root, entries);
    createLongestPlayedTile(root, entries);

    int totalGamesPlayed = 0;
    int totalTimePlayed = 0;

    for (TableAlxEntry alxEntry : entries) {
      totalGamesPlayed += alxEntry.getNumberOfPlays();
      totalTimePlayed += alxEntry.getTimePlayedSecs();
    }

    Tile totalGamesPlayedTile = TileBuilder.create().skinType(Tile.SkinType.CHARACTER)
      .backgroundColor(Color.web("#2a2a2a"))
      .autoScale(false)
      .borderColor(Color.TRANSPARENT)
      .borderWidth(0)
      .title("Total Games Played")
      .customFontEnabled(true)
      .description(String.valueOf(totalGamesPlayed))
      .decimals(0)
      .build();

    root.getChildren().add(totalGamesPlayedTile);

    String totalTimeFormatted = DurationFormatUtils.formatDuration(totalTimePlayed * 1000, "HH 'hr'", false);
    Tile totalTimePlayedTile = TileBuilder.create().skinType(Tile.SkinType.CHARACTER)
      .backgroundColor(Color.web("#2a2a2a"))
      .autoScale(false)
      .borderColor(Color.TRANSPARENT)
      .borderWidth(0)
      .title("Total Time Played")
      .customFontEnabled(true)
      .prefWidth(400)
      .description(totalTimeFormatted)
      .decimals(0)
      .build();

    root.getChildren().add(totalTimePlayedTile);
  }

  private static void createLongestPlayedTile(Pane root, List<TableAlxEntry> entries) {
    ArrayList<TableAlxEntry> mostPlayedEntries = new ArrayList<>(entries);
    Collections.sort(mostPlayedEntries, Comparator.comparingInt(TableAlxEntry::getTimePlayedSecs));
    Collections.reverse(mostPlayedEntries);

    Iterator<Color> iterator = colors.iterator();
    List<TableAlxEntry> tableAlxEntries = mostPlayedEntries.subList(0, 5);
    List<BarChartItem> filtered = new ArrayList<>();
    int maxTimePlayed = 0;
    int counter = 1;
    for (TableAlxEntry alxEntry : tableAlxEntries) {
      BarChartItem chartItem = new BarChartItem(" " + counter + ". " + alxEntry.getDisplayName(), alxEntry.getTimePlayedSecs(), Tile.BLUE);
      String s = DurationFormatUtils.formatDuration(alxEntry.getTimePlayedSecs()*1000, "HH 'hours', mm 'minutes'", false);
      chartItem.setFormatString(s);
      if (!iterator.hasNext()) {
        iterator = colors.iterator();
      }

      if (alxEntry.getTimePlayedSecs() > maxTimePlayed) {
        maxTimePlayed = alxEntry.getTimePlayedSecs();
      }

      chartItem.setBarBackgroundColor(Color.TRANSPARENT);
      chartItem.setBarColor(iterator.next());
      chartItem.setPadding(new Insets(12, 12, 12, 12));
      filtered.add(chartItem);
      counter++;
    }


    Tile longestPlayed = TileBuilder.create()
      .skinType(Tile.SkinType.BAR_CHART)
      .backgroundColor(Color.web("#2a2a2a"))
      .autoScale(false)
      .borderColor(Color.TRANSPARENT)
      .borderWidth(0)
      .title("Longest Played Tables")
      .customFontEnabled(true)
      .barChartItems(filtered)
      .maxValue(maxTimePlayed)
      .decimals(0)
      .build();
    root.getChildren().add(longestPlayed);
  }

  private static void createMostPlayedTile(Pane root, List<TableAlxEntry> entries) {
    ArrayList<TableAlxEntry> mostPlayedEntries = new ArrayList<>(entries);
    Collections.sort(mostPlayedEntries, Comparator.comparingInt(TableAlxEntry::getNumberOfPlays));
    Collections.reverse(mostPlayedEntries);

    Iterator<Color> iterator = colors.iterator();
    List<TableAlxEntry> tableAlxEntries = mostPlayedEntries.subList(0, 5);
    List<BarChartItem> mostPlayedItems = new ArrayList<>();
    int maxNumberOfPlays = 0;
    int counter = 1;
    for (TableAlxEntry alxEntry : tableAlxEntries) {
      BarChartItem chartItem = new BarChartItem(" " + counter + ". " + alxEntry.getDisplayName(), alxEntry.getNumberOfPlays(), Tile.BLUE);
      if (!iterator.hasNext()) {
        iterator = colors.iterator();
      }
      if (alxEntry.getNumberOfPlays() > maxNumberOfPlays) {
        maxNumberOfPlays = alxEntry.getNumberOfPlays();
      }
      chartItem.setBarBackgroundColor(Color.TRANSPARENT);
      chartItem.setBarColor(iterator.next());
      chartItem.setPadding(new Insets(12, 12, 12, 12));
      mostPlayedItems.add(chartItem);
      counter++;
    }

    Tile mostPlayedTile = TileBuilder.create()
      .skinType(Tile.SkinType.BAR_CHART)
      .backgroundColor(Color.web("#2a2a2a"))
      .autoScale(false)
      .borderColor(Color.TRANSPARENT)
      .borderWidth(0)
      .title("Most Played Tables")
      .customFontEnabled(true)
      .barChartItems(mostPlayedItems)
      .maxValue(maxNumberOfPlays)
      .decimals(0)
      .build();
    root.getChildren().add(mostPlayedTile);
  }
}

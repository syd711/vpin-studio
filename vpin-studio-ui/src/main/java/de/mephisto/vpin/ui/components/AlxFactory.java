package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.components.AlxBarEntry;
import de.mephisto.vpin.ui.util.BindingUtil;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.skins.BarChartItem;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class AlxFactory {
  private final static Logger LOG = LoggerFactory.getLogger(AlxFactory.class);

  private static final List<Color> colors = Arrays.asList(Tile.BLUE, Tile.RED, Tile.ORANGE, Tile.GREEN, Tile.MAGENTA, Tile.PINK.grayscale(), Tile.YELLOW, Tile.DARK_BLUE);

  public static void createStats(Pane root) {
    List<TableAlxEntry> entries = client.getAlxService().getAlxEntries();

    createMostPlayedTile(root, entries);
    createLongestPlayedTile(root, entries);

    int totalGamesPlayed = 0;
    int totalTimePlayed = 0;

    for (TableAlxEntry alxEntry : entries) {
      totalGamesPlayed += alxEntry.getNumberOfPlays();
      totalTimePlayed += alxEntry.getTimePlayedSecs();
    }

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

    int maxValue = 0;
    for (TableAlxEntry mostPlayedEntry : mostPlayedEntries) {
      if(mostPlayedEntry.getNumberOfPlays() > maxValue) {
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

      if(counter >= colors.size()) {
        counter = 0;
      }
    }

//    Tile mostPlayedTile = TileBuilder.create()
//      .skinType(Tile.SkinType.BAR_CHART)
//      .backgroundColor(Color.web("#2a2a2a"))
//      .autoScale(false)
//      .borderColor(Color.TRANSPARENT)
//      .borderWidth(0)
//      .title("Most Played Tables")
//      .customFontEnabled(true)
//      .barChartItems(mostPlayedItems)
//      .maxValue(maxNumberOfPlays)
//      .decimals(0)
//      .build();
//    root.getChildren().add(mostPlayedTile);
  }
}

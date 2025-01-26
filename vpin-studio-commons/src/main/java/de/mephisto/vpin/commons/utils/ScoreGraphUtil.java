package de.mephisto.vpin.commons.utils;

import de.mephisto.vpin.restclient.highscores.ScoreListRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ScoreGraphUtil {

  @Nullable
  public static Tile createGraph(ScoreListRepresentation list) {
    if (!list.getScores().isEmpty()) {
      List<XYChart.Series<String, Long>> series = new ArrayList<>();
      ScoreSummaryRepresentation firstEntry = list.getScores().get(0);
      if(firstEntry == null) {
        return null;
      }

      List<ScoreRepresentation> scoreList = firstEntry.getScores();
      for (int i = 0; i < scoreList.size(); i++) {
        XYChart.Series<String, Long> scoreGraph = new XYChart.Series<>();
        scoreGraph.setName("#" + (i+1));
        series.add(scoreGraph);
      }

      //every summary is one history version
      List<ScoreSummaryRepresentation> scores = list.getScores();
      for (ScoreSummaryRepresentation score : scores) {
        List<ScoreRepresentation> entryList = score.getScores();
        for (int i = 0; i < entryList.size(); i++) {
          ScoreRepresentation s = entryList.get(i);
          if(i > (series.size()-1)) {
            break;
          }
          series.get(i).getData().add(new XYChart.Data<>(SimpleDateFormat.getDateTimeInstance().format(s.getCreatedAt()), s.getScore()));
        }
      }

      return TileBuilder.create()
          .skinType(Tile.SkinType.SMOOTHED_CHART)
          .maxWidth(568)
          .textSize(Tile.TextSize.SMALL)
          .chartType(Tile.ChartType.LINE)
          .borderWidth(1)
          .snapToTicks(true)
          .maxValue(10)
          .checkSectionsForValue(true)
          .startFromZero(true)
          .description("")
          .tickLabelsYVisible(true)
          .dataPointsVisible(true)
          .decimals(1)
          .borderColor(Color.web("#111111"))
          .animated(true)
          .smoothing(false)
          .series(series)
          .build();
    }
    return null;
  }
}

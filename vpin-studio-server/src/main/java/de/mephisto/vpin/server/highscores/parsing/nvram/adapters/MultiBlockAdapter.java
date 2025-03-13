package de.mephisto.vpin.server.highscores.parsing.nvram.adapters;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.parsing.ScoreListAdapter;
import de.mephisto.vpin.server.highscores.parsing.listadapters.DefaultAdapter;
import de.mephisto.vpin.server.highscores.parsing.listadapters.ScoreListAdapterBase;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class MultiBlockAdapter extends DefaultAdapter implements ScoreNvRamAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(MultiBlockAdapter.class);

  private String name;
  private int totalScores;

  public MultiBlockAdapter(String name, int totalScores) {
    this.name = name;
    this.totalScores = totalScores;
  }

  public MultiBlockAdapter() {

  }

  @Override
  public boolean isApplicable(@NonNull String nvRam, @NonNull List<String> lines) {
    return nvRam.equals(name);
  }

  @NonNull
  public String convert(@NonNull String nvRam, @NonNull List<String> lines) {
    List<String> builder = new ArrayList<>();

    int index = 1;
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      if (isScoreLine(line, index) || isScoreLine(line, index - totalScores / 2)) {
        line = line.substring(1);
        line = index + line;
        builder.add(line);
        index++;
      }

      if (builder.size() == totalScores) {
        break;
      }
    }

    return String.join("\n", builder);
  }
}
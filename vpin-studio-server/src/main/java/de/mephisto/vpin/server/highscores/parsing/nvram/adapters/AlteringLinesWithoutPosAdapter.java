package de.mephisto.vpin.server.highscores.parsing.nvram.adapters;

import de.mephisto.vpin.server.highscores.parsing.listadapters.DefaultAdapter;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AlteringLinesWithoutPosAdapter extends DefaultAdapter implements ScoreNvRamAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(AlteringLinesWithoutPosAdapter.class);

  private String name;
  private int totalScores;

  public AlteringLinesWithoutPosAdapter(String name, int totalScores) {
    this.name = name;
    this.totalScores = totalScores;
  }

  public AlteringLinesWithoutPosAdapter() {

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
      if (i % 2 == 1) {
        line = "#" + index + "   "  + line;
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
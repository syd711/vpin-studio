package de.mephisto.vpin.server.highscores.parsing.text.adapters;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AlteringScoreInitialsLinesAdapter extends ScoreTextFileAdapterImpl {

  private final int lineCount;
  private final int start;
  private final int entryCount;

  public AlteringScoreInitialsLinesAdapter(int lineCount, int start, int entryCount) {
    this.lineCount = lineCount;
    this.start = start;
    this.entryCount = entryCount;
  }

  @Override
  public boolean isApplicable(@NonNull File file, @NonNull List<String> lines) {
    return lines.size() == lineCount;
  }

  @Override
  public String convert(@NotNull File file, @NotNull List<String> lines) {
    return super.convertAlteringScoreLines(start, entryCount, lines);
  }

  @Override
  public List<String> resetHighscore(@NotNull File file, @NotNull List<String> lines) {
    List<String> newScoreText = new ArrayList<>();
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      if (i >= start && i < start + (entryCount * 2) && i % 2 == 0) {
        line = "0";
      }
      else if (i >= start && i < start + (entryCount * 2) && i % 2 == 1) {
        line = "???";
      }
      newScoreText.add(line);
    }
    return newScoreText;
  }
}

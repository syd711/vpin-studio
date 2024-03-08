package de.mephisto.vpin.server.highscores.parsing.text.adapters;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class AlteringScoreInitialsBlocksAdapter extends ScoreTextFileAdapterImpl {

  private int lineCount;
  private String name;

  private final int start;
  private final int size;

  public AlteringScoreInitialsBlocksAdapter(int lineCount, int start, int size) {
    this.lineCount = lineCount;
    this.start = start;
    this.size = size;
  }

  public AlteringScoreInitialsBlocksAdapter(String name, int start, int size) {
    this.name = name;
    this.start = start;
    this.size = size;
  }

  @Override
  public boolean isApplicable(@NonNull File file, @NonNull List<String> lines) {
    if (name != null) {
      return file.getName().equals(name);
    }
    return lines.size() == lineCount;
  }

  @Override
  public String convert(@NotNull File file, @NotNull List<String> lines, @Nullable String defaultInitials) {
    return super.convertAlteringScoreBlocks(start, size, lines);
  }
}

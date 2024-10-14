package de.mephisto.vpin.server.highscores.parsing.text.adapters;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AlteringScoreInitialsBlocksAdapter extends ScoreTextFileAdapterImpl {

  private int lineCount;
  private List<String> fileNames;

  private int start;
  private int size;

  public AlteringScoreInitialsBlocksAdapter() {
  }

  public int getLineCount() {
    return lineCount;
  }

  public void setLineCount(int lineCount) {
    this.lineCount = lineCount;
  }

  public List<String> getFileNames() {
    return fileNames;
  }

  public void setFileNames(List<String> fileNames) {
    this.fileNames = fileNames;
  }

  public int getStart() {
    return start;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  @Override
  public boolean isApplicable(@NonNull File file, @NonNull List<String> lines) {
    if (fileNames != null) {
      return fileNames.contains(file.getName());
    }
    return lines.size() == lineCount;
  }

  @Override
  public String convert(@NotNull File file, @NotNull List<String> lines) {
    return super.convertAlteringScoreBlocks(start, size, lines);
  }

  @Override
  public List<String> resetHighscore(@NotNull File file, @NotNull List<String> lines) {
    List<String> newScoreText = new ArrayList<>();
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      if (i >= start && i < start + size) {
        line = "0";
      }
      else if (i >= start + size && i < (start + size +size)) {
        line = "???";
      }
      newScoreText.add(line);
    }
    return newScoreText;
  }
}

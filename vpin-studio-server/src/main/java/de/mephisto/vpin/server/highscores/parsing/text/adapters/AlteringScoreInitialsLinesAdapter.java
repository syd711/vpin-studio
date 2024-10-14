package de.mephisto.vpin.server.highscores.parsing.text.adapters;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AlteringScoreInitialsLinesAdapter extends ScoreTextFileAdapterImpl {

  private int start;
  private int size;
  private int lineCount;
  private List<String> fileNames;

  public AlteringScoreInitialsLinesAdapter() {
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

  @Override
  public boolean isApplicable(@NonNull File file, @NonNull List<String> lines) {
    if(fileNames != null && fileNames.contains(file.getName())) {
      return true;
    }
    return lines.size() == lineCount;
  }

  @Override
  public String convert(@NotNull File file, @NotNull List<String> lines) {
    return super.convertAlteringScoreLines(start, size, lines);
  }

  @Override
  public List<String> resetHighscore(@NotNull File file, @NotNull List<String> lines) {
    List<String> newScoreText = new ArrayList<>();
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      if (i >= start && i < start + (size * 2) && i % 2 == 0) {
        line = "0";
      }
      else if (i >= start && i < start + (size * 2) && i % 2 == 1) {
        line = "???";
      }
      newScoreText.add(line);
    }
    return newScoreText;
  }
}

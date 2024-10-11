package de.mephisto.vpin.server.highscores.parsing.text.adapters;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ThreePlayersAdapter implements ScoreTextFileAdapter {
  private List<String> fileNames;
  private int start = 1;
  private int lineCount;

  public ThreePlayersAdapter() {
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

  public int getLineCount() {
    return lineCount;
  }

  public void setLineCount(int lineCount) {
    this.lineCount = lineCount;
  }

  @Override
  public boolean isApplicable(@NotNull File file, @NotNull List<String> lines) {
    if (fileNames != null && fileNames.contains(file.getName())) {
      return true;
    }
    return lines.size() == lineCount;
  }

  @Override
  public List<String> resetHighscore(@NotNull File file, @NotNull List<String> lines) {
    List<String> newScoreText = new ArrayList<>();

    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      if (i >= start && i < start + 3) {
        newScoreText.add("0");
        continue;
      }
      newScoreText.add(line);
    }
    return newScoreText;
  }

  @Override
  public String convert(@NotNull File file, @NotNull List<String> lines) {
    StringBuilder builder = new StringBuilder("HIGHEST SCORES\n");
    String score1 = lines.get(start);
    String score2 = lines.get(start + 1);
    String score3 = lines.get(start + 2);
    builder.append("#1");
    builder.append(" ");
    builder.append("???");
    builder.append("   ");
    builder.append(score1);
    builder.append("\n");

    builder.append("#2");
    builder.append(" ");
    builder.append("???");
    builder.append("   ");
    builder.append(score2);
    builder.append("\n");

    builder.append("#3");
    builder.append(" ");
    builder.append("???");
    builder.append("   ");
    builder.append(score3);
    builder.append("\n");

    return builder.toString();
  }
}

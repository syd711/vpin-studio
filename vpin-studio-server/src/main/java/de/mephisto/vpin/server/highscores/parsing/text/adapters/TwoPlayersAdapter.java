package de.mephisto.vpin.server.highscores.parsing.text.adapters;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TwoPlayersAdapter implements ScoreTextFileAdapter {
  private List<String> fileNames;
  private int scoreLine1 = 1;
  private int scoreLine2 = 2;

  private int lineCount;

  public TwoPlayersAdapter() {
  }

  public List<String> getFileNames() {
    return fileNames;
  }

  public void setFileNames(List<String> fileNames) {
    this.fileNames = fileNames;
  }

  public int getScoreLine1() {
    return scoreLine1;
  }

  public void setScoreLine1(int scoreLine1) {
    this.scoreLine1 = scoreLine1;
  }

  public int getScoreLine2() {
    return scoreLine2;
  }

  public void setScoreLine2(int scoreLine2) {
    this.scoreLine2 = scoreLine2;
  }

  public int getLineCount() {
    return lineCount;
  }

  public void setLineCount(int lineCount) {
    this.lineCount = lineCount;
  }

  @Override
  public boolean isApplicable(@NonNull File file, @NonNull List<String> lines) {
    if (fileNames != null && fileNames.contains(file.getName())) {
      return true;
    }
    return lines.size() == lineCount;
  }

  @Override
  public List<String> resetHighscore(@NonNull File file, @NonNull List<String> lines, long score) {
    List<String> newScoreText = new ArrayList<>();

    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      if (i == scoreLine1 || i == scoreLine2) {
        newScoreText.add(String.valueOf(score));
        continue;
      }
      newScoreText.add(line);
    }
    return newScoreText;
  }

  @Override
  public String convert(@NonNull File file, @NonNull List<String> lines) {
    StringBuilder builder = new StringBuilder("HIGHEST SCORES\n");
    String score1 = lines.get(scoreLine1);
    String score2 = lines.get(scoreLine2);
    builder.append("#1");
    builder.append(" ");
    builder.append("???");
    builder.append("   ");
    builder.append(score2);
    builder.append("\n");

    builder.append("#2");
    builder.append(" ");
    builder.append("???");
    builder.append("   ");
    builder.append(score1);
    builder.append("\n");

    return builder.toString();
  }
}

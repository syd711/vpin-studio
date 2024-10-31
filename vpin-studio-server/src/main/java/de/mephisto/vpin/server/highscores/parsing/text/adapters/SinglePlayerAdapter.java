package de.mephisto.vpin.server.highscores.parsing.text.adapters;

import de.mephisto.vpin.restclient.util.ScoreFormatUtil;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SinglePlayerAdapter extends ScoreTextFileAdapterImpl {

  private List<String> fileNames;
  private int scoreLine;

  public SinglePlayerAdapter() {

  }

  public List<String> getFileNames() {
    return fileNames;
  }

  public void setFileNames(List<String> fileNames) {
    this.fileNames = fileNames;
  }

  public int getScoreLine() {
    return scoreLine;
  }

  public void setScoreLine(int scoreLine) {
    this.scoreLine = scoreLine;
  }

  @Override
  public boolean isApplicable(@NonNull File file, @NonNull List<String> lines) {
    if (fileNames != null) {
      return fileNames.contains(file.getName()) || file.getName().toLowerCase().endsWith("postit.txt");
    }
    return lines.size() == 1;
  }

  @Override
  public String convert(@NonNull File file, @NonNull List<String> lines) {
    StringBuilder builder = new StringBuilder("HIGHEST SCORES\n");
    String score1 = lines.get(scoreLine);
    builder.append("#1");
    builder.append(" ");
    builder.append("???");
    builder.append("   ");
    builder.append(ScoreFormatUtil.cleanScore(score1));
    builder.append("\n");
    return builder.toString();
  }

  @Override
  public List<String> resetHighscore(@NonNull File file, @NonNull List<String> lines) {
    List<String> newScoreText = new ArrayList<>();
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      if (i == scoreLine) {
        newScoreText.add("0");
        continue;
      }
      newScoreText.add(line);
    }
    return newScoreText;
  }
}

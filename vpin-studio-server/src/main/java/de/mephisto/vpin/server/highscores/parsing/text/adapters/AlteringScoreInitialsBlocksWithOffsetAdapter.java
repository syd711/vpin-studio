package de.mephisto.vpin.server.highscores.parsing.text.adapters;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * HS09
 * 83208800
 * 53508150
 * 44853190
 * 40642430
 * 38026640
 * 33
 * 38026640
 * DAD
 * DDD
 * AAA
 * AAA
 * AAA
 * 19
 * 50000000
 * 1
 * 1
 * <p>
 * BountyHunter.txt
 */
public class AlteringScoreInitialsBlocksWithOffsetAdapter extends ScoreTextFileAdapterImpl {

  private int lineCount;
  private List<String> fileNames;

  private int start;
  private int size;
  private int offset;

  public AlteringScoreInitialsBlocksWithOffsetAdapter() {
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

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
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
    StringBuilder builder = new StringBuilder(HIGHEST_SCORES + "\n");

    for (int index = 0; index < size; index++) {
      String score = lines.get(index + start);
      String initials = lines.get(index + start + size + offset);

      builder.append("#");
      builder.append(index + 1);
      builder.append(" ");
      builder.append(initials);
      builder.append("   ");
      builder.append(score);
      builder.append("\n");
    }
    return builder.toString();
  }

  @Override
  public List<String> resetHighscore(@NotNull File file, @NotNull List<String> lines) {
    List<String> newScoreText = new ArrayList<>();
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      if (i >= start && i < start + size) {
        line = "0";
      }
      else if (i >= start + size + offset && i < (start + size + size + offset)) {
        line = "???";
      }
      newScoreText.add(line);
    }
    return newScoreText;
  }
}

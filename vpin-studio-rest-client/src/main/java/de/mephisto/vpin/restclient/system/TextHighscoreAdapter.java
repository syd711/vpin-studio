package de.mephisto.vpin.restclient.system;

import java.util.List;

public class TextHighscoreAdapter {
  private String parser;
  private List<String> fileNames;
  private int start;
  private int size;
  private int scoreLine;
  private int scoreLine1;
  private int scoreLine2;
  private int offset;
  private int lineCount;
  private String comment;

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

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getParser() {
    return parser;
  }

  public void setParser(String parser) {
    this.parser = parser;
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

  public int getScoreLine() {
    return scoreLine;
  }

  public void setScoreLine(int scoreLine) {
    this.scoreLine = scoreLine;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public int getLineCount() {
    return lineCount;
  }

  public void setLineCount(int lineCount) {
    this.lineCount = lineCount;
  }
}

package de.mephisto.vpin.server.score;

public class FrameText {

  private String text;
  private int x;
  private int y;
  private int width;
  private int height;

  public FrameText(String text, int x, int y, int width, int height) {
    this.text = text;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  public String getText() {
    return text;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }
  
}
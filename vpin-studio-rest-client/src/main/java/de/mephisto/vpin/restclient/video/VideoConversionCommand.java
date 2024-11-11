package de.mephisto.vpin.restclient.video;

import java.util.Objects;

public class VideoConversionCommand {

  public static final int TYPE_FILE = 1;
  public static final int TYPE_FFMEPG = 2;

  private String name;
  private String command;
  private int type;

  public VideoConversionCommand() {
  }
  public VideoConversionCommand(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public VideoConversionCommand setFile(String absolutePath) {
    this.type = TYPE_FILE;
    this.command = absolutePath;
    return this;
  }
  public VideoConversionCommand setFFmpegArgs(String args) {
    this.type = TYPE_FFMEPG;
    this.command = args;
    return this;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    VideoConversionCommand that = (VideoConversionCommand) object;
    return Objects.equals(name, that.name) && Objects.equals(command, that.command);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, command);
  }

}

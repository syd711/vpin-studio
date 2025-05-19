package de.mephisto.vpin.restclient.converter;

import java.util.Objects;

public class MediaConversionCommand {

  public static final int TYPE_FILE = 1;
  public static final int TYPE_FFMEPG = 2;
  public static final int TYPE_IMAGE = 3;

  public static enum ImageOp {
    ROTATE_90, ROTATE_90_CCW, ROTATE_180
  }

  private String name;
  private String command;
  private int type;

  public MediaConversionCommand() {
  }

  public MediaConversionCommand(String name) {
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

  public MediaConversionCommand setFile(String absolutePath) {
    this.type = TYPE_FILE;
    this.command = absolutePath;
    return this;
  }

  public MediaConversionCommand setFFmpegArgs(String args) {
    this.type = TYPE_FFMEPG;
    this.command = args;
    return this;
  }

  public MediaConversionCommand setImageArgs(ImageOp op) {
    this.type = TYPE_IMAGE;
    this.command = op.name();
    return this;
  }

  public boolean isActiveForType(String mediaType) {
    if ("video".equalsIgnoreCase(mediaType)) {
      return type == TYPE_FILE || type == TYPE_FFMEPG;
    }
    else if ("image".equalsIgnoreCase(mediaType)) {
      return type == TYPE_IMAGE;
    }
    return false;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    MediaConversionCommand that = (MediaConversionCommand) object;
    return Objects.equals(name, that.name) && Objects.equals(command, that.command);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, command);
  }

}

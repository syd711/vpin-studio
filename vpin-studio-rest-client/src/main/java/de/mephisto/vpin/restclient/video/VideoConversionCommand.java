package de.mephisto.vpin.restclient.video;

import java.util.Objects;

public class VideoConversionCommand {
  private String name;
  private String file;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getFile() {
    return file;
  }

  public void setFile(String file) {
    this.file = file;
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
    return Objects.equals(name, that.name) && Objects.equals(file, that.file);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, file);
  }
}

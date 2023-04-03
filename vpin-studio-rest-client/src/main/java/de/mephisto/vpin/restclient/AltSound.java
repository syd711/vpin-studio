package de.mephisto.vpin.restclient;

import java.util.ArrayList;
import java.util.List;

public class AltSound {
  private List<AltSoundEntry> entries = new ArrayList<>();
  private int files;
  private long filesize;
  private List<Integer> channels = new ArrayList<>();

  public int getFiles() {
    return files;
  }

  public void setFiles(int files) {
    this.files = files;
  }

  public long getFilesize() {
    return filesize;
  }

  public void setFilesize(long filesize) {
    this.filesize = filesize;
  }

  public List<AltSoundEntry> getEntries() {
    return entries;
  }

  public void setEntries(List<AltSoundEntry> entries) {
    this.entries = entries;
  }

  public List<Integer> getChannels() {
    return channels;
  }

  public void setChannels(List<Integer> channels) {
    this.channels = channels;
  }
}

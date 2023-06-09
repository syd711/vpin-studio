package de.mephisto.vpin.restclient;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AltSound {
  private List<AltSoundEntry> entries = new ArrayList<>();
  private List<String> headers = new ArrayList<>();
  private int files;
  private long filesize;
  private Date modificationDate;
  private List<Integer> channels = new ArrayList<>();
  private boolean missingAudioFiles;

  public boolean isMissingAudioFiles() {
    return missingAudioFiles;
  }

  public void setMissingAudioFiles(boolean missingAudioFiles) {
    this.missingAudioFiles = missingAudioFiles;
  }

  public List<String> getHeaders() {
    return headers;
  }

  public void setHeaders(List<String> headers) {
    this.headers = headers;
  }

  public Date getModificationDate() {
    return modificationDate;
  }

  public void setModificationDate(Date modificationDate) {
    this.modificationDate = modificationDate;
  }

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

  public String toCSV() {
    StringBuilder builder = new StringBuilder();
    builder.append("\"");
    builder.append(String.join("\",\"", this.headers));
    builder.append("\"");
    builder.append("\n");

    for (AltSoundEntry entry : this.entries) {
      builder.append(entry.toCSV(this));
      builder.append("\n");
    }

    return builder.toString();
  }
}

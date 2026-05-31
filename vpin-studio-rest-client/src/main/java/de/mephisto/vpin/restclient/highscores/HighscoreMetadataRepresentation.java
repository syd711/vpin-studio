package de.mephisto.vpin.restclient.highscores;

import java.time.Instant;

public class HighscoreMetadataRepresentation {

  private String type;
  private String filename;
  private Instant modified;
  private Instant scanned;
  private String raw;
  private String rom;
  private String status;

  public Instant getScanned() {
    return scanned;
  }

  public void setScanned(Instant scanned) {
    this.scanned = scanned;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public Instant getModified() {
    return modified;
  }

  public void setModified(Instant modified) {
    this.modified = modified;
  }

  public String getRaw() {
    return raw;
  }

  public void setRaw(String raw) {
    this.raw = raw;
  }

  public String getRom() {
    return rom;
  }

  public void setRom(String rom) {
    this.rom = rom;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}

package de.mephisto.vpin.server.highscores;

import java.util.Date;

public class HighscoreMetadata {
  public static final String TYPE_VREG = "VReg";
  public static final String TYPE_EM = "EM";
  public static final String TYPE_NVRAM = "NVRam";

  private String type;
  private String displayName;
  private String filename;
  private Date modified;
  private Date scanned;
  private String raw;
  private String rom;
  private String status;

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public Date getScanned() {
    return scanned;
  }

  public void setScanned(Date scanned) {
    this.scanned = scanned;
  }

  public String getRom() {
    return rom;
  }

  public void setRom(String rom) {
    this.rom = rom;
  }

  public String getRaw() {
    return raw;
  }

  public void setRaw(String raw) {
    this.raw = raw;
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

  public Date getModified() {
    return modified;
  }

  public void setModified(Date modified) {
    this.modified = modified;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}

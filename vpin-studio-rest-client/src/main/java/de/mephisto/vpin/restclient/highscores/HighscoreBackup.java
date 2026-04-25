package de.mephisto.vpin.restclient.highscores;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class HighscoreBackup {
  private String filename;
  private String highscoreFilename;
  private OffsetDateTime creationDate;
  private String raw;
  private String rom;
  private HighscoreType highscoreType;

  public String getHighscoreFilename() {
    return highscoreFilename;
  }

  public void setHighscoreFilename(String highscoreFilename) {
    this.highscoreFilename = highscoreFilename;
  }

  public String getRom() {
    return rom;
  }

  public void setRom(String rom) {
    this.rom = rom;
  }

  public HighscoreType getHighscoreType() {
    return highscoreType;
  }

  public void setHighscoreType(HighscoreType highscoreType) {
    this.highscoreType = highscoreType;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public OffsetDateTime getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(OffsetDateTime creationDate) {
    this.creationDate = creationDate;
  }

  public String getRaw() {
    return raw;
  }

  public void setRaw(String raw) {
    this.raw = raw;
  }

  @Override
  public String toString() {
    try {
      String name = this.filename.substring(0, filename.indexOf("."));
      DateTimeFormatter parser = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
      LocalDateTime localDateTime = LocalDateTime.parse(name, parser);
      return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(localDateTime);
    } catch (Exception e) {
      return this.getFilename();
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof HighscoreBackup)) return false;

    HighscoreBackup that = (HighscoreBackup) o;

    return filename.equals(that.filename);
  }

  @Override
  public int hashCode() {
    return filename.hashCode();
  }
}

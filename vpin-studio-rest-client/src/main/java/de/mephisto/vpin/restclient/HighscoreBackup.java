package de.mephisto.vpin.restclient;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HighscoreBackup {
  private String filename;
  private String highscoreFilename;
  private Date creationDate;
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

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
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
      Date date = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").parse(name);
      return DateFormat.getDateTimeInstance().format(date);
    } catch (ParseException e) {
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

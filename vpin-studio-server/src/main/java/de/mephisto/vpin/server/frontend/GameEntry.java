package de.mephisto.vpin.server.frontend;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Hold mapping between filename and gameId for connectors that do not manage id
 * pinballX, Standalone
 */
@Entity
@Table(name = "GameEntry")
public class GameEntry {

  @Id
  @Column(name = "id", nullable = false)
  private int id;

  @Column(nullable = false)
  private int emuId;

  @Column(nullable = false)
  private String filename;


  public GameEntry() {
  }

  public GameEntry(int emuId, String filename, int gameId) {
    this.emuId = emuId;
    this.filename = filename;
    this.id = gameId;
  }
  
  @Override
  public int hashCode() {
    return id;
  }
  @Override
  public boolean equals(Object o) {
    return o instanceof GameEntry ? this.id == ((GameEntry) o).id : false;
  }
  
  public int getEmuId() {
    return emuId;
  }
  public void setEmuId(int emuId) {
    this.emuId = emuId;
  }

  public String getFilename() {
    return filename;
  }
  public void setFilename(String filename) {
    this.filename = filename;
  }

  public int getId() {
    return id;
  }
  public void setId(int id) {
    this.id = id;
  }
}
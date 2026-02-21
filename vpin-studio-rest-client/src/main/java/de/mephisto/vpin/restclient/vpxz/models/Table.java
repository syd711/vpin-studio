package de.mephisto.vpin.restclient.vpxz.models;

import java.util.Objects;

/**
 *     {
 *       "uuid": "d1d4f3fb-29fe-46ab-8ad5-8a02a1eaebde",
 *       "name": "exampleTable",
 *       "path": "exampleTable/exampleTable.vpx",
 *       "image": "",
 *       "createdAt": 1771446723,
 *       "modifiedAt": 1771446723
 *     }
 */
public class Table {
  private String uuid;
  private String name;
  private String path;
  private long createdAt;
  private long modifiedAt;

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public long getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(long createdAt) {
    this.createdAt = createdAt;
  }

  public long getModifiedAt() {
    return modifiedAt;
  }

  public void setModifiedAt(long modifiedAt) {
    this.modifiedAt = modifiedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Table table = (Table) o;
    return Objects.equals(uuid, table.uuid) && Objects.equals(name, table.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid, name);
  }
}

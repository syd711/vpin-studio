package de.mephisto.vpin.restclient.archiving;

import java.util.HashMap;
import java.util.Map;

public class ArchiveMameData {
  private String rom;
  private String alias;
  private Map<String,Object> registryData = new HashMap<>();

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public String getRom() {
    return rom;
  }

  public void setRom(String rom) {
    this.rom = rom;
  }

  public Map<String, Object> getRegistryData() {
    return registryData;
  }

  public void setRegistryData(Map<String, Object> registryData) {
    this.registryData = registryData;
  }
}

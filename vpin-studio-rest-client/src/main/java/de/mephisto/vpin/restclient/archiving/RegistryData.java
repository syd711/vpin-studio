package de.mephisto.vpin.restclient.archiving;

import java.util.HashMap;
import java.util.Map;

public class RegistryData {
  private String rom;
  private Map<String,Object> data = new HashMap<>();

  public String getRom() {
    return rom;
  }

  public void setRom(String rom) {
    this.rom = rom;
  }

  public Map<String, Object> getData() {
    return data;
  }

  public void setData(Map<String, Object> data) {
    this.data = data;
  }
}

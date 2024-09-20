package de.mephisto.vpin.restclient.assets;

import java.util.HashMap;
import java.util.Map;

public class AssetMetaData {
  private Map<String, Object> data = new HashMap<>();

  public Map<String, Object> getData() {
    return data;
  }

  public void setData(Map<String, Object> data) {
    this.data = data;
  }
}

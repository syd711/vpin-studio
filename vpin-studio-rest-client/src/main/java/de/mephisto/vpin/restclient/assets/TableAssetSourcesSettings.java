package de.mephisto.vpin.restclient.assets;

import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TableAssetSourcesSettings extends JsonSettings {
  private List<TableAssetSource> sources = new ArrayList<>();

  public List<TableAssetSource> getSources() {
    return sources;
  }

  public void setSources(List<TableAssetSource> sources) {
    this.sources = sources;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.ASSET_SOURCES_SETTINGS;
  }
}

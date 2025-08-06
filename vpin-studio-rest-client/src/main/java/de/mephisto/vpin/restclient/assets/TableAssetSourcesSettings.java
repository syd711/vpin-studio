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
  private List<TableAssetSource> tableAssetSources = new ArrayList<>();


  public List<TableAssetSource> getMediaSources() {
    return tableAssetSources;
  }

  public void setMediaSources(List<TableAssetSource> tableAssetSources) {
    this.tableAssetSources = tableAssetSources;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.ASSET_SOURCES_SETTINGS;
  }
}

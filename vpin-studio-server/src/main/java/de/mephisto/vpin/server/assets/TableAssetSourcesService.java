package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.TableAssetSourcesSettings;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TableAssetSourcesService implements PreferenceChangedListener, InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(TableAssetSourcesService.class);

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private TableAssetsService tableAssetsService;

  @Autowired
  private FrontendService frontendService;

  private TableAssetSourcesSettings tableAssetSourcesSettings;

  @Nullable
  public TableAssetSource getDefaultAssetSource() {
    TableAssetsAdapter<Game> tableAssetAdapter = frontendService.getTableAssetAdapter();
    if (tableAssetAdapter != null) {
      return tableAssetAdapter.getAssetSource();
    }
    return null;
  }

  @Nullable
  public TableAssetSource getAssetSource(@Nullable String sourceId) {
    if (sourceId == null) {
      return null;
    }
    Optional<TableAssetSource> first = getAssetSources().stream().filter(m -> m.getId().equalsIgnoreCase(sourceId)).findFirst();
    if (first.isPresent()) {
      return first.get();
    }

    TableAssetSource defaultAssetSource = getDefaultAssetSource();
    if (defaultAssetSource != null && defaultAssetSource.getId().equals(sourceId)) {
      return defaultAssetSource;
    }
    return null;
  }

  @NonNull
  public List<TableAssetSource> getAssetSources() {
    return TableAssetDefaultSourcesFactory.createDefaults(tableAssetSourcesSettings.getSources());
  }

  public boolean deleteAssetSource(String id) throws Exception {
    List<TableAssetSource> filtered = tableAssetSourcesSettings.getSources().stream().filter(m -> !m.getId().equalsIgnoreCase(id)).collect(Collectors.toList());
    tableAssetSourcesSettings.setSources(filtered);
    preferencesService.savePreference(tableAssetSourcesSettings);
    tableAssetsService.invalidateMediaSources(filtered);
    return true;
  }

  public TableAssetSource save(TableAssetSource tableAssetSource) throws Exception {
    List<TableAssetSource> filtered = new ArrayList<>(tableAssetSourcesSettings.getSources().stream().filter(m -> !m.getId().equalsIgnoreCase(tableAssetSource.getId())).collect(Collectors.toList()));
    filtered.add(tableAssetSource);
    tableAssetSourcesSettings.setSources(filtered);
    preferencesService.savePreference(tableAssetSourcesSettings);
    tableAssetsService.invalidateMediaSources(getAssetSources());
    return tableAssetSource;
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (PreferenceNames.ASSET_SOURCES_SETTINGS.equalsIgnoreCase(propertyName)) {
      this.tableAssetSourcesSettings = preferencesService.getJsonPreference(PreferenceNames.ASSET_SOURCES_SETTINGS, TableAssetSourcesSettings.class);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    preferenceChanged(PreferenceNames.ASSET_SOURCES_SETTINGS, null, null);
    preferencesService.addChangeListener(this);
    tableAssetsService.invalidateMediaSources(getAssetSources());
  }
}

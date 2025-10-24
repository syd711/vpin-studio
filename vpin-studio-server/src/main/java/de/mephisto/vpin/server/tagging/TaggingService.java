package de.mephisto.vpin.server.tagging;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.tagging.TaggingSettings;
import de.mephisto.vpin.restclient.tagging.TaggingUtil;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.*;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class TaggingService implements InitializingBean, GameDataChangedListener, GameLifecycleListener, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(TaggingService.class);

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private GameLifecycleService gameLifecycleService;

  @Autowired
  private PreferencesService preferencesService;

  private TaggingSettings taggingSettings;

  private final static Set<String> tags = new LinkedHashSet<>();

  public List<String> getTags() {
    return new ArrayList<>(tags);
  }

  @Override
  public void gameDataChanged(@NotNull GameDataChangedEvent changedEvent) {
    refreshTags();
  }

  @Override
  public void gameAssetChanged(@NotNull GameAssetChangedEvent changedEvent) {
    AssetType assetType = changedEvent.getAssetType();
    switch (assetType) {
      case DIRECTB2S: {
        if (taggingSettings.isAutoTagBackglassEnabled()) {
          TableDetails tableDetails = frontendService.getTableDetails(changedEvent.getGameId());
          if (tableDetails != null && !taggingSettings.getBackglassTags().isEmpty()) {
            autoApplyTags(changedEvent.getGameId(), tableDetails, taggingSettings.getBackglassTags());
          }
        }
        break;
      }
    }
  }

  @Override
  public void gameScreenAssetChanged(@NotNull GameScreenAssetChangedEvent changedEvent) {
    if (taggingSettings.isAutoTagScreensEnabled() && taggingSettings.getTaggedScreens().contains(changedEvent.getVPinScreen())) {
      TableDetails tableDetails = frontendService.getTableDetails(changedEvent.getGameId());
      if (tableDetails != null && !taggingSettings.getTaggedScreens().isEmpty()) {
        autoApplyTags(changedEvent.getGameId(), tableDetails, taggingSettings.getScreenTags());
      }
    }
  }

  @Override
  public void gameCreated(int gameId) {

  }

  @Override
  public void gameUpdated(int gameId) {

  }


  private void autoApplyTags(int gameId, @NonNull TableDetails tableDetails, List<String> autoTags) {
    List<String> gameTags = TaggingUtil.getTags(tableDetails.getTags());
    boolean added = false;
    for (String autoTag : autoTags) {
      if (!gameTags.contains(autoTag)) {
        gameTags.add(autoTag);
        added = true;
      }
    }
    if (added) {
      LOG.info("Auto-applied new tags to {}, updating tags.", tableDetails.getGameDisplayName());
      tableDetails.setTags(String.join(",", gameTags));
      frontendService.saveTableDetails(gameId, tableDetails);
    }
  }

  @Override
  public void gameDeleted(int gameId) {
    refreshTags();
  }

  private void refreshTags() {
    new Thread(() -> {
      tags.clear();

      long start = System.currentTimeMillis();
      //pick the games from the frontend connector to read them un-augmented
      List<Integer> gameIds = frontendService.getGameIds();
      for (Integer gameId : gameIds) {
        TableDetails tableDetails = frontendService.getTableDetails(gameId);
        List<String> tableTags = TaggingUtil.getTags(tableDetails.getTags());
        tags.addAll(tableTags);
      }

      LOG.info("Collected {} tags from {} games in {}ms", tags.size(), gameIds.size(), System.currentTimeMillis() - start);

    }).start();
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (PreferenceNames.TAGGING_SETTINGS.equals(propertyName)) {
      taggingSettings = preferencesService.getJsonPreference(PreferenceNames.TAGGING_SETTINGS);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    refreshTags();
    gameLifecycleService.addGameDataChangedListener(this);
    gameLifecycleService.addGameLifecycleListener(this);
    preferencesService.addChangeListener(this);
    preferenceChanged(PreferenceNames.TAGGING_SETTINGS, null, null);
  }
}

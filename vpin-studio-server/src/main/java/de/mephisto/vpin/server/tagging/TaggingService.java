package de.mephisto.vpin.server.tagging;

import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.tagging.TaggingUtil;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.*;
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
public class TaggingService implements InitializingBean, GameDataChangedListener, GameLifecycleListener {
  private final static Logger LOG = LoggerFactory.getLogger(TaggingService.class);

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private GameLifecycleService gameLifecycleService;

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

  }


  @Override
  public void gameCreated(int gameId) {

  }

  @Override
  public void gameUpdated(int gameId) {

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
  public void afterPropertiesSet() throws Exception {
    refreshTags();
    gameLifecycleService.addGameDataChangedListener(this);
    gameLifecycleService.addGameLifecycleListener(this);
  }
}

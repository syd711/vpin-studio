package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.server.GameInfo;
import de.mephisto.vpin.server.system.SystemInfo;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class HighscoreManager implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreManager.class);

  private final Map<Integer, Highscore> cache = new HashMap<>();
  private HighscoreResolver highscoreResolver;

  @Autowired
  private SystemInfo systemInfo;

  @Nullable
  public Highscore getHighscore(@NonNull GameInfo game) {
    if (StringUtils.isEmpty(game.getRom())) {
      return null;
    }

    if (!cache.containsKey(game.getId())) {
      Highscore highscore = highscoreResolver.loadHighscore(game);
      cache.put(game.getId(), highscore);
    }

    return cache.get(game.getId());
  }

  public void invalidateHighscore(@NonNull GameInfo game) {
    highscoreResolver.refresh();
    cache.remove(game.getId());
    LOG.info("Invalidated cached highscore of " + game);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.highscoreResolver = new HighscoreResolver(systemInfo);
  }
}

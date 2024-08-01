package de.mephisto.vpin.server.mania;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.connectors.mania.VPinManiaClient;
import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.connectors.mania.model.TableScore;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.mania.ManiaConfig;
import de.mephisto.vpin.restclient.mania.ManiaHighscoreSyncResult;
import de.mephisto.vpin.restclient.util.SystemUtil;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.vps.VpsService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ManiaService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaService.class);

  @Value("${vpinmania.server.host}")
  private String maniaHost;

  private VPinManiaClient maniaClient;

  @Autowired
  private GameService gameService;

  @Autowired
  private VpsService vpsService;

  @Autowired
  private ManiaServiceCache maniaServiceCache;

  public ManiaHighscoreSyncResult synchronizeHighscores(String vpsTableId) {
    ManiaHighscoreSyncResult result = new ManiaHighscoreSyncResult();
    VpsTable vpsTable = vpsService.getTableById(vpsTableId);
    if (vpsTable == null) {
      LOG.info("Skipped highscore sync for \"" + vpsTable.getDisplayName() + "\", because invalid VPS table id " + vpsTableId);
      return result;
    }

    maniaServiceCache.preCache();

    Game game = maniaServiceCache.getGame(vpsTableId);
    if (game == null || StringUtils.isEmpty(game.getExtTableId()) || StringUtils.isEmpty(game.getExtTableVersionId())) {
      LOG.info("Skipped highscore sync for \"" + vpsTable.getDisplayName() + "\", no matching game found.");
      return result;
    }

    LOG.info("Synchronizing mania table scores for \"" + game + "\"");
    ScoreSummary scores = gameService.getScores(game.getId());
    List<Score> scoreList = scores.getScores();
    for (Score score : scoreList) {
      try {
        String playerInitials = score.getPlayerInitials();
        if (maniaServiceCache.containsAccountForInitials(playerInitials)) {
          Account account = maniaServiceCache.getAccountForInitials(playerInitials);

          TableScore tableScore = new TableScore();
          tableScore.setScoreText(score.getScore());
          tableScore.setScore((long) score.getNumericScore());
          tableScore.setVpsTableId(game.getExtTableId());
          tableScore.setVpsVersionId(game.getExtTableVersionId());
          tableScore.setTableName(game.getGameDisplayName());
          tableScore.setAccountId(account.getId());
          tableScore.setCreationDate(score.getCreatedAt());
          tableScore.setScoreSource(game.getRom());
          tableScore.setCreationDate(score.getCreatedAt());

          LOG.info("Found score match to synchronize for " + playerInitials + ": " + score);
          TableScore submitted = maniaClient.getHighscoreClient().submitOrUpdate(tableScore);
          result.getTableScores().add(submitted);
        }
      }
      catch (Exception e) {
        LOG.error("Failed to submit mania highscore during sync: " + e.getMessage(), e);
      }
    }

    LOG.info("Highscore sync finished for \"" + vpsTable.getDisplayName() + ": " + result.getTableScores().size() + " scores have been submitted.");

    return result;
  }

  public VPinManiaClient getClient() {
    return maniaClient;
  }

  public ManiaConfig getConfig() throws Exception {
    ManiaConfig config = new ManiaConfig();
    config.setUrl(maniaHost);
    config.setSystemId(SystemUtil.getUniqueSystemId());
    return config;
  }

  public boolean clearCache() {
    return maniaServiceCache.clear();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (Features.MANIA_ENABLED) {
      try {
        ManiaConfig config = getConfig();
        maniaClient = new VPinManiaClient(config.getUrl(), config.getSystemId());
        maniaServiceCache.setManiaService(this);

        Cabinet cabinet = maniaClient.getCabinetClient().getCabinet();
        if (cabinet != null) {
          LOG.info("Cabinet is registered on VPin-Mania");
        }
        else {
          LOG.info("Cabinet is not registered on VPin-Mania");
        }
      }
      catch (Exception e) {
        LOG.error("Failed to init mania services: " + e.getMessage());
        Features.MANIA_ENABLED = false;
      }
    }
  }
}

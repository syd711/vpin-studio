package de.mephisto.vpin.server.mania;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.connectors.mania.VPinManiaClient;
import de.mephisto.vpin.connectors.mania.model.*;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.restclient.mania.ManiaConfig;
import de.mephisto.vpin.restclient.mania.ManiaHighscoreSyncResult;
import de.mephisto.vpin.restclient.util.SystemUtil;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.frontend.FrontendStatusChangeListener;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.vps.VpsService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ManiaService implements InitializingBean, FrontendStatusChangeListener {
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

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private FrontendStatusService frontendStatusService;

  private List<Cabinet> contacts;

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
    List<Account> cachedPlayerAccounts = maniaServiceCache.getCachedPlayerAccounts();
    LOG.info("Found " + cachedPlayerAccounts.size() + " eligable local players to synchronize.");

    ScoreSummary scores = gameService.getScores(game.getId());
    List<Score> scoreList = scores.getScores();
    if (scoreList.isEmpty()) {
      LOG.info("No highscores found for \"" + game.getGameDisplayName() + "\", VPS ids: " + game.getExtTableId() + "/" + game.getExtTableVersionId());
    }

    List<String> submittedInitials = new ArrayList<>();
    for (Score score : scoreList) {
      if (isOnDenyList(game, score)) {
        continue;
      }

      try {
        String playerInitials = score.getPlayerInitials();
        //we only synchronize the highest score of each table
        if (submittedInitials.contains(playerInitials)) {
          continue;
        }
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

          submittedInitials.add(playerInitials);
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
    this.contacts = null;
    return maniaServiceCache.clear();
  }

  public boolean isOnDenyList(@NonNull Game game, @NonNull Score score) {
    String vpsTableId = game.getExtTableId();
    if (!StringUtils.isEmpty(vpsTableId)) {
      List<DeniedScore> deniedScoresByTableId = maniaClient.getHighscoreClient().getDeniedScoresByTableId(vpsTableId);
      for (DeniedScore deniedScore : deniedScoresByTableId) {
        if (score.isDenied(deniedScore)) {
          LOG.info("Skipped submitting VPinMania score {} for {}, the score is on the deny list.", score, game.getGameDisplayName());
          SLOG.info("Skipped submitting VPinMania score " + score + " for \"" + game.getGameDisplayName() + "\", the score is on the deny list.");
          return true;
        }
      }
    }
    return false;
  }

  public List<Score> getFriendsScoresFor(Game game) {
    List<Score> result = new ArrayList<>();
    if (!Features.MANIA_ENABLED && !Features.MANIA_SOCIAL_ENABLED) {
      return result;
    }

    String vpsTableId = game.getExtTableId();
    String vpsVersionId = game.getExtTableVersionId();

    if (StringUtils.isEmpty(vpsTableId) || StringUtils.isEmpty(vpsVersionId)) {
      return Collections.emptyList();
    }


    Cabinet cabinet = maniaClient.getCabinetClient().getCabinetCached();
    if (cabinet != null) {
      List<Cabinet> contacts = getContacts();

      for (Cabinet contact : contacts) {
        List<Account> accounts = maniaClient.getCabinetClient().getAccounts(contact.getUuid());
        for (Account account : accounts) {
          List<TableScore> highscoresByAccount = maniaClient.getHighscoreClient().getHighscoresByAccountAndTable(account.getUuid(), vpsTableId, vpsVersionId);
          List<Score> scores = highscoresByAccount.stream().map(h -> toScores(game, account, h)).collect(Collectors.toList());
          result.addAll(scores);
        }
      }
    }
    return result;
  }

  private List<Cabinet> getContacts() {
    if (this.contacts == null) {
      contacts = maniaClient.getContactClient().getContacts();
    }
    return contacts;
  }

  private Score toScores(Game game, Account account, TableScore accountScore) {
    Score score = new Score(accountScore.getCreationDate(), game.getId(), account.getInitials(), null, accountScore.getScoreText(), accountScore.getScore(), -1);
    score.setExternal(true);
    return score;
  }

  public void setOffline() {
    if (Features.MANIA_ENABLED) {
      try {
        Cabinet cabinet = getClient().getCabinetClient().getCabinet();
        if (cabinet != null) {
          cabinet.getStatus().setStatus(CabinetOnlineStatus.offline);
          cabinet.getStatus().setActiveGame(null);
          getClient().getCabinetClient().update(cabinet);
        }
        LOG.info("Switched cabinet to modus: {}", CabinetOnlineStatus.offline);
      }
      catch (Exception e) {
        LOG.error("Error during tournament service shutdown: " + e.getMessage(), e);
      }
    }
  }

  public void setOnline() {
    if (Features.MANIA_ENABLED) {
      try {
        Cabinet cabinet = getClient().getCabinetClient().getCabinet();
        if (cabinet != null) {
          cabinet.getStatus().setStatus(CabinetOnlineStatus.online);
          cabinet.getStatus().setActiveGame(null);
          getClient().getCabinetClient().update(cabinet);
        }
        LOG.info("Switched cabinet to modus: {}", CabinetOnlineStatus.online);
      }
      catch (Exception e) {
        LOG.error("Error during tournament service shutdown: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (Features.MANIA_ENABLED) {
      try {
        frontendStatusService.addFrontendStatusChangeListener(this);

        ManiaConfig config = getConfig();
        maniaClient = new VPinManiaClient(config.getUrl(), config.getSystemId());
        maniaServiceCache.setManiaService(this);
        ServerFX.maniaClient = maniaClient;

        Cabinet cabinet = maniaClient.getCabinetClient().getCabinetCached();
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

    highscoreService.setManiaService(this);
  }

  @Override
  public void frontendLaunched() {
    this.setOnline();
  }

  @Override
  public void frontendRestarted() {
    this.setOnline();
  }

  @Override
  public void frontendExited() {
    this.setOffline();
  }

  @PreDestroy
  public void onShutdown() {
    this.setOffline();
  }
}

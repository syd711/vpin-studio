package de.mephisto.vpin.server.mania;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.connectors.mania.VPinManiaClient;
import de.mephisto.vpin.connectors.mania.model.*;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.restclient.mania.ManiaConfig;
import de.mephisto.vpin.restclient.mania.ManiaRegistration;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
import de.mephisto.vpin.restclient.mania.ManiaTableSyncResult;
import de.mephisto.vpin.restclient.util.SystemUtil;
import de.mephisto.vpin.server.assets.Asset;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.frontend.FrontendStatusChangeListener;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.frontend.TableStatusChangeListener;
import de.mephisto.vpin.server.games.*;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.resources.ResourceLoader;
import de.mephisto.vpin.server.vps.VpsService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.imageio.ImageIO;

import static de.mephisto.vpin.server.VPinStudioServer.Features;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ManiaService implements InitializingBean, FrontendStatusChangeListener, PreferenceChangedListener, TableStatusChangeListener, GameDataChangedListener, GameLifecycleListener, ApplicationListener<ApplicationReadyEvent> {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaService.class);

  @Value("${vpinmania.server.host}")
  private String maniaHost;

  private VPinManiaClient maniaClient;

  @Autowired
  private GameService gameService;

  @Autowired
  private GameLifecycleService gameLifecycleService;

  @Autowired
  private VpsService vpsService;

  @Autowired
  private PlayerService playerService;

  @Autowired
  private ManiaServiceCache maniaServiceCache;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private FrontendStatusService frontendStatusService;

  @Autowired
  private PreferencesService preferencesService;

  private List<CabinetContact> contacts;
  private ManiaSettings maniaSettings;
  private Cabinet cabinet;

  public ManiaTableSyncResult synchronizeHighscore(String vpsTableId) {
    ManiaTableSyncResult result = new ManiaTableSyncResult();
    VpsTable vpsTable = vpsService.getTableById(vpsTableId);
    if (vpsTable == null) {
      return result;
    }

    maniaServiceCache.preCache();

    Game game = maniaServiceCache.getGame(vpsTableId);
    if (game == null || StringUtils.isEmpty(game.getExtTableId()) || StringUtils.isEmpty(game.getExtTableVersionId())) {
      String msg = "Skipped VPin Mania synchronization for \"" + vpsTable.getDisplayName() + "\", no matching game found.";
      LOG.info(msg);
      result.setResult(msg);
      return result;
    }

    synchronizeHighscores(result, game, vpsTable);
    return result;
  }

  private void synchronizeHighscores(@NonNull ManiaTableSyncResult result, @NonNull Game game, @NonNull VpsTable vpsTable) {
    if (!maniaSettings.isSubmitAllScores()) {
      return;
    }

    LOG.info("Synchronizing mania table scores for \"" + game + "\"");
    List<Account> cachedPlayerAccounts = maniaServiceCache.getCachedPlayerAccounts();
    LOG.info("Found " + cachedPlayerAccounts.size() + " eligable local players to synchronize.");

    ScoreSummary scores = gameService.getScores(game.getId());
    List<Score> scoreList = scores.getScores();
    if (scoreList.isEmpty()) {
      String msg = "No highscores found for \"" + game.getGameDisplayName() + "\", VPS ids: " + game.getExtTableId() + "/" + game.getExtTableVersionId();
      LOG.info(msg);
      return;
    }

    List<String> submittedInitials = new ArrayList<>();
    List<DeniedScore> deniedScoresByTableId = Collections.emptyList();
    deniedScoresByTableId = maniaClient.getHighscoreClient().getDeniedScoresByTableId(vpsTable.getId());

    for (Score score : scoreList) {
      try {
        String playerInitials = score.getPlayerInitials();
        //we only synchronize the highest score of each table
        if (submittedInitials.contains(playerInitials)) {
          continue;
        }
        if (maniaServiceCache.containsAccountForInitials(playerInitials)) {
          Account account = maniaServiceCache.getAccountForInitials(playerInitials);

          TableScore tableScore = new TableScore();
          tableScore.setScoreText(score.getFormattedScore());
          tableScore.setScore(score.getScore());
          tableScore.setVpsTableId(game.getExtTableId());
          tableScore.setVpsVersionId(game.getExtTableVersionId());
          tableScore.setTableName(game.getGameDisplayName());
          tableScore.setAccountId(account.getId());
          tableScore.setCreationDate(score.getCreatedAt());
          tableScore.setScoreSource(game.getRom());
          tableScore.setCreationDate(score.getCreatedAt());

          if (isOnDenyList(deniedScoresByTableId, score, game)) {
            result.setTableScore(tableScore);
            result.setDenied(true);
            continue;
          }

          LOG.info("Found score match to synchronize for " + playerInitials + ": " + score);
          TableScore submitted = maniaClient.getHighscoreClient().submitOrUpdate(tableScore);
          result.setTableScore(submitted);
          submittedInitials.add(playerInitials);
        }
      }
      catch (Exception e) {
        LOG.error("Failed to submit mania highscore during sync: " + e.getMessage(), e);
        result.setResult("Failed to submit mania highscore during sync: " + e.getMessage());
      }
    }

    LOG.info("Highscore sync finished for \"{}\"/{}/{}.", vpsTable.getDisplayName(), game.getExtTableId(), game.getExtTableVersionId());
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
    this.cabinet = this.maniaClient.getCabinetClient().getCabinet();
    return maniaServiceCache.clear();
  }

  public boolean isOnDenyList(@NonNull Game game, @NonNull Score score) {
    String vpsTableId = game.getExtTableId();
    if (!StringUtils.isEmpty(vpsTableId)) {
      List<DeniedScore> deniedScoresByTableId = maniaClient.getHighscoreClient().getDeniedScoresByTableId(vpsTableId);
      return isOnDenyList(deniedScoresByTableId, score, game);
    }
    return false;
  }

  private boolean isOnDenyList(@NonNull List<DeniedScore> deniedScores, @NonNull Score score, @NonNull Game game) {
    for (DeniedScore deniedScore : deniedScores) {
      if (score.isDenied(deniedScore)) {
        LOG.info("Skipped submitting VPinMania score {} for {}, the score is on the deny list.", score, game.getGameDisplayName());
        SLOG.info("Skipped submitting VPinMania score " + score + " for \"" + game.getGameDisplayName() + "\", the score is on the deny list.");
        return true;
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


    if (cabinet != null) {
      List<CabinetContact> contacts = getContacts();

      for (CabinetContact contact : contacts) {
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

  private List<CabinetContact> getContacts() {
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

  /**
   * In case the Studio has been re-installed, the players of the VPin Mania accounts should be restored too.
   * //TODO
   */
  private void checkPlayerRestoring() {
    List<Account> accounts = maniaClient.getAccountClient().getAccounts();
    for (Account account : accounts) {
      checkAccount(account);
    }
  }

  private void checkAccount(Account account) {
    List<Player> buildInPlayers = playerService.getBuildInPlayers();
    boolean adminExists = !buildInPlayers.stream().filter(p -> p.isAdministrative()).collect(Collectors.toList()).isEmpty();

    String name = account.getDisplayName();
    String initial = account.getInitials();

    for (Player buildInPlayer : buildInPlayers) {
      //check if a matching player exists
      if (buildInPlayer.getName().equals(name) && buildInPlayer.getInitials().equals(initial)) {
        //check if the account id is set
        if (buildInPlayer.getTournamentUserUuid() != null) {
          return;
        }

        //the player exists, but no mania account id is set
        buildInPlayer.setTournamentUserUuid(account.getUuid());
        playerService.save(buildInPlayer);
        return;
      }
    }

    Player restoredPlayer = new Player();
    restoredPlayer.setInitials(initial);
    restoredPlayer.setName(name);
    restoredPlayer.setTournamentUserUuid(account.getUuid());
    restoredPlayer.setAdministrative(!adminExists);
    Player update = playerService.save(restoredPlayer);
    LOG.info("Restored VPin Mania player for account {}: {}", account.getUuid(), update.toString());
  }

  /**
   * Only register here or register players, do not sync because this is triggered by the UI.
   */
  public ManiaRegistration register(ManiaRegistration registration) {
    try {
      Asset avatarEntry = (Asset) preferencesService.getPreferenceValue(PreferenceNames.AVATAR);
      String systemName = (String) preferencesService.getPreferenceValue(PreferenceNames.SYSTEM_NAME);

      ManiaSettings maniaSettings = preferencesService.getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
      maniaSettings.setSubmitPlayed(registration.isSubmitPlayCount());
      maniaSettings.setSubmitRatings(registration.isSubmitRatings());
      maniaSettings.setEnabled(true);

      BufferedImage avatar = ResourceLoader.getResource("avatar-default.png");
      if (avatarEntry != null) {
        avatar = ImageIO.read(new ByteArrayInputStream(avatarEntry.getData()));
      }

      Cabinet newCab = new Cabinet();
      newCab.setCreationDate(new Date());
      newCab.setSettings(new CabinetSettings());
      newCab.setDisplayName(systemName != null ? systemName : "My VPin");
      Cabinet registeredCabinet = maniaClient.getCabinetClient().create(newCab, avatar, null);
      if (registeredCabinet == null) {
        LOG.error("Mania registration failed, no cabinet created.");
        registration.setResult("Registration failed.");
        return registration;
      }

      List<Long> playerIds = registration.getPlayerIds();
      for (Long playerId : playerIds) {
        Player buildInPlayer = playerService.getBuildInPlayer(playerId);
        if (buildInPlayer == null) {
          continue;
        }

        Account account = new Account();
        account.setInitials(buildInPlayer.getInitials());
        account.setDisplayName(buildInPlayer.getName());
        account.setUuid(buildInPlayer.getTournamentUserUuid());

        Asset playerAvatarAsset = buildInPlayer.getAvatar();
        BufferedImage playerAvatar = avatar;
        if (playerAvatarAsset != null) {
          playerAvatar = ImageIO.read(new ByteArrayInputStream(playerAvatarAsset.getData()));
        }

        Account register = maniaClient.getAccountClient().create(account, playerAvatar, null);
        buildInPlayer.setTournamentUserUuid(register.getUuid());
        Player save = playerService.save(buildInPlayer);
        LOG.info("Registered VPin Mania player for account {}: {}", account.getUuid(), save.toString());
      }

      maniaServiceCache.clear();
      preferencesService.savePreference(PreferenceNames.MANIA_SETTINGS, maniaSettings);
    }
    catch (Exception e) {
      LOG.error("Mania Registration failed: {}", e.getMessage(), e);
      registration.setResult(e.getMessage());
    }
    return registration;
  }

  /**
   * Pushes the list of all local VPX tables to VPin Mania
   */
  public boolean synchronizeTables() {
    try {
      if (!Features.MANIA_ENABLED) {
        return false;
      }

      ManiaSettings maniaSettings = preferencesService.getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
      if (maniaSettings.isSubmitTables()) {
        if (cabinet != null) {
          long start = System.currentTimeMillis();
          List<Game> knownGames = gameService.getKnownGames(-1);
          List<InstalledTable> installedTables = new ArrayList<>();
          for (Game game : knownGames) {
            if (!StringUtils.isEmpty(game.getExtTableVersionId()) && !StringUtils.isEmpty(game.getExtTableVersionId())) {
              InstalledTable installedTable = new InstalledTable();
              installedTable.setVpsTableId(game.getExtTableId());
              installedTable.setVpsVersionId(game.getExtTableVersionId());

              if (maniaSettings.isSubmitRatings()) {
                installedTable.setRating(game.getRating());
              }
              installedTables.add(installedTable);
            }
          }

          cabinet.getSettings().setInstalledTables(installedTables);
          new Thread(() -> {
            try {
              maniaClient.getCabinetClient().update(cabinet);
            }
            catch (Exception e) {
              LOG.error("Cabinet update for sync failed: {}", e.getMessage(), e);
            }
          }).start();

          long duration = System.currentTimeMillis() - start;
          LOG.info("VPin Mania table synchronization finished, {} tables synchronized in {}ms.", installedTables.size(), duration);
          return true;
        }
      }
      else {
        if (cabinet != null && !cabinet.getSettings().getInstalledTables().isEmpty()) {
          cabinet.getSettings().setInstalledTables(new ArrayList<>());
          new Thread(() -> {
            try {
              maniaClient.getCabinetClient().update(cabinet);
            }
            catch (Exception e) {
              LOG.error("Cabinet update for sync failed: {}", e.getMessage(), e);
            }
          }).start();

          LOG.info("VPin Mania table synchronization has been resetted.");
        }

        LOG.info("VPin Mania table synchronization not enabled.");
      }
    }
    catch (Exception e) {
      LOG.error("VPin Mania table synchronization failed: {}", e.getMessage(), e);
    }
    return false;
  }

  //-------------------- FrontendStatusChangeListener ------------------------------------------------------------------

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

  //-------------------- PreferenceChangedListener ---------------------------------------------------------------------

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (propertyName.equals(PreferenceNames.MANIA_SETTINGS)) {
      maniaSettings = preferencesService.getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
      cabinet = maniaClient.getCabinetClient().getCabinet();
    }
  }

  //-------------------- TableStatusChangeListener ---------------------------------------------------------------------

  @Override
  public void tableLaunched(TableStatusChangedEvent event) {
    if (Features.MANIA_ENABLED) {
      ManiaSettings maniaSettings = preferencesService.getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
      Game game = event.getGame();
      if (maniaSettings.isSubmitPlayed() && !StringUtils.isEmpty(game.getExtTableId()) && !StringUtils.isEmpty(game.getExtTableVersionId())) {
        Cabinet cabinet = maniaClient.getCabinetClient().getCabinetCached();
        if (cabinet != null) {
          LOG.info("Updating mania played counter for \"{}\"", game.getGameDisplayName());
          new Thread(() -> {
            maniaClient.getVpsTableClient().updatePlayedCount(game.getExtTableId(), game.getExtTableVersionId());
          }).start();
        }
      }
    }
  }

  @Override
  public void tableExited(TableStatusChangedEvent event) {
    //ignore
  }

  //-------------------- GameDataChangedListener -----------------------------------------------------------------------

  @Override
  public void gameDataChanged(@NonNull GameDataChangedEvent event) {
    if (Features.MANIA_ENABLED) {
      synchronizeTables();
    }
  }

  @Override
  public void gameAssetChanged(@NonNull GameAssetChangedEvent changedEvent) {
    //not of interest
  }


  //------------------------ GameLifecycleListener ---------------------------------------------------------------------
  @Override
  public void gameCreated(int gameId) {
    if (Features.MANIA_ENABLED) {
      synchronizeTables();
    }
  }

  @Override
  public void gameUpdated(int gameId) {
    if (Features.MANIA_ENABLED) {
      //do not sync for games updates as this may be an action for all tables
    }
  }

  @Override
  public void gameDeleted(int gameId) {
    if (Features.MANIA_ENABLED) {
      synchronizeTables();
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (Features.MANIA_ENABLED) {
      try {
        LOG.info("Initializing VPin Mania Service, using unique id: {}", SystemUtil.getUniqueSystemId());
        frontendStatusService.addFrontendStatusChangeListener(this);
        frontendStatusService.addTableStatusChangeListener(this);
        gameLifecycleService.addGameDataChangedListener(this);
        gameLifecycleService.addGameLifecycleListener(this);


        ManiaConfig config = getConfig();
        maniaClient = new VPinManiaClient(config.getUrl(), config.getSystemId());
        maniaServiceCache.setManiaService(this);
        ServerFX.maniaClient = maniaClient;

        cabinet = maniaClient.getCabinetClient().getCabinet();

        preferencesService.addChangeListener(this);
        preferenceChanged(PreferenceNames.MANIA_SETTINGS, null, null);
      }
      catch (Exception e) {
        LOG.error("Failed to init mania services: {}", e.getMessage(), e);
        Features.MANIA_ENABLED = false;
      }
    }

    highscoreService.setManiaService(this);
    setOnline();
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }

  public Cabinet getCabinet() {
    return this.cabinet;
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    if (Features.MANIA_ENABLED) {
      Cabinet cabinet = maniaClient.getCabinetClient().getCabinetCached();
      if (cabinet != null) {
        LOG.info("Cabinet is registered on VPin-Mania");
        Thread.currentThread().setName("VPin Mania Tables Synchronizer");
        synchronizeTables();
      }
      else {
        LOG.info("Cabinet is not registered on VPin-Mania");
      }
    }
  }

  @PreDestroy
  public void onShutdown() {
    this.setOffline();
  }
}

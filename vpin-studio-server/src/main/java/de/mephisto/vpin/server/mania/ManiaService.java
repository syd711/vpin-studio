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
import de.mephisto.vpin.server.assets.Asset;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.frontend.FrontendStatusChangeListener;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.frontend.TableStatusChangeListener;
import de.mephisto.vpin.server.games.*;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.resources.ResourceLoader;
import de.mephisto.vpin.server.vps.VpsService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static de.mephisto.vpin.server.VPinStudioServer.Features;

@Service
public class ManiaService implements InitializingBean, FrontendStatusChangeListener, PreferenceChangedListener, TableStatusChangeListener, GameDataChangedListener, GameLifecycleListener, ApplicationListener<ApplicationReadyEvent> {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaService.class);

  //the file contains the UUID of the cabinet
  public static final String VPIN_MANIA_ID_TXT = ".vpin-mania-id.txt";

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
  private FrontendStatusService frontendStatusService;

  @Autowired
  private GameDetailsRepositoryService gameDetailsRepositoryService;

  @Autowired
  private PreferencesService preferencesService;

  private List<CabinetContact> contacts;
  private ManiaSettings maniaSettings;

  public ManiaTableSyncResult synchronizeHighscore(String vpsTableId) {
    ManiaTableSyncResult result = new ManiaTableSyncResult();
    VpsTable vpsTable = vpsService.getTableById(vpsTableId);
    if (vpsTable == null) {
      result.setValid(false);
      result.setResult("No matching VPS table found.");
      return result;
    }
    result.setTableName(vpsTable.getDisplayName());

    maniaServiceCache.preCache();

    Game game = maniaServiceCache.getGame(vpsTableId);
    if (game == null || StringUtils.isEmpty(game.getExtTableId()) || StringUtils.isEmpty(game.getExtTableVersionId())) {
      String msg = "Skipped VPin Mania synchronization for \"" + vpsTable.getDisplayName() + "\", no matching game found.";
      LOG.info(msg);
      result.setValid(false);
      result.setResult(msg);
      return result;
    }

    result.setTableName(game.getGameDisplayName());
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
      result.setResult(msg);
      return;
    }

    List<String> submittedInitials = new ArrayList<>();
    List<DeniedScore> deniedScoresByTableId = maniaClient.getHighscoreClient().getDeniedScoresByTableId(vpsTable.getId());
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
            result.setResult("A matching score has been found, but it is on the ignore list.");
            continue;
          }

          LOG.info("Found score match to synchronize for " + playerInitials + ": " + score);
          TableScore submitted = maniaClient.getHighscoreClient().submitOrUpdate(tableScore);
          result.setTableScore(submitted);
          result.setResult("The highscore was successfully synchronized.");
          submittedInitials.add(playerInitials);
        }
        else {
          result.setResult("No matching account found for initials \"" + playerInitials + "\"");
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
    return config;
  }

  public boolean clearCache() {
    refreshDefaultCabinet();
    this.getContacts();
    return maniaServiceCache.clear();
  }

  private Cabinet refreshDefaultCabinet() {
    try {
      if (!StringUtils.isEmpty(maniaSettings.getApiKey()) && !StringUtils.isEmpty(maniaSettings.getCabinetUuid())) {
        this.maniaClient.getRestClient().setApiKey(maniaSettings.getApiKey());
        return maniaClient.getCabinetClient().getDefaultCabinetCached(maniaSettings.getCabinetUuid());
      }
      else {
        LOG.info("Mania cabinet refresh failed, no API key set or API key invalid.");
      }
    }
    catch (Exception e) {
      LOG.error("Failed to refresh default cabinet: {}", e.getMessage());
    }
    return null;
  }

  public boolean isOnDenyList(@NonNull Game game, @NonNull Score score) {
    Cabinet cabinet = maniaClient.getCabinetClient().getDefaultCabinetCached();
    if (Features.MANIA_ENABLED && cabinet != null) {
      String vpsTableId = game.getExtTableId();
      if (!StringUtils.isEmpty(vpsTableId)) {
        List<DeniedScore> deniedScoresByTableId = maniaClient.getHighscoreClient().getDeniedScoresByTableId(vpsTableId);
        return isOnDenyList(deniedScoresByTableId, score, game);
      }
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


    Cabinet cabinet = maniaClient.getCabinetClient().getDefaultCabinetCached();
    if (cabinet != null) {
      List<CabinetContact> contacts = getContacts();

      for (CabinetContact contact : contacts) {
        Cabinet contactCabinet = maniaClient.getCabinetClient().getCabinet(contact.getUuid());
        if (contactCabinet != null) {
          List<Account> accounts = maniaClient.getCabinetClient().getAccounts(contactCabinet.getId());
          for (Account account : accounts) {
            List<TableScore> highscoresByAccount = maniaClient.getHighscoreClient().getHighscoresByAccountAndTable(account.getUuid(), vpsTableId, vpsVersionId);
            List<Score> scores = highscoresByAccount.stream().map(h -> toScores(game, account, h)).collect(Collectors.toList());
            result.addAll(scores);
          }
        }
      }
    }
    return result;
  }

  private List<CabinetContact> getContacts() {
    if (this.contacts == null) {
      Cabinet cabinet = maniaClient.getCabinetClient().getDefaultCabinetCached();
      if (cabinet != null) {
        contacts = maniaClient.getContactClient().getContacts(cabinet.getId());
      }
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
        Cabinet cabinet = getClient().getCabinetClient().getDefaultCabinetCached();
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

  public void setOnline(@Nullable Cabinet cabinet) {
    if (Features.MANIA_ENABLED) {
      try {
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

  private void checkAccount(Account account) {
    List<Player> buildInPlayers = playerService.getBuildInPlayers();
    boolean adminExists = !buildInPlayers.stream().filter(p -> p.isAdministrative()).collect(Collectors.toList()).isEmpty();

    String name = account.getDisplayName();
    String initial = account.getInitials();

    for (Player buildInPlayer : buildInPlayers) {
      //check if a matching player exists
      if (buildInPlayer.getName().equals(name) && buildInPlayer.getInitials().equals(initial)) {
        //check if the account id is set
        if (buildInPlayer.getManiaAccountUuid() != null) {
          return;
        }

        //the player exists, but no mania account id is set
        buildInPlayer.setManiaAccountUuid(account.getUuid());
        playerService.save(buildInPlayer);
        return;
      }
    }

    Player restoredPlayer = new Player();
    restoredPlayer.setInitials(initial);
    restoredPlayer.setName(name);
    restoredPlayer.setManiaAccountUuid(account.getUuid());
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
      maniaSettings.setApiKey(registration.getApiKey());
      preferencesService.savePreference(maniaSettings);

      maniaClient.getRestClient().setApiKey(registration.getApiKey());

      BufferedImage avatar = ResourceLoader.getResource("avatar-default.png");
      if (avatarEntry != null) {
        avatar = ImageIO.read(new ByteArrayInputStream(avatarEntry.getData()));
      }

      Cabinet newCab = new Cabinet();
      newCab.setCreationDate(new Date());
      newCab.setSettings(new CabinetSettings());
      newCab.setUuid(getCabinetUuidFileValue());//refresh an existing one from a former installation
      newCab.setDisplayName(systemName != null ? systemName : "My VPin");
      Cabinet registeredCabinet = maniaClient.getCabinetClient().register(newCab, avatar, null);
      if (registeredCabinet == null) {
        LOG.error("Mania registration failed, no cabinet created.");
        registration.setResult("Registration failed.");
        return registration;
      }

      //refresh instance
      Cabinet cabinet = maniaClient.getCabinetClient().getDefaultCabinetCached(registeredCabinet.getUuid());

      //save cabinet id
      maniaSettings.setCabinetUuid(registeredCabinet.getUuid());
      preferencesService.savePreference(maniaSettings);

      List<Long> playerIds = registration.getPlayerIds();
      for (Long playerId : playerIds) {
        Player buildInPlayer = playerService.getBuildInPlayer(playerId);
        if (buildInPlayer == null) {
          continue;
        }

        Account account = new Account();
        account.setInitials(buildInPlayer.getInitials());
        account.setDisplayName(buildInPlayer.getName());
        account.setUuid(buildInPlayer.getManiaAccountUuid());

        Asset playerAvatarAsset = buildInPlayer.getAvatar();
        BufferedImage playerAvatar = avatar;
        if (playerAvatarAsset != null) {
          playerAvatar = ImageIO.read(new ByteArrayInputStream(playerAvatarAsset.getData()));
        }


        Account register = maniaClient.getAccountClient().create(cabinet.getId(), account, playerAvatar, null);
        buildInPlayer.setManiaAccountUuid(register.getUuid());
        Player save = playerService.save(buildInPlayer);
        LOG.info("Registered VPin Mania player for account {}: {}", account.getUuid(), save.toString());
      }

      maniaServiceCache.clear();
      preferencesService.savePreference(maniaSettings);
    }
    catch (Exception e) {
      LOG.error("Mania Registration failed: {}", e.getMessage(), e);
      registration.setResult(String.valueOf(e.getMessage()).replaceAll("500", "").replaceAll("INTERNAL_SERVER_ERROR", ""));
    }
    return registration;
  }

  /**
   * Pushes the list of all local VPX tables to VPin Mania
   */
  public boolean synchronizeTables() {
    try {
      refreshDefaultCabinet();
      Cabinet cabinet = maniaClient.getCabinetClient().getDefaultCabinetCached();
      if (!Features.MANIA_ENABLED || cabinet == null) {
        return false;
      }

      // The check is made for re-installation of servers that are already registered on mania
      // If the database for the GameDetails is empty, this is the initial installation boot up.
      List<GameDetails> rawGames = gameDetailsRepositoryService.findAll();
      if (rawGames.isEmpty()) {
        LOG.info("Skipped table synchronization, no games have been imported yet.");
        return false;
      }

      ManiaSettings maniaSettings = preferencesService.getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
      if (maniaSettings.isSubmitTables()) {
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
            long duration = System.currentTimeMillis() - start;
            LOG.info("VPin Mania table synchronization finished, {} tables synchronized in {}ms.", installedTables.size(), duration);
          }
          catch (Exception e) {
            LOG.error("Cabinet update for sync failed: {}", e.getMessage(), e);
          }
        }).start();
        return true;
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
    this.setOnline(maniaClient.getCabinetClient().getDefaultCabinetCached());
  }

  @Override
  public void frontendRestarted() {
    this.setOnline(maniaClient.getCabinetClient().getDefaultCabinetCached());
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

      refreshDefaultCabinet();
      Cabinet cabinet = maniaClient.getCabinetClient().getDefaultCabinetCached();
      if (cabinet != null) {
        updateCabinetUuidFile(cabinet.getUuid());
      }
    }
  }

  //-------------------- TableStatusChangeListener ---------------------------------------------------------------------

  @Override
  public void tableLaunched(TableStatusChangedEvent event) {
    if (Features.MANIA_ENABLED) {
      ManiaSettings maniaSettings = preferencesService.getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
      Game game = event.getGame();
      if (maniaSettings.isSubmitPlayed() && !StringUtils.isEmpty(game.getExtTableId()) && !StringUtils.isEmpty(game.getExtTableVersionId())) {
        refreshDefaultCabinet();
        Cabinet cabinet = maniaClient.getCabinetClient().getDefaultCabinetCached();

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
      //do not sync for games updates as this may be an action for a lot of tables
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

  private static void updateCabinetUuidFile(@NonNull String uuid) {
    try {
      String localAppData = System.getenv("LOCALAPPDATA");
      Path appDataPath = Paths.get(localAppData, "VPin-Studio");
      Files.createDirectories(appDataPath);
      File idFile = new File(appDataPath.toFile(), VPIN_MANIA_ID_TXT);
      if (idFile.exists() && !idFile.delete()) {
        LOG.error("Failed to delete mania id file");
        return;
      }
      Files.writeString(idFile.toPath(), uuid);
    }
    catch (Exception e) {
      LOG.error("Failed to write mania id file: {}", e.getMessage());
    }
  }

  private static String getCabinetUuidFileValue() {
    try {
      String localAppData = System.getenv("LOCALAPPDATA");
      Path appDataPath = Paths.get(localAppData, "VPin-Studio");
      File idFile = new File(appDataPath.toFile(), VPIN_MANIA_ID_TXT);
      if (idFile.exists()) {
        List<String> strings = FileUtils.readLines(idFile, Charset.defaultCharset());
        if (!strings.isEmpty()) {
          return strings.get(0).trim();
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to read mania id file: {}", e.getMessage());
    }
    return null;
  }


  public boolean deleteCabinet() {
    try {
      maniaSettings = preferencesService.getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
      maniaSettings.setCabinetUuid(null);
      preferencesService.savePreference(maniaSettings);

      return maniaClient.getCabinetClient().deleteCabinet();
    }
    catch (Exception e) {
      LOG.info("Failed to delete cabinet from VPin Mania: {}", e.getMessage(), e);
    }
    return false;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (Features.MANIA_ENABLED) {
      try {
        LOG.info("Initializing VPin Mania Service");
        frontendStatusService.addFrontendStatusChangeListener(this);
        frontendStatusService.addTableStatusChangeListener(this);
        gameLifecycleService.addGameDataChangedListener(this);
        gameLifecycleService.addGameLifecycleListener(this);

        maniaSettings = preferencesService.getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);

        ManiaConfig config = getConfig();
        maniaClient = new VPinManiaClient(config.getUrl(), maniaSettings.getApiKey());
        maniaServiceCache.setManiaService(this);
        ServerFX.maniaClient = maniaClient;


        maniaSettings = preferencesService.getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);

        refreshDefaultCabinet();
        Cabinet cabinet = maniaClient.getCabinetClient().getDefaultCabinetCached();
        if (cabinet != null) {
          updateCabinetUuidFile(cabinet.getUuid());
        }
        preferencesService.addChangeListener(this);

        if (cabinet != null) {
          updateIdFile(cabinet.getUuid());
        }

        new Thread(() -> {
          setOnline(cabinet);
        }).start();
      }
      catch (Exception e) {
        LOG.error("Failed to init mania services: {}", e.getMessage(), e);
//        Features.MANIA_ENABLED = false;
      }
    }
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }

  private static void updateIdFile(@NonNull String uuid) {
    try {
      String localAppData = System.getenv("LOCALAPPDATA");
      Path appDataPath = Paths.get(localAppData, "VPin-Studio");
      Files.createDirectories(appDataPath);
      File idFile = new File(appDataPath.toFile(), VPIN_MANIA_ID_TXT);
      if (idFile.exists() && !idFile.delete()) {
        LOG.error("Failed to delete mania id file");
        return;
      }
      Files.writeString(idFile.toPath(), uuid);
    }
    catch (Exception e) {
      LOG.error("Failed to write mania id file: {}", e.getMessage());
    }
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    try {
      if (Features.MANIA_ENABLED) {
        refreshDefaultCabinet();
        Cabinet cabinet = maniaClient.getCabinetClient().getDefaultCabinetCached();
        if (cabinet != null) {
          new Thread(() -> {
            Thread.currentThread().setName("VPin Mania Tables Synchronizer");
            LOG.info("Cabinet is registered on VPin-Mania");
            synchronizeTables();
          }).start();
        }
        else {
          LOG.info("Cabinet is not registered on VPin-Mania");
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to initialize mania service after startup: {}", e.getMessage(), e);
    }
  }

  public void shutdown() {
    this.setOffline();
    LOG.info("Cabinet has been set into offline mode.");
  }
}

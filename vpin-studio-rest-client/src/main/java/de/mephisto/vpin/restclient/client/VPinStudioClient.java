package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.OverlayClient;
import de.mephisto.vpin.restclient.RestClient;
import de.mephisto.vpin.restclient.altcolor.AltColorServiceClient;
import de.mephisto.vpin.restclient.altsound.AltSoundServiceClient;
import de.mephisto.vpin.restclient.alx.AlxServiceClient;
import de.mephisto.vpin.restclient.archiving.ArchiveServiceClient;
import de.mephisto.vpin.restclient.assets.AssetServiceClient;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.cards.HighscoreCardTemplatesServiceClient;
import de.mephisto.vpin.restclient.cards.HighscoreCardsServiceClient;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.competitions.CompetitionsServiceClient;
import de.mephisto.vpin.restclient.components.ComponentServiceClient;
import de.mephisto.vpin.restclient.directb2s.BackglassServiceClient;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.discord.DiscordServiceClient;
import de.mephisto.vpin.restclient.dmd.DMDPositionServiceClient;
import de.mephisto.vpin.restclient.dmd.DMDServiceClient;
import de.mephisto.vpin.restclient.dof.DOFServiceClient;
import de.mephisto.vpin.restclient.doflinx.DOFLinxServiceClient;
import de.mephisto.vpin.restclient.frontend.FrontendServiceClient;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.*;
import de.mephisto.vpin.restclient.highscores.HigscoreBackupServiceClient;
import de.mephisto.vpin.restclient.highscores.ScoreListRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
import de.mephisto.vpin.restclient.hooks.HooksServiceClient;
import de.mephisto.vpin.restclient.ini.IniServiceClient;
import de.mephisto.vpin.restclient.jobs.JobsServiceClient;
import de.mephisto.vpin.restclient.mame.MameServiceClient;
import de.mephisto.vpin.restclient.mania.ManiaServiceClient;
import de.mephisto.vpin.restclient.patcher.PatcherServiceClient;
import de.mephisto.vpin.restclient.players.PlayersServiceClient;
import de.mephisto.vpin.restclient.players.RankedPlayerRepresentation;
import de.mephisto.vpin.restclient.playlists.PlaylistMediaServiceClient;
import de.mephisto.vpin.restclient.playlists.PlaylistsServiceClient;
import de.mephisto.vpin.restclient.preferences.PreferencesServiceClient;
import de.mephisto.vpin.restclient.puppacks.PupPackServiceClient;
import de.mephisto.vpin.restclient.recorder.RecorderServiceClient;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.res.ResServiceClient;
import de.mephisto.vpin.restclient.system.SystemServiceClient;
import de.mephisto.vpin.restclient.textedit.TextEditorServiceClient;
import de.mephisto.vpin.restclient.tournaments.TournamentsServiceClient;
import de.mephisto.vpin.restclient.util.SystemUtil;
import de.mephisto.vpin.restclient.video.VideoConversionServiceClient;
import de.mephisto.vpin.restclient.vpbm.VpbmServiceClient;
import de.mephisto.vpin.restclient.vps.VpsServiceClient;
import de.mephisto.vpin.restclient.vpx.VpxServiceClient;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.List;

public class VPinStudioClient implements OverlayClient {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);
  public final static String API = "api/v1/";

  private final RestClient restClient;

  private final AltSoundServiceClient altSoundServiceClient;
  private final AltColorServiceClient altColorServiceClient;
  private final ArchiveServiceClient archiveServiceClient;
  private final AlxServiceClient alxServiceClient;
  private final AssetServiceClient assetServiceClient;
  private final CompetitionsServiceClient competitions;
  private final ComponentServiceClient componentServiceClient;
  private final BackglassServiceClient backglassServiceClient;
  private final DiscordServiceClient discordServiceClient;
  private final DMDServiceClient dmdServiceClient;
  private final DMDPositionServiceClient dmdPositionServiceClient;
  private final DOFServiceClient dofServiceClient;
  private final DOFLinxServiceClient dofLinxServiceClient;
  private final GamesServiceClient gamesServiceClient;
  private final GameMediaServiceClient gameMediaServiceClient;
  private final GameStatusServiceClient gameStatusServiceClient;
  private final HighscoreCardsServiceClient highscoreCardsServiceClient;
  private final HighscoreCardTemplatesServiceClient highscoreCardTemplatesServiceClient;
  private final HigscoreBackupServiceClient higscoreBackupServiceClient;
  private final HooksServiceClient hooksServiceClient;
  private final IniServiceClient iniServiceClient;
  private final ImageCache imageCache;
  private final JobsServiceClient jobsServiceClient;
  private final MameServiceClient mameServiceClient;
  private final ManiaServiceClient maniaServiceClient;
  private final NVRamsServiceClient nvRamsServiceClient;
  private final PlayersServiceClient playersServiceClient;
  private final FrontendServiceClient frontendServiceClient;
  private final PreferencesServiceClient preferencesServiceClient;
  private final RecorderServiceClient recorderServiceClient;
  private final PupPackServiceClient pupPackServiceClient;
  private final PatcherServiceClient patcherServiceClient;
  private final SystemServiceClient systemServiceClient;
  private final TournamentsServiceClient tournamentsServiceClient;
  private final TextEditorServiceClient textEditorServiceClient;
  private final PinVolServiceClient pinVolServiceClient;
  private final ResServiceClient resServiceClient;
  private final PINemHiServiceClient pinemHiServiceClient;
  private final PlaylistsServiceClient playlistsServiceClient;
  private final PlaylistMediaServiceClient playlistMediaServiceClient;
  private final VideoConversionServiceClient videoConversionServiceClient;
  private final VpbmServiceClient vpbmServiceClient;
  private final VpxServiceClient vpxServiceClient;
  private final VpsServiceClient vpsServiceClient;

  public VPinStudioClient(String host) {
    restClient = RestClient.createInstance(host, SystemUtil.getPort());
    this.preferencesServiceClient = new PreferencesServiceClient(this);

    this.alxServiceClient = new AlxServiceClient(this);
    this.altColorServiceClient = new AltColorServiceClient(this);
    this.altSoundServiceClient = new AltSoundServiceClient(this);
    this.archiveServiceClient = new ArchiveServiceClient(this);
    this.assetServiceClient = new AssetServiceClient(this);
    this.competitions = new CompetitionsServiceClient(this);
    this.componentServiceClient = new ComponentServiceClient(this);
    this.backglassServiceClient = new BackglassServiceClient(this);
    this.dmdServiceClient = new DMDServiceClient(this);
    this.dmdPositionServiceClient = new DMDPositionServiceClient(this);
    this.dofServiceClient = new DOFServiceClient(this);
    this.dofLinxServiceClient = new DOFLinxServiceClient(this);
    this.discordServiceClient = new DiscordServiceClient(this);
    this.gamesServiceClient = new GamesServiceClient(this);
    this.gameMediaServiceClient = new GameMediaServiceClient(this);
    this.gameStatusServiceClient = new GameStatusServiceClient(this);
    this.highscoreCardsServiceClient = new HighscoreCardsServiceClient(this);
    this.highscoreCardTemplatesServiceClient = new HighscoreCardTemplatesServiceClient(this);
    this.hooksServiceClient = new HooksServiceClient(this);
    this.imageCache = new ImageCache(this);
    this.iniServiceClient = new IniServiceClient(this);
    this.jobsServiceClient = new JobsServiceClient(this);
    this.mameServiceClient = new MameServiceClient(this);
    this.maniaServiceClient = new ManiaServiceClient(this);
    this.nvRamsServiceClient = new NVRamsServiceClient(this);
    this.playersServiceClient = new PlayersServiceClient(this);
    this.resServiceClient = new ResServiceClient(this);
    this.recorderServiceClient = new RecorderServiceClient(this);
    this.pupPackServiceClient = new PupPackServiceClient(this);
    this.patcherServiceClient = new PatcherServiceClient(this);
    this.frontendServiceClient = new FrontendServiceClient(this);
    this.systemServiceClient = new SystemServiceClient(this);
    this.textEditorServiceClient = new TextEditorServiceClient(this);
    this.vpxServiceClient = new VpxServiceClient(this);
    this.vpsServiceClient = new VpsServiceClient(this);
    this.vpbmServiceClient = new VpbmServiceClient(this);
    this.pinVolServiceClient = new PinVolServiceClient(this);
    this.pinemHiServiceClient = new PINemHiServiceClient(this);
    this.playlistsServiceClient = new PlaylistsServiceClient(this);
    this.playlistMediaServiceClient = new PlaylistMediaServiceClient(this);
    this.higscoreBackupServiceClient = new HigscoreBackupServiceClient(this);
    this.videoConversionServiceClient = new VideoConversionServiceClient(this);

    this.tournamentsServiceClient = new TournamentsServiceClient(this, preferencesServiceClient);
  }

  public HooksServiceClient getHooksService() {
    return hooksServiceClient;
  }

  public DMDPositionServiceClient getDmdPositionService() {
    return dmdPositionServiceClient;
  }

  public PatcherServiceClient getPatcherService() {
    return patcherServiceClient;
  }

  public RecorderServiceClient getRecorderService() {
    return recorderServiceClient;
  }

  public VideoConversionServiceClient getVideoConversionService() {
    return videoConversionServiceClient;
  }

  public ManiaServiceClient getManiaService() {
    return maniaServiceClient;
  }

  public DOFLinxServiceClient getDofLinxService() {
    return dofLinxServiceClient;
  }

  public PlaylistMediaServiceClient getPlaylistMediaService() {
    return playlistMediaServiceClient;
  }

  public IniServiceClient getIniService() {
    return iniServiceClient;
  }

  public ResServiceClient getResService() {
    return resServiceClient;
  }

  public HighscoreCardTemplatesServiceClient getHighscoreCardTemplatesClient() {
    return highscoreCardTemplatesServiceClient;
  }

  public TextEditorServiceClient getTextEditorService() {
    return textEditorServiceClient;
  }

  public GameStatusServiceClient getGameStatusService() {
    return gameStatusServiceClient;
  }

  public TournamentsServiceClient getTournamentsService() {
    return tournamentsServiceClient;
  }

  public void setErrorHandler(VPinStudioClientErrorHandler errorHandler) {
    restClient.setErrorHandler(errorHandler);
  }

  public AlxServiceClient getAlxService() {
    return alxServiceClient;
  }

  public DOFServiceClient getDofService() {
    return dofServiceClient;
  }

  public ComponentServiceClient getComponentService() {
    return componentServiceClient;
  }

  public DMDServiceClient getDmdService() {
    return dmdServiceClient;
  }

  public NVRamsServiceClient getNvRamsService() {
    return nvRamsServiceClient;
  }

  public VpsServiceClient getVpsService() {
    return vpsServiceClient;
  }

  public HigscoreBackupServiceClient getHigscoreBackupService() {
    return higscoreBackupServiceClient;
  }

  public PlaylistsServiceClient getPlaylistsService() {
    return playlistsServiceClient;
  }

  public PINemHiServiceClient getPINemHiService() {
    return pinemHiServiceClient;
  }

  public PinVolServiceClient getPinVolService() {
    return pinVolServiceClient;
  }

  public AltColorServiceClient getAltColorService() {
    return altColorServiceClient;
  }

  public MameServiceClient getMameService() {
    return mameServiceClient;
  }

  public PupPackServiceClient getPupPackService() {
    return pupPackServiceClient;
  }

  public AltSoundServiceClient getAltSoundService() {
    return altSoundServiceClient;
  }

  public ArchiveServiceClient getArchiveService() {
    return archiveServiceClient;
  }

  public AssetServiceClient getAssetService() {
    return assetServiceClient;
  }

  public CompetitionsServiceClient getCompetitionService() {
    return competitions;
  }

  public BackglassServiceClient getBackglassServiceClient() {
    return backglassServiceClient;
  }

  public DiscordServiceClient getDiscordService() {
    return discordServiceClient;
  }

  public GamesServiceClient getGameService() {
    return gamesServiceClient;
  }

  public GameMediaServiceClient getGameMediaService() {
    return gameMediaServiceClient;
  }

  public HighscoreCardsServiceClient getHighscoreCardsService() {
    return highscoreCardsServiceClient;
  }

  public ImageCache getImageCache() {
    return imageCache;
  }

  public JobsServiceClient getJobsService() {
    return jobsServiceClient;
  }

  public PlayersServiceClient getPlayerService() {
    return playersServiceClient;
  }

  public FrontendServiceClient getFrontendService() {
    return frontendServiceClient;
  }

  public PreferencesServiceClient getPreferenceService() {
    return preferencesServiceClient;
  }

  public SystemServiceClient getSystemService() {
    return systemServiceClient;
  }

  public VpxServiceClient getVpxService() {
    return vpxServiceClient;
  }

  public VpbmServiceClient getVpbmService() {
    return vpbmServiceClient;
  }

  @Override
  public DiscordServer getDiscordServer(long serverId) {
    return discordServiceClient.getDiscordServer(serverId);
  }

  @Override
  public List<CompetitionRepresentation> getFinishedCompetitions(int limit) {
    return getCompetitionService().getFinishedCompetitions(limit);
  }

  @Override
  public CompetitionRepresentation getActiveCompetition(CompetitionType type) {
    return getCompetitionService().getActiveCompetition(type);
  }

  @Override
  public GameRepresentation getGame(int id) {
    return getGameService().getGame(id);
  }

  @Override
  public FrontendMediaRepresentation getFrontendMedia(int id) {
    return getFrontendService().getFrontendMedia(id);
  }

  /**
   * @param id
   * @return
   */
  @Override
  public GameRepresentation getGameCached(int id) {
    return getGameService().getVpxGameCached(id);
  }

  @Override
  public InputStream getCachedUrlImage(String url) {
    return getImageCache().getCachedUrlImage(url);
  }

  @Override
  public InputStream getPersistentCachedUrlImage(String cache, String url) {
    try {
      String asset = url.substring(url.lastIndexOf("/") + 1, url.length());
      File folder = new File("./resources/cache/" + cache + "/");
      if (!folder.exists()) {
        folder.mkdirs();
      }
      File file = new File(folder, asset);
      if (file.exists()) {
        return new FileInputStream(file);
      }

      InputStream in = getImageCache().getCachedUrlImage(url);
      if (in != null) {
        FileOutputStream out = new FileOutputStream(file);
        IOUtils.copy(in, out);
        LOG.info("Persisted for cache '" + cache + "': " + file.getAbsolutePath());
        in.close();
        out.close();
      }

      if (file.exists()) {
        return new FileInputStream(file);
      }
    }
    catch (Exception e) {
      LOG.error("Caching error: " + e.getMessage(), e);
    }
    return null;
  }

  @Override
  public ScoreSummaryRepresentation getCompetitionScore(long id) {
    return getCompetitionService().getCompetitionScore(id);
  }

  @Override
  public VpsTableVersion getVpsTableVersion(@Nullable String tableId, @Nullable String versionId) {
    VpsTable table = getVpsService().getTableById(tableId);
    if (table != null && versionId != null) {
      return table.getTableVersionById(versionId);
    }
    return null;
  }

  @Override
  public List<CompetitionRepresentation> getIScoredSubscriptions() {
    return getCompetitionService().getIScoredSubscriptions();
  }

  @Override
  public ByteArrayInputStream getCompetitionBackground(long gameId) {
    return getCompetitionService().getCompetitionBackground(gameId);
  }

  @Override
  public ScoreListRepresentation getCompetitionScoreList(long id) {
    return getCompetitionService().getCompetitionScoreList(id);
  }

  @Override
  public ByteArrayInputStream getAsset(AssetType assetType, String uuid) {
    return getAssetService().getAsset(assetType, uuid);
  }

  @Override
  public ScoreSummaryRepresentation getRecentScores(int count) {
    return getGameService().getRecentScores(count);
  }

  @Override
  public ByteArrayInputStream getGameMediaItem(int id, @Nullable VPinScreen screen) {
    return getAssetService().getGameMediaItem(id, screen);
  }

  @Override
  public PreferenceEntryRepresentation getPreference(String key) {
    return this.preferencesServiceClient.getPreference(key);
  }

  @Override
  public <T> T getJsonPreference(String key, Class<T> clazz) {
    return this.preferencesServiceClient.getJsonPreference(key, clazz);
  }

  @Override
  public List<RankedPlayerRepresentation> getRankedPlayers() {
    return getPlayerService().getRankedPlayers();
  }

  public RestClient getRestClient() {
    return restClient;
  }


  public void clearDiscordCache() {
    restClient.clearCache("discord/");
  }


  /*********************************************************************************************************************
   * Utils
   */
  public void download(@NonNull String url, @NonNull File target) throws Exception {
    RestTemplate template = new RestTemplate();
    LOG.info("HTTP Download " + restClient.getBaseUrl() + VPinStudioClientService.API + url);
    File file = template.execute(restClient.getBaseUrl() + VPinStudioClientService.API + url, HttpMethod.GET, null, clientHttpResponse -> {
      FileOutputStream out = null;
      try {
        out = new FileOutputStream(target);
        StreamUtils.copy(clientHttpResponse.getBody(), out);
        return target;
      }
      catch (Exception e) {
        throw e;
      }
      finally {
        out.close();
      }
    });
  }

  public String getURL(String segment) {
    if (!segment.startsWith("http") && !segment.contains(VPinStudioClientService.API)) {
      return restClient.getBaseUrl() + VPinStudioClientService.API + segment;
    }
    return segment;
  }

  public void clearCache() {
    getManiaService().clearCache();
    getHooksService().clearCache();
    getNvRamsService().clearCache();
    getPinVolService().clearCache();
    getBackglassServiceClient().clearCache();
    getDiscordService().clearCache();
    getImageCache().clearCache();
    getGameService().clearCache();
    getSystemService().clearCache();
    getMameService().clearCache();
    getPupPackService().clearCache();
    getDmdService().clearCache();
    getFrontendService().clearCache();
  }

  public void clearWheelCache() {
    getImageCache().clearWheelCache();
  }
}

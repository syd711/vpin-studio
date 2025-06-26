package de.mephisto.vpin.server.vps;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.VpsDiffer;
import de.mephisto.vpin.connectors.vps.matcher.VpsMatch;
import de.mephisto.vpin.connectors.vps.matcher.VpsAutomatcher;
import de.mephisto.vpin.connectors.vps.model.VPSChange;
import de.mephisto.vpin.connectors.vps.model.VPSChanges;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.vps.VpsInstallLink;
import de.mephisto.vpin.restclient.vpf.VPFSettings;
import de.mephisto.vpin.restclient.vps.VpsSettings;
import de.mephisto.vpin.restclient.vpu.VPUSettings;
import de.mephisto.vpin.restclient.vpx.TableInfo;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameDetails;
import de.mephisto.vpin.server.games.GameDetailsRepository;
import de.mephisto.vpin.server.games.GameLifecycleService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.vpsdb.VpsDbEntry;
import de.mephisto.vpin.server.vpsdb.VpsEntryService;
import de.mephisto.vpin.server.vpx.VPXService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static de.mephisto.vpin.server.VPinStudioServer.Features;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VpsService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(VpsService.class);

  @Autowired
  private VPXService vpxService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private GameDetailsRepository gameDetailsRepository;

  @Autowired
  private GameLifecycleService gameLifecycleService;

  @Autowired
  private VpsEntryService vpsEntryService;

  /**
   * Internal VPS database
   */
  private VPS vpsDatabase;

  private VpsAutomatcher automatcher = new VpsAutomatcher(null);

  /**
   * Match game and fill associated TableDetail with VPS Database mapping
   *
   * @return non null array of ids if matching was done
   */
  public VpsMatch autoMatch(Game game, boolean overwrite) {
    TableInfo tableInfo = vpxService.getTableInfo(game);
    return autoMatch(vpsDatabase, game, tableInfo, false, overwrite, game.isFxGame());
  }

  /**
   * Match game and return a GameVpsMatch with the VPS Database mapping
   *
   * @return GameVpsMatch of ids
   */
  public VpsMatch autoMatch(VPS vpsDatabase, Game game, @Nullable TableInfo tableInfo, boolean checkall, boolean overwrite, boolean useDisplayName) {
    VpsMatch vpsMatch = new VpsMatch();
    vpsMatch.setGameId(game.getId());
    vpsMatch.setExtTableId(game.getExtTableId());
    vpsMatch.setExtTableVersionId(game.getExtTableVersionId());
    vpsMatch.setVersion(game.getVersion());

    String gameFileName = game.getGameFileName();
    if (useDisplayName) {
      gameFileName = game.getGameDisplayName();
    }
    try {
      gameFileName = FilenameUtils.getBaseName(gameFileName);
    }
    catch (Exception e) {
      //may happen for FX games
      LOG.warn("Failed to determine base name: {}", e.getMessage());
    }

    String[] tableFormats = game.getEmulator().getVpsEmulatorFeatures();
    File gamefile = game.getGameFile();
    long lastmodified = gamefile != null && gamefile.exists() ? gamefile.lastModified() : -1;

    automatcher.autoMatch(vpsMatch, vpsDatabase, tableFormats, gameFileName, game.getRom(),
        tableInfo != null ? tableInfo.getTableName() : null,
        tableInfo != null ? tableInfo.getAuthorName() : null,
        tableInfo != null ? tableInfo.getTableVersion() : null,
        lastmodified, overwrite);
    return vpsMatch;
  }


  /**
   * Checks the available table data for updates again the VPS.
   *
   * @param game the game to check
   */
  public void applyVersionInfo(@NonNull Game game) {
    String gameVersion = game.getVersion();
    game.setUpdateAvailable(false);

    if (StringUtils.isEmpty(gameVersion)) {
      return;
    }

    VpsTableVersion tableVersion = getVpsVersion(game);
    if (tableVersion == null) {
      return;
    }

    String vpsVersion = tableVersion.getVersion();
    game.setExtVersion(vpsVersion);

    if (!StringUtils.isEmpty(vpsVersion)) {
      if (gameVersion.equalsIgnoreCase(vpsVersion)) {
        return;
      }
    }

    game.setUpdateAvailable(true);
  }

  private VpsTableVersion getVpsVersion(@NonNull Game game) {
    return getVpsVersion(game.getExtTableId(), game.getExtTableVersionId());
  }

  public VpsTableVersion getVpsVersion(@Nullable String vpsTableId, @Nullable String vpsVersionId) {
    if (StringUtils.isEmpty(vpsTableId) || StringUtils.isEmpty(vpsVersionId)) {
      return null;
    }

    VpsTable vpsTable = vpsDatabase.getTableById(vpsTableId);
    if (vpsTable == null) {
      return null;
    }

    // Not needed ?
    //augmentTable(vpsTable);

    VpsTableVersion tableVersion = vpsTable.getTableVersionById(vpsVersionId);
    if (tableVersion == null || StringUtils.isEmpty(tableVersion.getVersion())) {
      return null;
    }

    return tableVersion;
  }

  private void augmentTable(VpsTable vpsTable, VpsDbEntry vpsDbEntry) {
    if (vpsTable != null && vpsDbEntry != null) {
      vpsTable.setComment(vpsDbEntry.getComment());
    }
  }

  public List<VpsTable> getTables() {
    List<VpsDbEntry> entries = vpsEntryService.getAllVpsEntries();
    Map<String, VpsDbEntry> entriesById = entries.stream().collect(Collectors.toMap(v -> v.getVpsTableId(), v -> v));
    vpsDatabase.getTables().forEach(v -> augmentTable(v, entriesById.get(v.getId())));
    return vpsDatabase.getTables();
  }

  public VpsTable getTableById(String extTableId) {
    VpsTable tableById = vpsDatabase.getTableById(extTableId);
    if (tableById != null) {
      VpsDbEntry vpsDbEntry = vpsEntryService.getVpsEntry(tableById.getId());
      augmentTable(tableById, vpsDbEntry);
    }
    return tableById;
  }

  public List<VpsTable> find(String term, String rom) {
    return vpsDatabase.find(term, rom);
  }

  public boolean update(List<Game> games) {
    VpsSettings vpsSettings = preferencesService.getJsonPreference(PreferenceNames.VPS_SETTINGS, VpsSettings.class);
    List<String> denyList = new ArrayList<>();
    if (!StringUtils.isEmpty(vpsSettings.getAuthorDenyList())) {
      String[] split = vpsSettings.getAuthorDenyList().split(",");
      for (String s : split) {
        if (!StringUtils.isEmpty(s)) {
          denyList.add(s.trim());
        }
      }
    }


    List<VpsDiffer> update = vpsDatabase.update(denyList);
    applyVPSDiff(update, games);
    return update.isEmpty();
  }

  public boolean reload() {
    return vpsDatabase.reload();
  }

  public Date getChangeDate() {
    return vpsDatabase.getChangeDate();
  }

  private void applyVPSDiff(List<VpsDiffer> diff, List<Game> games) {
    for (VpsDiffer tableDiff : diff) {
      try {
        List<Game> collect = games.stream().filter(g -> String.valueOf(g.getExtTableId()).equals(tableDiff.getId())).collect(Collectors.toList());
        for (Game game : collect) {
          GameDetails gameDetails = gameDetailsRepository.findByPupId(game.getId());
          if (gameDetails != null) {
            VPSChanges changes = tableDiff.getTableChanges();
            String json = changes.toJson();
            List<String> changeTypes = changes.getChanges().stream().map(c -> c.getDiffType().name()).collect(Collectors.toList());
            LOG.info("Updating change list for \"" + game.getGameDisplayName() + "\" (" + tableDiff.getChanges().getChanges().size() + " entries): " + String.join(", ", changeTypes));
            gameDetails.setUpdates(json);
            gameDetailsRepository.saveAndFlush(gameDetails);
            gameLifecycleService.notifyGameUpdated(game.getId());
          }
        }
      }
      catch (Exception e) {
        LOG.error("Failed to update game details for VPS changes: " + e.getMessage(), e);
      }
    }
  }


  //----------------------------------------------------------------
  // Installation of Assets from external sources
  public String checkLogin(String link) {
    try {
      VpsInstaller installer = getInstaller(link);
      return installer != null ? installer.login() : "Source not supported";
    }
    catch (IOException ioe) {
      LOG.error("Check login for " + link + " failed, " + ioe.getMessage());
      return "Error while authenticating, please try again";
    }
  }

  public List<VpsInstallLink> getInstallLinks(String link) {
    try {
      VpsInstaller installer = getInstaller(link);
      if (installer != null) {
        LOG.info("Get all links for {}:", link);
        List<VpsInstallLink> links = installer.getInstallLinks(link);
        for (VpsInstallLink l : links) {
          LOG.info("link " + l.getOrder() + ", " + l.getName() + " (" + l.getSize() + ")");
        }
        return links;
      }
    }
    catch (IOException ioe) {
      LOG.error("Couldn't get links for " + link + ", " + ioe.getMessage());
    }
    return new ArrayList<>();
  }

  public void downloadLink(FileOutputStream fout, String link, int order) throws IOException {
    VpsInstaller installer = getInstaller(link);
    if (installer != null) {
      installer.downloadLink(fout, link, order);
    }
  }

  private VpsInstaller getInstaller(String link) {
    if (Features.VP_UNIVERSE && link.contains("vpuniverse.com")) {
      VPUSettings settings = preferencesService.getJsonPreference(PreferenceNames.VPU_SETTINGS, VPUSettings.class);
      return new VpsInstallerFromVPU(settings);
    }
    else if (Features.VP_FORUMS && link.contains("vpforums.org")) {
      VPFSettings settings = preferencesService.getJsonPreference(PreferenceNames.VPF_SETTINGS, VPFSettings.class);
      return new VpsInstallerFromVPF(settings);
    }
    return null;
  }

  public VpsTable save(VpsTable vpsTable) {
    vpsEntryService.save(vpsTable);
    return getTableById(vpsTable.getId());
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    try {
      // create and load from file the VPS Database
      this.vpsDatabase = new VPS();
      this.vpsDatabase.reload();
    }
    catch (Exception e) {
      LOG.info("Failed to initialize VPS service: " + e.getMessage(), e);
    }
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}

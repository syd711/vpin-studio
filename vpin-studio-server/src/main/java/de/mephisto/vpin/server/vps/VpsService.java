package de.mephisto.vpin.server.vps;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.VpsDiffer;
import de.mephisto.vpin.connectors.vps.VpsSheetChangedListener;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.vpx.TableInfo;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameDetails;
import de.mephisto.vpin.server.games.GameDetailsRepository;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.vpx.VPXService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VpsService implements ApplicationContextAware, InitializingBean, VpsSheetChangedListener, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(VpsService.class);

  private ApplicationContext applicationContext;

  @Autowired
  private VPXService vpxService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private GameDetailsRepository gameDetailsRepository;

  /**
   * Internal VPS database
   */
  private VPS vpsDatabase;

  private ServerSettings serverSettings;

  /**
   * Match game and fill associated TableDetail with VPS Database mapping
   *
   * @return true if matching was done and TableDetail modified
   */
  public boolean autoMatch(Game game, TableDetails tableDetails, boolean overwrite) {
    try {
      String mappingVpsTableId = serverSettings.getMappingVpsTableId();
      String mappingVpsTableVersionId = serverSettings.getMappingVpsTableVersionId();

      TableMatcher matcher = TableMatcher.getInstance();

      VpsTable vpsTable = null;

      // first check already mapped table and confirm mapping
      if (StringUtils.isNotEmpty(game.getExtTableId())) {
        VpsTable vpsTableById = vpsDatabase.getTableById(game.getExtTableId());
        if (matcher.isClose(game.getGameDisplayName(), game.getRom(), vpsTableById)) {
          vpsTable = vpsTableById;
        }
      }
      // if not found, find closest
      if (vpsTable == null) {
        VpsTable vpsCloseTable = matcher.findClosest(game.getGameDisplayName(), game.getRom(), vpsDatabase.getTables());
        if (vpsCloseTable != null) {
          vpsTable = vpsCloseTable;
        }
      }

      if (vpsTable != null) {
        // table found => update the TableId
        if (StringUtils.isEmpty(game.getExtTableId()) || overwrite) {
          tableDetails.setMappedValue(mappingVpsTableId, vpsTable.getId());
        }

        if (StringUtils.isEmpty(game.getExtTableVersionId()) || overwrite) {

          TableInfo tableInfo = vpxService.getTableInfo(game);

          VpsTableVersion version = matcher.findVersion(vpsTable, game, tableInfo);
          if (version != null) {
            LOG.info(game.getGameDisplayName() + ": Applied table version \"" + version + "\"");
            tableDetails.setMappedValue(mappingVpsTableVersionId, version.getId());
          }
          else {
            LOG.info(game.getGameDisplayName() + ": Emptied table version");
            tableDetails.setMappedValue(mappingVpsTableVersionId, null);
          }
        }
      }
      LOG.info("Finished auto-match for \"" + game.getGameDisplayName() + "\"");
      return true;
    } catch (Exception e) {
      LOG.error("Error auto-matching table data: " + e.getMessage(), e);
    }
    return false;
  }

  /**
   * Checks the available table data for updates again the VPS.
   *
   * @param game the game to check
   */
  public void applyVersionInfo(@NonNull Game game) {
    String gameVersion = game.getVersion();
    game.setUpdateAvailable(false);

    if (gameVersion == null) {
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
    if (StringUtils.isEmpty(game.getExtTableId()) || StringUtils.isEmpty(game.getExtTableVersionId())) {
      return null;
    }

    VpsTable vpsTable = vpsDatabase.getTableById(game.getExtTableId());
    if (vpsTable == null) {
      return null;
    }

    VpsTableVersion tableVersion = vpsTable.getTableVersionById(game.getExtTableVersionId());
    if (tableVersion == null || StringUtils.isEmpty(tableVersion.getVersion())) {
      return null;
    }
    return tableVersion;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Override
  public void vpsSheetChanged(List<VpsDiffer> diff) {
    LOG.info("Updating VPS diff messages queue for " + diff.size() + " updates.");
    GameService gameService = applicationContext.getBean(GameService.class);
    List<Game> knownGames = gameService.getKnownGames(-1);

    for (VpsDiffer tableDiff : diff) {
      try {
        List<Game> collect = knownGames.stream().filter(g -> String.valueOf(g.getExtTableId()).equals(tableDiff.getId())).collect(Collectors.toList());
        for (Game game : collect) {
          GameDetails gameDetails = gameDetailsRepository.findByPupId(game.getId());
          if (gameDetails != null) {
            String json = tableDiff.getChanges().toJson();
            LOG.info("Updating change list for \"" + game.getGameDisplayName() + "\" (" + tableDiff.getChanges().getChanges().size() + " entries)");
            gameDetails.setUpdates(json);
            gameDetailsRepository.saveAndFlush(gameDetails);
          }
        }
      } catch (Exception e) {
        LOG.error("Failed to update game details for VPS changes: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldVa11lue, Object newValue) {
    if (propertyName.equals(PreferenceNames.SERVER_SETTINGS)) {
      serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    try {
      // create and load from file the VPS Database
      this.vpsDatabase = new VPS();
      // update database from VPU
      vpsDatabase.update();

      vpsDatabase.addChangeListener(this);
      preferencesService.addChangeListener(this);
      preferenceChanged(PreferenceNames.SERVER_SETTINGS, null, null);
    } catch (Exception e) {
      LOG.info("Failed to initialize VPS service: " + e.getMessage(), e);
    }
  }

  //-------------------------------------
  // Expose VPS Database method

  public List<VpsTable> getTables() {
    return vpsDatabase.getTables();
  }

  public VpsTable getTableById(String extTableId) {
    return vpsDatabase.getTableById(extTableId);
  }

  public List<VpsTable> find(String term, String rom) {
    return vpsDatabase.find(term, rom);
  }

  public List<VpsDiffer> update() {
    return vpsDatabase.update();
  }

  public boolean reload() {
    return vpsDatabase.reload();
  }

  public Date getChangeDate() {
    return vpsDatabase.getChangeDate();
  }

}

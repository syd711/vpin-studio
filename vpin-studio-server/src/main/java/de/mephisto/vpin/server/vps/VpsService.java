package de.mephisto.vpin.server.vps;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.VpsSheetChangedListener;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.VpsDiffer;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.PreferenceNames;
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
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VpsService implements ApplicationContextAware, ApplicationListener<ContextRefreshedEvent>, InitializingBean, VpsSheetChangedListener, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(VpsService.class);

  private ApplicationContext applicationContext;

  @Autowired
  private VPXService vpxService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private GameDetailsRepository gameDetailsRepository;

  private ServerSettings serverSettings;

  public VpsService() {
  }

  public boolean autoMatch(Game game, boolean overwrite) {
    try {
      String term = game.getGameDisplayName();
      List<VpsTable> vpsTables = VPS.getInstance().find(term, game.getRom());
      if (!vpsTables.isEmpty()) {
        VpsTable vpsTable = vpsTables.get(0);

        if (StringUtils.isEmpty(game.getExtTableId()) || overwrite) {
          saveExternalTableId(game, vpsTable.getId());
        }

        TableInfo tableInfo = vpxService.getTableInfo(game);
        String tableVersion = null;
        if (tableInfo != null) {
          tableVersion = tableInfo.getTableVersion();
        }

        if (StringUtils.isEmpty(game.getExtTableVersionId()) || overwrite) {
          VpsTableVersion version = VPS.getInstance().findVersion(vpsTable, game.getGameFileName(), game.getGameDisplayName(), tableVersion);
          if (version != null) {
            LOG.info(game.getGameDisplayName() + ": Applied table version \"" + version + "\"");
            saveExternalTableVersionId(game, version.getId());
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

      if ((gameVersion + ".0").equalsIgnoreCase(vpsVersion) || gameVersion.equalsIgnoreCase(vpsVersion + ".0")) {
        return;
      }
      if ((gameVersion + ".0.0").equalsIgnoreCase(vpsVersion) || gameVersion.equalsIgnoreCase(vpsVersion + ".0.0")) {
        return;
      }

      if (("v" + gameVersion).equalsIgnoreCase(vpsVersion) || gameVersion.equalsIgnoreCase("v" + vpsVersion)) {
        return;
      }

      if (game.getGameFileName().contains(vpsVersion) || game.getGameDisplayName().contains(vpsVersion)) {
        return;
      }

      if (game.getGameFileName().contains("v" + vpsVersion) || game.getGameDisplayName().contains("v" + vpsVersion)) {
        return;
      }
    }

    game.setUpdateAvailable(true);
  }

  private VpsTableVersion getVpsVersion(@NonNull Game game) {
    if (StringUtils.isEmpty(game.getExtTableId()) || StringUtils.isEmpty(game.getExtTableVersionId())) {
      return null;
    }

    VpsTable vpsTable = VPS.getInstance().getTableById(game.getExtTableId());
    if (vpsTable == null) {
      return null;
    }

    VpsTableVersion tableVersion = vpsTable.getVersion(game.getExtTableVersionId());
    if (tableVersion == null || StringUtils.isEmpty(tableVersion.getVersion())) {
      return null;
    }
    return tableVersion;
  }

  public boolean saveExternalTableVersionId(Game game, String vpsId) throws Exception {
    if (vpsId.equals("null")) {
      vpsId = null;
    }
    (applicationContext.getBean(GameService.class)).save(game);
    return true;
  }

  //TODO save to popper with mapping!
  public boolean saveExternalTableId(Game game, String vpsId) throws Exception {
    game.setExtTableId(vpsId);
    game.setExtTableVersionId(null);
    (applicationContext.getBean(GameService.class)).save(game);
    return true;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    new VpsUpdateThread(preferencesService).start();
  }

  @Override
  public void vpsSheetChanged(List<VpsDiffer> diff) {
    LOG.info("Updating VPS diff messages queue for " + diff.size() + " updates.");
    GameService gameService = applicationContext.getBean(GameService.class);
    List<Game> knownGames = gameService.getKnownGames();

    for (VpsDiffer tableDiff : diff) {
      try {
        List<Game> collect = knownGames.stream().filter(g -> String.valueOf(g.getExtTableId()).equals(tableDiff.getId())).collect(Collectors.toList());
        for (Game game : collect) {
          GameDetails gameDetails = gameDetailsRepository.findByPupId(game.getId());
          if (gameDetails != null) {
            String value = tableDiff.getDifferences().stream().map(Enum::name).collect(Collectors.joining(","));
            LOG.info("Updating change list for \"" + game.getGameDisplayName() + "\" (" + value + ")");
            gameDetails.setUpdates(value);
            gameDetailsRepository.saveAndFlush(gameDetails);
          }
        }
      } catch (Exception e) {
        LOG.error("Failed to update game details for VPS changes: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) {
    serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    VPS.getInstance().addChangeListener(this);
    preferencesService.addChangeListener(this);
    preferenceChanged(PreferenceNames.SERVER_SETTINGS, null, null);
  }
}

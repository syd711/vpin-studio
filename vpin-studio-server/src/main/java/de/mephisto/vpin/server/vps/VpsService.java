package de.mephisto.vpin.server.vps;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.VpsDiffer;
import de.mephisto.vpin.connectors.vps.VpsSheetChangedListener;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.games.GameVpsMatch;
import de.mephisto.vpin.restclient.vpx.TableInfo;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameDetails;
import de.mephisto.vpin.server.games.GameDetailsRepository;
import de.mephisto.vpin.server.games.GameService;
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
public class VpsService implements ApplicationContextAware, InitializingBean, VpsSheetChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(VpsService.class);

  private ApplicationContext applicationContext;

  @Autowired
  private VPXService vpxService;
  @Autowired
  private GameDetailsRepository gameDetailsRepository;

  /**
   * Internal VPS database
   */
  private VPS vpsDatabase;

  /**
   * Match game and fill associated TableDetail with VPS Database mapping
   * @return non null array of ids if matching was done
   */
  public GameVpsMatch autoMatch(Game game, boolean overwrite) {

    TableInfo tableInfo = vpxService.getTableInfo(game);

    VpsAutomatcher automatcher = VpsAutomatcher.getInstance();
    return automatcher.autoMatch(vpsDatabase, game, tableInfo, false, overwrite);
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
  public void afterPropertiesSet() throws Exception {
    try {
      // create and load from file the VPS Database
      this.vpsDatabase = new VPS();
      // update database from VPU
      vpsDatabase.update();

      vpsDatabase.addChangeListener(this);
    } 
    catch (Exception e) {
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

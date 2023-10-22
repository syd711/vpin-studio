package de.mephisto.vpin.server.vps;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableFile;
import de.mephisto.vpin.restclient.vpx.TableInfo;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.vpx.VPXService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VpsService {
  private final static Logger LOG = LoggerFactory.getLogger(VpsService.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private VPXService vpxService;

  public VpsService() {
  }

  public boolean autofill(int gameId, boolean overwrite) {
    try {
      Game game = gameService.getGame(gameId);
      String term = game.getGameDisplayName();
      List<VpsTable> vpsTables = VPS.getInstance().find(term);
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

        VpsTableFile version = VPS.getInstance().findVersion(vpsTable, game.getGameFileName(), game.getGameDisplayName(), tableVersion);
        if (version != null) {
          if (StringUtils.isEmpty(game.getExtTableVersionId()) || overwrite) {
            saveExternalTableVersionId(game, version.getId());
          }
        }
      }
      LOG.info("Finished auto-fill for \"" + game.getGameDisplayName() + "\"");
      return true;
    } catch (Exception e) {
      LOG.error("Error auto-filling table data: " + e.getMessage(), e);
    }
    return false;
  }

  public boolean saveExternalTableId(int gameId, String id) {
    Game game = gameService.getGame(gameId);
    return saveExternalTableId(game, id);
  }

  public boolean saveExternalTableId(Game game, String id) {
    try {
      game.setExtTableId(id);
      gameService.save(game);
      return true;
    } catch (Exception e) {
      LOG.error("Failed update VPS id: " + e.getMessage());
    }
    return false;
  }

  public boolean saveExternalTableVersionId(int gameId, String id) {
    Game game = gameService.getGame(gameId);
    return saveExternalTableVersionId(game, id);
  }

  public boolean saveExternalTableVersionId(Game game, String id) {
    try {
      game.setExtTableVersionId(id);
      gameService.save(game);
      return true;
    } catch (Exception e) {
      LOG.error("Failed update VPS version id: " + e.getMessage());
    }
    return false;
  }
}

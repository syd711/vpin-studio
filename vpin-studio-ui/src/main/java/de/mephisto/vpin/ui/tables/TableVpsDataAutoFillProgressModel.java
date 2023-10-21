package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableFile;
import de.mephisto.vpin.restclient.tables.GameRepresentation;
import de.mephisto.vpin.restclient.vpx.TableInfo;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class TableVpsDataAutoFillProgressModel extends ProgressModel<GameRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(TableVpsDataAutoFillProgressModel.class);
  private List<GameRepresentation> games;

  private final TablesSidebarVpsController controller;
  private final boolean overwrite;
  private final Iterator<GameRepresentation> gameIterator;

  public TableVpsDataAutoFillProgressModel(TablesSidebarVpsController controller, List<GameRepresentation> games, boolean overwrite) {
    super("Auto-Fill");
    this.controller = controller;
    this.overwrite = overwrite;
    this.games = games;
    this.gameIterator = games.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return games.size();
  }

  @Override
  public boolean hasNext() {
    return this.gameIterator.hasNext();
  }

  @Override
  public GameRepresentation getNext() {
    return gameIterator.next();
  }

  @Override
  public String nextToString(GameRepresentation game) {
    return game.getGameDisplayName();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, GameRepresentation game) {
    try {
      String term = game.getGameDisplayName();
      List<VpsTable> vpsTables = VPS.getInstance().find(term);
      if (!vpsTables.isEmpty()) {
        VpsTable vpsTable = vpsTables.get(0);

        if (StringUtils.isEmpty(game.getExtTableId()) || overwrite) {
          controller.saveExternalTableId(game, vpsTable.getId());
        }

        TableInfo tableInfo = Studio.client.getVpxService().getTableInfo(game);
        String tableVersion = null;
        if (tableInfo != null) {
          tableVersion = tableInfo.getTableVersion();
        }

        VpsTableFile version = VPS.getInstance().findVersion(vpsTable, game.getGameFileName(), game.getGameDisplayName(), tableVersion);
        if (version != null) {
          if (StringUtils.isEmpty(game.getExtTableVersionId()) || overwrite) {
            controller.saveExternalTableVersionId(game, version.getId());
          }
        }
        progressResultModel.addProcessed();
      }
    } catch (Exception e) {
      LOG.error("Error auto-filling table data: " + e.getMessage(), e);
    }
  }
}

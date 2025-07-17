package de.mephisto.vpin.ui.playlistmanager;

import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScored;
import de.mephisto.vpin.connectors.iscored.IScoredGame;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class StudioPlaylistFactory {
  private final static Logger LOG = LoggerFactory.getLogger(StudioPlaylistFactory.class);

  public final static List<PlaylistTemplatesController.PlaylistTemplate> TEMPLATE_LIST = Arrays.asList(
      new PlaylistTemplatesController.PlaylistTemplate(0, "Tables with an iScored subscription (visible only)"),
      new PlaylistTemplatesController.PlaylistTemplate(1, "Tables with an iScored subscription (include hidden)"),
//      new PlaylistTemplatesController.PlaylistTemplate(2, "Tables where I have no highscore"),
      new PlaylistTemplatesController.PlaylistTemplate(3, "Tables with PUP pack")
  );

  public static List<GameRepresentation> create(int templateId, int count, boolean shuffle) {
    List<GameRepresentation> result = new ArrayList<>();
    switch (templateId) {
      case 0: {
        List<CompetitionRepresentation> iScoredSubscriptions = client.getCompetitionService().getIScoredSubscriptions();
        for (CompetitionRepresentation value : iScoredSubscriptions) {
          GameRoom gameRoom = IScored.getGameRoom(value.getUrl(), false);
          IScoredGame gameByVps = gameRoom.getGameByVps(value.getVpsTableId(), value.getVpsTableVersionId());
          if (gameByVps != null && !gameByVps.isGameHidden()) {
            int gameId = value.getGameId();
            GameRepresentation game = client.getGameService().getGame(gameId);
            if (game != null) {
              result.add(game);
            }
          }
        }
        break;
      }
      case 1: {
        List<CompetitionRepresentation> iScoredSubscriptions = client.getCompetitionService().getIScoredSubscriptions();
        for (CompetitionRepresentation iScoredSubscription : iScoredSubscriptions) {
          int gameId = iScoredSubscription.getGameId();
          GameRepresentation game = client.getGameService().getGame(gameId);
          if (game != null) {
            result.add(game);
          }
        }
        break;
      }
      case 3: {
        List<GameRepresentation> vpxGamesCached = client.getGameService().getVpxGamesCached();
        for (GameRepresentation gameRepresentation : vpxGamesCached) {
          if (gameRepresentation.getPupPackName() != null) {
            result.add(gameRepresentation);
          }
        }
        break;
      }
      default: {
        LOG.error("No valid playlist template found for id {}", templateId);
      }
    }

    Collections.sort(result, Comparator.comparing(o -> o.getGameDisplayName().toLowerCase()));
    if (shuffle) {
      Collections.shuffle(result);
    }
    if (count > 0) {
      result = result.subList(0, count);
    }
    return result;
  }

  //could be used instead of SQL
  private static List<GameRepresentation> getByVPSFeature(String feature) {
    List<GameRepresentation> result = new ArrayList<>();
    List<GameEmulatorRepresentation> gameEmulators = client.getEmulatorService().getValidatedGameEmulators();
    for (GameEmulatorRepresentation gameEmulator : gameEmulators) {
      List<GameRepresentation> gamesByEmulator = client.getGameService().getGamesByEmulator(gameEmulator.getId());
      for (GameRepresentation gameRepresentation : gamesByEmulator) {
        VpsTable vpsTable = client.getVpsService().getTableById(gameRepresentation.getExtTableId());
        if (vpsTable != null) {
          VpsTableVersion tableVersion = vpsTable.getTableVersionById(gameRepresentation.getExtTableVersionId());
          if (tableVersion != null && tableVersion.getFeatures() != null && tableVersion.getFeatures().contains(feature)) {
            result.add(gameRepresentation);
          }
        }
      }
    }
    return result;
  }
}

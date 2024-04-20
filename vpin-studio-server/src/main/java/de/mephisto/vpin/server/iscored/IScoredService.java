package de.mephisto.vpin.server.iscored;

import de.mephisto.vpin.connectors.iscored.Game;
import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScored;
import de.mephisto.vpin.connectors.mania.model.TableScore;
import de.mephisto.vpin.connectors.mania.model.Tournament;
import de.mephisto.vpin.connectors.mania.model.TournamentTable;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IScoredService {
  private final static Logger LOG = LoggerFactory.getLogger(IScoredService.class);

  public void submitTableScore(@NonNull Tournament tournament, @NonNull TournamentTable tournamentTable, @NonNull TableScore tableScore) {
    try {
      String dashboardUrl = tournament.getDashboardUrl();
      if (dashboardUrl != null && dashboardUrl.contains("iscored")) {
        GameRoom gameRoom = IScored.loadGameRoom(dashboardUrl);
        if (gameRoom != null) {
          if (!gameRoom.getSettings().isPublicScoresEnabled()) {
            LOG.warn("Cancelling iscord score submission, public score submission is not enabled!");
            return;
          }

          String vpsTableId = tournamentTable.getVpsTableId();
          String vpsVersionId = tournamentTable.getVpsVersionId();

          List<Game> games = gameRoom.getGames();
          for (Game game : games) {
            if(game.isDisabled()) {
              LOG.info("Skipped iScored score submission, because table " + game + " has disabled flag set.");
            }

            List<String> tags = game.getTags();
            for (String tag : tags) {
              if (tag.contains(vpsTableId) && tag.contains(vpsVersionId)) {
                IScored.submitScore(gameRoom, game, tableScore.getPlayerName(), tableScore.getPlayerInitials(), tableScore.getScore());
                return;
              }
            }
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to load game room from " + tournament.getDashboardUrl() + ": " + e.getMessage(), e);
    }
  }

  public boolean isIscoredGameRoomUrl(String dashboardUrl) {
    return dashboardUrl.toLowerCase().contains("iscored.info") && dashboardUrl.contains("user=") && dashboardUrl.toLowerCase().contains("mode=public");
  }
}

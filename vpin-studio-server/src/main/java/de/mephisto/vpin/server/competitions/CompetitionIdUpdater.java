package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.games.GameMediaService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CompetitionIdUpdater implements CompetitionChangeListener, InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionIdUpdater.class);

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private DiscordService discordService;

  @Autowired
  private GameMediaService gameMediaService;

  @Override
  public void competitionStarted(@NonNull Competition competition) {
    setGamesTournamentId(competition);
  }

  @Override
  public void competitionCreated(@NonNull Competition competition) {
    CompetitionType type = CompetitionType.valueOf(competition.getType());
    switch (type) {
      case SUBSCRIPTION: {
        setGamesTournamentId(competition);
        break;
      }
      case ISCORED: {
        if (Features.ISCORED_ENABLED) {
          setGamesTournamentId(competition);
        }
        break;
      }
    }
  }

  @Override
  public void competitionChanged(@NonNull Competition competition) {
    //ignore
  }

  @Override
  public void competitionFinished(@NonNull Competition competition, @Nullable Player winner, @NonNull ScoreSummary scoreSummary) {
    unsetGamesTournamentId(competition);
  }

  @Override
  public void competitionDeleted(@NonNull Competition competition) {
    unsetGamesTournamentId(competition);
  }

  private void setGamesTournamentId(@NonNull Competition competition) {
    TableDetails tableDetails = gameMediaService.getTableDetails(competition.getGameId());
    if (tableDetails != null) {
      boolean isOwner = competition.getOwner() == null || competition.getOwner().equals(String.valueOf(discordService.getBotId()));
      String competitionId = CompetitionIdFactory.createId(competition, isOwner);
      List<String> updated = new ArrayList<>();
      String tournamentId = tableDetails.getTourneyId();
      if (tournamentId == null) {
        tableDetails.setTourneyId(competitionId);
      }
      else {
        String[] split = tournamentId.split(",");
        for (String s : split) {
          if (s.equalsIgnoreCase(competitionId)) {
            continue;
          }
          updated.add(s);
        }
        updated.add(competitionId);
        tableDetails.setTourneyId(String.join(",", updated));
      }

      gameMediaService.saveTableDetails(tableDetails, competition.getGameId(), false);
      LOG.info("Written competition id of game " + competition.getGameId() + ", updated TourneyId to \"" + tableDetails.getTourneyId() + "\"");
    }
  }

  private void unsetGamesTournamentId(@NonNull Competition competition) {
    TableDetails tableDetails = gameMediaService.getTableDetails(competition.getGameId());
    if (tableDetails != null) {
      boolean isOwner = competition.getOwner() == null || competition.getOwner().equals(String.valueOf(discordService.getBotId()));
      String competitionId = CompetitionIdFactory.createId(competition, isOwner);
      String tournamentId = tableDetails.getTourneyId();
      if (tournamentId != null) {
        List<String> updated = new ArrayList<>();
        String[] split = tournamentId.split(",");
        for (String s : split) {
          if (StringUtils.isEmpty(s)) {
            continue;
          }
          if (s.equalsIgnoreCase(competitionId)) {
            continue;
          }
          updated.add(s);
        }
        tableDetails.setTourneyId(String.join(",", updated));
        gameMediaService.saveTableDetails(tableDetails, competition.getGameId(), false);
        LOG.info("Removed competition id from game " + competition.getGameId() + ", updated TourneyId to \"" + tableDetails.getTourneyId() + "\"");
      }
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.competitionService.addCompetitionChangeListener(this);
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}

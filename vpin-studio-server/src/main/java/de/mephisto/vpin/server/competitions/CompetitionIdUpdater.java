package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.popper.PopperService;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
  private PopperService popperService;

  @Autowired
  private CompetitionService competitionService;

  @Override
  public void competitionStarted(@NotNull Competition competition) {
    setGamesTournamentId(competition);
  }

  @Override
  public void competitionCreated(@NotNull Competition competition) {
    CompetitionType type = CompetitionType.valueOf(competition.getType());
    switch (type) {
      case SUBSCRIPTION: {
        setGamesTournamentId(competition);
        break;
      }
      case ISCORED: {
        setGamesTournamentId(competition);
        break;
      }
    }
  }

  @Override
  public void competitionChanged(@NotNull Competition competition) {
    //ignore
  }

  @Override
  public void competitionFinished(@NotNull Competition competition, @Nullable Player winner, @NotNull ScoreSummary scoreSummary) {
    unsetGamesTournamentId(competition);
  }

  @Override
  public void competitionDeleted(@NotNull Competition competition) {
    unsetGamesTournamentId(competition);
  }

  private void setGamesTournamentId(@NotNull Competition competition) {
    TableDetails tableDetails = popperService.getTableDetails(competition.getGameId());
    String competitionId = CompetitionIdFactory.createId(competition);
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

    popperService.saveTableDetails(tableDetails, competition.getGameId(), false);
    LOG.info("Written competition id of \"" + tableDetails.getGameDisplayName() + "\", updated TourneyId to \"" + tableDetails.getTourneyId() + "\"");
  }

  private void unsetGamesTournamentId(@NotNull Competition competition) {
    TableDetails tableDetails = popperService.getTableDetails(competition.getGameId());
    String competitionId = CompetitionIdFactory.createId(competition);
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
      popperService.saveTableDetails(tableDetails, competition.getGameId(), false);
      LOG.info("Removed competition id from \"" + tableDetails.getGameDisplayName() + "\", updated TourneyId to \"" + tableDetails.getTourneyId() + "\"");
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.competitionService.addCompetitionChangeListener(this);
  }
}

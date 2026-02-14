package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.iscored.IScoredSettings;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameMediaService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static de.mephisto.vpin.server.VPinStudioServer.Features;

import java.util.ArrayList;
import java.util.List;

@Service
public class CompetitionIdUpdater implements CompetitionChangeListener, InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionIdUpdater.class);

  @Autowired
  private CompetitionLifecycleService competitionLifecycleService;

  @Autowired
  private DiscordService discordService;

  @Autowired
  private GameMediaService gameMediaService;

  @Autowired
  private GameService gameService;

  @Autowired
  private PreferencesService preferencesService;

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
        IScoredSettings iScoredSettings = preferencesService.getJsonPreference(PreferenceNames.ISCORED_SETTINGS, IScoredSettings.class);
        if (Features.ISCORED_ENABLED && iScoredSettings.isEnabled()) {
          setGamesTournamentId(competition);
        }
        break;
      }
    }
  }

  @Override
  public void competitionFinished(@NonNull Competition competition, @Nullable Player winner, @NonNull ScoreSummary scoreSummary) {
    unsetGamesTournamentId(competition);
  }

  @Override
  public void competitionDeleted(@NonNull Competition competition) {
    unsetGamesTournamentId(competition);
  }

  @Override
  public void competitionChanged(@NotNull Competition competition) {
    TableDetails tableDetails = gameMediaService.getTableDetails(competition.getGameId());
    if (tableDetails != null) {
      setTourneyId(competition, tableDetails, competition.getGameId());
    }
  }

  private void setGamesTournamentId(@NonNull Competition competition) {
    CompetitionType competitionType = CompetitionType.valueOf(competition.getType().toUpperCase());
    if (competitionType.equals(CompetitionType.ISCORED)) {
      for (Game game : gameService.getGamesByVpsTableId(competition.getVpsTableId(), competition.getVpsTableVersionId())) {
        TableDetails tableDetails = gameMediaService.getTableDetails(game.getId());
        setTourneyId(competition, tableDetails, game.getId());
      }
    }
    else {
      TableDetails tableDetails = gameMediaService.getTableDetails(competition.getGameId());
      setTourneyId(competition, tableDetails, competition.getGameId());
    }
  }

  private void unsetGamesTournamentId(@NonNull Competition competition) {
    CompetitionType competitionType = CompetitionType.valueOf(competition.getType().toUpperCase());
    if (competitionType.equals(CompetitionType.ISCORED)) {
      List<Game> matches = gameService.getGamesByVpsTableId(competition.getVpsTableId(), competition.getVpsTableVersionId());
      for (Game game : matches) {
        TableDetails tableDetails = gameMediaService.getTableDetails(game.getId());
        unsetTourneyId(competition, tableDetails, game.getId());
      }
    }
    else {
      TableDetails tableDetails = gameMediaService.getTableDetails(competition.getGameId());
      unsetTourneyId(competition, tableDetails, competition.getGameId());
    }
  }

  private void unsetTourneyId(@NonNull Competition competition, @Nullable TableDetails tableDetails, int gameId) {
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
        gameMediaService.saveTableDetails(tableDetails, gameId, false);
        LOG.info("Removed competition id from game " + tableDetails.getGameFileName() + ", updated TourneyId to \"" + tableDetails.getTourneyId() + "\"");
      }
    }
  }

  private void setTourneyId(@NonNull Competition competition, @Nullable TableDetails tableDetails, int gameId) {
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

      gameMediaService.saveTableDetails(tableDetails, gameId, false);
      LOG.info("Written competition id of game " + tableDetails.getGameFileName() + ", updated TourneyId to \"" + tableDetails.getTourneyId() + "\"");
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.competitionLifecycleService.addCompetitionChangeListener(this);
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}

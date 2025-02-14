package de.mephisto.vpin.server.alx;

import de.mephisto.vpin.restclient.alx.AlxSummary;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.highscores.HighscoreVersion;
import de.mephisto.vpin.server.highscores.HighscoreVersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlxService {

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private HighscoreVersionRepository highscoreVersionRepository;

  public AlxSummary getAlxSummary(int gameId) {
    AlxSummary summary = new AlxSummary();

    summary.setStartDate(frontendService.getStartDate());

    List<TableAlxEntry> alxData = new ArrayList<>();
    if (gameId > 0) {
      alxData = frontendService.getAlxData(gameId);
    }
    else {
      alxData = frontendService.getAlxData();
    }

    for (TableAlxEntry entry : alxData) {
      List<HighscoreVersion> byGameId = highscoreVersionRepository.findByGameId(entry.getGameId());
      List<HighscoreVersion> collect = byGameId.stream().filter(score -> score.getChangedPosition() > 0).collect(Collectors.toList());
      List<HighscoreVersion> highscores = byGameId.stream().filter(score -> score.getChangedPosition() == 1).collect(Collectors.toList());
      entry.setScores(collect.size());
      entry.setHighscores(highscores.size());

      summary.getEntries().add(entry);
    }
    return summary;
  }

  public AlxSummary getAlxSummary() {
    return getAlxSummary(-1);
  }

  public boolean deleteNumberPlaysForGame(int gameId) {
    return updateNumberOfPlaysForGame(gameId, 0);
  }

  public boolean deleteNumberOfPlaysForEmulator(int emulatorId) {
    if (emulatorId == -1) {
      List<GameEmulator> gameEmulators = frontendService.getValidGameEmulators();
      for (GameEmulator gameEmulator : gameEmulators) {
        deleteNumberOfPlaysForEmu(gameEmulator.getId());
      }
    }
    else {
      deleteNumberOfPlaysForEmu(emulatorId);
    }
    return true;
  }

  public boolean deleteTimePlayedForGame(int gameId) {
    return updateSecondsPlayedForGame(gameId, 0);
  }

  public boolean deleteTimePlayedForEmulator(int emulatorId) {
    if (emulatorId == -1) {
      List<GameEmulator> gameEmulators = frontendService.getValidGameEmulators();
      for (GameEmulator gameEmulator : gameEmulators) {
        deleteTimePlayedForEmu(gameEmulator.getId());
      }
    }
    else {
      deleteTimePlayedForEmu(emulatorId);
    }
    return true;
  }

  public boolean updateNumberOfPlaysForGame(int gameId, long value) {
    return frontendService.getFrontendConnector().updateNumberOfPlaysForGame(gameId, value);
  }

  public boolean updateSecondsPlayedForGame(int gameId, long seconds) {
    return frontendService.getFrontendConnector().updateSecondsPlayedForGame(gameId, seconds);
  }

  //---------- Helper --------------------

  private void deleteNumberOfPlaysForEmu(int emulatorId) {
    List<Game> gamesByEmulator = frontendService.getGamesByEmulator(emulatorId);
    for (Game game : gamesByEmulator) {
      updateNumberOfPlaysForGame(game.getId(), 0);
    }
  }
  private void deleteTimePlayedForEmu(int emulatorId) {
    List<Game> gamesByEmulator = frontendService.getGamesByEmulator(emulatorId);
    for (Game game : gamesByEmulator) {
      updateSecondsPlayedForGame(game.getId(), 0);
    }
  }
}

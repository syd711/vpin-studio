package de.mephisto.vpin.server.tournaments;

import de.mephisto.vpin.connectors.mania.VPinManiaClient;
import de.mephisto.vpin.connectors.mania.model.Tournament;
import de.mephisto.vpin.connectors.mania.model.TournamentTable;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.tournaments.TournamentMetaData;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.popper.GameMediaItem;
import de.mephisto.vpin.server.popper.WheelAugmenter;
import de.mephisto.vpin.server.system.SystemService;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TournamentSynchronizer {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentSynchronizer.class);

  @Autowired
  private SystemService systemService;

  @Autowired
  private GameService gameService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private TournamentTablesRepository tournamentTablesRepository;
  private VPinManiaClient maniaClient;


  public boolean synchronize(TournamentMetaData metaData) {
    Tournament tournament = maniaClient.getTournamentClient().getTournament(metaData.getTournamentId());
    if (tournament != null) {
      List<TournamentTable> tournamentTables = maniaClient.getTournamentClient().getTournamentTables(tournament.getId());
      for (TournamentTable tournamentTable : tournamentTables) {
        TournamentTableInfo info = createTournamentTableInfo(metaData, tournamentTable);
        TournamentTableInfo tournamentTableInfo = tournamentTablesRepository.saveAndFlush(info);
        LOG.info("\tWritten " + tournamentTableInfo);
      }
      return true;
    }
    else {
      LOG.error("Tournament tables initialization failed, no tournament found with id " + metaData.getTournamentId());
    }
    return false;
  }

  @NotNull
  private TournamentTableInfo createTournamentTableInfo(TournamentMetaData metaData, TournamentTable tournamentTable) {
    TournamentTableInfo info = new TournamentTableInfo();
    info.setTournamentId(metaData.getTournamentId());
    info.setBadge(metaData.getBadge());
    info.setStartDate(tournamentTable.getStartDate());
    info.setEndDate(tournamentTable.getEndDate());
    info.setVpsTableId(tournamentTable.getVpsTableId());
    info.setVpsTableVersionId(tournamentTable.getVpsVersionId());

    Game game = gameService.getGameByVpsTable(tournamentTable.getVpsTableId(), tournamentTable.getVpsVersionId());
    if (game != null) {
      info.setGameId(game.getId());
    }
    return info;
  }

  public boolean synchronize() {
    List<Tournament> tournaments = maniaClient.getTournamentClient().getTournaments();
    return synchronize(tournaments);
  }

  public boolean synchronize(List<Tournament> tournaments) {
    try {
      LOG.info("S---------------------Synchronization of Tournaments-----------------------------");
      //this returns only my tournaments since the cabinet id is passed
      for (Tournament tournament : tournaments) {
        LOG.info("  Synchronization of " + tournament);
        TournamentMetaData metaData = getMetaData(tournament.getId());
        List<TournamentTable> maniaTournamentTables = maniaClient.getTournamentClient().getTournamentTables(tournament.getId());

        //delete all finished tournaments
        if (tournament.isFinished()) {
          finishTables(tournament, maniaTournamentTables);
        }

        //delete all finished tables
        for (TournamentTable maniaTournamentTable : maniaTournamentTables) {
          if (maniaTournamentTable.isFinished()) {
            Optional<TournamentTableInfo> tableInfo = tournamentTablesRepository.findByTournamentIdAndVpsTableIdAndVpsTableVersionId(tournament.getId(), maniaTournamentTable.getVpsTableId(), maniaTournamentTable.getVpsVersionId());
            if (tableInfo.isPresent()) {
              finishTable(tournament, tableInfo.get());
            }
          }
        }

        //delete those entries where the game can't be mapped anymore
        List<TournamentTableInfo> tournamentTableInfos = tournamentTablesRepository.findAllByTournamentId(tournament.getId());
        for (TournamentTableInfo tournamentTableInfo : tournamentTableInfos) {
          Game game = gameService.getGame(tournamentTableInfo.getGameId());
          if (game == null) {
            finishTable(tournament, tournamentTableInfo);
          }
        }

        //fill up tables that have been added later on
        for (TournamentTable maniaTournamentTable : maniaTournamentTables) {
          Optional<TournamentTableInfo> tableInfo = tournamentTablesRepository.findByTournamentIdAndVpsTableIdAndVpsTableVersionId(tournament.getId(), maniaTournamentTable.getVpsTableId(), maniaTournamentTable.getVpsVersionId());
          if (tableInfo.isEmpty()) {
            TournamentTableInfo tournamentTableInfo = createTournamentTableInfo(metaData, maniaTournamentTable);
            if (tournamentTableInfo.getGameId() != 0) {
              TournamentTableInfo newInfo = tournamentTablesRepository.saveAndFlush(tournamentTableInfo);
              LOG.info("\tAdded missing " + newInfo + " for " + tournament);
            }
            else {
              LOG.info(maniaTournamentTable + " has no matching game.");
            }
          }
        }

        //start active tables
        if (tournament.isActive()) {
          List<TournamentTableInfo> unstartedTables = tournamentTablesRepository.findByTournamentIdAndStarted(tournament.getId(), false);
          startTables(tournament, unstartedTables, metaData);
        }
      }

      //clean corpses
      List<Long> tournamentIds = tournaments.stream().map(t -> t.getId()).collect(Collectors.toList());
      List<TournamentTableInfo> all = tournamentTablesRepository.findAll();
      List<TournamentTableInfo> corpses = all.stream().filter(t -> !tournamentIds.contains(t.getTournamentId())).collect(Collectors.toList());
      for (TournamentTableInfo corps : corpses) {
        tournamentTablesRepository.deleteById(corps.getId());
        LOG.info("\tDeleted non-existing " + corps);
      }
      LOG.info("----------------------------/end of sync -------------------------------------------");
    } catch (Exception e) {
      LOG.error("Failed to synchronize tournaments: " + e.getMessage(), e);
    }

    return false;
  }

  private void finishTables(Tournament tournament, List<TournamentTable> tournamentTables) {
    for (TournamentTable tournamentTable : tournamentTables) {
      Optional<TournamentTableInfo> tournamentId = tournamentTablesRepository.findByTournamentIdAndVpsTableIdAndVpsTableVersionId(tournament.getId(), tournamentTable.getVpsTableId(), tournamentTable.getVpsVersionId());
      if (tournamentId.isPresent()) {
        TournamentTableInfo tournamentTableInfo = tournamentId.get();
        finishTable(tournament, tournamentTableInfo);
      }
    }
  }

  private void startTables(Tournament tournament, List<TournamentTableInfo> tournamentTables, TournamentMetaData metaData) {
    for (TournamentTableInfo tournamentTable : tournamentTables) {
      startTable(tournament, tournamentTable, metaData);
    }
  }

  private void finishTable(Tournament tournament, TournamentTableInfo tournamentTableInfo) {
    Game game = gameService.getGameByVpsTable(tournamentTableInfo.getVpsTableId(), tournamentTableInfo.getVpsTableVersionId());
    if (game != null) {
      GameMediaItem gameMediaItem = game.getGameMedia().getDefaultMediaItem(PopperScreen.Wheel);
      if (gameMediaItem != null) {
        WheelAugmenter augmenter = new WheelAugmenter(gameMediaItem.getFile());
        augmenter.deAugment();
        LOG.info("\tDe-augmented " + tournamentTableInfo);
      }
    }
    tournamentTablesRepository.deleteById(tournamentTableInfo.getId());
    LOG.info("\tDeleted finished " + tournamentTableInfo);
  }


  private void startTable(Tournament tournament, TournamentTableInfo tournamentTableInfo, TournamentMetaData metaData) {
    Game game = gameService.getGameByVpsTable(tournamentTableInfo.getVpsTableId(), tournamentTableInfo.getVpsTableVersionId());
    if (game != null) {
      GameMediaItem gameMediaItem = game.getGameMedia().getDefaultMediaItem(PopperScreen.Wheel);
      if (gameMediaItem != null) {
        if (!StringUtils.isEmpty(metaData.getBadge())) {
          File badgeFile = systemService.getBadgeFile(metaData.getBadge());
          if (badgeFile.exists()) {
            WheelAugmenter augmenter = new WheelAugmenter(gameMediaItem.getFile());
            augmenter.augment(badgeFile);
          }
        }
      }

      if (tournamentTableInfo.isHighscoreReset()) {
        if (highscoreService.resetHighscore(game)) {
          LOG.info("\tResetted highscore for " + game);
        }
      }
    }
  }

  private TournamentMetaData getMetaData(long id) {
    List<TournamentTableInfo> allByTournamentId = tournamentTablesRepository.findAllByTournamentId(id);
    TournamentMetaData metaData = new TournamentMetaData();
    metaData.setTournamentId(id);

    if (allByTournamentId.isEmpty()) {
      metaData.setResetHighscores(true);
    }
    else {
      TournamentTableInfo tournamentTableInfo = allByTournamentId.get(0);
      metaData.setBadge(tournamentTableInfo.getBadge());
      metaData.setResetHighscores(tournamentTableInfo.isHighscoreReset());
    }
    return metaData;
  }

  public void setClient(VPinManiaClient maniaClient) {
    this.maniaClient = maniaClient;
  }
}

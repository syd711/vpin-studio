package de.mephisto.vpin.server.tournaments;

import de.mephisto.vpin.connectors.mania.VPinManiaClient;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.connectors.mania.model.Tournament;
import de.mephisto.vpin.connectors.mania.model.TournamentTable;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
import de.mephisto.vpin.restclient.tournaments.TournamentMetaData;
import de.mephisto.vpin.server.frontend.WheelAugmenter;
import de.mephisto.vpin.server.frontend.WheelIconDelete;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.mania.ManiaService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TournamentSynchronizer implements ApplicationListener<ApplicationReadyEvent> {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentSynchronizer.class);

  @Autowired
  private SystemService systemService;

  @Autowired
  private GameService gameService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private TournamentTablesRepository tournamentTablesRepository;

  @Autowired
  private ManiaService maniaService;

  public synchronized boolean synchronize(TournamentMetaData metaData) {
    VPinManiaClient maniaClient = maniaService.getClient();
    if (maniaClient.getCabinetClient().getCabinet() != null) {
      ManiaSettings maniaSettings = preferencesService.getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
      if (maniaSettings.isTournamentsEnabled()) {
        List<Game> knownGames = gameService.getKnownGames(-1);
        Tournament tournament = maniaClient.getTournamentClient().getTournament(metaData.getTournamentId());
        if (tournament != null) {
          List<TournamentTable> tournamentTables = maniaClient.getTournamentClient().getTournamentTables(tournament.getId());
          for (TournamentTable tournamentTable : tournamentTables) {
            TournamentTableInfo info = createTournamentTableInfo(knownGames, metaData, tournamentTable);
            if (info.getGameId() != 0) {
              TournamentTableInfo tournamentTableInfo = tournamentTablesRepository.saveAndFlush(info);
              LOG.info("\tWritten " + tournamentTableInfo);
            }
          }
          return true;
        }
        else {
          LOG.error("Tournament tables initialization failed, no tournament found with id " + metaData.getTournamentId());
        }
      }
    }

    return false;
  }

  @NonNull
  private TournamentTableInfo createTournamentTableInfo(List<Game> knownGames, TournamentMetaData metaData, TournamentTable tournamentTable) {
    TournamentTableInfo info = new TournamentTableInfo();
    info.setTournamentId(metaData.getTournamentId());
    info.setBadge(metaData.getBadge());
    info.setHighscoreReset(metaData.isResetHighscores());
    info.setStartDate(tournamentTable.getStartDate());
    info.setEndDate(tournamentTable.getEndDate());
    info.setVpsTableId(tournamentTable.getVpsTableId());
    info.setVpsTableVersionId(tournamentTable.getVpsVersionId());

    Game game = gameService.getGameByVpsTable(knownGames, tournamentTable.getVpsTableId(), tournamentTable.getVpsVersionId());
    if (game != null) {
      info.setGameId(game.getId());
    }
    return info;
  }

  public void synchronizeTournaments() {
    try {
      VPinManiaClient maniaClient = maniaService.getClient();
      Cabinet cabinet = maniaClient.getCabinetClient().getCabinetCached();
      if (cabinet != null) {
        ManiaSettings maniaSettings = preferencesService.getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
        if (maniaSettings.isTournamentsEnabled()) {
          List<Tournament> tournaments = maniaClient.getTournamentClient().getTournaments();
          synchronize(tournaments);
        }
      }
    }
    catch (Exception e) {
      LOG.error("Tournaments synchronization failed: " + e.getMessage(), e);
    }
  }

  public void synchronize(Game game) {
    VPinManiaClient maniaClient = maniaService.getClient();
    Cabinet cabinet = maniaClient.getCabinetClient().getCabinet();
    if (cabinet != null) {
      ManiaSettings maniaSettings = preferencesService.getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);
      if (maniaSettings.isTournamentsEnabled()) {
        List<Tournament> tournaments = maniaClient.getTournamentClient().getTournaments();
        List<TournamentTableInfo> byGameId = tournamentTablesRepository.findByGameId(game.getId());
        List<Tournament> filtered = new ArrayList<>();
        for (TournamentTableInfo tournamentTableInfo : byGameId) {
          Optional<Tournament> first = tournaments.stream().filter(t -> t.getId() == tournamentTableInfo.getTournamentId()).findFirst();
          if (first.isPresent()) {
            Tournament tournament = first.get();
            if (!filtered.contains(tournament)) {
              filtered.add(tournament);
            }
          }
        }

        synchronize(filtered);
      }
    }
  }

  public void synchronize(List<Tournament> tournaments) {
    try {
      VPinManiaClient maniaClient = maniaService.getClient();
      Cabinet cabinet = maniaClient.getCabinetClient().getCabinet();
      if (cabinet == null) {
        return;
      }

      LOG.info("-----------------------Synchronization of Tournaments-----------------------------");
      //this returns only my tournaments since the cabinet id is passed
      List<Game> knownGames = gameService.getKnownGames(-1);

      for (Tournament tournament : tournaments) {
        LOG.info("  Synchronization of " + tournament);
        TournamentMetaData metaData = getMetaData(tournament.getId());
        List<TournamentTable> maniaTournamentTables = maniaClient.getTournamentClient().getTournamentTables(tournament.getId());

        //delete all finished tournaments
        if (tournament.isFinished()) {
          finishTables(knownGames, tournament, maniaTournamentTables);
        }

        //delete all finished tables
        for (TournamentTable maniaTournamentTable : maniaTournamentTables) {
          if (maniaTournamentTable.isFinished()) {
            List<TournamentTableInfo> tableInfos = tournamentTablesRepository.findByTournamentIdAndVpsTableIdAndVpsTableVersionId(tournament.getId(), maniaTournamentTable.getVpsTableId(), maniaTournamentTable.getVpsVersionId());
            for (TournamentTableInfo tableInfo : tableInfos) {
              finishTable(knownGames, tableInfo);
            }
          }
        }

        //delete those entries where the game can't be mapped anymore
        List<TournamentTableInfo> tournamentTableInfos = tournamentTablesRepository.findAllByTournamentId(tournament.getId());
        for (TournamentTableInfo tournamentTableInfo : tournamentTableInfos) {
          Game game = gameService.getGame(tournamentTableInfo.getGameId());
          if (game == null) {
            finishTable(knownGames, tournamentTableInfo);
          }
        }

        //start active tables
        if (tournament.isActive()) {
          //fill up tables that have been added later on
          for (TournamentTable maniaTournamentTable : maniaTournamentTables) {
            List<TournamentTableInfo> tableInfos = tournamentTablesRepository.findByTournamentIdAndVpsTableIdAndVpsTableVersionId(tournament.getId(), maniaTournamentTable.getVpsTableId(), maniaTournamentTable.getVpsVersionId());
            if (tableInfos.isEmpty()) {
              TournamentTableInfo tournamentTableInfo = createTournamentTableInfo(knownGames, metaData, maniaTournamentTable);
              if (tournamentTableInfo.getGameId() != 0) {
                TournamentTableInfo newInfo = tournamentTablesRepository.saveAndFlush(tournamentTableInfo);
                LOG.info("\tAdded missing " + newInfo + " for " + tournament);
              }
              else {
                LOG.info("\t" + maniaTournamentTable + " has no matching game.");
              }
            }
          }

          List<TournamentTableInfo> unstartedTables = tournamentTablesRepository.findByTournamentIdAndStarted(tournament.getId(), false);
          startTables(knownGames, tournament, unstartedTables, metaData);
        }
      }

      //clean corpses
      List<Long> tournamentIds = tournaments.stream().map(t -> t.getId()).collect(Collectors.toList());
      List<TournamentTableInfo> all = tournamentTablesRepository.findAll();
      List<TournamentTableInfo> corpses = all.stream().filter(t -> !tournamentIds.contains(t.getTournamentId())).collect(Collectors.toList());
      for (TournamentTableInfo corps : corpses) {
        finishTable(knownGames, corps);
      }
      LOG.info("----------------------------/end of sync -------------------------------------------");
    }
    catch (Exception e) {
      LOG.error("Failed to synchronize tournaments: " + e.getMessage(), e);
    }
  }

  private void finishTables(List<Game> knownGames, Tournament tournament, List<TournamentTable> tournamentTables) {
    for (TournamentTable tournamentTable : tournamentTables) {
      List<TournamentTableInfo> tableInfos = tournamentTablesRepository.findByTournamentIdAndVpsTableIdAndVpsTableVersionId(tournament.getId(), tournamentTable.getVpsTableId(), tournamentTable.getVpsVersionId());
      for (TournamentTableInfo tableInfo : tableInfos) {
        finishTable(knownGames, tableInfo);
      }
    }
  }

  private void startTables(List<Game> knownGames, Tournament tournament, List<TournamentTableInfo> tournamentTables, TournamentMetaData metaData) {
    for (TournamentTableInfo tournamentTable : tournamentTables) {
      startTable(knownGames, tournament, tournamentTable, metaData);
    }
  }

  private void finishTable(List<Game> knownGames, TournamentTableInfo tournamentTableInfo) {
    Game game = gameService.getGameByVpsTable(knownGames, tournamentTableInfo.getVpsTableId(), tournamentTableInfo.getVpsTableVersionId());
    if (game != null) {
      File wheelFile = game.getWheelImage();
      if (wheelFile != null && wheelFile.exists()) {
        new WheelAugmenter(wheelFile).deAugment();
        new WheelIconDelete(wheelFile).delete();
      }
    }
    tournamentTablesRepository.deleteById(tournamentTableInfo.getId());
    LOG.info("\tDeleted " + tournamentTableInfo);
  }


  private void startTable(List<Game> knownGames, Tournament tournament, TournamentTableInfo tournamentTableInfo, TournamentMetaData metaData) {
    Game game = gameService.getGameByVpsTable(knownGames, tournamentTableInfo.getVpsTableId(), tournamentTableInfo.getVpsTableVersionId());
    if (game != null) {
      File wheelFile = game.getWheelImage();
      if (wheelFile != null && wheelFile.exists()) {
        if (!StringUtils.isEmpty(metaData.getBadge())) {
          File badgeFile = systemService.getBadgeFile(metaData.getBadge());
          if (badgeFile.exists()) {
            WheelAugmenter augmenter = new WheelAugmenter(wheelFile);
            augmenter.augment(badgeFile);
          }
        }
      }

      if (tournamentTableInfo.isHighscoreReset()) {
        if (highscoreService.resetHighscore(game)) {
          LOG.info("\tResetted highscore for " + game);
        }
      }

      tournamentTableInfo.setStarted(true);
      tournamentTablesRepository.saveAndFlush(tournamentTableInfo);
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

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    synchronizeTournaments();
  }
}

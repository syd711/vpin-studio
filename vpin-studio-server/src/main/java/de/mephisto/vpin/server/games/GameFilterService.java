package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.games.FilterSettings;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.vpreg.VPReg;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class GameFilterService {
  private final static Logger LOG = LoggerFactory.getLogger(GameFilterService.class);

  @Autowired
  private PinUPConnector pinUPConnector;

  @Autowired
  private SystemService systemService;

  @Autowired
  private GameValidationService gameValidator;

  public List<Integer> filterGames(GameService gameService, FilterSettings filterSettings) {
    List<Integer> result = new ArrayList<>();
    List<Game> knownGames = gameService.getKnownGames();
    ScoringDB scoringDB = systemService.getScoringDatabase();

    List<File> vpRegFiles = new ArrayList<>();
    List<String> vpRegEntries = new ArrayList<>();
    List<String> highscoreFiles = new ArrayList<>();

    List<GameEmulator> gameEmulators = pinUPConnector.getGameEmulators();
    for (GameEmulator gameEmulator : gameEmulators) {
      File vpRegFile = gameEmulator.getVPRegFile();
      if (vpRegFile.exists() && !vpRegFiles.contains(vpRegFile)) {
        vpRegFiles.add(vpRegFile);
        VPReg reg = new VPReg(vpRegFile);
        vpRegEntries.addAll(reg.getEntries());
      }

      File[] files = gameEmulator.getUserFolder().listFiles((dir, name) -> name.endsWith(".txt"));
      if (files != null) {
        for (File file : files) {
          if (!highscoreFiles.contains(file.getName())) {
            highscoreFiles.add(file.getName());
          }
        }
      }
    }

    for (Game game : knownGames) {
      if (filterSettings.getEmulatorId() >= 0 && filterSettings.getEmulatorId() != game.getEmulatorId()) {
        continue;
      }
      if (filterSettings.isNoHighscoreSettings() && (!StringUtils.isEmpty(game.getRom()) || !StringUtils.isEmpty(game.getHsFileName()) || !StringUtils.isEmpty(game.getHsFileName()))) {
        continue;
      }
      if (filterSettings.isWithAltSound() && !game.isAltSoundAvailable()) {
        continue;
      }
      if (filterSettings.isWithAltColor() && game.getAltColorType() == null) {
        continue;
      }
      if (filterSettings.isWithBackglass() && !game.getDirectB2SFile().exists()) {
        continue;
      }
      if (filterSettings.isWithPupPack() && game.getPupPack() == null) {
        continue;
      }
      if (filterSettings.isWithPovIni() && !game.getPOVFile().exists() && !game.getIniFile().exists()) {
        continue;
      }
      if (filterSettings.isVpsUpdates() && game.getUpdates().isEmpty()) {
        continue;
      }
      if (filterSettings.isVersionUpdates() && !game.isUpdateAvailable()) {
        continue;
      }

      List<ValidationState> states = gameService.validate(game);
      if (filterSettings.isMissingAssets() && !gameValidator.hasMissingAssets(states)) {
        continue;
      }
      if (filterSettings.isOtherIssues() && !gameValidator.hasOtherIssues(states)) {
        continue;
      }
      if (filterSettings.isNoVpsMapping() && !gameValidator.hasNoVpsMapping(states)) {
        continue;
      }

      TableDetails tableDetails = pinUPConnector.getTableDetails(game.getId());
      boolean played = (tableDetails.getNumberPlays() != null && tableDetails.getNumberPlays() > 0);
      if (filterSettings.isNotPlayed() && played) {
        continue;
      }

      if (filterSettings.getGameStatus() != -1 && tableDetails.getStatus() != filterSettings.getGameStatus()) {
        continue;
      }

      if (filterSettings.isNoHighscoreSupport()) {
        String rom = game.getRom();
        String tableName = game.getTableName();
        String hsfile = game.getHsFileName();

        //the ROM was found as nvram file
        if (scoringDB.getSupportedNvRams().contains(rom) || scoringDB.getSupportedNvRams().contains(tableName)) {
          continue;
        }

        //the ROM was found in VPReg.stg
        if (vpRegEntries.contains(rom) || vpRegEntries.contains(tableName)) {
          continue;
        }

        //the highscore file was found
        if (!StringUtils.isEmpty(hsfile) && highscoreFiles.contains(hsfile)) {
          continue;
        }
      }

      result.add(game.getId());
    }
    return result;
  }
}
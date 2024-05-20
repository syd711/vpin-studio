package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.games.FilterSettings;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.popper.PinUPConnector;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameFilterService {
  private final static Logger LOG = LoggerFactory.getLogger(GameFilterService.class);

  @Autowired
  private PinUPConnector pinUPConnector;

  @Autowired
  private GameValidationService gameValidator;

  @Autowired
  private GameDetailsRepository gameDetailsRepository;


  public List<Integer> filterGames(GameService gameService, FilterSettings filterSettings) {
    List<Integer> result = new ArrayList<>();
    List<Game> knownGames = gameService.getKnownGames();
    for (Game game : knownGames) {
      if (filterSettings.getEmulatorId() >= 0 && filterSettings.getEmulatorId() != game.getEmulatorId()) {
        continue;
      }
      if (filterSettings.isNoHighscoreSettings() && (!StringUtils.isEmpty(game.getRom()) || !StringUtils.isEmpty(game.getHsFileName()) || !StringUtils.isEmpty(game.getHsFileName()))) {
        continue;
      }
      if (filterSettings.isWithNVOffset() && game.getNvOffset() == 0) {
        continue;
      }
      if (filterSettings.isWithAlias() && StringUtils.isEmpty(game.getRomAlias())) {
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
      if (filterSettings.isVpsUpdates() && game.getVpsUpdates() != null && game.getVpsUpdates().isEmpty()) {
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
      if (filterSettings.isNoVpsTableMapping() && !StringUtils.isEmpty(game.getExtTableId())) {
        continue;
      }
      if (filterSettings.isNoVpsVersionMapping() && !StringUtils.isEmpty(game.getExtTableVersionId())) {
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

      if (filterSettings.isNoHighscoreSupport() && gameValidator.validateHighscoreStatus(game, gameDetailsRepository.findByPupId(game.getId()), tableDetails).isValidScoreConfiguration()) {
        continue;
      }

      result.add(game.getId());
    }
    return result;
  }
}

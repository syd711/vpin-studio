package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.games.FilterSettings;
import de.mephisto.vpin.restclient.games.NoteType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.frontend.FrontendService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameFilterService {
  private final static Logger LOG = LoggerFactory.getLogger(GameFilterService.class);

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private GameValidationService gameValidator;

  @Autowired
  private GameDetailsRepository gameDetailsRepository;


  public List<Integer> filterGames(GameService gameService, FilterSettings filterSettings) {
    long start = System.currentTimeMillis();
    List<Integer> result = new ArrayList<>();
    List<Game> knownGames = gameService.getKnownGames(filterSettings.getEmulatorId());
    if (filterSettings.getEmulatorId() == -1 && filterSettings.isResetted(true)) {
      LOG.info("Unfiltered result " + knownGames.size() + " games took " + (System.currentTimeMillis() - start) + "ms");
      return knownGames.stream().map(g -> g.getId()).collect(Collectors.toList());
    }

    for (Game game : knownGames) {
      boolean vpxGame = game.isVpxGame();
      if (vpxGame) {
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
        if (filterSettings.isWithIni() && !game.getIniFile().exists()) {
          continue;
        }
        if (filterSettings.isWithPov() && !game.getPOVFile().exists()) {
          continue;
        }
        if (filterSettings.isWithRes() && !game.getResFile().exists()) {
          continue;
        }
        if (filterSettings.isVpsUpdates() && game.getVpsUpdates() != null && game.getVpsUpdates().isEmpty()) {
          continue;
        }
        if (filterSettings.isVersionUpdates() && !game.isUpdateAvailable()) {
          continue;
        }
      }


      NoteType noteType = filterSettings.getNoteType();
      if (noteType != null) {
        if (noteType.equals(NoteType.Any) && StringUtils.isEmpty(game.getNotes())) {
          continue;
        }
        if (noteType.equals(NoteType.Errors) && (StringUtils.isEmpty(game.getNotes()) || !game.getNotes().contains("//ERROR"))) {
          continue;
        }
        if (noteType.equals(NoteType.Outdated) && (StringUtils.isEmpty(game.getNotes()) || !game.getNotes().contains("//OUTDATED"))) {
          continue;
        }
        if (noteType.equals(NoteType.Todos) && (StringUtils.isEmpty(game.getNotes()) || !game.getNotes().contains("//TODO"))) {
          continue;
        }
      }

      List<ValidationState> states = gameService.validate(game);
      if (filterSettings.isMissingAssets() && !gameValidator.hasMissingAssets(states)) {
        continue;
      }

      if (vpxGame) {
        if (filterSettings.isOtherIssues() && !gameValidator.hasOtherIssues(states)) {
          continue;
        }
        if (filterSettings.isNoVpsTableMapping() && !StringUtils.isEmpty(game.getExtTableId())) {
          continue;
        }
        if (filterSettings.isNoVpsVersionMapping() && !StringUtils.isEmpty(game.getExtTableVersionId())) {
          continue;
        }

        TableDetails tableDetails = frontendService.getTableDetails(game.getId());
        boolean played = tableDetails == null || (tableDetails.getNumberPlays() != null && tableDetails.getNumberPlays() > 0);
        if (filterSettings.isNotPlayed() && played) {
          continue;
        }

        if (tableDetails != null && filterSettings.getGameStatus() != -1 && tableDetails.getStatus() != filterSettings.getGameStatus()) {
          continue;
        }

        if (filterSettings.isNoHighscoreSupport() && gameValidator.validateHighscoreStatus(game, gameDetailsRepository.findByPupId(game.getId()), tableDetails).isValidScoreConfiguration()) {
          continue;
        }
      }

      result.add(game.getId());
    }
    LOG.info("Filtering " + result.size() + " from " + knownGames.size() + " games took " + (System.currentTimeMillis() - start) + "ms");
    return result;
  }
}

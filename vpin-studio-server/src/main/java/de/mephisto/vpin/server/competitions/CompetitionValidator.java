package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.restclient.competitions.CompetitionValidationCode;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Filters competition on the data level:
 * If the server is gone or the game not available anymore, the competition if filtered here.
 */
@Component
public class CompetitionValidator {

  @Autowired
  private GameService gameService;

  public Competition validate(@NonNull Competition competition) {
    ValidationState validationState = new ValidationState();
    competition.setValidationState(validationState);
    Game game = gameService.getGame(competition.getGameId());
    if(game == null || game.getEmulator() == null) {
      validationState.setCode(CompetitionValidationCode.GAME_NOT_FOUND);
    }

    return competition;
  }
}

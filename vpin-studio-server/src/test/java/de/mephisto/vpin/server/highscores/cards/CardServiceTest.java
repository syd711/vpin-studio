package de.mephisto.vpin.server.highscores.cards;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.preferences.PreferencesService;

import java.io.File;
import java.util.List;

@SpringBootTest
public class CardServiceTest extends AbstractVPinServerTest {

  @Autowired
  protected CardTemplatesService cardTemplatesService;
  @Autowired
  protected CardService cardService;
  @Autowired
  protected PreferencesService preferencesService;

  @Test
  public void generateHighscoreCard() throws Exception {

    // Set screen for 
    String useDMD = VPinScreen.DMD.name();
    CardSettings cardSettings = preferencesService.getJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS);
    cardSettings.setPopperScreen(useDMD);
    preferencesService.savePreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, cardSettings);

    List<CardTemplate> templates = cardTemplatesService.getTemplates();
    CardTemplate template = templates.stream()
      .filter(t -> "Default".equalsIgnoreCase(t.getName()))
      .findFirst().orElse(null);

    List<Game> games = gameService.getGames();
    for (Game game : games) {
      if (cardService.generateCard(game, false, template)) {
        File cardFile = cardService.getCardFile(game, useDMD);
        System.out.println(cardFile);
      }
    }
  }
}

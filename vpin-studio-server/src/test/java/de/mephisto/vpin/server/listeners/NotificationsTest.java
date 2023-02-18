package de.mephisto.vpin.server.listeners;

import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class NotificationsTest extends AbstractVPinServerTest {

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private GameService gameService;

  @Test
  public void testDisabledNotifications() {
    Game game = gameService.getGameByFilename(AbstractVPinServerTest.TEST_GAME_FILENAME);

    Competition competition = new Competition();
    competition.setType(CompetitionType.OFFLINE.name());
    competition.setGameId(game.getId());
    competition.setName(String.valueOf(new Date().getTime()));
    competition.setStartDate(new Date());


    Calendar c = Calendar.getInstance();
    c.setTime(competition.getStartDate());
    c.add(Calendar.DATE, 1);
    Date newEndDate = c.getTime();
    competition.setEndDate(newEndDate);

    Competition save = competitionService.save(competition);
    assertNotNull(save);
    assertTrue(save.isActive());
    assertNotNull(save.getCreatedAt());

    boolean delete = competitionService.delete(save.getId());
    assertTrue(delete);
  }

  @Test
  public void testNotifications() throws InterruptedException {
    Game game = gameService.getGameByFilename(AbstractVPinServerTest.TEST_GAME_FILENAME);

    Competition competition = new Competition();
    competition.setGameId(game.getId());
    competition.setName(String.valueOf(new Date().getTime()));
    competition.setStartDate(new Date());

    // convert date to calendar
    Calendar c = Calendar.getInstance();
    c.setTime(new Date());

    c.add(Calendar.DATE, 1); //same with c.add(Calendar.DAY_OF_MONTH, 1);
    competition.setEndDate(c.getTime());

    competition.setDiscordServerId(1043199618172858500l);
    competition.setDiscordChannelId(1043199618172858503l);

    Competition save = competitionService.save(competition);
    assertNotNull(save);
    assertTrue(save.isActive());
    assertNotNull(save.getCreatedAt());

    Thread.sleep(3000);

    boolean delete = competitionService.delete(save.getId());
    assertTrue(delete);
  }
}

package de.mephisto.vpin.server.notifications;

import de.mephisto.vpin.server.VPinServerTest;
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
public class NotificationsTest extends VPinServerTest {

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private GameService gameService;

  @Autowired
  private NotificationService notificationService;


  @Test
  public void testDisabledNotifications() {
    Game game = gameService.getGameByFilename(VPinServerTest.TEST_GAME_FILENAME);

    Competition competition = new Competition();
    competition.setGameId(game.getId());
    competition.setName(String.valueOf(new Date().getTime()));
    competition.setStartDate(new Date());
    competition.setEndDate(new Date());

    Competition save = competitionService.save(competition);
    assertNotNull(save);
    assertTrue(save.isActive());
    assertNotNull(save.getCreatedAt());

    boolean delete = competitionService.delete(save.getId());
    assertTrue(delete);
  }

  @Test
  public void testNotifications() throws InterruptedException {
    Game game = gameService.getGameByFilename(VPinServerTest.TEST_GAME_FILENAME);

    Competition competition = new Competition();
    competition.setGameId(game.getId());
    competition.setName(String.valueOf(new Date().getTime()));
    competition.setStartDate(new Date());

    // convert date to calendar
    Calendar c = Calendar.getInstance();
    c.setTime(new Date());

    c.add(Calendar.DATE, 1); //same with c.add(Calendar.DAY_OF_MONTH, 1);
    competition.setEndDate(c.getTime());


//    competition.setDiscordNotifications(true);

    Competition save = competitionService.save(competition);
    assertNotNull(save);
    assertTrue(save.isActive());
    assertNotNull(save.getCreatedAt());

    Thread.sleep(3000);

    boolean delete = competitionService.delete(save.getId());
    assertTrue(delete);
  }
}

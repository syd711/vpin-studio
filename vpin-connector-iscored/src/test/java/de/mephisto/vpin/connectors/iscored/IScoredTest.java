package de.mephisto.vpin.connectors.iscored;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IScoredTest {

  private final static int gameIndex = 7;

  @Test
  public void testIscored() throws Exception {
    GameRoom gameRoom = IScored.getGameRoom("https://www.iScored.info/Syd", true);
    assertNotNull(gameRoom);
    assertNotNull(gameRoom.getSettings());
    List<IScoredGame> games = gameRoom.getGames();
    assertFalse(games.isEmpty());
//    assertFalse(gameRoom.getGames().get(gameIndex).getScores().isEmpty());
//    assertFalse(gameRoom.getGames().get(gameIndex).getTags().isEmpty());
    assertTrue(gameRoom.getSettings().isLongNameInputEnabled());

    gameRoom = IScored.getGameRoom("https://www.iScored.info?mode=public&user=Syd", false);
    assertNotNull(gameRoom);

    IScoredResult iScoredResult = IScored.submitScore(gameRoom, games.get(0), "Matthias", "MFA", 999999);
    assertNotNull(iScoredResult);
    assertEquals(200, iScoredResult.getReturnCode());
  }
}

package de.mephisto.vpin.connectors.iscored;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class IScoredTest {

  @Test
  public void testIscored() throws Exception {
    GameRoom gameRoom = IScored.loadGameRoom("https://www.iScored.info/Syd");
    assertNotNull(gameRoom);
    assertNotNull(gameRoom.getSettings());
    assertFalse(gameRoom.getGames().isEmpty());
//    assertFalse(gameRoom.getGames().get(0).getScores().isEmpty());
    assertFalse(gameRoom.getGames().get(0).getTags().isEmpty());

    gameRoom = IScored.loadGameRoom("https://www.iScored.info?mode=public&user=Syd");
    assertNotNull(gameRoom);
  }
}

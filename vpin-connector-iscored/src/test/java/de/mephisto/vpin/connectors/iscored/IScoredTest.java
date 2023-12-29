package de.mephisto.vpin.connectors.iscored;

import de.mephisto.vpin.connectors.iscored.models.GameRoomModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IScoredTest {

  @Test
  public void testIscored() throws Exception {
    GameRoom gameRoom = IScored.loadGameRoom("https://www.iScored.info/Syd");
    assertNotNull(gameRoom);
    assertFalse(gameRoom.getGames().isEmpty());
    assertFalse(gameRoom.getGames().get(0).getScores().isEmpty());

    gameRoom = IScored.loadGameRoom("https://www.iScored.info?mode=public&user=Syd");
    assertNotNull(gameRoom);
  }
}

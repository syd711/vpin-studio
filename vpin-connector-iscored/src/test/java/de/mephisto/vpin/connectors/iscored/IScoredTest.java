package de.mephisto.vpin.connectors.iscored;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class IScoredTest {

  @Test
  public void testIscored() throws Exception {
    GameRoom gameRoom = IScored.loadGameRoom("https://www.iScored.info/Syd");
    assertNotNull(gameRoom);

    gameRoom = IScored.loadGameRoom("https://www.iScored.info?mode=public&user=Syd");
    assertNotNull(gameRoom);
  }
}

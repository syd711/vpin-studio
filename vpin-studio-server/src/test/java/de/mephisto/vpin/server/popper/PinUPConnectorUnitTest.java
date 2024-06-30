package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.restclient.popper.Playlist;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Used for user's database testing
 */
public class PinUPConnectorUnitTest {


  @Ignore
  @Test
  public void testPupPlaylists() {
    PinUPConnector connector = new PinUPConnector();
    connector.dbFilePath = "C:\\vPinball\\PinUPSystem\\PUPDatabase.db";
    List<Playlist> playLists = connector.getPlayLists(false);

    for (Playlist playList : playLists) {
      System.out.println(playList.getName() + ": " + playList.getGames().size());
      if(playList.containsGame(2420)) {
        System.out.println(playList);
      }
    }

    assertFalse(playLists.isEmpty());
  }
}

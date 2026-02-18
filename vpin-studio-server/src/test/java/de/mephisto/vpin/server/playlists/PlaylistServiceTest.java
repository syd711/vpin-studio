package de.mephisto.vpin.server.playlists;

import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.games.Game;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PlaylistServiceTest extends AbstractVPinServerTest {

  @Autowired
  private PlaylistService playlistService;

  @BeforeAll
  public void setup() {
    setupSystem();
  }

  @Test
  public void testGetPlaylists() {
    List<Playlist> playlists = playlistService.getPlaylists();
    assertNotNull(playlists);
  }

  @Test
  public void testAddAndRemoveFromPlaylist() {
    List<Playlist> playlists = playlistService.getPlaylists();
    assertNotNull(playlists);
    if (playlists.isEmpty()) {
      return;
    }

    Playlist playlist = playlists.get(0);
    Game game = gameService.getGameByFilename(1, EM_TABLE_NAME);
    assertNotNull(game);

    try {
      Playlist updated = playlistService.addToPlaylist(playlist.getId(), game.getId(), 0);
      assertNotNull(updated);

      updated = playlistService.removeFromPlaylist(playlist.getId(), game.getId());
      assertNotNull(updated);
    }
    catch (Exception e) {
      // cleanup on failure
      try {
        playlistService.removeFromPlaylist(playlist.getId(), game.getId());
      }
      catch (Exception ignored) {
      }
      fail("Add/remove from playlist failed: " + e.getMessage());
    }
  }

  @Test
  public void testClearCache() {
    boolean result = playlistService.clearCache();
    assertTrue(result);
  }
}

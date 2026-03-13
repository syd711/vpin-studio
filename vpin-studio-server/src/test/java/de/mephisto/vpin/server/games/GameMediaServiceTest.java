package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.frontend.MediaService;
import de.mephisto.vpin.server.playlists.Playlist;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GameMediaServiceTest extends AbstractVPinServerTest {

  @BeforeAll
  public void setup(){
    setupSystem();
  }

  @Test
  public void testPlaylistMediaManipulation() throws Exception {
    List<Playlist> playlists = frontendService.getPlaylists();
    Playlist playlist = playlists.get(0);
    try {
      doTest(playlistMediaService, playlist.getId(), playlist.getName(), VPinScreen.BackGlass);
    }
    finally {
      frontendService.deletePlaylistMediaFolder(playlist, VPinScreen.BackGlass, null);
      frontendService.deletePlaylist(playlist.getId());
    }
  }

  @Test
  public void testGameMediaManipulation() throws Exception {
    Game game = frontendService.getGameByFilename(1, EM_TABLE_NAME);
    try {
      doTest(gameMediaService, game.getId(), game.getGameName(), VPinScreen.BackGlass);
    }
    finally {
      // delete the backglass folder that has been created
      frontendService.deleteMediaFolder(game, VPinScreen.BackGlass, null);
    }
  }

  private void doTest(MediaService mediaService, int objectId, String name, VPinScreen screen) throws Exception {
    // check presence of media files in DMD folder
    List<File> files = mediaService.getMediaFiles(objectId, VPinScreen.DMD);
    assertEquals(2, files.size());
    assertEquals(name + ".mp4", files.get(0).getName());
    assertEquals(name + ".png", files.get(1).getName());

    // check absence of files for wheel
    files = mediaService.getMediaFiles(objectId, screen);
    assertEquals(0, files.size());

    // add a blank file
    mediaService.addBlank(objectId, screen);
    files = mediaService.getMediaFiles(objectId, screen);
    assertEquals(1, files.size());
    assertEquals(name + ".mp4", files.get(0).getName());
    File empty = files.get(0);
    assertEquals(MediaService.EMPTY_MP4.length, empty.length());

    // copy from one folder to another
    mediaService.copyAsset(objectId, VPinScreen.DMD, name + ".mp4", screen);
    mediaService.copyAsset(objectId, VPinScreen.DMD, name + ".png", screen);
    // wait for monitor to refresh files
    Thread.sleep(400);
    files = mediaService.getMediaFiles(objectId, screen);
    assertEquals(3, files.size());
    // check unique asset name
    assertEquals(name + ".mp4", files.get(0).getName());
    assertEquals(name + ".png", files.get(1).getName());
    assertEquals(name + "01.mp4", files.get(2).getName());
    // the blank file should still be the firstone
    assertEquals(MediaService.EMPTY_MP4.length, files.get(0).length());

    // rename asset
    mediaService.renameAsset(objectId, screen, name + "01.mp4", name + "02.mp4");
    // wait for monitor to refresh files
    Thread.sleep(400);
    files = mediaService.getMediaFiles(objectId, screen);
    assertEquals(3, files.size());
    assertEquals(name + ".mp4", files.get(0).getName());
    assertEquals(name + ".png", files.get(1).getName());
    assertEquals(name + "02.mp4", files.get(2).getName());
    
    // move file as default
    mediaService.setDefaultAsset(objectId, screen, name + "02.mp4");
    // wait for monitor to refresh files
    Thread.sleep(400);
    files = mediaService.getMediaFiles(objectId, screen);
    assertEquals(3, files.size());
    assertEquals(name + ".mp4", files.get(0).getName());
    assertEquals(name + ".png", files.get(1).getName());
    assertEquals(name + "01.mp4", files.get(2).getName());

    // after default asset, the blank file should now be the second mp4
    assertEquals(MediaService.EMPTY_MP4.length, files.get(2).length());

    // delete assets
    mediaService.deleteMedia(objectId, screen, name + ".png");
    mediaService.deleteMedia(objectId, screen, name + ".mp4");
    // wait for monitor to refresh files
    Thread.sleep(400);
    files = mediaService.getMediaFiles(objectId, screen);
    assertEquals(1, files.size());
    assertEquals(name + "01.mp4", files.get(0).getName());
    assertEquals(MediaService.EMPTY_MP4.length, files.get(0).length());
  }

}

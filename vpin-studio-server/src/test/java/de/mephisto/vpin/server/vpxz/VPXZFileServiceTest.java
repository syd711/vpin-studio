package de.mephisto.vpin.server.vpxz;

import de.mephisto.vpin.restclient.dmd.DMDPackage;
import de.mephisto.vpin.restclient.frontend.FrontendMediaItem;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.server.dmd.DMDService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.music.MusicService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.puppack.PupPack;
import de.mephisto.vpin.server.puppack.PupPacksService;
import de.mephisto.vpin.server.vpx.FolderLookupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VPXZFileServiceTest {

  @Mock
  private FrontendService frontendService;

  @Mock
  private MusicService musicService;

  @Mock
  private PupPacksService pupPacksService;

  @Mock
  private DMDService dmdService;

  @Mock
  private PreferencesService preferencesService;

  @Mock
  private FolderLookupService folderLookupService;

  @InjectMocks
  private VPXZFileService vpxzFileService;

  private Game gameWithNoFiles() {
    Game game = mock(Game.class);
    File nonExistentFile = mock(File.class);
    when(nonExistentFile.exists()).thenReturn(false);
    when(game.getGameFile()).thenReturn(nonExistentFile);
    when(game.getDirectB2SFile()).thenReturn(nonExistentFile);
    when(frontendService.getMediaItems(eq(game), any(VPinScreen.class))).thenReturn(Collections.emptyList());
    return game;
  }

  // ---- calculateTotalSize ----

  @Test
  void calculateTotalSize_returnsZero_whenNothingExists() {
    Game game = gameWithNoFiles();
    when(musicService.getGameMusicFolder(game)).thenReturn(null);
    when(pupPacksService.getPupPack(game)).thenReturn(null);
    when(dmdService.getDMDPackage(game)).thenReturn(null);

    long result = vpxzFileService.calculateTotalSize(game);

    assertEquals(0, result);
  }

  @Test
  void calculateTotalSize_includesGameFileSize_whenExists() {
    Game game = mock(Game.class);
    File gameFile = mock(File.class);
    when(gameFile.exists()).thenReturn(true);
    when(gameFile.length()).thenReturn(5000L);
    when(game.getGameFile()).thenReturn(gameFile);

    File b2sFile = mock(File.class);
    when(b2sFile.exists()).thenReturn(false);
    when(game.getDirectB2SFile()).thenReturn(b2sFile);

    when(frontendService.getMediaItems(eq(game), any(VPinScreen.class))).thenReturn(Collections.emptyList());
    when(musicService.getGameMusicFolder(game)).thenReturn(null);
    when(pupPacksService.getPupPack(game)).thenReturn(null);
    when(dmdService.getDMDPackage(game)).thenReturn(null);

    long result = vpxzFileService.calculateTotalSize(game);

    assertEquals(5000L, result);
  }

  @Test
  void calculateTotalSize_includesDirectB2SSize_whenExists() {
    Game game = mock(Game.class);
    File gameFile = mock(File.class);
    when(gameFile.exists()).thenReturn(false);
    when(game.getGameFile()).thenReturn(gameFile);

    File b2sFile = mock(File.class);
    when(b2sFile.exists()).thenReturn(true);
    when(b2sFile.length()).thenReturn(2000L);
    when(game.getDirectB2SFile()).thenReturn(b2sFile);

    when(frontendService.getMediaItems(eq(game), any(VPinScreen.class))).thenReturn(Collections.emptyList());
    when(musicService.getGameMusicFolder(game)).thenReturn(null);
    when(pupPacksService.getPupPack(game)).thenReturn(null);
    when(dmdService.getDMDPackage(game)).thenReturn(null);

    long result = vpxzFileService.calculateTotalSize(game);

    assertEquals(2000L, result);
  }

  @Test
  void calculateTotalSize_includesMediaItemSizes() {
    Game game = gameWithNoFiles();
    when(musicService.getGameMusicFolder(game)).thenReturn(null);
    when(pupPacksService.getPupPack(game)).thenReturn(null);
    when(dmdService.getDMDPackage(game)).thenReturn(null);

    FrontendMediaItem item = mock(FrontendMediaItem.class);
    File mediaFile = mock(File.class);
    when(mediaFile.length()).thenReturn(300L);
    when(item.getFile()).thenReturn(mediaFile);

    // Only respond with an item for the first VPinScreen value; rest return empty
    VPinScreen firstScreen = VPinScreen.values()[0];
    when(frontendService.getMediaItems(eq(game), eq(firstScreen))).thenReturn(java.util.List.of(item));

    long result = vpxzFileService.calculateTotalSize(game);

    assertEquals(300L, result);
  }

  @Test
  void calculateTotalSize_includesDmdPackageSize_whenValid() {
    Game game = gameWithNoFiles();
    when(musicService.getGameMusicFolder(game)).thenReturn(null);
    when(pupPacksService.getPupPack(game)).thenReturn(null);

    DMDPackage dmdPackage = mock(DMDPackage.class);
    when(dmdPackage.isValid()).thenReturn(true);
    when(dmdPackage.getSize()).thenReturn(1500L);
    when(dmdService.getDMDPackage(game)).thenReturn(dmdPackage);

    long result = vpxzFileService.calculateTotalSize(game);

    assertEquals(1500L, result);
  }

  @Test
  void calculateTotalSize_skipsDmdPackage_whenNotValid() {
    Game game = gameWithNoFiles();
    when(musicService.getGameMusicFolder(game)).thenReturn(null);
    when(pupPacksService.getPupPack(game)).thenReturn(null);

    DMDPackage dmdPackage = mock(DMDPackage.class);
    when(dmdPackage.isValid()).thenReturn(false);
    when(dmdService.getDMDPackage(game)).thenReturn(dmdPackage);

    long result = vpxzFileService.calculateTotalSize(game);

    assertEquals(0L, result);
    verify(dmdPackage, never()).getSize();
  }
}

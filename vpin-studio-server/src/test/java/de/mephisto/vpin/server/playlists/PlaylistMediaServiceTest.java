package de.mephisto.vpin.server.playlists;

import de.mephisto.vpin.restclient.frontend.FrontendMedia;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.server.frontend.FrontendService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlaylistMediaServiceTest {

  @Mock
  private PlaylistService playlistService;

  @Mock
  private FrontendService frontendService;

  @InjectMocks
  private PlaylistMediaService service;

  private Playlist buildPlaylist(int id, String name) {
    Playlist playlist = new Playlist();
    playlist.setId(id);
    playlist.setName(name);
    return playlist;
  }

  @Test
  void getPlaylistMedia_returnsNull_whenPlaylistNotFound() {
    when(frontendService.getPlayList(99)).thenReturn(null);

    FrontendMedia result = service.getPlaylistMedia(99);

    assertNull(result);
  }

  @Test
  void getPlaylistMedia_returnsEmptyMedia_whenNoScreensSupported() {
    Playlist playlist = buildPlaylist(1, "testPlaylist");
    when(frontendService.getPlayList(1)).thenReturn(playlist);

    Frontend frontend = mock(Frontend.class);
    when(frontendService.getFrontend()).thenReturn(frontend);
    when(frontend.getSupportedScreens()).thenReturn(Collections.emptyList());

    FrontendMedia result = service.getPlaylistMedia(1);

    assertNotNull(result);
    assertThat(result.getMedia()).isEmpty();
  }

  @Test
  void getPlaylistMediaFiles_returnsEmptyList_whenFolderIsNull() {
    Playlist playlist = buildPlaylist(1, "myPlaylist");
    when(frontendService.getPlaylistMediaFolder(playlist, VPinScreen.Wheel, false)).thenReturn(null);

    List<File> files = service.getPlaylistMediaFiles(playlist, VPinScreen.Wheel);

    assertThat(files).isEmpty();
  }

  @Test
  void getPlaylistMediaFiles_returnsEmptyList_whenFolderDoesNotExist() {
    Playlist playlist = buildPlaylist(1, "myPlaylist");
    File nonExistentFolder = new File(System.getProperty("java.io.tmpdir"), "nonexistent-playlist-folder-" + System.nanoTime());
    when(frontendService.getPlaylistMediaFolder(playlist, VPinScreen.Wheel, false)).thenReturn(nonExistentFolder);

    List<File> files = service.getPlaylistMediaFiles(playlist, VPinScreen.Wheel);

    assertThat(files).isEmpty();
  }

  @Test
  void getMediaFiles_returnsEmpty_whenPlaylistNotFound() {
    when(playlistService.getPlaylist(42)).thenReturn(null);

    List<File> files = service.getMediaFiles(42, VPinScreen.Wheel);

    assertThat(files).isEmpty();
  }

  @Test
  void deleteMedia_returnsFalse_whenPlaylistNotFound() {
    Playlist playlist = buildPlaylist(5, "list");
    when(playlistService.getPlaylist(5)).thenReturn(playlist);
    File nonExistentFolder = new File(System.getProperty("java.io.tmpdir"), "missing-folder-" + System.nanoTime());
    when(frontendService.getPlaylistMediaFolder(playlist, VPinScreen.Wheel, false)).thenReturn(nonExistentFolder);

    boolean result = service.deleteMedia(5, VPinScreen.Wheel, "image.png");

    assertFalse(result);
  }
}

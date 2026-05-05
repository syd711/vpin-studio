package de.mephisto.vpin.server.steam;

import de.mephisto.vpin.restclient.frontend.EmulatorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SteamServiceTest {

  @InjectMocks
  private SteamService steamService;

  // ---- getGameFolder ----

  @Test
  void getGameFolder_returnsNull_whenEmulatorTypeHasNoFolderName() {
    try (MockedStatic<SteamUtil> util = mockStatic(SteamUtil.class)) {
      util.when(SteamUtil::getGameFolders).thenReturn(Collections.emptyMap());

      // VisualPinball has no Steam folder name
      File result = steamService.getGameFolder(EmulatorType.VisualPinball);

      assertNull(result);
    }
  }

  @Test
  void getGameFolder_returnsNull_whenFolderNameNotInSteamGameFolders() {
    try (MockedStatic<SteamUtil> util = mockStatic(SteamUtil.class)) {
      util.when(SteamUtil::getGameFolders).thenReturn(Collections.emptyMap());

      File result = steamService.getGameFolder(EmulatorType.ZenFX);

      assertNull(result);
    }
  }

  @Test
  void getGameFolder_returnsFolder_whenFolderNameMatchesInSteamGameFolders() {
    try (MockedStatic<SteamUtil> util = mockStatic(SteamUtil.class)) {
      File expectedFolder = new File("C:/Steam/steamapps/common/Pinball FX");
      Map<String, File> folders = new HashMap<>();
      // ZenFX folder name — look up via EmulatorType.ZenFX.folderName()
      String folderName = EmulatorType.ZenFX.folderName();
      if (folderName != null) {
        folders.put(folderName, expectedFolder);
      }
      util.when(SteamUtil::getGameFolders).thenReturn(folders);

      File result = steamService.getGameFolder(EmulatorType.ZenFX);

      // If ZenFX has a non-null folder name, it should resolve; otherwise null
      if (folderName != null) {
        assertEquals(expectedFolder, result);
      } else {
        assertNull(result);
      }
    }
  }

  // ---- getSteamFolder ----

  @Test
  void getSteamFolder_delegatesToSteamUtil() {
    File expected = new File("C:/Program Files/Steam");
    try (MockedStatic<SteamUtil> util = mockStatic(SteamUtil.class)) {
      util.when(SteamUtil::getSteamFolder).thenReturn(expected);

      File result = steamService.getSteamFolder();

      assertEquals(expected, result);
    }
  }

  @Test
  void getSteamFolder_returnsNull_whenSteamNotInstalled() {
    try (MockedStatic<SteamUtil> util = mockStatic(SteamUtil.class)) {
      util.when(SteamUtil::getSteamFolder).thenReturn(null);

      File result = steamService.getSteamFolder();

      assertNull(result);
    }
  }

  // ---- afterPropertiesSet ----

  @Test
  void afterPropertiesSet_doesNotThrow() throws Exception {
    assertDoesNotThrow(() -> steamService.afterPropertiesSet());
  }
}

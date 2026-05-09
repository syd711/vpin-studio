package de.mephisto.vpin.server.system;

import de.mephisto.vpin.restclient.backups.StudioBackupDescriptor;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.pinemhi.PINemHiService;
import de.mephisto.vpin.server.pinvol.PinVolService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import de.mephisto.vpin.server.preferences.Preferences;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.vpsdb.VpsEntryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SystemBackupServiceTest {

  @Mock
  private VpsEntryService vpsEntryService;

  @Mock
  private PlayerService playerService;

  @Mock
  private PreferencesService preferencesService;

  @Mock
  private GameService gameService;

  @Mock
  private PINemHiService pinemhiService;

  @Mock
  private PinVolService pinVolService;

  @InjectMocks
  private SystemBackupService systemBackupService;

  private String originalResources;

  @BeforeEach
  void saveResources() {
    originalResources = SystemService.RESOURCES;
  }

  @AfterEach
  void restoreResources() {
    SystemService.RESOURCES = originalResources;
  }

  @Test
  void create_returnsValidJson_withEmptyData(@TempDir Path tempDir) throws Exception {
    SystemService.RESOURCES = tempDir.toAbsolutePath() + "/";

    when(preferencesService.getPreferences()).thenReturn(new Preferences());
    when(playerService.getBuildInPlayers()).thenReturn(Collections.emptyList());
    when(gameService.getGames()).thenReturn(Collections.emptyList());
    when(vpsEntryService.getAllVpsEntries()).thenReturn(Collections.emptyList());
    when(pinVolService.getPinVolVolIniFile()).thenReturn(new File(tempDir.toFile(), "nonexistent.ini"));
    when(pinVolService.getPinVolSettingsIniFile()).thenReturn(new File(tempDir.toFile(), "settings.ini"));
    when(pinVolService.getPinVolTablesIniFile()).thenReturn(new File(tempDir.toFile(), "tables.ini"));

    String json = systemBackupService.create();

    assertNotNull(json);
    assertTrue(json.contains("\"players\""));
    assertTrue(json.contains("\"preferences\""));
    assertTrue(json.contains("\"vpsEntries\""));
  }

  @Test
  void create_includesGameEntries_whenGamesExist(@TempDir Path tempDir) throws Exception {
    SystemService.RESOURCES = tempDir.toAbsolutePath() + "/";

    Game game = mock(Game.class);
    when(game.getGameFileName()).thenReturn("funhouse.vpx");
    when(game.getComment()).thenReturn("Classic pinball");
    when(game.getExtTableId()).thenReturn("tbl-001");
    when(game.getExtTableVersionId()).thenReturn("v1.0");
    when(game.getVersion()).thenReturn("1.0");
    when(game.isCardDisabled()).thenReturn(false);

    when(preferencesService.getPreferences()).thenReturn(new Preferences());
    when(playerService.getBuildInPlayers()).thenReturn(Collections.emptyList());
    when(gameService.getGames()).thenReturn(List.of(game));
    when(vpsEntryService.getAllVpsEntries()).thenReturn(Collections.emptyList());
    when(pinVolService.getPinVolVolIniFile()).thenReturn(new File(tempDir.toFile(), "nonexistent.ini"));
    when(pinVolService.getPinVolSettingsIniFile()).thenReturn(new File(tempDir.toFile(), "settings.ini"));
    when(pinVolService.getPinVolTablesIniFile()).thenReturn(new File(tempDir.toFile(), "tables.ini"));

    String json = systemBackupService.create();

    assertTrue(json.contains("funhouse.vpx"));
    assertTrue(json.contains("tbl-001"));
  }

  @Test
  void restore_returnsFalse_whenJsonIsInvalid() {
    boolean result = systemBackupService.restore("not-valid-json", "{}");

    assertFalse(result);
  }

  @Test
  void restore_returnsTrue_whenNothingIsSelected() throws Exception {
    StudioBackupDescriptor descriptor = new StudioBackupDescriptor();
    descriptor.setPlayers(false);
    descriptor.setPreferences(false);
    descriptor.setGames(false);
    descriptor.setVpsComments(false);
    descriptor.setPinemhi(false);
    descriptor.setPinvol(false);

    JsonMapper mapper = JsonMapper.builder().build();
    String backupJson = mapper.writeValueAsString(Collections.emptyMap());
    String descriptorJson = mapper.writeValueAsString(descriptor);

    boolean result = systemBackupService.restore(backupJson, descriptorJson);

    assertTrue(result);
  }
}

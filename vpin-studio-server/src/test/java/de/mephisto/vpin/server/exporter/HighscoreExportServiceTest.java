package de.mephisto.vpin.server.exporter;

import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.GameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HighscoreExportServiceTest {

  @Mock
  private FrontendService frontendService;

  @Mock
  private EmulatorService emulatorService;

  @Mock
  private GameService gameService;

  @InjectMocks
  private HighscoreExportService service;

  @Test
  void export_returnsNonEmptyString_whenNoEmulators() throws Exception {
    when(emulatorService.getValidGameEmulators()).thenReturn(Collections.emptyList());

    String result = service.export(new HashMap<>());

    assertNotNull(result);
    assertFalse(result.isEmpty());
  }

  @Test
  void export_withSpecificEmulatorId_doesNotCallEmulatorService() throws Exception {
    when(frontendService.getGamesByEmulator(99)).thenReturn(Collections.emptyList());
    Map<String, String> query = new HashMap<>();
    query.put("emulatorId", "99");

    String result = service.export(query);

    assertNotNull(result);
    verify(emulatorService, never()).getValidGameEmulators();
  }

  @Test
  void export_withCustomDelimiter_appliesDelimiter() throws Exception {
    when(emulatorService.getValidGameEmulators()).thenReturn(Collections.emptyList());
    Map<String, String> query = new HashMap<>();
    query.put("delimiter", "|");

    String result = service.export(query);

    assertNotNull(result);
    assertTrue(result.contains("|"));
  }
}

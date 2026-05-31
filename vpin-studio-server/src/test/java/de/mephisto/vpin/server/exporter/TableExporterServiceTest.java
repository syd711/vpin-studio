package de.mephisto.vpin.server.exporter;

import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendService;
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
public class TableExporterServiceTest {

  @Mock
  private FrontendService frontendService;

  @Mock
  private EmulatorService emulatorService;

  @InjectMocks
  private TableExporterService service;

  // ---- export — empty emulator list produces headers only ----

  @Test
  void export_returnsNonEmptyString_whenNoEmulators() throws Exception {
    when(emulatorService.getValidGameEmulators()).thenReturn(Collections.emptyList());

    String result = service.export(new HashMap<>());

    assertNotNull(result);
    assertFalse(result.isEmpty());
  }

  @Test
  void export_returnsNonEmptyString_whenNoGamesForEmulator() throws Exception {
    when(frontendService.getGamesByEmulator(99)).thenReturn(Collections.emptyList());
    Map<String, String> query = new HashMap<>();
    query.put("emulatorId", "99");

    String result = service.export(query);

    assertNotNull(result);
    assertFalse(result.isEmpty());
    verify(emulatorService, never()).getValidGameEmulators();
  }

  // ---- resolveSources — custom source param ----

  @Test
  void export_withSourceStudio_doesNotThrow() throws Exception {
    when(emulatorService.getValidGameEmulators()).thenReturn(Collections.emptyList());
    Map<String, String> query = new HashMap<>();
    query.put("source", "studio");

    String result = service.export(query);

    assertNotNull(result);
  }

  @Test
  void export_withSourceFrontend_doesNotThrow() throws Exception {
    when(emulatorService.getValidGameEmulators()).thenReturn(Collections.emptyList());
    Map<String, String> query = new HashMap<>();
    query.put("source", "frontend");

    String result = service.export(query);

    assertNotNull(result);
  }

  // ---- custom delimiter ----

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

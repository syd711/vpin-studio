package de.mephisto.vpin.server.exporter;

import de.mephisto.vpin.server.directb2s.BackglassService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BackglassExportServiceTest {

  @Mock
  private FrontendService frontendService;

  @Mock
  private EmulatorService emulatorService;

  @Mock
  private BackglassService backglassService;

  @Mock
  private GameService gameService;

  @InjectMocks
  private BackglassExportService service;

  @Test
  void export_returnsNonEmptyString_whenNoGames() throws Exception {
    when(gameService.getKnownGames(-1)).thenReturn(Collections.emptyList());
    when(emulatorService.getValidGameEmulators()).thenReturn(Collections.emptyList());

    String result = service.export(new HashMap<>());

    assertNotNull(result);
    assertFalse(result.isEmpty());
  }

  @Test
  void export_doesNotCallBackglassService_whenNoGames() throws Exception {
    when(gameService.getKnownGames(-1)).thenReturn(Collections.emptyList());
    when(emulatorService.getValidGameEmulators()).thenReturn(Collections.emptyList());

    service.export(new HashMap<>());

    verify(backglassService, never()).getDirectB2SData(any());
    verify(backglassService, never()).getTableSettings(any());
  }
}

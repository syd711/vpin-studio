package de.mephisto.vpin.server.exporter;

import de.mephisto.vpin.restclient.frontend.Frontend;
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
public class MediaExportServiceTest {

  @Mock
  private FrontendService frontendService;

  @Mock
  private EmulatorService emulatorService;

  @InjectMocks
  private MediaExportService service;

  @Test
  void export_returnsNonEmptyString_whenNoEmulators() throws Exception {
    Frontend frontend = mock(Frontend.class);
    when(frontendService.getFrontend()).thenReturn(frontend);
    when(frontend.getSupportedScreens()).thenReturn(Collections.emptyList());
    when(emulatorService.getValidGameEmulators()).thenReturn(Collections.emptyList());

    String result = service.export(new HashMap<>());

    assertNotNull(result);
    assertFalse(result.isEmpty());
  }

  @Test
  void export_withSpecificEmulatorId_doesNotCallEmulatorService() throws Exception {
    Frontend frontend = mock(Frontend.class);
    when(frontendService.getFrontend()).thenReturn(frontend);
    when(frontend.getSupportedScreens()).thenReturn(Collections.emptyList());
    when(frontendService.getGamesByEmulator(99)).thenReturn(Collections.emptyList());

    Map<String, String> query = new HashMap<>();
    query.put("emulatorId", "99");

    String result = service.export(query);

    assertNotNull(result);
    verify(emulatorService, never()).getValidGameEmulators();
  }
}

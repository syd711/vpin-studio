package de.mephisto.vpin.server.fp;

import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.system.SystemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FPCommandLineServiceTest {

  @Mock
  private SystemService systemService;

  @InjectMocks
  private FPCommandLineService service;

  @Mock
  private ApplicationContext applicationContext;

  @BeforeEach
  void setUp() {
    service.setApplicationContext(applicationContext);
  }

  // ---- launch ----

  @Test
  void launch_noFpEmulatorConfigured_returnsFalse() {
    EmulatorService emulatorService = mock(EmulatorService.class);
    when(applicationContext.getBean(EmulatorService.class)).thenReturn(emulatorService);
    when(emulatorService.getValidGameEmulators()).thenReturn(Collections.emptyList());

    boolean result = service.launch();

    assertThat(result).isFalse();
  }

  @Test
  void launch_onlyNonFpEmulators_returnsFalse() {
    EmulatorService emulatorService = mock(EmulatorService.class);
    GameEmulator vpxEmulator = mock(GameEmulator.class);

    when(applicationContext.getBean(EmulatorService.class)).thenReturn(emulatorService);
    when(emulatorService.getValidGameEmulators()).thenReturn(Collections.singletonList(vpxEmulator));
    when(vpxEmulator.isFpEmulator()).thenReturn(false);

    boolean result = service.launch();

    assertThat(result).isFalse();
  }

  // ---- execute ----

  @Test
  void execute_alwaysConsultsTableDetails() {
    FrontendService frontendService = mock(FrontendService.class);
    when(applicationContext.getBean(FrontendService.class)).thenReturn(frontendService);

    Game game = mock(Game.class);
    GameEmulator emulator = mock(GameEmulator.class);
    File fpExe = new File("C:/FP/FuturePinball.exe");
    File gameFile = new File("C:/tables/SomeTable.fpt");

    when(game.getGameFile()).thenReturn(gameFile);
    when(game.getEmulator()).thenReturn(emulator);
    when(game.getId()).thenReturn(42);
    when(emulator.getExe()).thenReturn(fpExe);
    when(frontendService.getTableDetails(42)).thenReturn(null);

    service.execute(game, null);

    verify(frontendService).getTableDetails(42);
  }

  @Test
  void execute_withAltExe_usesInstallationFolderForExe() {
    FrontendService frontendService = mock(FrontendService.class);
    when(applicationContext.getBean(FrontendService.class)).thenReturn(frontendService);

    Game game = mock(Game.class);
    GameEmulator emulator = mock(GameEmulator.class);
    File fpExe = new File("C:/FP/FuturePinball.exe");
    File gameFile = new File("C:/tables/SomeTable.fpt");
    File installFolder = new File("C:/FP");

    when(game.getGameFile()).thenReturn(gameFile);
    when(game.getEmulator()).thenReturn(emulator);
    when(game.getId()).thenReturn(1);
    when(emulator.getExe()).thenReturn(fpExe);
    when(emulator.getInstallationFolder()).thenReturn(installFolder);
    when(frontendService.getTableDetails(1)).thenReturn(null);

    service.execute(game, "FuturePinballAlt.exe");

    // When altExe is provided, the installation folder is used to build the exe path
    verify(emulator).getInstallationFolder();
  }

  @Test
  void execute_tableDetailsHasAltLaunchExe_usesInstallationFolder() {
    FrontendService frontendService = mock(FrontendService.class);
    when(applicationContext.getBean(FrontendService.class)).thenReturn(frontendService);

    Game game = mock(Game.class);
    GameEmulator emulator = mock(GameEmulator.class);
    TableDetails tableDetails = mock(TableDetails.class);
    File fpExe = new File("C:/FP/FuturePinball.exe");
    File gameFile = new File("C:/tables/SomeTable.fpt");
    File installFolder = new File("C:/FP");

    when(game.getGameFile()).thenReturn(gameFile);
    when(game.getEmulator()).thenReturn(emulator);
    when(game.getId()).thenReturn(5);
    when(emulator.getExe()).thenReturn(fpExe);
    when(emulator.getInstallationFolder()).thenReturn(installFolder);
    when(frontendService.getTableDetails(5)).thenReturn(tableDetails);
    when(tableDetails.getAltLaunchExe()).thenReturn("FuturePinballAlt.exe");

    service.execute(game, null);

    verify(emulator).getInstallationFolder();
  }

  @Test
  void execute_noAltExe_noAltLaunchExe_usesDefaultEmulatorExe() {
    FrontendService frontendService = mock(FrontendService.class);
    when(applicationContext.getBean(FrontendService.class)).thenReturn(frontendService);

    Game game = mock(Game.class);
    GameEmulator emulator = mock(GameEmulator.class);
    TableDetails tableDetails = mock(TableDetails.class);
    File fpExe = new File("C:/FP/FuturePinball.exe");
    File gameFile = new File("C:/tables/SomeTable.fpt");

    when(game.getGameFile()).thenReturn(gameFile);
    when(game.getEmulator()).thenReturn(emulator);
    when(game.getId()).thenReturn(7);
    when(emulator.getExe()).thenReturn(fpExe);
    when(frontendService.getTableDetails(7)).thenReturn(tableDetails);
    when(tableDetails.getAltLaunchExe()).thenReturn(null);

    service.execute(game, null);

    // No alt exe, so installation folder is not needed
    verify(emulator, never()).getInstallationFolder();
  }

  // ---- setApplicationContext ----

  @Test
  void setApplicationContext_updatesContext() {
    ApplicationContext ctx = mock(ApplicationContext.class);
    EmulatorService emulatorService = mock(EmulatorService.class);

    service.setApplicationContext(ctx);
    when(ctx.getBean(EmulatorService.class)).thenReturn(emulatorService);
    when(emulatorService.getValidGameEmulators()).thenReturn(Collections.emptyList());

    assertThat(service.launch()).isFalse();
    verify(ctx).getBean(EmulatorService.class);
  }
}

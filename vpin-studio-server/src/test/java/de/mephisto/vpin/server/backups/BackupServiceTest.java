package de.mephisto.vpin.server.backups;

import de.mephisto.vpin.server.backups.adapters.TableBackupAdapterFactory;
import de.mephisto.vpin.server.backups.adapters.vpa.VpaService;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.UniversalUploadService;
import de.mephisto.vpin.server.highscores.cards.CardService;
import de.mephisto.vpin.server.jobs.JobService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vpauthenticators.VPAuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BackupServiceTest {

  @Mock
  private SystemService systemService;
  @Mock
  private GameService gameService;
  @Mock
  private BackupSourceRepository backupSourceRepository;
  @Mock
  private JobService jobService;
  @Mock
  private TableBackupAdapterFactory tableBackupAdapterFactory;
  @Mock
  private EmulatorService emulatorService;
  @Mock
  private FrontendService frontendService;
  @Mock
  private CardService cardService;
  @Mock
  private UniversalUploadService universalUploadService;
  @Mock
  private VpaService vpaService;
  @Mock
  private VPAuthenticationService vpAuthenticationService;
  @Mock
  private PreferencesService preferencesService;

  @InjectMocks
  private BackupService service;

  // ---- getBackupDescriptors ----

  @Test
  void getBackupDescriptors_emptyCacheReturnsEmptyList() {
    assertThat(service.getBackupDescriptors()).isEmpty();
  }

  @Test
  void getBackupDescriptors_bySourceId_notAuthenticated_returnsEmptyList() {
    when(vpAuthenticationService.isAuthenticated()).thenReturn(false);

    assertThat(service.getBackupDescriptors(1L)).isEmpty();
  }

  // ---- getBackupSources ----

  @Test
  void getBackupSources_emptyCacheReturnsEmptyList() {
    assertThat(service.getBackupSources()).isEmpty();
  }

  // ---- deleteBackupSource ----

  @Test
  void deleteBackupSource_unknownIdReturnsFalse() {
    assertThat(service.deleteBackupSource(999L)).isFalse();
  }

  // ---- getBackupSourceAdapter ----

  @Test
  void getBackupSourceAdapter_unknownIdReturnsNull() {
    assertThat(service.getBackupSourceAdapter(999L)).isNull();
  }
}

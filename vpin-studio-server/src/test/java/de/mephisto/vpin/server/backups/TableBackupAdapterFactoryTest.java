package de.mephisto.vpin.server.backups;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.preferences.BackupSettings;
import de.mephisto.vpin.server.backups.adapters.TableBackupAdapter;
import de.mephisto.vpin.server.backups.adapters.TableBackupAdapterFactory;
import de.mephisto.vpin.server.backups.adapters.vpa.VpaService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TableBackupAdapterFactoryTest {

  @Mock
  private FrontendService frontendService;
  @Mock
  private VpaService vpaService;
  @Mock
  private PreferencesService preferencesService;

  @InjectMocks
  private TableBackupAdapterFactory factory;

  @Test
  void createAdapter_returnsNonNullAdapter() {
    Game game = mock(Game.class);
    when(game.getId()).thenReturn(1);
    BackupSource backupSource = mock(BackupSource.class);
    when(frontendService.getTableDetails(1)).thenReturn(new TableDetails());
    when(preferencesService.getJsonPreference(PreferenceNames.BACKUP_SETTINGS, BackupSettings.class))
        .thenReturn(new BackupSettings());

    TableBackupAdapter adapter = factory.createAdapter(game, backupSource);

    assertThat(adapter).isNotNull();
  }

  @Test
  void createAdapter_toleratesNullTableDetails() {
    Game game = mock(Game.class);
    when(game.getId()).thenReturn(2);
    BackupSource backupSource = mock(BackupSource.class);
    when(frontendService.getTableDetails(2)).thenReturn(null);
    when(preferencesService.getJsonPreference(PreferenceNames.BACKUP_SETTINGS, BackupSettings.class))
        .thenReturn(new BackupSettings());

    TableBackupAdapter adapter = factory.createAdapter(game, backupSource);

    assertThat(adapter).isNotNull();
  }

  @Test
  void createAdapter_returnsNullOnException() {
    Game game = mock(Game.class);
    when(game.getId()).thenReturn(3);
    BackupSource backupSource = mock(BackupSource.class);
    when(frontendService.getTableDetails(3)).thenThrow(new RuntimeException("service unavailable"));

    TableBackupAdapter adapter = factory.createAdapter(game, backupSource);

    assertThat(adapter).isNull();
  }
}

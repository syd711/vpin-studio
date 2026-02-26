package de.mephisto.vpin.server.backups.adapters;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.preferences.BackupSettings;
import de.mephisto.vpin.server.backups.BackupSource;
import de.mephisto.vpin.server.backups.adapters.vpa.TableBackupAdapterVpa;
import de.mephisto.vpin.server.backups.adapters.vpa.VpaService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TableBackupAdapterFactory {

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private VpaService vpaService;

  @Autowired
  private PreferencesService preferencesService;

  public TableBackupAdapter createAdapter(@NonNull Game game, @NonNull BackupSource backupSource) {
    TableDetails tableDetails = frontendService.getTableDetails(game.getId());
    BackupSettings backupSettings = preferencesService.getJsonPreference(PreferenceNames.BACKUP_SETTINGS, BackupSettings.class);
    return new TableBackupAdapterVpa(vpaService, backupSource, game, tableDetails, backupSettings);
  }
}

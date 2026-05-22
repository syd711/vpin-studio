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
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TableBackupAdapterFactory {
  private final static Logger LOG = LoggerFactory.getLogger(TableBackupAdapterFactory.class);

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private VpaService vpaService;

  @Autowired
  private PreferencesService preferencesService;

  public TableBackupAdapter createAdapter(@NonNull Game game, @NonNull BackupSource backupSource) {
    try {
      TableDetails tableDetails = frontendService.getTableDetails(game.getId());
      BackupSettings backupSettings = preferencesService.getJsonPreference(PreferenceNames.BACKUP_SETTINGS, BackupSettings.class);
      return new TableBackupAdapterVpa(vpaService, backupSource, game, tableDetails, backupSettings);
    }
    catch (Exception e) {
      LOG.error("Failed to create backup adapter: {}", e.getMessage(), e);
      return null;
    }
  }
}

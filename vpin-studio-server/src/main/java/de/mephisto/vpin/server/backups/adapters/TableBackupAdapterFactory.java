package de.mephisto.vpin.server.backups.adapters;

import de.mephisto.vpin.restclient.backups.BackupType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.server.backups.BackupSource;
import de.mephisto.vpin.server.backups.adapters.vpa.TableBackupAdapterVpa;
import de.mephisto.vpin.server.backups.adapters.vpa.VpaService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TableBackupAdapterFactory {

  @Autowired
  private SystemService systemService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private VpaService vpaService;

  public TableBackupAdapter createAdapter(@NonNull Game game, @NonNull BackupSource backupSource) {
    BackupType backupType = systemService.getBackupType();
    TableDetails tableDetails = frontendService.getTableDetails(game.getId());

    switch (backupType) {
      case VPA: {
        return new TableBackupAdapterVpa(vpaService, backupSource, game, tableDetails);
      }
      default: {
        throw new UnsupportedOperationException("Unkown archive type " + backupType);
      }
    }
  }
}

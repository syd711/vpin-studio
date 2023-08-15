package de.mephisto.vpin.server.archiving.adapters;

import de.mephisto.vpin.restclient.ArchiveType;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.server.archiving.ArchiveSourceAdapter;
import de.mephisto.vpin.server.archiving.adapters.vpa.TableBackupAdapterVpa;
import de.mephisto.vpin.server.archiving.adapters.vpbm.TableBackupAdapterVpbm;
import de.mephisto.vpin.server.archiving.adapters.vpbm.VpbmService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TableBackupAdapterFactory {

  @Autowired
  private SystemService systemService;

  @Autowired
  private PinUPConnector pinUPConnector;

  @Autowired
  private VpbmService vpbmService;

  public TableBackupAdapter createAdapter(@NonNull ArchiveSourceAdapter archiveSourceAdapter, @NonNull Game game) {
    ArchiveType archiveType = systemService.getArchiveType();
    TableDetails tableDetails = pinUPConnector.getTableDetails(game.getId());

    switch (archiveType) {
      case VPA: {
        return new TableBackupAdapterVpa(systemService, archiveSourceAdapter, game, tableDetails);
      }
      case VPBM: {
        return new TableBackupAdapterVpbm(vpbmService, archiveSourceAdapter, game, tableDetails);
      }
      default: {
        throw new UnsupportedOperationException("Unkown archive type " +archiveType);
      }
    }
  }
}

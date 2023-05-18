package de.mephisto.vpin.server.backup.adapters;

import de.mephisto.vpin.restclient.TableDetails;
import de.mephisto.vpin.server.backup.ArchiveSourceAdapter;
import de.mephisto.vpin.server.backup.adapters.vpa.TableBackupAdapterVpa;
import de.mephisto.vpin.server.backup.adapters.vpinzip.TableBackupAdapterVpinzip;
import de.mephisto.vpin.server.backup.adapters.vpinzip.VpinzipService;
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
  private VpinzipService vpinzipService;

  public TableBackupAdapter createAdapter(@NonNull ArchiveSourceAdapter archiveSourceAdapter, @NonNull Game game) {
    ArchiveType archiveType = systemService.getArchiveType();
    TableDetails tableDetails = pinUPConnector.getTableDetails(game.getId());

    switch (archiveType) {
      case VPA: {
        return new TableBackupAdapterVpa(systemService, archiveSourceAdapter, game, tableDetails);
      }
      case VPINZIP: {
        return new TableBackupAdapterVpinzip(vpinzipService, archiveSourceAdapter, game, tableDetails);
      }
      default: {
        throw new UnsupportedOperationException("Unkown archive type " +archiveType);
      }
    }
  }
}

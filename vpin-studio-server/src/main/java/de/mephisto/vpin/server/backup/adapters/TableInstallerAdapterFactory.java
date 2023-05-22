package de.mephisto.vpin.server.backup.adapters;

import de.mephisto.vpin.server.backup.ArchiveDescriptor;
import de.mephisto.vpin.server.backup.adapters.vpa.TableInstallerAdapterVpa;
import de.mephisto.vpin.server.backup.adapters.vpbm.TableInstallerAdapterVpinzip;
import de.mephisto.vpin.server.backup.adapters.vpbm.VpbmService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TableInstallerAdapterFactory {

  @Autowired
  private SystemService systemService;

  @Autowired
  private PinUPConnector pinUPConnector;

  @Autowired
  private VpbmService vpbmService;

  @Autowired
  private GameService gameService;

  public TableInstallerAdapter createAdapter(@NonNull ArchiveDescriptor archiveDescriptor) {
    ArchiveType archiveType = systemService.getArchiveType();

    switch (archiveType) {
      case VPA: {
        return new TableInstallerAdapterVpa(systemService, gameService, pinUPConnector, archiveDescriptor);
      }
      case VPINZIP: {
        return new TableInstallerAdapterVpinzip(gameService, vpbmService, archiveDescriptor);
      }
      default: {
        throw new UnsupportedOperationException("Unkown archive type " + archiveType);
      }
    }
  }
}

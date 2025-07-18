package de.mephisto.vpin.server.archiving.adapters;

import de.mephisto.vpin.restclient.archiving.ArchiveType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.server.archiving.ArchiveSourceAdapter;
import de.mephisto.vpin.server.archiving.adapters.vpa.TableBackupAdapterVpa;
import de.mephisto.vpin.server.archiving.adapters.vpa.VpaService;
import de.mephisto.vpin.server.archiving.adapters.vpbm.TableBackupAdapterVpbm;
import de.mephisto.vpin.server.archiving.adapters.vpbm.VpbmService;
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

  @Autowired
  private VpbmService vpbmService;

  public TableBackupAdapter createAdapter(@NonNull ArchiveSourceAdapter archiveSourceAdapter, @NonNull Game game) {
    ArchiveType archiveType = systemService.getArchiveType();
    TableDetails tableDetails = frontendService.getTableDetails(game.getId());

    switch (archiveType) {
      case VPA: {
        return new TableBackupAdapterVpa(vpaService, archiveSourceAdapter, game, tableDetails);
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

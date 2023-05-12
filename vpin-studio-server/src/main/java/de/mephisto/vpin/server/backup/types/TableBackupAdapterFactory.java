package de.mephisto.vpin.server.backup.types;

import de.mephisto.vpin.restclient.TableDetails;
import de.mephisto.vpin.server.backup.ArchiveService;
import de.mephisto.vpin.server.backup.ArchiveSourceAdapterFileSystem;
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

  public TableBackupAdapter createAdapter(@NonNull Game game) {
    TableDetails tableDetails = pinUPConnector.getGameManifest(game.getId());
    return new TableBackupAdapterVpa(systemService, game, tableDetails);
  }
}

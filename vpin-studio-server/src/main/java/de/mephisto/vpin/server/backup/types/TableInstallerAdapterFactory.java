package de.mephisto.vpin.server.backup.types;

import de.mephisto.vpin.server.backup.ArchiveDescriptor;
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
  private GameService gameService;

  public TableInstallerAdapter createAdapter(@NonNull ArchiveDescriptor archiveDescriptor) {
    return new TableInstallerAdapterVpa(systemService, gameService, pinUPConnector, archiveDescriptor);
  }
}

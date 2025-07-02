package de.mephisto.vpin.server.archiving.adapters;

import de.mephisto.vpin.restclient.archiving.ArchiveType;
import de.mephisto.vpin.server.archiving.ArchiveDescriptor;
import de.mephisto.vpin.server.archiving.adapters.vpa.TableInstallerAdapterVpa;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TableInstallerAdapterFactory {

  @Autowired
  private SystemService systemService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private GameService gameService;

  public TableInstallerAdapter createAdapter(@NonNull ArchiveDescriptor archiveDescriptor, @NonNull GameEmulator emulator) {
    ArchiveType archiveType = systemService.getArchiveType();

    switch (archiveType) {
      case VPA: {
        return new TableInstallerAdapterVpa(gameService, frontendService, archiveDescriptor, emulator);
      }
      default: {
        throw new UnsupportedOperationException("Unkown archive type " + archiveType);
      }
    }
  }
}

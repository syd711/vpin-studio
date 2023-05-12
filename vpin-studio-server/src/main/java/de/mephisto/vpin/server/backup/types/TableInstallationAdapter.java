package de.mephisto.vpin.server.backup.types;

import de.mephisto.vpin.server.backup.ArchiveDescriptor;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface TableInstallationAdapter {

  @Nullable
  Game installTable(@NonNull ArchiveDescriptor archiveDescriptor);
}

package de.mephisto.vpin.server.backup.adapters;

import de.mephisto.vpin.restclient.Job;
import de.mephisto.vpin.server.backup.ArchiveDescriptor;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface TableInstallerAdapter extends Job {

  @Nullable
  Game installTable();

  @NonNull
  ArchiveDescriptor getArchiveDescriptor();
}

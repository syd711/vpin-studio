package de.mephisto.vpin.server.archiving.adapters;

import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.server.archiving.ArchiveDescriptor;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface TableInstallerAdapter extends Job {

  @Nullable
  void installTable(JobDescriptor descriptor);

  @NonNull
  ArchiveDescriptor getArchiveDescriptor();
}

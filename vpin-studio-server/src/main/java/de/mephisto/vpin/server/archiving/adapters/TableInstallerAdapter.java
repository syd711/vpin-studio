package de.mephisto.vpin.server.archiving.adapters;

import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.server.archiving.ArchiveDescriptor;
import de.mephisto.vpin.server.games.GameEmulator;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface TableInstallerAdapter extends Job {

  @Nullable
  JobExecutionResult installTable();

  @NonNull
  ArchiveDescriptor getArchiveDescriptor();
}

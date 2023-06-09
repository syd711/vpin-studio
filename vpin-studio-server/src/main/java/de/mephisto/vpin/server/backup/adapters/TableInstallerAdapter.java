package de.mephisto.vpin.server.backup.adapters;

import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.server.backup.ArchiveDescriptor;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface TableInstallerAdapter extends Job {

  @Nullable
  JobExecutionResult installTable();

  @NonNull
  ArchiveDescriptor getArchiveDescriptor();
}

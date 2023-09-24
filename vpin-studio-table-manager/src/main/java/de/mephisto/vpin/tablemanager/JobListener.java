package de.mephisto.vpin.tablemanager;

import de.mephisto.vpin.restclient.tables.descriptors.JobDescriptor;

public interface JobListener {

  void updated(JobDescriptor descriptor);

  void finished(JobDescriptor descriptor);
}

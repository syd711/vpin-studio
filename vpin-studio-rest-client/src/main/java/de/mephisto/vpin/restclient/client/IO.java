package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.descriptors.ArchiveDownloadDescriptor;
import de.mephisto.vpin.restclient.descriptors.ArchiveRestoreDescriptor;
import de.mephisto.vpin.restclient.descriptors.BackupDescriptor;

/*********************************************************************************************************************
 * IO
 ********************************************************************************************************************/
public class IO extends AbstractStudioClientModule{
  IO(VPinStudioClient client) {
    super(client);
  }

  public boolean backupTable(BackupDescriptor exportDescriptor) throws Exception {
    return getRestClient().post(API + "io/backup", exportDescriptor, Boolean.class);
  }

  public boolean installTable(ArchiveRestoreDescriptor descriptor) throws Exception {
    return getRestClient().post(API + "io/install", descriptor, Boolean.class);
  }

  public boolean downloadArchive(ArchiveDownloadDescriptor descriptor) throws Exception {
    return getRestClient().post(API + "io/download", descriptor, Boolean.class);
  }

}

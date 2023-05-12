package de.mephisto.vpin.server.io;

import de.mephisto.vpin.restclient.descriptors.ArchiveDownloadDescriptor;
import de.mephisto.vpin.restclient.descriptors.BackupDescriptor;
import de.mephisto.vpin.restclient.descriptors.ArchiveInstallDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "io")
public class IOResource {
  private final static Logger LOG = LoggerFactory.getLogger(IOResource.class);

  @Autowired
  private IOService ioService;

  @PostMapping("/backup")
  public Boolean backupTable(@RequestBody BackupDescriptor descriptor) {
    return ioService.backupTable(descriptor);
  }

  @PostMapping("/install")
  public Boolean installArchive(@RequestBody ArchiveInstallDescriptor descriptor) {
    return ioService.installArchive(descriptor);
  }

  @PostMapping("/download")
  public Boolean downloadArchive(@RequestBody ArchiveDownloadDescriptor descriptor) {
    return ioService.downloadArchive(descriptor);
  }
}

package de.mephisto.vpin.server.io;

import de.mephisto.vpin.restclient.BackupDescriptor;
import de.mephisto.vpin.restclient.VpaImportDescriptor;
import de.mephisto.vpin.restclient.TableDetails;
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

  @PostMapping("/export")
  public Boolean exportArchive(@RequestBody BackupDescriptor descriptor) {
    return ioService.exportArchive(descriptor);
  }

  @PostMapping("/import")
  public Boolean importArchive(@RequestBody VpaImportDescriptor descriptor) {
    return ioService.importVpa(descriptor);
  }
}

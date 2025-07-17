package de.mephisto.vpin.server.backup;

import de.mephisto.vpin.restclient.backup.BackupDescriptor;
import de.mephisto.vpin.server.VPinStudioServer;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 *
 */
@RestController
@RequestMapping(VPinStudioServer.API_SEGMENT + "backup")
public class BackupResource {
  private final static Logger LOG = LoggerFactory.getLogger(BackupResource.class);

  @Autowired
  private BackupService backupService;

  @PostMapping("/create")
  public String createBackup() {
    try {
      return backupService.create();
    }
    catch (Exception e) {
      LOG.error("Backup creation failed: {}", e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Backup creation failed: " + e.getMessage());
    }
  }

  @PostMapping("/restore")
  public Boolean restoreBackup(@RequestParam(value = "file") MultipartFile file,
                               @RequestParam(value = "backupDescriptor") String backupDescriptor) {
    try {
      String json = new String(file.getBytes());
      return backupService.restore(json, backupDescriptor);
    }
    catch (Exception e) {
      LOG.error("Backup restore failed: {}", e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Backup restoring failed: " + e.getMessage());
    }
  }
}

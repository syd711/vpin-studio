package de.mephisto.vpin.server.backups;

import de.mephisto.vpin.restclient.backups.BackupDescriptorRepresentation;
import de.mephisto.vpin.restclient.backups.BackupSourceRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.ArchiveRestoreDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.BackupExportDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobDescriptorFactory;
import de.mephisto.vpin.server.util.UploadUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "backups")
public class BackupResource {
  private final static Logger LOG = LoggerFactory.getLogger(BackupResource.class);

  @Autowired
  private BackupService backupService;

  @PostMapping("/backup")
  public Boolean backupTable(@RequestBody BackupExportDescriptor descriptor) {
    return backupService.backupTable(descriptor);
  }

  @PostMapping("/restore")
  public Boolean restoreBackup(@RequestBody ArchiveRestoreDescriptor descriptor) {
    return backupService.restoreBackup(descriptor);
  }

  @GetMapping()
  public List<BackupDescriptorRepresentation> getBackups() {
    List<BackupDescriptor> descriptors = backupService.getBackupSourceDescriptors();
    return toRepresentation(descriptors);
  }

  @GetMapping("/{sourceId}")
  public List<BackupDescriptorRepresentation> getBackups(@PathVariable("sourceId") long sourceId) {
    List<BackupDescriptor> descriptors = backupService.getBackupSourceDescriptors(sourceId);
    return toRepresentation(descriptors);
  }

  @NotNull
  private List<BackupDescriptorRepresentation> toRepresentation(List<BackupDescriptor> descriptors) {
    List<BackupDescriptorRepresentation> result = new ArrayList<>();
    for (BackupDescriptor backupDescriptor : descriptors) {
      BackupDescriptorRepresentation descriptorRepresentation = toRepresentation(backupDescriptor);
      result.add(descriptorRepresentation);
    }
    return result;
  }

  @GetMapping("/sources")
  public List<BackupSourceRepresentation> getSources() {
    return backupService.getBackupSources().stream().map(source -> toRepresentation(source)).collect(Collectors.toList());
  }

  @DeleteMapping("/{sourceId}/{filename}")
  public boolean deleteBackup(@PathVariable("sourceId") long sourceId,
                              @PathVariable("filename") String filename) {
    return backupService.deleteBackup(sourceId, filename);
  }

  @DeleteMapping("/source/{id}")
  public boolean deleteArchiveSource(@PathVariable("id") long id) {
    return backupService.deleteBackupSource(id);
  }

  @GetMapping("/game/{id}")
  public List<BackupDescriptorRepresentation> getBackupsForGame(@PathVariable("id") int gameId) {
    List<BackupDescriptor> backupDescriptors = backupService.getBackupDescriptorForGame(gameId);
    return toRepresentation(backupDescriptors);
  }

  @GetMapping("/invalidate")
  public boolean invalidateCache() {
    backupService.invalidateCache();
    return true;
  }

  @GetMapping("/download/file/{sourceId}/{filename}")
  public void downloadBackupFile(@PathVariable("sourceId") long sourceId,
                                 @PathVariable("filename") String fn,
                                 HttpServletResponse response) {
    InputStream in = null;
    OutputStream out = null;
    String filename = URLDecoder.decode(fn, StandardCharsets.UTF_8);

    try {
      BackupDescriptor backupDescriptor = backupService.getBackupDescriptors(sourceId, filename);
      BackupSourceAdapter sourceAdapter = backupService.getBackupSourceAdapter(sourceId);

      in = sourceAdapter.getBackupInputStream(backupDescriptor);
      out = response.getOutputStream();
      IOUtils.copy(in, out);
      response.flushBuffer();
      in.close();
      out.close();

      LOG.info("Finished download of \"" + backupDescriptor.getTableDetails().getGameDisplayName() + "\"");
      invalidateCache();
    }
    catch (IOException ex) {
      LOG.info("Error writing archive to output stream. Filename was '{}'", filename, ex);
      throw new RuntimeException("IOError writing file to output stream");
    }
  }

  @PostMapping("/upload")
  public JobDescriptor uploadArchive(@RequestParam(value = "file", required = false) MultipartFile file,
                                     @RequestParam("objectId") Integer repositoryId) {
    try {
      if (file == null) {
        LOG.error("Archive upload request did not contain a file object.");
        return null;
      }
      BackupSourceAdapter sourceAdapter = backupService.getBackupSourceAdapter(repositoryId);
      File out = new File(sourceAdapter.getBackupSource().getLocation(), file.getOriginalFilename());
      if (UploadUtil.upload(file, out)) {
        backupService.invalidateCache();
      }
    }
    catch (Exception e) {
      LOG.error("Archive upload failed: " + e.getMessage(), e);
      return JobDescriptorFactory.error("Archive upload failed: " + e.getMessage());
    }
    return JobDescriptorFactory.error(null);
  }

  @PostMapping("/save")
  public BackupSourceRepresentation save(@RequestBody BackupSourceRepresentation backupSourceRepresentation) {
    BackupSource update = backupService.save(backupSourceRepresentation);
    return toRepresentation(update);
  }


  private BackupSourceRepresentation toRepresentation(BackupSource source) {
    BackupSourceRepresentation representation = new BackupSourceRepresentation();
    representation.setType(source.getType());
    representation.setLocation(source.getLocation());
    representation.setName(source.getName());
    representation.setId(source.getId());
    representation.setLogin(source.getLogin());
    representation.setPassword(source.getPassword());
    representation.setEnabled(source.isEnabled());
    representation.setSettings(source.getSettings());
    representation.setAuthenticationType(source.getAuthenticationType());
    return representation;
  }

  private BackupDescriptorRepresentation toRepresentation(BackupDescriptor descriptor) {
    BackupSourceRepresentation source = toRepresentation(descriptor.getSource());
    BackupDescriptorRepresentation representation = new BackupDescriptorRepresentation();
    representation.setFilename(descriptor.getFilename());
    representation.setTableDetails(descriptor.getTableDetails());
    representation.setCreatedAt(descriptor.getCreatedAt());
    representation.setSize(descriptor.getSize());
    representation.setArchiveType(FilenameUtils.getExtension(descriptor.getFilename()));
    representation.setPackageInfo(descriptor.getPackageInfo());
    representation.setSource(source);
    return representation;
  }
}

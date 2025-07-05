package de.mephisto.vpin.server.archiving;

import de.mephisto.vpin.restclient.games.descriptors.*;
import de.mephisto.vpin.restclient.util.ZipUtil;
import de.mephisto.vpin.restclient.archiving.ArchiveDescriptorRepresentation;
import de.mephisto.vpin.restclient.archiving.ArchiveSourceRepresentation;
import de.mephisto.vpin.restclient.jobs.JobDescriptorFactory;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.UploadUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "archives")
public class ArchivesResource {
  private final static Logger LOG = LoggerFactory.getLogger(ArchivesResource.class);

  @Autowired
  private ArchiveService archiveService;

  @Autowired
  private SystemService systemService;


  @PostMapping("/backup")
  public Boolean backupTable(@RequestBody BackupDescriptor descriptor) {
    return archiveService.backupTable(descriptor);
  }

  @PostMapping("/install")
  public Boolean installArchive(@RequestBody ArchiveRestoreDescriptor descriptor) {
    return archiveService.installArchive(descriptor);
  }

  @GetMapping("/{sourceId}")
  public List<ArchiveDescriptorRepresentation> getArchives(@PathVariable("sourceId") long sourceId) {
    List<ArchiveDescriptor> descriptors = archiveService.getArchiveDescriptors(sourceId);
    List<ArchiveDescriptorRepresentation> result = new ArrayList<>();
    for (ArchiveDescriptor archiveDescriptor : descriptors) {
      ArchiveDescriptorRepresentation descriptorRepresentation = toRepresentation(archiveDescriptor);
      result.add(descriptorRepresentation);
    }
    return result;
  }

  @GetMapping("/sources")
  public List<ArchiveSourceRepresentation> getSources() {
    return archiveService.getArchiveSources().stream().map(source -> toRepresentation(source)).collect(Collectors.toList());
  }

  @DeleteMapping("/descriptor/{sourceId}/{filename}")
  public boolean deleteArchive(@PathVariable("sourceId") long sourceId,
                               @PathVariable("filename") String filename) {
    return archiveService.deleteArchiveDescriptor(sourceId, filename);
  }

  @DeleteMapping("/source/{id}")
  public boolean deleteArchiveSource(@PathVariable("id") long id) {
    return archiveService.deleteArchiveSource(id);
  }

  @GetMapping("/game/{id}")
  public List<ArchiveDescriptorRepresentation> getArchives(@PathVariable("id") int gameId) {
    List<ArchiveDescriptor> archiveDescriptors = archiveService.getArchiveDescriptorForGame(gameId);
    List<ArchiveDescriptorRepresentation> result = new ArrayList<>();
    for (ArchiveDescriptor archiveDescriptor : archiveDescriptors) {
      ArchiveDescriptorRepresentation descriptorRepresentation = toRepresentation(archiveDescriptor);
      result.add(descriptorRepresentation);
    }
    return result;
  }

  @GetMapping("/invalidate")
  public boolean invalidateCache() {
    archiveService.invalidateCache();
    return true;
  }

  @GetMapping("/download/file/{sourceId}/{filename}")
  public void downloadArchiveFile(@PathVariable("sourceId") long sourceId,
                                  @PathVariable("filename") String fn,
                                  HttpServletResponse response) {
    InputStream in = null;
    OutputStream out = null;
    String filename = URLDecoder.decode(fn, StandardCharsets.UTF_8);

    try {
      ArchiveDescriptor archiveDescriptor = archiveService.getArchiveDescriptor(sourceId, filename);
      ArchiveSourceAdapter sourceAdapter = archiveService.getArchiveSourceAdapter(sourceId);

      in = sourceAdapter.getArchiveInputStream(archiveDescriptor);
      out = response.getOutputStream();
      IOUtils.copy(in, out);
      response.flushBuffer();
      in.close();
      out.close();

      LOG.info("Finished download of \"" + archiveDescriptor.getTableDetails().getGameDisplayName() + "\"");
      invalidateCache();
    } catch (IOException ex) {
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
      ArchiveSourceAdapter sourceAdapter = archiveService.getArchiveSourceAdapter(repositoryId);
      File out = new File(sourceAdapter.getArchiveSource().getLocation(), file.getOriginalFilename());

      if (file.getOriginalFilename().endsWith(".zip")) {
        out = File.createTempFile(FilenameUtils.getBaseName(file.getOriginalFilename()), ".zip");
      }

      if (UploadUtil.upload(file, out)) {
        if (file.getOriginalFilename().endsWith(".zip")) {
          ZipUtil.unzip(out, new File(sourceAdapter.getArchiveSource().getLocation()));
        }

        archiveService.invalidateCache();
      }
    } catch (Exception e) {
      LOG.error("Archive upload failed: " + e.getMessage(), e);
      return JobDescriptorFactory.error("Archive upload failed: " + e.getMessage());
    }
    return JobDescriptorFactory.error(null);
  }

  @PostMapping("/save")
  public ArchiveSourceRepresentation save(@RequestBody ArchiveSourceRepresentation archiveSourceRepresentation) {
    ArchiveSource update = archiveService.save(archiveSourceRepresentation);
    return toRepresentation(update);
  }


  private ArchiveSourceRepresentation toRepresentation(ArchiveSource source) {
    ArchiveSourceRepresentation representation = new ArchiveSourceRepresentation();
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

  private ArchiveDescriptorRepresentation toRepresentation(ArchiveDescriptor descriptor) {
    ArchiveSourceRepresentation source = toRepresentation(descriptor.getSource());
    ArchiveDescriptorRepresentation representation = new ArchiveDescriptorRepresentation();
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

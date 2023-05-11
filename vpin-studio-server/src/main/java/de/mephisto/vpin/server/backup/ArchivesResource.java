package de.mephisto.vpin.server.backup;

import de.mephisto.vpin.restclient.representations.ArchiveDescriptorRepresentation;
import de.mephisto.vpin.restclient.representations.ArchiveSourceRepresentation;
import de.mephisto.vpin.server.util.UploadUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping(API_SEGMENT + "archives")
public class ArchivesResource {
  private final static Logger LOG = LoggerFactory.getLogger(ArchivesResource.class);

  @Autowired
  private ArchiveService archiveService;

  @GetMapping
  public List<ArchiveDescriptorRepresentation> getArchives() {
    List<ArchiveDescriptor> descriptors = archiveService.getArchiveDescriptors();
    List<ArchiveDescriptorRepresentation> result = new ArrayList<>();
    for (ArchiveDescriptor vpaDescriptor : descriptors) {
      ArchiveDescriptorRepresentation descriptorRepresentation = toRepresentation(vpaDescriptor);
      result.add(descriptorRepresentation);
    }
    return result;
  }


  @GetMapping("/filtered")
  public List<ArchiveDescriptorRepresentation> getFilteredArchives() {
    List<ArchiveDescriptor> descriptors = archiveService.getArchiveDescriptors();
    Map<String, ArchiveDescriptorRepresentation> result = new HashMap<>();
    for (ArchiveDescriptor vpaDescriptor : descriptors) {
      ArchiveDescriptorRepresentation descriptorRepresentation = toRepresentation(vpaDescriptor);
      result.put(vpaDescriptor.getFilename(), descriptorRepresentation);
    }
    return new ArrayList<>(result.values());
  }

  @GetMapping("/sources")
  public List<ArchiveSourceRepresentation> getSources() {
    return archiveService.getArchiveSources().stream().map(source -> toRepresentation(source)).collect(Collectors.toList());
  }

  @DeleteMapping("/descriptor/{sourceId}/{uuid}")
  public boolean deleteVpaDescriptor(@PathVariable("sourceId") long sourceId,
                                     @PathVariable("uuid") String uuid) {
    return archiveService.deleteArchiveDescriptor(sourceId, uuid);
  }

  @DeleteMapping("/source/{id}")
  public boolean deleteVpaSource(@PathVariable("id") long id) {
    return archiveService.deleteVpaSource(id);
  }

  @GetMapping("/game/{id}")
  public List<ArchiveDescriptorRepresentation> getArchives(@PathVariable("id") int gameId) {
    List<ArchiveDescriptor> vpaDescriptors = archiveService.getArchiveDescriptors(gameId);
    List<ArchiveDescriptorRepresentation> result = new ArrayList<>();
    for (ArchiveDescriptor vpaDescriptor : vpaDescriptors) {
      ArchiveDescriptorRepresentation descriptorRepresentation = toRepresentation(vpaDescriptor);
      result.add(descriptorRepresentation);
    }
    return result;
  }

  @GetMapping("/invalidate/{sourceId}")
  public boolean invalidateCache(@PathVariable("sourceId") long sourceId) {
    archiveService.invalidateCache(sourceId);
    return true;
  }

  @GetMapping("/download/file/{sourceId}/{filename}")
  public void downloadArchiveFile(@PathVariable("sourceId") long sourceId,
                                  @PathVariable("uuid") String uuid,
                                  HttpServletResponse response) {
    InputStream in = null;
    OutputStream out = null;
    try {
      ArchiveDescriptor vpaDescriptor = archiveService.getArchiveDescriptor(sourceId, uuid);
      ArchiveSourceAdapter vpaSourceAdapter = archiveService.getArchiveSourceAdapter(sourceId);

      in = vpaSourceAdapter.getDescriptorInputStream(vpaDescriptor);
      out = response.getOutputStream();
      IOUtils.copy(in, out);
      response.flushBuffer();
      in.close();
      out.close();

      LOG.info("Finished download of \"" + vpaDescriptor.getTableDetails().getGameDisplayName() + "\"");
      invalidateCache(vpaSourceAdapter.getArchiveSource().getId());
    } catch (IOException ex) {
      LOG.info("Error writing VPA to output stream. UUID was '{}'", uuid, ex);
      throw new RuntimeException("IOError writing file to output stream");
    }
  }

  @PostMapping("/upload")
  public ArchiveDescriptorRepresentation uploadArchive(@RequestParam(value = "file", required = false) MultipartFile file,
                              @RequestParam("objectId") Integer repositoryId) {
    try {
      if (file == null) {
        LOG.error("VPA upload request did not contain a file object.");
        return null;
      }
      ArchiveSourceAdapterFileSystem vpaSourceAdapter = (ArchiveSourceAdapterFileSystem) archiveService.getArchiveSourceAdapter(repositoryId);
      File out = new File(vpaSourceAdapter.getFolder(), file.getOriginalFilename());
      if (UploadUtil.upload(file, out)) {
        archiveService.invalidateCache(repositoryId);
        ArchiveDescriptor descriptor = archiveService.getArchiveDescriptor(out);
        return toRepresentation(descriptor);
      }
      return null;
    } catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "VPA upload failed: " + e.getMessage());
    }
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

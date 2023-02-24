package de.mephisto.vpin.server.vpa;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.restclient.representations.VpaDescriptorRepresentation;
import de.mephisto.vpin.restclient.representations.VpaSourceRepresentation;
import de.mephisto.vpin.server.util.UploadUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping(API_SEGMENT + "vpa")
public class VpaResource {
  private final static Logger LOG = LoggerFactory.getLogger(VpaResource.class);

  @Autowired
  private VpaService vpaService;

  @GetMapping
  public List<VpaDescriptorRepresentation> getArchives() {
    List<VpaDescriptor> vpaDescriptors = vpaService.getVpaDescriptors();
    List<VpaDescriptorRepresentation> result = new ArrayList<>();
    for (VpaDescriptor vpaDescriptor : vpaDescriptors) {
      VpaDescriptorRepresentation descriptorRepresentation = toRepresentation(vpaDescriptor);
      result.add(descriptorRepresentation);
    }
    return result;
  }

  @GetMapping("/sources")
  public List<VpaSourceRepresentation> getSources() {
    return vpaService.getVpaSources().stream().map(source -> toRepresentation(source)).collect(Collectors.toList());
  }

  @DeleteMapping("/descriptor/{sourceId}/{uuid}")
  public boolean deleteVpaDescriptor(@PathVariable("sourceId") long sourceId,
                                     @PathVariable("uuid") String uuid) {
    return vpaService.deleteVpaDescriptor(sourceId, uuid);
  }

  @DeleteMapping("/source/{id}")
  public boolean deleteVpaSource(@PathVariable("id") long id) {
    return vpaService.deleteVpaSource(id);
  }

  @GetMapping("/game/{id}")
  public List<VpaDescriptorRepresentation> getArchives(@PathVariable("id") int gameId) {
    List<VpaDescriptor> vpaDescriptors = vpaService.getVpaDescriptors(gameId);
    List<VpaDescriptorRepresentation> result = new ArrayList<>();
    for (VpaDescriptor vpaDescriptor : vpaDescriptors) {
      VpaDescriptorRepresentation descriptorRepresentation = toRepresentation(vpaDescriptor);
      result.add(descriptorRepresentation);
    }
    return result;
  }

  @GetMapping("/invalidate/{sourceId}")
  public boolean invalidateCache(@PathVariable("sourceId") long sourceId) {
    vpaService.invalidateCache(sourceId);
    return true;
  }

  @GetMapping("/download/archive/{sourceId}/{uuid}")
  public void downloadArchive(@PathVariable("sourceId") long sourceId,
                              @PathVariable("uuid") String uuid) {
    InputStream in = null;
    BufferedOutputStream out = null;
    try {
      VpaDescriptor vpaDescriptor = vpaService.getVpaDescriptor(sourceId, uuid);
      VpaSourceAdapter vpaSourceAdapter = vpaService.getVpaSourceAdapter(sourceId);
      in = vpaSourceAdapter.getDescriptorInputStream(vpaDescriptor);

      File target = new File(vpaService.getDefaultVpaSourceAdapter().getFolder(), vpaDescriptor.getFilename());
      target = FileUtils.uniqueFile(target);
      out = new BufferedOutputStream(new FileOutputStream(target));
      IOUtils.copy(in, out);

      LOG.info("Finished copying of \"" + vpaDescriptor.getManifest().getGameDisplayName() + "\" to " + target.getAbsolutePath());
      invalidateCache(vpaSourceAdapter.getVpaSource().getId());
    } catch (IOException ex) {
      LOG.info("Error writing VPA to output stream. UUID was '{}'", uuid, ex);
      throw new RuntimeException("IOError writing file to output stream");
    } finally {
      try {
        if (in != null) {
          in.close();
        }

        if(out != null) {
          out.close();
        }
      } catch (IOException e) {
        //ignore
      }
    }
  }

  @GetMapping("/download/file/{sourceId}/{uuid}")
  public void downloadArchiveFile(@PathVariable("sourceId") long sourceId,
                                  @PathVariable("uuid") String uuid,
                                  HttpServletResponse response) {
    InputStream in = null;
    try {
      VpaDescriptor vpaDescriptor = vpaService.getVpaDescriptor(sourceId, uuid);
      VpaSourceAdapter vpaSourceAdapter = vpaService.getVpaSourceAdapter(sourceId);
      in = vpaSourceAdapter.getDescriptorInputStream(vpaDescriptor);
      IOUtils.copy(in, response.getOutputStream());
      response.flushBuffer();

      LOG.info("Finished download of \"" + vpaDescriptor.getManifest().getGameDisplayName() + "\"");
      invalidateCache(vpaSourceAdapter.getVpaSource().getId());
    } catch (IOException ex) {
      LOG.info("Error writing VPA to output stream. UUID was '{}'", uuid, ex);
      throw new RuntimeException("IOError writing file to output stream");
    } finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (IOException e) {
        //ignore
      }
    }
  }

  @PostMapping("/upload")
  public String uploadVpa(@RequestParam(value = "file", required = false) MultipartFile file,
                          @RequestParam("objectId") Integer repositoryId) {
    try {
      if (file == null) {
        LOG.error("VPA upload request did not contain a file object.");
        return null;
      }

      VpaSourceAdapterFileSystem vpaSourceAdapter = (VpaSourceAdapterFileSystem) vpaService.getVpaSourceAdapter(repositoryId);
      File out = new File(vpaSourceAdapter.getFolder(), file.getOriginalFilename());
      if (UploadUtil.upload(file, out)) {
        vpaService.invalidateCache(repositoryId);
        VpaDescriptor vpaDescriptor = vpaService.getVpaDescriptor(out);
        return vpaDescriptor.getManifest().getUuid();
      }
      return null;
    } catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "VPA upload failed: " + e.getMessage());
    }
  }

  @PostMapping("/save")
  public VpaSourceRepresentation save(@RequestBody VpaSourceRepresentation vpaSourceRepresentation) {
    VpaSource update = vpaService.save(vpaSourceRepresentation);
    return toRepresentation(update);
  }


  private VpaSourceRepresentation toRepresentation(VpaSource source) {
    VpaSourceRepresentation representation = new VpaSourceRepresentation();
    representation.setType(source.getType());
    representation.setLocation(source.getLocation());
    representation.setName(source.getName());
    representation.setId(source.getId());
    representation.setLogin(source.getLogin());
    representation.setPassword(source.getPassword());
    representation.setAuthenticationType(source.getAuthenticationType());
    return representation;
  }

  private VpaDescriptorRepresentation toRepresentation(VpaDescriptor vpaDescriptor) {
    VpaSourceRepresentation source = toRepresentation(vpaDescriptor.getSource());
    VpaDescriptorRepresentation representation = new VpaDescriptorRepresentation();
    representation.setFilename(vpaDescriptor.getFilename());
    representation.setManifest(vpaDescriptor.getManifest());
    representation.setCreatedAt(vpaDescriptor.getCreatedAt());
    representation.setSize(vpaDescriptor.getSize());
    representation.setSource(source);
    return representation;
  }
}

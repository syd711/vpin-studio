package de.mephisto.vpin.server.vpa;

import de.mephisto.vpin.restclient.representations.VpaDescriptorRepresentation;
import de.mephisto.vpin.restclient.representations.VpaSourceRepresentation;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.UploadUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

  @Autowired
  private SystemService systemService;

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

  @DeleteMapping("/source/{id}")
  public Boolean getSources(@PathVariable("id") long id) {
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

  @GetMapping("/invalidate")
  public boolean invalidateCache() {
    vpaService.invalidateDefaultCache();
    return true;
  }

  @GetMapping(value = "/download/{uuid}")
  public void getFile(@PathVariable("uuid") String uuid, HttpServletResponse response) {
    FileInputStream in = null;
    try {
      VpaDescriptor vpaDescriptor = vpaService.getVpaDescriptor(uuid);
      File vpaFile = vpaService.getDefaultVpaSourceAdapter().getFile(vpaDescriptor);
      in = new FileInputStream(vpaFile);
      IOUtils.copy(in, response.getOutputStream());
      response.flushBuffer();
    } catch (IOException ex) {
      LOG.info("Error writing VPA to output stream. UUID was '{}'", uuid, ex);
      throw new RuntimeException("IOError writing file to output stream");
    } finally {
      try {
        in.close();
      } catch (IOException e) {
        //ignore
      }
    }
  }

  @DeleteMapping("/{id}")
  public boolean delete(@PathVariable("id") String uuid) {
    return vpaService.deleteVpa(uuid);
  }

  @PostMapping("/upload")
  public String uploadVpa(@RequestParam(value = "file", required = false) MultipartFile file) {
    try {
      if (file == null) {
        LOG.error("VPA upload request did not contain a file object.");
        return null;
      }
      File out = new File(systemService.getVpaArchiveFolder(), file.getOriginalFilename());
      if (UploadUtil.upload(file, out)) {
        vpaService.invalidateDefaultCache();
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
    return vpaService.save(vpaSourceRepresentation);
  }


  private VpaSourceRepresentation toRepresentation(VpaSource source) {
    VpaSourceRepresentation representation = new VpaSourceRepresentation();
    representation.setType(source.getType());
    representation.setLocation(source.getLocation());
    representation.setName(source.getName());
    representation.setId(source.getId());
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

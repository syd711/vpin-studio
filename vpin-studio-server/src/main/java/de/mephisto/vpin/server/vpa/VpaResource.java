package de.mephisto.vpin.server.vpa;

import de.mephisto.vpin.restclient.VpaManifest;
import de.mephisto.vpin.restclient.representations.VpaDescriptorRepresentation;
import de.mephisto.vpin.restclient.representations.VpaSourceRepresentation;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.UploadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
      VpaSourceRepresentation source = new VpaSourceRepresentation();
      source.setType(vpaDescriptor.getSource().getType().name());
      source.setLocation(vpaDescriptor.getSource().getLocation());

      VpaDescriptorRepresentation representation = new VpaDescriptorRepresentation();
      representation.setName(vpaDescriptor.getName());
      representation.setManifest(vpaDescriptor.getManifest());
      representation.setCreatedAt(vpaDescriptor.getCreatedAt());
      representation.setSize(vpaDescriptor.getSize());
      representation.setSource(source);

      result.add(representation);
    }
    return result;
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
        return out.getName();
      }
      return null;
    } catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "VPA upload failed: " + e.getMessage());
    }
  }


}

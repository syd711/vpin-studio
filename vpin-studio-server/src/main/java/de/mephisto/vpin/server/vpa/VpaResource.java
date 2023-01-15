package de.mephisto.vpin.server.vpa;

import de.mephisto.vpin.restclient.ExportDescriptor;
import de.mephisto.vpin.restclient.ImportDescriptor;
import de.mephisto.vpin.restclient.VpaManifest;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.UploadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "vpa")
public class VpaResource {
  private final static Logger LOG = LoggerFactory.getLogger(VpaResource.class);

  @Autowired
  private VpaService vpaService;

  @Autowired
  private SystemService systemService;

  @PostMapping("/export")
  public Boolean exportVpa(@RequestBody ExportDescriptor descriptor) {
    return vpaService.exportVpa(descriptor);
  }

  @PostMapping("/import")
  public Boolean importVpa(@RequestBody ImportDescriptor descriptor) {
    return vpaService.importVpa(descriptor);
  }

  @PostMapping("/upload")
  public String uploadVpa(@RequestParam(value = "file", required = false) MultipartFile file) {
    if (file == null) {
      LOG.error("VPA upload request did not contain a file object.");
      return null;
    }
    File out = new File(systemService.getVpaArchiveFolder(), file.getOriginalFilename());
    if (UploadUtil.upload(file, out)) {
      return out.getName();
    }
    return null;
  }

  @GetMapping("/manifest/{id}")
  public VpaManifest getManifest(@PathVariable("id") int id) {
    return vpaService.getManifest(id);
  }

}

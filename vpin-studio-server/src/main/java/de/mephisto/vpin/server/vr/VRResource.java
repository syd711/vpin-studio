package de.mephisto.vpin.server.vr;

import de.mephisto.vpin.restclient.emulators.GameEmulatorScript;
import de.mephisto.vpin.restclient.vr.VRFilesInfo;
import de.mephisto.vpin.server.util.UploadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping(API_SEGMENT + "vr")
public class VRResource {
  private final static Logger LOG = LoggerFactory.getLogger(VRResource.class);

  @Autowired
  private VRService vrService;

  @GetMapping("toggle")
  public boolean toggleVR() {
    boolean b = vrService.toggleVRMode();
    LOG.info("VR Mode enabled: {}", b);
    return b;
  }

  @GetMapping("launchscript/{emulatorId}")
  public GameEmulatorScript getEmulatorVRLaunchScript(@PathVariable("emulatorId") int emulatorId) {
    return vrService.getEmulatorVRLaunchScript(emulatorId);
  }

  @GetMapping("files/{emulatorId}")
  public VRFilesInfo getVRFiles(@PathVariable("emulatorId") int emulatorId) {
    return vrService.getVRFiles(emulatorId);
  }

  @PostMapping("/save/{emulatorId}")
  public GameEmulatorScript saveVrLaunchScript(@PathVariable("emulatorId") int emulatorId,
                                               @RequestBody GameEmulatorScript script) {
    return vrService.saveVRLaunchScript(emulatorId, script);
  }

  @PostMapping("/files/{emulatorId}")
  public boolean fileUpload(@PathVariable("emulatorId") int emulatorId,
                            @RequestParam(value = "file", required = false) MultipartFile file) {
    try {
      File vrResourcesFolder = vrService.getVRResourcesFolder(emulatorId);
      File out = new File(vrResourcesFolder, file.getOriginalFilename());
      UploadUtil.upload(file, out);
      return true;
    }
    catch (Exception e) {
      LOG.error("VR file upload failed: {}", e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "VR file upload failed: " + e.getMessage());
    }
  }
}

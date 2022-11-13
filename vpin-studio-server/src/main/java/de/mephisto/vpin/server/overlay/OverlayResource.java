package de.mephisto.vpin.server.overlay;

import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.RequestUtil;
import de.mephisto.vpin.server.util.UploadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "overlay")
public class OverlayResource {
  private final static Logger LOG = LoggerFactory.getLogger(OverlayResource.class);

  @Autowired
  private OverlayService overlayService;

  @GetMapping
  public ResponseEntity<byte[]> getOverlayImage() throws Exception {
    return RequestUtil.serializeImage(new File(SystemService.RESOURCES, "overlay.jpg"));
  }

  @GetMapping("/generate")
  public boolean generateCards() throws Exception {
    return overlayService.generateOverlay();
  }

  @GetMapping("/backgrounds")
  public List<String> getBackgrounds() {
    return overlayService.getBackgrounds();
  }

  @PostMapping(value = "/backgroundupload")
  public Boolean upload(@RequestPart(value = "file", required = false) MultipartFile file) {
    if (file == null) {
      LOG.error("Upload request did not contain a file object.");
      return false;
    }

    String name = file.getOriginalFilename().replaceAll("/", "").replaceAll("\\\\", "");
    File out = new File(overlayService.getOverlayBackgroundsFolder(), name);
    return UploadUtil.upload(file, out);
  }
}

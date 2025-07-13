package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static de.mephisto.vpin.server.VPinStudioServer.Features;

@RestController
@RequestMapping(API_SEGMENT + "games")
public class UniversalUploadResource {
  private final static Logger LOG = LoggerFactory.getLogger(UniversalUploadResource.class);

  @Autowired
  private UniversalUploadService universalUploadService;

  @PostMapping("/upload")
  public UploadDescriptor upload(@RequestParam(value = "file") MultipartFile file,
                                 @RequestParam(value = "gameId") int gameId,
                                 @RequestParam(value = "emuId") int emuId,
                                 @RequestParam(value = "mode") UploadType mode) {
    UploadDescriptor descriptor = universalUploadService.create(file, gameId);
    try {
      descriptor.setUploadType(mode);
      descriptor.setEmulatorId(emuId);

      descriptor.upload();
    }
    catch (Exception e) {
      LOG.error("Table upload failed: " + e.getMessage(), e);
      descriptor.setError("Table upload failed: " + e.getMessage());
      descriptor.finalizeUpload();
    }
    return descriptor;
  }

  @PostMapping("/process")
  public UploadDescriptor processUploaded(@RequestBody UploadDescriptor uploadDescriptor) {
    return universalUploadService.process(uploadDescriptor);
  }

}

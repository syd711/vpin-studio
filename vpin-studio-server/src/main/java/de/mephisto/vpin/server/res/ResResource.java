package de.mephisto.vpin.server.res;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.server.games.UniversalUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping(API_SEGMENT + "res")
public class ResResource {
  private final static Logger LOG = LoggerFactory.getLogger(ResResource.class);

  @Autowired
  private UniversalUploadService universalUploadService;

  @Autowired
  private ResService resService;

  @DeleteMapping("{gameId}")
  public boolean delete(@PathVariable("gameId") int gameId) {
    return resService.delete(gameId);
  }

  @PostMapping("/upload")
  public UploadDescriptor uploadRes(@RequestParam(value = "file", required = false) MultipartFile file,
                                          @RequestParam("objectId") Integer gameId) {
    UploadDescriptor descriptor = universalUploadService.create(file, gameId);
    try {
      descriptor.upload();
      universalUploadService.importFileBasedAssets(descriptor, AssetType.RES);
      return descriptor;
    }
    catch (Exception e) {
      LOG.error("Res upload failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Res upload failed: " + e.getMessage());
    }
    finally {
      descriptor.finalizeUpload();
    }
  }

}

package de.mephisto.vpin.server.fp;

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
@RequestMapping(API_SEGMENT + "fp")
public class FpResource {
  private final static Logger LOG = LoggerFactory.getLogger(FpResource.class);

  @Autowired
  private UniversalUploadService universalUploadService;

  @PostMapping("/upload/cfg/{gameId}")
  public UploadDescriptor uploadCfg(@PathVariable("gameId") int gameId, @RequestParam(value = "file", required = false) MultipartFile file) {
    UploadDescriptor descriptor = universalUploadService.create(file);
    descriptor.setGameId(gameId);
    try {
      descriptor.upload();
      universalUploadService.importArchiveBasedAssets(descriptor, null, AssetType.BAM_CFG);
      return descriptor;
    }
    catch (Exception e) {
      LOG.error(AssetType.BAM_CFG.name() + " upload failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, AssetType.BAM_CFG + " upload failed: " + e.getMessage());
    }
    finally {
      descriptor.finalizeUpload();
    }
  }
}

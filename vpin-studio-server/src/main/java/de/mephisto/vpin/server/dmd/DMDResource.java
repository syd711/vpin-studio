package de.mephisto.vpin.server.dmd;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.components.ComponentSummary;
import de.mephisto.vpin.restclient.dmd.DMDPackage;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
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
@RequestMapping(API_SEGMENT + "dmd")
public class DMDResource {
  private final static Logger LOG = LoggerFactory.getLogger(DMDResource.class);

  @Autowired
  private DMDService dmdService;

  @Autowired
  private GameService gameService;

  @Autowired
  private UniversalUploadService universalUploadService;

  @GetMapping("{id}")
  public DMDPackage get(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game != null) {
      return dmdService.getDMDPackage(game);
    }
    return null;
  }

  @GetMapping("/clearcache")
  public boolean clearCache() {
    return dmdService.clearCache();
  }

  @GetMapping("/freezy")
  public ComponentSummary getFreezySummary() {
    return dmdService.getFreezySummary();
  }

  @PostMapping("/upload")
  public UploadDescriptor upload(@RequestParam(value = "file", required = false) MultipartFile file,
                                 @RequestParam("objectId") Integer gameId) {
    UploadDescriptor descriptor = universalUploadService.create(file);
    descriptor.setGameId(gameId);
    try {
      descriptor.upload();
      universalUploadService.importArchiveBasedAssets(descriptor, null, AssetType.DMD_PACK);
      return descriptor;
    }
    catch (Exception e) {
      LOG.error(AssetType.DMD_PACK.name() + " upload failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, AssetType.DMD_PACK.name() + " upload failed: " + e.getMessage());
    } finally {
      descriptor.finalizeUpload();
    }
  }
}

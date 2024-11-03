package de.mephisto.vpin.server.patcher;

import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "patcher")
public class PatchingResource {
  private final static Logger LOG = LoggerFactory.getLogger(PatchingResource.class);

  @Autowired
  private PatchingService patchingService;

  @Autowired
  private GameService gameService;

  @PostMapping("/process")
  public UploadDescriptor processUploaded(@RequestBody UploadDescriptor uploadDescriptor) {
    Thread.currentThread().setName("Patcher Upload Thread");
    long start = System.currentTimeMillis();
    Game game = gameService.getGame(uploadDescriptor.getGameId());
    LOG.info("*********** Patching " + game.getGameDisplayName() + " ****************");
    try {

//      patchingService.patch(game, )

      if (uploadDescriptor.getUploadType().equals(UploadType.uploadAndClone)) {

      }
    }
    catch (Exception e) {
      LOG.error("Processing \"" + uploadDescriptor.getTempFilename() + "\" failed: " + e.getMessage(), e);
      uploadDescriptor.setError("Processing failed: " + e.getMessage());
    }
    finally {
      uploadDescriptor.finalizeUpload();
      LOG.info("Import finished, took " + (System.currentTimeMillis() - start) + " ms.");
    }
    LOG.info("****************************** /Patcher Finished *************************************");
    return uploadDescriptor;
  }
}

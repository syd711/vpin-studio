package de.mephisto.vpin.server.dmd;

import de.mephisto.vpin.restclient.dmd.DMDInfo;
import de.mephisto.vpin.restclient.dmd.DMDInfoZone;
import de.mephisto.vpin.restclient.dmd.DMDType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.server.directb2s.BackglassService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.system.DefaultPictureService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "dmdposition")
public class DMDPositionResource {
  private final static Logger LOG = LoggerFactory.getLogger(DMDPositionResource.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private BackglassService backglassService;

  @Autowired
  private DMDPositionService dmdPositionService;

  @Autowired
  private DefaultPictureService defaultPictureService;

  @GetMapping("/{gameId}")
  public DMDInfo getDMD(@PathVariable("gameId") int gameId) {
    return dmdPositionService.getDMDInfo(gameId);
  }

  @GetMapping("/picture/{gameId}/{onScreen}.png")
  public ResponseEntity<Resource> getPicture(@PathVariable("gameId") int gameId, @PathVariable("onScreen") String onScreen) {
    Game game = gameService.getGame(gameId);
    return download(defaultPictureService.getPicture(game, VPinScreen.valueOf(onScreen)), onScreen + ".png", false);
  }

  @PostMapping("/switch")
  public DMDInfo switchDMD(@RequestBody DMDInfo dmdInfo, @RequestParam DMDType type) {
    return dmdPositionService.switchDMDInfo(dmdInfo, type);
  }

  @PostMapping("/resetToScores")
  public DMDInfo resetToScores(@RequestBody DMDInfo dmdInfo) {
    return dmdPositionService.resetToScores(dmdInfo);
  }

  @PostMapping("/useFrontendFullDMDMedia")
  public DMDInfo useFrontendFullDMDMedia(@RequestBody DMDInfo dmdInfo) {
    backglassService.useFrontendFullDMDMedia(dmdInfo.getGameId());
    return dmdInfo;
  }

  @PostMapping("/grabFrontendFullDMDMedia")
  public DMDInfo grabFrontendFullDMDMedia(@RequestBody DMDInfo dmdInfo) {
    Game game = gameService.getGame(dmdInfo.getGameId());
    if (game != null) {
      backglassService.grabFrontendFullDMDMedia(game);
    }
    return dmdInfo;
  }

  @PostMapping("/{gameId}/move")
  public DMDInfoZone moveDMD(@PathVariable("gameId") int gameId, @RequestBody DMDInfoZone dmdInfo, @RequestParam VPinScreen target) {
    return dmdPositionService.moveDMDInfo(gameId, dmdInfo, target);
  }

  @PostMapping("/{gameId}/autoPosition")
  public DMDInfoZone autoPosition(@PathVariable("gameId") int gameId, @RequestBody DMDInfoZone dmdInfo) {
    return dmdPositionService.autoPositionDMDInfo(gameId, dmdInfo);
  }

  @PostMapping("/save")
  public boolean saveDMD(@RequestBody DMDInfo dmdInfo) {
    try {
      return dmdPositionService.saveDMDInfo(dmdInfo);
    }
    catch (Exception e) {
      LOG.error("Saving DMD position failed", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Saving DMD position failed: " + e.getMessage());
    }
  }

  /**
   * Same as DirectB2sResource.download(), consider refactoring to a common utility class.
   */
  protected ResponseEntity<Resource> download(byte[] image, String name, boolean forceDownload) {
    if (image == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    HttpHeaders headers = new HttpHeaders();
    if (forceDownload) {
      headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + name);
    }

    headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
    headers.add("Pragma", "no-cache");
    headers.add("Expires", "0");
    headers.add("X-Frame-Options", "SAMEORIGIN");

    ByteArrayResource resource = new ByteArrayResource(image);
    return ResponseEntity.ok()
        .headers(headers)
        .contentLength(resource.contentLength())
        .contentType(forceDownload ? MediaType.APPLICATION_OCTET_STREAM : MediaType.IMAGE_PNG)
        .body(resource);
  }
}

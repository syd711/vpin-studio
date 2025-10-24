package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.restclient.JsonArg;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2SDetail;
import de.mephisto.vpin.restclient.directb2s.DirectB2SFrameType;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2sScreenRes;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.MimeTypeUtil;
import de.mephisto.vpin.restclient.util.ReturnMessage;
import de.mephisto.vpin.server.VPinStudioServer;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameLifecycleService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.UniversalUploadService;

import de.mephisto.vpin.server.system.DefaultPictureService;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.xml.bind.DatatypeConverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 *
 */
@RestController
@RequestMapping(VPinStudioServer.API_SEGMENT + "directb2s")
public class DirectB2SResource {
  private final static Logger LOG = LoggerFactory.getLogger(DirectB2SResource.class);

  @Autowired
  private BackglassService backglassService;

  @Autowired
  private FrontendService frontedService;

  @Autowired
  private GameService gameService;

  @Autowired
  private UniversalUploadService universalUploadService;

  @Autowired
  private DefaultPictureService defaultPictureService;

  @Autowired
  private GameLifecycleService gameLifecycleService;

  //--------------------------------------------------

  @PostMapping("/gameId")
  public Integer getGameId(@JsonArg("emulatorId") int emulatorId, @JsonArg("fileName") String fileName) {
    String basefileName = StringUtils.removeEndIgnoreCase(fileName, ".directb2s");
    Game game = frontedService.getGameByBaseFilename(emulatorId, basefileName);
    return game != null ? game.getId() : -1;
  }

  @GetMapping("/{gameId}")
  public DirectB2SData getData(@PathVariable("gameId") int gameId) {
    Game game = gameService.getGame(gameId);
    return backglassService.getDirectB2SData(game);
  }

  @PostMapping("/get")
  public DirectB2SData getData(@JsonArg("emulatorId") int emulatorId, @JsonArg("fileName") String fileName) {
    return backglassService.getDirectB2SData(emulatorId, fileName);
  }

  @PostMapping("/detail")
  public DirectB2SDetail getDetail(@JsonArg("emulatorId") int emulatorId, @JsonArg("fileName") String fileName, @JsonArg("gameId") int gameId) {
    Game game = gameService.getGame(gameId);
    return backglassService.getBackglassDetail(emulatorId, fileName, game);
  }

  @GetMapping
  public List<DirectB2S> getBackglasses() {
    return backglassService.getBackglasses();
  }

  @GetMapping("/{gameId}/versions")
  public DirectB2S getVersionsByGame(@PathVariable("gameId") int gameId) {
    Game game = gameService.getGame(gameId);
    return backglassService.getDirectB2SAndVersions(game);
  }

  @PostMapping("/versions")
  public DirectB2S reloadVersionsByName(@JsonArg("emulatorId") int emulatorId, @JsonArg("fileName") String fileName) {
    DirectB2S b2s = backglassService.getDirectB2SAndVersions(emulatorId, fileName);
    if (b2s != null && b2s.getGameId() > 0) {
      Game game = gameService.getGame(b2s.getGameId());
      defaultPictureService.deleteAllPictures(game);
    }
    return b2s;
  }

  @GetMapping("/clearcache")
  public boolean clearCache() {
    return backglassService.clearCache();
  }

  //--------------------------------------------------
  // DOWNLOAD IMAGES

  @GetMapping("/background/{gameId}")
  public ResponseEntity<Resource> getBackgroundForGame(@PathVariable("gameId") int gameId) {
    Game game = gameService.getGame(gameId);
    if (game == null) {
      throw new RuntimeException("No Game found for id " + gameId);
    }
    return getBackground(game.getEmulatorId(), game.getDirectB2SFilename());
  }

  @GetMapping("/background/{emulatorId}/{fileName}")
  public ResponseEntity<Resource> getBackground(@PathVariable("emulatorId") int emulatorId, @PathVariable("fileName") String fileName) {
    // first decoding done by the RestService but an extra one is needed
    fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);

    String name = StringUtils.indexOf(fileName, '/') >= 0 ? StringUtils.substringAfterLast(fileName, "/") : fileName;
    name = StringUtils.substringBeforeLast(name, ".") + ".png";
    return download(backglassService.getBackgroundBase64(emulatorId, fileName), name);
  }

  @GetMapping("/dmdimage/{gameId}")
  public ResponseEntity<Resource> getDmdImageForGame(@PathVariable("gameId") int gameId) {
    Game game = gameService.getGame(gameId);
    if (game == null) {
      throw new RuntimeException("No Game found for id " + gameId);
    }
    return getDmdImage(game.getEmulatorId(), game.getDirectB2SFilename());
  }

  @GetMapping("/dmdimage/{emulatorId}/{fileName}")
  public ResponseEntity<Resource> getDmdImage(@PathVariable("emulatorId") int emulatorId, @PathVariable("fileName") String fileName) {
    // first decoding done by the RestService but an extra one is needed
    fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
    return download(backglassService.getDmdBase64(emulatorId, fileName), FilenameUtils.getBaseName(fileName) + ".dmd.png");
  }

  @GetMapping("/previewBackground/{gameId}.png")
  public ResponseEntity<Resource> getPreviewBackgroundForGame(@PathVariable("gameId") int gameId, @RequestParam(required = false) boolean includeFrame) {
    Game game = gameService.getGame(gameId);
    return download(backglassService.getPreviewBackground(game, includeFrame), gameId + ".png", false);
  }

  @GetMapping("/previewBackground/{emulatorId}/{fileName}.png")
  public ResponseEntity<Resource> getPreviewBackground(@PathVariable("emulatorId") int emulatorId, @PathVariable("fileName") String fileName, @RequestParam(required = false) boolean includeFrame) {
    // first decoding done by the RestService but an extra one is needed as filename is encoded by caller too
    fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
    Game game = gameService.getGameByDirectB2S(emulatorId, fileName);
    return download(backglassService.getPreviewBackground(emulatorId, fileName, game, includeFrame), FilenameUtils.getBaseName(fileName) + ".png", false);
  }

  //-------
  // download utilities

  private ResponseEntity<Resource> download(String base64, String filename) {
    byte[] image = base64 != null ? DatatypeConverter.parseBase64Binary(base64) : null;
    //TODO check impact if we turn to false
    return download(image, filename, true);
  }

  private ResponseEntity<Resource> download(byte[] image, String name, boolean forceDownload) {
    if (image == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    ByteArrayResource resource = new ByteArrayResource(image);
    return download(resource, name, forceDownload);
  }

  private ResponseEntity<Resource> download(Resource resource, String name, boolean forceDownload) {
    if (resource == null) {
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

    ResponseEntity.BodyBuilder res = ResponseEntity.ok().headers(headers);

    try {
      res.contentLength(resource.contentLength());
    }
    catch (IOException ioe) {
      LOG.warn("Cannot determine content Length for " + name);
    }

    // add content Type
    res = res.contentType(forceDownload ? MediaType.APPLICATION_OCTET_STREAM : MediaType.IMAGE_PNG);

    return res.body(resource);
  }

  private ResponseEntity<StreamingResponseBody> download(File file) {
    if (file == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());
    headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
    headers.add("Pragma", "no-cache");
    headers.add("Expires", "0");
    headers.add("X-Frame-Options", "SAMEORIGIN");

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(MimeTypeUtil.determineMimeType(file)))
        .headers(headers)
        .body(out -> {
          try (FileInputStream fis = new FileInputStream(file)) {
            StreamUtils.copy(fis, out);
          }
        });
  }

  //--------------------------------------------------
  // OPERATIONS

  @PostMapping("/delete")
  public boolean deleteBackglass(@JsonArg("emulatorId") int emulatorId, @JsonArg("fileName") String fileName) {
    boolean b = backglassService.deleteBackglass(emulatorId, fileName);
    if (b) {
      Game game = gameService.getGameByDirectB2S(emulatorId, fileName);
      if (game != null) {
        gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.DIRECTB2S, fileName);
      }
    }
    return b;
  }

  @PutMapping
  public DirectB2S updateBackglass(@RequestBody Map<String, Object> values) throws IOException {
    int emulatorId = (Integer) values.get("emulatorId");
    String fileName = (String) values.get("fileName");
    String newName = (String) values.get("newName");
    DirectB2S directb2s = null;
    Game game = null;
    try {
      if (values.containsKey("newName") && !StringUtils.isEmpty(newName)) {
        directb2s = backglassService.rename(emulatorId, fileName, newName);
      }
      else if (values.containsKey("setVersionAsDefault")) {
        directb2s = backglassService.setAsDefault(emulatorId, fileName);
      }
      else if (values.containsKey("disable")) {
        directb2s = backglassService.disable(emulatorId, fileName);
      }
      else if (values.containsKey("deleteVersion")) {
        directb2s = backglassService.deleteVersion(emulatorId, fileName);
        // last version deleted, so cannot get game from there
        if (directb2s == null) {
          game = gameService.getGameByDirectB2S(emulatorId, fileName);
        }
      }
      return directb2s;
    }
    finally {
      // generate a game event
      if (directb2s != null) {
        gameLifecycleService.notifyGameAssetsChanged(directb2s.getGameId(), AssetType.DIRECTB2S, fileName);
      }
      else if (game != null) {
        gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.DIRECTB2S, fileName);
      }
    }
  }

  //--------------------------------------------------
  // SETTINGS

  @GetMapping("/tablesettings/{gameId}")
  public DirectB2STableSettings getTableSettings(@PathVariable("gameId") int gameId) {
    Game game = gameService.getGame(gameId);
    return backglassService.getTableSettings(game);
  }

  @PostMapping("/tablesettings/{gameId}")
  public DirectB2STableSettings saveTableSettings(@PathVariable("gameId") int gameId, @RequestBody DirectB2STableSettings settings) {
    try {
      Game game = gameService.getGame(gameId);
      return backglassService.saveTableSettings(game, settings);
    }
    catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Save error: " + e.getMessage());
    }
    finally {
      gameLifecycleService.notifyGameAssetsChanged(gameId, AssetType.DIRECTB2S, null);
    }
  }

  @GetMapping("/serversettings")
  public DirectB2ServerSettings getServerSettings() {
    return backglassService.getServerSettings();
  }

  @PostMapping("/serversettings")
  public DirectB2ServerSettings saveServerSettings(@RequestBody DirectB2ServerSettings settings) {
    try {
      return backglassService.saveServerSettings(settings);
    }
    catch (Exception e) {
      LOG.error("Saving b2s server settings failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Saving b2s server settings failed: " + e.getMessage());
    }
  }

  //--------------------------------------------------
  // UPLOADS

  @PostMapping("/upload")
  public UploadDescriptor uploadDirectB2s(@RequestParam(value = "file", required = false) MultipartFile file,
                                          @RequestParam("uploadType") String uploadType,
                                          @RequestParam("objectId") Integer gameId) {
    UploadDescriptor descriptor = universalUploadService.create(file, gameId);
    try {
      descriptor.upload();
      descriptor.setUploadType(UploadType.valueOf(uploadType.replaceAll("\"", ""))); //????
      universalUploadService.importFileBasedAssets(descriptor, AssetType.DIRECTB2S);
      gameService.resetUpdate(gameId, VpsDiffTypes.b2s);
      backglassService.clearCache();
      return descriptor;
    }
    catch (Exception e) {
      LOG.error("Directb2s upload failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "DirectB2S upload failed: " + e.getMessage());
    }
    finally {
      descriptor.finalizeUpload();
    }
  }

  @PostMapping("/uploadDmdImage")
  public Boolean uploadDmdImage(@RequestParam(value = "file", required = false) MultipartFile file,
                                @RequestParam("emulatorId") int emulatorId,
                                @RequestParam("fileName") String fileName) {
    if (file == null) {
      LOG.error("Upload request did not contain a file object.");
      return false;
    }

    try {
      String base64 = DatatypeConverter.printBase64Binary(file.getBytes());
      return updateDmdImage(emulatorId, fileName, file.getOriginalFilename(), base64);
    }
    catch (IOException ioe) {
      LOG.error("Error while converting image into base64 representation", ioe);
      return false;
    }
  }

  @PostMapping("/removeDmdImage")
  public Boolean removeDmdImage(@JsonArg("emulatorId") int emulatorId, @JsonArg("fileName") String fileName) {
    return updateDmdImage(emulatorId, fileName, null, null);
  }

  private boolean updateDmdImage(int emulatorId, String fileName, String originalFileName, String base64) {
    boolean success = backglassService.setDmdImage(emulatorId, fileName, originalFileName, base64);
    if (success && FileUtils.isMainFilename(fileName)) {
      Game game = gameService.getGameByDirectB2S(emulatorId, fileName);
      if (game != null) {
        gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.DIRECTB2S, null);
      }
    }
    return success;
  }

  //--------------------------------------------------
  // SCREENRES & FRAME

  @GetMapping("/screenRes")
  public DirectB2sScreenRes getScreenRes() {
    return backglassService.getGlobalScreenRes();
  }

  @PostMapping("/screenRes")
  public DirectB2sScreenRes getScreenRes(@JsonArg("emulatorId") int emulatorId, @JsonArg("fileName") String fileName, @RequestParam(required = false) boolean tableOnly) {
    Game game = gameService.getGameByDirectB2S(emulatorId, fileName);
    return backglassService.getScreenRes(emulatorId, fileName, game, tableOnly);
  }

  @GetMapping("/frame/{emuId}/{filename}")
  public ResponseEntity<StreamingResponseBody> getFrame(@PathVariable("emuId") int emuId, @PathVariable("filename") String filename) {
    // first decoding done by the RestService but an extra one is needed
    filename = URLDecoder.decode(filename, StandardCharsets.UTF_8);
    File screenRes = new File(filename);
    return download(screenRes);
  }

  @GetMapping("/frame/{emuId}/{filename}/{frameType}")
  public ResponseEntity<Resource> generateFrame(@PathVariable("emuId") int emulatorId, @PathVariable("filename") String filename, @PathVariable("frameType") DirectB2SFrameType frameType) {
    // first decoding done by the RestService but an extra one is needed
    filename = URLDecoder.decode(filename, StandardCharsets.UTF_8);

    if (DirectB2SFrameType.USE_FRAME.equals(frameType)) {
      DirectB2sScreenRes screenres = getScreenRes(emulatorId, filename, false);
      String filePath = screenres.getBackgroundFilePath();
      return download(new FileSystemResource(filePath), "frame.png", false);
    }

    Game game = gameService.getGameByDirectB2S(emulatorId, filename);
    byte[] frame = backglassService.generateFrame(emulatorId, filename, game, frameType);
    return download(frame, frameType + ".png", false);
  }

  @PostMapping("/screenRes/save")
  public ReturnMessage saveScreenRes(@RequestBody DirectB2sScreenRes screenres) throws Exception {
    try {
      Game game = gameService.getGameByDirectB2S(screenres.getEmulatorId(), screenres.getB2SFileName());
      backglassService.saveScreenRes(screenres, game);
      if (game != null) {
        gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.DIRECTB2S, null);
      }
      return null;
    }
    catch (IOException ioe) {
      LOG.error("Error in save", ioe);
      return ReturnMessage.error(ioe);
    }
  }

  @PostMapping("/screenRes/uploadFrame")
  public String uploadScreenResFrame(@RequestParam(value = "file", required = false) MultipartFile file,
                                     @RequestParam("emulatorId") int emulatorId, @RequestParam("fileName") String b2sFilename) {
    if (file == null) {
      LOG.error("Upload request did not contain a file object.");
      return null;
    }
    try {
      String frame = backglassService.setScreenResFrame(emulatorId, b2sFilename, file.getOriginalFilename(), file.getInputStream());

      if (frame != null)  {
        Game game = gameService.getGameByDirectB2S(emulatorId, b2sFilename);
        if (game != null) {
          gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.DIRECTB2S, null);
        }
      }

      return frame;
    }
    catch (IOException ioe) {
      LOG.error("Error while converting image into base64 representation", ioe);
      return null;
    }
  }

  @DeleteMapping("/screenRes/removeFrame")
  public boolean removeScreenResFrame(@RequestParam("emuid") int emuId, @RequestParam("filename") String b2sFilename) throws Exception {
    try {
      String filedeleted = backglassService.setScreenResFrame(emuId, b2sFilename, null, null);

      if (filedeleted != null) {
        Game game = gameService.getGameByDirectB2S(emuId, b2sFilename);
        if (game != null) {
          gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.DIRECTB2S, null);
        }
      }

      return filedeleted != null;
    }
    catch (IOException ioe) {
      LOG.error("Error while converting image into base64 representation", ioe);
      return false;
    }
  }
}

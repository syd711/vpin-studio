package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.restclient.JsonArg;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.directb2s.DirectB2SAndVersions;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2sScreenRes;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptorFactory;
import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import de.mephisto.vpin.restclient.util.MimeTypeUtil;
import de.mephisto.vpin.restclient.util.ReturnMessage;
import de.mephisto.vpin.server.VPinStudioServer;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.UniversalUploadService;

import de.mephisto.vpin.server.system.DefaultPictureService;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
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
import java.util.function.Function;

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

  @GetMapping
  public List<DirectB2SAndVersions> getBackglasses() {
    return backglassService.getBackglasses();
  }

  @GetMapping("/{gameId}/versions")
  public DirectB2SAndVersions getVersions(@PathVariable("gameId") int gameId) {
    return backglassService.getDirectB2SAndVersions(gameId);
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
    return download(backglassService.getDmdBase64(emulatorId, fileName), FilenameUtils.getBaseName(fileName)+".dmd.png");
  }

  @GetMapping("/previewBackground/{gameId}.png")
  public ResponseEntity<Resource> getPreviewBackgroundForGame(@PathVariable("gameId") int gameId, @RequestParam(required = false) boolean includeFrame) {
    return download(backglassService.getPreviewBackground(gameId, includeFrame), gameId + ".png", false);
  }
  @GetMapping("/previewBackground/{emulatorId}/{fileName}.png")
  public ResponseEntity<Resource> getPreviewBackground(@PathVariable("emulatorId") int emulatorId, @PathVariable("fileName") String fileName, @RequestParam(required = false) boolean includeFrame) {
    // first decoding done by the RestService but an extra one is needed as filename is encoded by caller too
      fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
    return download(backglassService.getPreviewBackground(emulatorId, fileName, includeFrame), FilenameUtils.getBaseName(fileName)+".png", false);
  }

  @GetMapping("/croppedBackground/{gameId}")
  public ResponseEntity<StreamingResponseBody> getCroppedBackground(@PathVariable("gameId") int gameId) {
    return download(gameId, game -> defaultPictureService.getCroppedDefaultPicture(game));
  }
  @GetMapping("/croppedDmd/{gameId}")
  public ResponseEntity<StreamingResponseBody> getCroppedDmd(@PathVariable("gameId") int gameId) {
    return download(gameId, game -> defaultPictureService.getDMDPicture(game));
  }

  //-------
  // download utilities

  protected ResponseEntity<Resource> download(String base64, String filename) {
    byte[] image = base64 != null ? DatatypeConverter.parseBase64Binary(base64) : null;
    //TODO check impact if we turn to false
    return download(image, filename, true);
  }

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

  private ResponseEntity<StreamingResponseBody> download(int gameId, Function<Game, File> provider) {
    Game game = gameService.getGame(gameId);
    if (game == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    return download(provider.apply(game));
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
    return backglassService.deleteBackglass(emulatorId, fileName);
  }

  @PutMapping
  public DirectB2SAndVersions updateBackglass(@RequestBody Map<String, Object> values) throws IOException {
    int emulatorId = (Integer) values.get("emulatorId");
    String fileName = (String) values.get("fileName");
    String newName = (String) values.get("newName");
    if (values.containsKey("newName") && !StringUtils.isEmpty(newName)) {
      return backglassService.rename(emulatorId, fileName, newName);
    }
    else if (values.containsKey("duplicate")) {
      return backglassService.duplicate(emulatorId, fileName);
    }
    else if (values.containsKey("setAsDefault")) {
      return backglassService.setAsDefault(emulatorId, fileName);
    }
    else if (values.containsKey("disable")) {
      return backglassService.disable(emulatorId, fileName);
    }
    return null;
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
      return backglassService.saveTableSettings(gameId, settings);
    }
    catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Table not supported: " + e.getMessage());
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
                                          @RequestParam("uploadType") UploadType uploadType,
                                          @RequestParam("objectId") Integer gameId) {
    UploadDescriptor descriptor = UploadDescriptorFactory.create(file, gameId);
    try {
      descriptor.upload();
      descriptor.setUploadType(uploadType);
      universalUploadService.importFileBasedAssets(descriptor, AssetType.DIRECTB2S);
      gameService.resetUpdate(gameId, VpsDiffTypes.b2s);
      backglassService.clearCache();

      Game game = gameService.getGame(gameId);
      if (game != null) {
        defaultPictureService.extractDefaultPicture(game);
      }
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
      return backglassService.setDmdImage(emulatorId, fileName, file.getOriginalFilename(), base64);
    }
    catch (IOException ioe) {
      LOG.error("Error while converting image into base64 representation", ioe);
      return false;
    }
  }

  @PostMapping("/removeDmdImage")
  public Boolean removeDmdImage(@JsonArg("emulatorId") int emulatorId, @JsonArg("fileName") String fileName) {
    return backglassService.setDmdImage(emulatorId, fileName, null, null);
  }

  //--------------------------------------------------
  // SCREENRES & FRAME

  @PostMapping("/screenRes")
  public DirectB2sScreenRes getScreenRes(@JsonArg("emulatorId") int emulatorId, @JsonArg("fileName") String fileName, @RequestParam(required=false) boolean tableOnly) {
    return backglassService.getScreenRes(emulatorId, fileName, tableOnly);
  }

  @GetMapping("/frame/{emuId}/{filename}")
  public ResponseEntity<StreamingResponseBody> getFrame(@PathVariable("emuId") int emuId, @PathVariable("filename") String filename) {
    // first decoding done by the RestService but an extra one is needed
    filename = URLDecoder.decode(filename, StandardCharsets.UTF_8);
    File screenRes = new File(filename);
    return download(screenRes);
  }

  @PostMapping("/screenRes/save")
  public ReturnMessage saveScreenRes(@RequestBody DirectB2sScreenRes screenres) throws Exception {
    try {
      backglassService.saveScreenRes(screenres);
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
      return backglassService.setScreenResFrame(emulatorId, b2sFilename, file.getOriginalFilename(), file.getInputStream());
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
      return filedeleted != null;
    }
    catch (IOException ioe) {
      LOG.error("Error while converting image into base64 representation", ioe);
      return false;
    }
  }
}

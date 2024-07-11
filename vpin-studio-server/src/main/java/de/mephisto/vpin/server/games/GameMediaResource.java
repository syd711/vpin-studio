package de.mephisto.vpin.server.games;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptorFactory;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.TableAssetSearch;
import de.mephisto.vpin.server.assets.TableAssetsService;
import de.mephisto.vpin.server.frontend.FrontendStatusEventsResource;
import de.mephisto.vpin.restclient.frontend.GameMedia;
import de.mephisto.vpin.restclient.frontend.GameMediaItem;
import de.mephisto.vpin.server.util.UploadUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static de.mephisto.vpin.server.util.RequestUtil.CONTENT_LENGTH;
import static de.mephisto.vpin.server.util.RequestUtil.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping(API_SEGMENT + "media")
public class GameMediaResource {
  public static final byte[] EMPTY_MP4 = Base64.getDecoder().decode("AAAAGGZ0eXBpc29tAAAAAGlzb21tcDQxAAAACGZyZWUAAAAmbWRhdCELUCh9wBQ+4cAhC1AAfcAAPuHAIQtQAH3AAD7hwAAAAlNtb292AAAAbG12aGQAAAAAxzFHd8cxR3cAAV+QAAAYfQABAAABAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADAAAAG2lvZHMAAAAAEA0AT////xX/DgQAAAACAAABxHRyYWsAAABcdGtoZAAAAAfHMUd3xzFHdwAAAAIAAAAAAAAYfQAAAAAAAAAAAAAAAAEAAAAAAQAAAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAWBtZGlhAAAAIG1kaGQAAAAAxzFHd8cxR3cAAKxEAAAL/xXHAAAAAAA0aGRscgAAAAAAAAAAc291bgAAAAAAAAAAAAAAAFNvdW5kIE1lZGlhIEhhbmRsZXIAAAABBG1pbmYAAAAQc21oZAAAAAAAAAAAAAAAJGRpbmYAAAAcZHJlZgAAAAAAAAABAAAADHVybCAAAAABAAAAyHN0YmwAAABkc3RzZAAAAAAAAAABAAAAVG1wNGEAAAAAAAAAAQAAAAAAAAAAAAIAEAAAAACsRAAAAAAAMGVzZHMAAAAAA4CAgB8AQBAEgICAFEAVAAYAAAANdQAADXUFgICAAhIQBgECAAAAGHN0dHMAAAAAAAAAAQAAAAMAAAQAAAAAHHN0c2MAAAAAAAAAAQAAAAEAAAADAAAAAQAAABRzdHN6AAAAAAAAAAoAAAADAAAAFHN0Y28AAAAAAAAAAQAAACg=");
  public static final byte[] EMPTY_MP3 = Base64.getDecoder().decode("SUQzAwAAAAADJVRGTFQAAAAPAAAB//5NAFAARwAvADMAAABDT01NAAAAggAAAGRldWlUdW5TTVBCACAwMDAwMDAwMCAwMDAwMDAwMCAwMDAwMDAwMCAwMDAwMDAwMDAwMDAxMmMxIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/+7RAAAAE4ABLgAAACAAACXAAAAEAAAEuAAAAIAAAJcAAAAT/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////+7RAwAAP/ABLgAAACByACXAAAAEAAAEuAAAAIAAAJcAAAAT/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////+7RAwAAP/ABLgAAACByACXAAAAEAAAEuAAAAIAAAJcAAAAT/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////+7RAwAAP/ABLgAAACByACXAAAAEAAAEuAAAAIAAAJcAAAAT///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////8=");
  private final static Logger LOG = LoggerFactory.getLogger(FrontendStatusEventsResource.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private UniversalUploadService universalUploadService;

  @Autowired
  private GameMediaService gameMediaService;

  @Autowired
  private TableAssetsService tableAssetsService;

  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  @GetMapping("/{id}")
  public GameMedia getGameMedia(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game == null) {
      throw new ResponseStatusException(NOT_FOUND, "Not game found for id " + id);
    }
    return game.getGameMedia();
  }

  @PostMapping("/assets/search")
  public TableAssetSearch searchTableAssets(@RequestBody TableAssetSearch search) throws Exception {
    Game game = gameService.getGame(search.getGameId());
    EmulatorType emulatorType = game.getEmulator().getEmulatorType();

    List<TableAsset> result = tableAssetsService.search(emulatorType, search.getScreen(), search.getTerm());
    search.setResult(result);
    return search;
  }

  @PostMapping("/assets/download/{gameId}/{screen}/{append}")
  public boolean downloadTableAsset(@PathVariable("gameId") int gameId,
                                    @PathVariable("screen") String screen,
                                    @PathVariable("append") boolean append,
                                    @RequestBody TableAsset asset) throws Exception {
    screen = screen.replace("Backglass", "BackGlass");
    LOG.info("Starting download of " + asset.getName() + "(appending: " + append + ")");
    Game game = gameService.getGame(gameId);
    VPinScreen s = VPinScreen.valueOf(screen);
    File mediaFolder = game.getMediaFolder(s);
    File target = new File(mediaFolder, game.getGameName() + "." + asset.getFileSuffix());
    if (target.exists() && append) {
      target = FileUtils.uniqueAsset(target);
    }
    tableAssetsService.download(asset, target);
    return true;
  }

  @GetMapping("/assets/test")
  public boolean testConnection() {
    return tableAssetsService.testConnection();
  }

  @GetMapping("/assets/invalidateMediaCache")
  public boolean invalidateMediaCache() {
    return tableAssetsService.invalidateMediaCache();
  }

  @GetMapping("/assets/d/{screen}/{gameId}/{url}")
  public ResponseEntity<InputStreamResource> getAsset(@PathVariable("screen") String screen,
                                                      @PathVariable("gameId") int gameId,
                                                      @PathVariable("url") String url) throws Exception {
    screen = screen.replace("Backglass", "BackGlass");
    Game game = gameService.getGame(gameId);
    EmulatorType emulatorType = game.getEmulator().getEmulatorType();

    String decode = URLDecoder.decode(url, StandardCharsets.UTF_8);
    String folder = decode.substring(0, decode.lastIndexOf("/"));
    String name = decode.substring(decode.lastIndexOf("/") + 1);
    Optional<TableAsset> result = tableAssetsService.get(emulatorType, VPinScreen.valueOf(screen), folder, name);
    if (result.isEmpty()) {
      throw new ResponseStatusException(NOT_FOUND);
    }

    TableAsset tableAsset = result.get();
    // put the "/" back that we removed in GameMediaServiceClient.getUrl()
    InputStream in = tableAssetsService.download("/" + url);
    InputStreamResource inputStreamResource = new InputStreamResource(in);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(tableAsset.getMimeType()))
        .header("X-Frame-Options", "SAMEORIGIN")
        .body(inputStreamResource);
  }

  @GetMapping("/{id}/{screen}/{name}")
  public ResponseEntity<Resource> handleRequestWithName(@PathVariable("id") int id, @PathVariable("screen") String screen, @PathVariable("name") String name) throws IOException {
    screen = screen.replace("Backglass", "BackGlass");
    VPinScreen vPinScreen = VPinScreen.valueOf(screen);
    Game game = gameService.getGame(id);
    if (game != null) {
      GameMedia gameMedia = game.getGameMedia();
      GameMediaItem gameMediaItem = gameMedia.getDefaultMediaItem(vPinScreen);
      if (!StringUtils.isEmpty(name)) {
        name = name.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
        name = name.replaceAll("\\+", "%2B");
        name = URLDecoder.decode(name, Charset.defaultCharset());
        gameMediaItem = gameMedia.getMediaItem(vPinScreen, name);
      }

      if (gameMediaItem != null) {
        File file = gameMediaItem.getFile();
        FileInputStream in = new FileInputStream(file);
        byte[] bytes = IOUtils.toByteArray(in);
        ByteArrayResource bytesResource = new ByteArrayResource(bytes);
        in.close();

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(CONTENT_LENGTH, String.valueOf(file.length()));
        responseHeaders.set(CONTENT_TYPE, gameMediaItem.getMimeType());
        responseHeaders.set("Access-Control-Allow-Origin", "*");
        responseHeaders.set("Access-Control-Expose-Headers", "origin, range");
        responseHeaders.set("Cache-Control", "public, max-age=3600");
        return ResponseEntity.ok().headers(responseHeaders).body(bytesResource);
      }
    }

    return ResponseEntity.notFound().build();
  }

  @GetMapping("/{id}/{screen}")
  public ResponseEntity<Resource> handleRequest(@PathVariable("id") int id, @PathVariable("screen") String screen) throws IOException {
    return handleRequestWithName(id, screen, null);
  }

  @PostMapping("/upload/{screen}/{append}")
  public JobExecutionResult upload(@PathVariable("screen") VPinScreen VPinScreen,
                                   @PathVariable("append") boolean append,
                                   @RequestParam(value = "file", required = false) MultipartFile file,
                                   @RequestParam("objectId") Integer gameId) {
    try {
      if (file == null) {
        LOG.error("Upload request did not contain a file object.");
        return JobExecutionResultFactory.error("Upload request did not contain a file object.");
      }

      Game game = gameService.getGame(gameId);
      if (game == null) {
        LOG.error("No game found for media upload.");
        return JobExecutionResultFactory.error("No game found for media upload.");
      }

      String suffix = FilenameUtils.getExtension(file.getOriginalFilename());
      File out = gameMediaService.buildMediaAsset(game, VPinScreen, suffix, append);
      LOG.info("Uploading " + out.getAbsolutePath());
      UploadUtil.upload(file, out);

      return JobExecutionResultFactory.empty();
    }
    catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "ALT sound upload failed: " + e.getMessage());
    }
  }

  @PostMapping("/packupload")
  public UploadDescriptor uploadPack(@RequestParam(value = "file", required = false) MultipartFile file,
                                     @RequestParam("objectId") Integer gameId) {
    UploadDescriptor descriptor = UploadDescriptorFactory.create(file, gameId);
    try {
      descriptor.getAssetsToImport().add(AssetType.POPPER_MEDIA);
      descriptor.upload();
      universalUploadService.importArchiveBasedAssets(descriptor, null, AssetType.POPPER_MEDIA);
      return descriptor;
    }
    catch (Exception e) {
      LOG.error(AssetType.POPPER_MEDIA.name() + " upload failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, AssetType.POPPER_MEDIA.name() + " upload failed: " + e.getMessage());
    }
    finally {
      descriptor.finalizeUpload();
    }
  }

  @DeleteMapping("/media/{gameId}/{screen}/{file}")
  public boolean deleteMedia(@PathVariable("gameId") int gameId, @PathVariable("screen") VPinScreen screen, @PathVariable("file") String filename) {
    Game game = gameService.getGame(gameId);
    File mediaFolder = game.getMediaFolder(screen);
    File media = new File(mediaFolder, filename);
    if (media.exists()) {
      return media.delete();
    }
    return false;
  }

  @PutMapping("/media/{gameId}/{screen}")
  public boolean doPut(@PathVariable("gameId") int gameId, @PathVariable("screen") VPinScreen screen, @RequestBody Map<String, String> data) throws Exception {
    try {
      if (data.containsKey("fullscreen")) {
        return toFullscreenMedia(gameId, screen);
      }
      if (data.containsKey("blank")) {
        return addBlank(gameId, screen);
      }
      if (data.containsKey("oldName")) {
        return renameAsset(gameId, screen, data.get("oldName"), data.get("newName"));
      }
      return true;
    }
    catch (Exception e) {
      LOG.error("Failed to execute media change request: " + e.getMessage(), e);
    }
    return false;
  }

  private boolean renameAsset(int gameId, VPinScreen screen, String oldName, String newName) {
    Game game = gameService.getGame(gameId);
    List<File> mediaFiles = game.getMediaFiles(screen);
    for (File file : mediaFiles) {
      if (file.getName().equals(oldName)) {
        File renamed = new File(file.getParentFile(), newName);
        if (file.renameTo(renamed)) {
          LOG.info("Renamed \"" + file.getAbsolutePath() + "\" to \"" + renamed.getAbsolutePath() + "\"");
          return true;
        }
      }
    }
    return false;
  }

  private boolean toFullscreenMedia(int gameId, VPinScreen screen) throws IOException {
    Game game = gameService.getGame(gameId);
    List<File> mediaFiles = game.getMediaFiles(screen);
    if (mediaFiles.size() == 1) {
      File mediaFile = mediaFiles.get(0);
      String name = mediaFile.getName();
      String baseName = FilenameUtils.getBaseName(name);
      String suffix = FilenameUtils.getExtension(name);
      String updatedBaseName = baseName + "(SCREEN3)." + suffix;

      LOG.info("Renaming " + mediaFile.getAbsolutePath() + " to '" + updatedBaseName + "'");
      boolean renamed = mediaFile.renameTo(new File(mediaFile.getParentFile(), updatedBaseName));
      if (!renamed) {
        LOG.error("Renaming to " + updatedBaseName + " failed.");
        return false;
      }

      File target = new File(mediaFile.getParentFile(), name);

      LOG.info("Copying blank asset to " + target.getAbsolutePath());
      FileOutputStream out = new FileOutputStream(target);
      //copy base64 encoded 0s video
      IOUtils.write(EMPTY_MP4, out);
      out.close();

      return true;
    }
    return false;
  }

  private boolean addBlank(int gameId, VPinScreen screen) throws IOException {
    Game game = gameService.getGame(gameId);
    File target = gameMediaService.uniqueMediaAsset(game, screen);
    FileOutputStream out = new FileOutputStream(target);
    //copy base64 asset
    if (screen.equals(VPinScreen.AudioLaunch) || screen.equals(VPinScreen.Audio)) {
      IOUtils.write(EMPTY_MP3, out);
    }
    else {
      IOUtils.write(EMPTY_MP4, out);
    }
    LOG.info("Written blank asset \"" + target.getAbsolutePath() + "\"");
    out.close();
    return true;
  }

}

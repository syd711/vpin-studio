package de.mephisto.vpin.server.games;

import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.frontend.*;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobDescriptorFactory;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.server.assets.TableAssetSourcesService;
import de.mephisto.vpin.server.assets.TableAssetsService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.FrontendStatusEventsResource;
import de.mephisto.vpin.server.frontend.WheelAugmenter;
import de.mephisto.vpin.server.frontend.WheelIconDelete;
import de.mephisto.vpin.server.util.UploadUtil;
import de.mephisto.vpin.server.system.JCodec;

import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static de.mephisto.vpin.server.util.RequestUtil.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping(API_SEGMENT + "media")
public class GameMediaResource {
  private final static Logger LOG = LoggerFactory.getLogger(FrontendStatusEventsResource.class);

  public static final byte[] EMPTY_MP4 = Base64.getDecoder().decode("AAAAGGZ0eXBpc29tAAAAAGlzb21tcDQxAAAACGZyZWUAAAAmbWRhdCELUCh9wBQ+4cAhC1AAfcAAPuHAIQtQAH3AAD7hwAAAAlNtb292AAAAbG12aGQAAAAAxzFHd8cxR3cAAV+QAAAYfQABAAABAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADAAAAG2lvZHMAAAAAEA0AT////xX/DgQAAAACAAABxHRyYWsAAABcdGtoZAAAAAfHMUd3xzFHdwAAAAIAAAAAAAAYfQAAAAAAAAAAAAAAAAEAAAAAAQAAAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAWBtZGlhAAAAIG1kaGQAAAAAxzFHd8cxR3cAAKxEAAAL/xXHAAAAAAA0aGRscgAAAAAAAAAAc291bgAAAAAAAAAAAAAAAFNvdW5kIE1lZGlhIEhhbmRsZXIAAAABBG1pbmYAAAAQc21oZAAAAAAAAAAAAAAAJGRpbmYAAAAcZHJlZgAAAAAAAAABAAAADHVybCAAAAABAAAAyHN0YmwAAABkc3RzZAAAAAAAAAABAAAAVG1wNGEAAAAAAAAAAQAAAAAAAAAAAAIAEAAAAACsRAAAAAAAMGVzZHMAAAAAA4CAgB8AQBAEgICAFEAVAAYAAAANdQAADXUFgICAAhIQBgECAAAAGHN0dHMAAAAAAAAAAQAAAAMAAAQAAAAAHHN0c2MAAAAAAAAAAQAAAAEAAAADAAAAAQAAABRzdHN6AAAAAAAAAAoAAAADAAAAFHN0Y28AAAAAAAAAAQAAACg=");
  public static final byte[] EMPTY_MP3 = Base64.getDecoder().decode("SUQzAwAAAAADJVRGTFQAAAAPAAAB//5NAFAARwAvADMAAABDT01NAAAAggAAAGRldWlUdW5TTVBCACAwMDAwMDAwMCAwMDAwMDAwMCAwMDAwMDAwMCAwMDAwMDAwMDAwMDAxMmMxIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwIDAwMDAwMDAwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD/+7RAAAAE4ABLgAAACAAACXAAAAEAAAEuAAAAIAAAJcAAAAT/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////+7RAwAAP/ABLgAAACByACXAAAAEAAAEuAAAAIAAAJcAAAAT/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////+7RAwAAP/ABLgAAACByACXAAAAEAAAEuAAAAIAAAJcAAAAT/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////+7RAwAAP/ABLgAAACByACXAAAAEAAAEuAAAAIAAAJcAAAAT///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////8=");

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private GameMediaService gameMediaService;

  @Autowired
  private TableAssetsService tableAssetsService;

  @Autowired
  private GameLifecycleService gameLifecycleService;

  @Autowired
  private GameService gameService;

  @Autowired
  private TableAssetSourcesService tableAssetSourcesService;

  @GetMapping("/{id}")
  public FrontendMedia getGameMedia(@PathVariable("id") int id) {
    Game game = frontendService.getOriginalGame(id);
    if (game == null) {
      throw new ResponseStatusException(NOT_FOUND, "Not game found for id " + id);
    }
    return frontendService.getGameMedia(game);
  }

  @GetMapping("/assets/search/conf")
  public TableAssetSource getTableAssetConf() throws Exception {
    TableAssetsAdapter<Game> assetAdapter = frontendService.getTableAssetAdapter();
    return assetAdapter != null ? assetAdapter.getAssetSource() : null;
  }

  @PostMapping("/assets/search")
  public TableAssetSearch searchTableAssets(@RequestBody TableAssetSearch search) throws Exception {
    Game game = gameService.getGame(search.getGameId());
    EmulatorType emulatorType = game != null && game.getEmulator() != null ? game.getEmulator().getType() : EmulatorType.VisualPinball;
    TableAssetSource source = tableAssetSourcesService.getAssetSource(search.getAssetSourceId());

    List<TableAsset> result = tableAssetsService.search(source, emulatorType, search.getScreen(), game, search.getTerm());
    search.setResult(result);
    return search;
  }

  @PostMapping("/assets/download/{gameId}/{screen}/{append}")
  public boolean downloadTableAsset(@PathVariable("gameId") int gameId,
                                    @PathVariable("screen") String screen,
                                    @PathVariable("append") boolean append,
                                    @RequestBody TableAsset asset) throws Exception {
    try {
      VPinScreen vPinScreen = VPinScreen.valueOfSegment(screen);
      LOG.info("Starting download of " + asset.getName() + "(appending: " + append + ")");
      Game game = frontendService.getOriginalGame(gameId);
      File mediaFolder = frontendService.getMediaFolder(game, vPinScreen, asset.getFileSuffix(), false);
      File target = new File(mediaFolder, game.getGameName() + "." + asset.getFileSuffix());
      if (target.exists() && append) {
        target = FileUtils.uniqueAsset(target);
      }
      tableAssetsService.download(asset, target);
      return true;
    }
    finally {
      gameLifecycleService.notifyGameAssetsChanged(gameId, AssetType.FRONTEND_MEDIA, null);
    }
  }

  @GetMapping("/assets/{assetSourceId}/test")
  public boolean testConnection(@PathVariable("assetSourceId") String assetSourceId) {
    return tableAssetsService.testConnection(assetSourceId);
  }

  @GetMapping("/assets/{assetSourceId}/invalidateMediaCache")
  public boolean invalidateMediaCache(@PathVariable("assetSourceId") String assetSourceId) {
    return tableAssetsService.invalidateMediaCache(assetSourceId);
  }

  @GetMapping("/assets/d/{screen}/{assetSourceId}/{gameId}/{url}")
  public void getTableAsset(HttpServletResponse response, HttpServletRequest request,
                                                        @PathVariable("screen") String screen,
                                                        @PathVariable("assetSourceId") String assetSourceId,
                                                        @PathVariable("gameId") int gameId,
                                                        @PathVariable("url") String url) throws Exception {
    VPinScreen vPinScreen = VPinScreen.valueOfSegment(screen);
    Game game = gameService.getGame(gameId);
    EmulatorType emulatorType = game != null && game.getEmulator() != null ? game.getEmulator().getType() : EmulatorType.VisualPinball;

    String decode = URLDecoder.decode(url, StandardCharsets.UTF_8);
    String folder = decode.substring(0, decode.lastIndexOf("/"));
    String name = decode.substring(decode.lastIndexOf("/") + 1);

    TableAssetSource source = tableAssetSourcesService.getAssetSource(assetSourceId);
    Optional<TableAsset> result = tableAssetsService.get(source, emulatorType, vPinScreen, game, folder, name);
    if (result.isEmpty()) {
      throw new ResponseStatusException(NOT_FOUND);
    }

    TableAsset tableAsset = result.get();

    // Set headers
    if (tableAsset.getLength() > 0) {
      response.setContentLength((int) tableAsset.getLength());
    }
    response.setHeader(CONTENT_TYPE, tableAsset.getMimeType());
    response.setHeader("Access-Control-Allow-Origin", "*");
    response.setHeader("Access-Control-Expose-Headers", "origin, range");
    response.setHeader("Cache-Control", "public, max-age=3600");

    // For HEAD, do not write the body
    if ("HEAD".equals(request.getMethod())) {
      response.setStatus(HttpStatus.OK.value());
      return;
    }
    // else normal download
    try (ServletOutputStream outputStream = response.getOutputStream()) {
      tableAssetsService.download(outputStream, tableAsset);
      response.flushBuffer();
      response.setStatus(HttpStatus.OK.value());
    } 
    catch (ClientAbortException cae) {
      LOG.info("Connection aborted while streaming media {} from {}", name, assetSourceId);
      response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
    catch (IOException e) {
      LOG.error("Failed to stream media {} from {}: {}", name, assetSourceId, e.getMessage(), e);
      response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
  }

  @GetMapping("/{id}/{screen}/{name}")
  public void getMedia(HttpServletResponse response, HttpServletRequest request,
                                                        @PathVariable("id") int id, 
                                                        @PathVariable("screen") String screen, 
                                                        @PathVariable("name") String name, 
                                                        @RequestParam(value = "preview", required = false) boolean preview) 
                                                        throws IOException {

    screen = screen.replaceAll("@2x", "");
    VPinScreen vPinScreen = VPinScreen.valueOfSegment(screen);
    if (vPinScreen == null) {
      LOG.error("Failed to resolve screen for value {}", screen);
    }
    Game game = frontendService.getOriginalGame(id);
    if (game == null) {
      throw new ResponseStatusException(NOT_FOUND);
    }

    FrontendMedia frontendMedia = frontendService.getGameMedia(game);
    FrontendMediaItem frontendMediaItem = frontendMedia.getDefaultMediaItem(vPinScreen);
    if (!StringUtils.isEmpty(name)) {
      name = name.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
      name = name.replaceAll("\\+", "%2B");
      name = URLDecoder.decode(name, Charset.defaultCharset());
      frontendMediaItem = frontendMedia.getMediaItem(vPinScreen, name);
    }

    if (frontendMediaItem == null) {
      throw new ResponseStatusException(NOT_FOUND);
    }

    File file = frontendMediaItem.getFile();

    boolean getPreview = false;
    int contentLength = -1;
    String mimeType = frontendMediaItem.getMimeType();
    if (preview && StringUtils.startsWithIgnoreCase(mimeType, "video/")) {
      // will return only a frame, content length cannot be calculated
      mimeType = "image/png";
      getPreview = true;
    }
    else {
      contentLength = (int) frontendMediaItem.getSize();
    }

    // Set headers
    if (contentLength > 0) {
      response.setContentLength(contentLength);
    }
    response.setHeader(CONTENT_TYPE, mimeType);
    response.setHeader("Access-Control-Allow-Origin", "*");
    response.setHeader("Access-Control-Expose-Headers", "origin, range");
    response.setHeader("Cache-Control", "public, max-age=3600");

    // For HEAD, do not write the body
    if ("HEAD".equals(request.getMethod())) {
      response.setStatus(HttpStatus.OK.value());
      return;
    }
    // else normal download
    try (ServletOutputStream outputStream = response.getOutputStream()) {

      if (getPreview) {
        byte[] bytes = JCodec.grab(frontendMediaItem.getFile());
        if (bytes != null) {
          response.setContentLength(bytes.length);
          response.getOutputStream().write(bytes);
        }
        else {
          response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
          return;
        }
      }
      else {
        try (FileInputStream in = new FileInputStream(file)) {
          IOUtils.copy(in, outputStream);
        }
      }
      response.flushBuffer();
      response.setStatus(HttpStatus.OK.value());
    } 
    catch (ClientAbortException cae) {
      LOG.info("Connection aborted while downloading {} for game {}", name, id);
      response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
    catch (IOException e) {
      LOG.error("Failed to stream media {} for game {}: {}", name, id, e.getMessage(), e);
      response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
  }

  @GetMapping("/{id}/{screen}")
  public void getMedia(HttpServletResponse response, HttpServletRequest request,
                                                @PathVariable("id") int id, 
                                                @PathVariable("screen") String screen,
                                                @RequestParam(value = "preview", required = false) boolean preview) 
                                                throws IOException {
    getMedia(response, request, id, screen, null, preview);
  }


  @PostMapping("/upload/{screen}/{append}")
  public JobDescriptor upload(@PathVariable("screen") VPinScreen screen,
                              @PathVariable("append") boolean append,
                              @RequestParam(value = "file", required = false) MultipartFile file,
                              @RequestParam("objectId") Integer gameId) {
    try {
      if (file == null) {
        LOG.error("Upload request did not contain a file object.");
        return JobDescriptorFactory.error("Upload request did not contain a file object.");
      }

      Game game = frontendService.getOriginalGame(gameId);
      if (game == null) {
        LOG.error("No game found for media upload.");
        return JobDescriptorFactory.error("No game found for media upload.");
      }

      String suffix = FilenameUtils.getExtension(file.getOriginalFilename());
      File mediaFolder = frontendService.getMediaFolder(game, screen, suffix, true);
      File out = GameMediaService.buildMediaAsset(mediaFolder, game, suffix, append);
      LOG.info("Uploading " + out.getAbsolutePath());
      UploadUtil.upload(file, out);
      return JobDescriptorFactory.empty();
    }
    catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Playlist media upload failed: " + e.getMessage());
    }
    finally {
      gameLifecycleService.notifyGameAssetsChanged(gameId, AssetType.FRONTEND_MEDIA, null);
    }
  }

  @DeleteMapping("/media/{gameId}/{screen}/{file}")
  public boolean deleteMedia(@PathVariable("gameId") int gameId, @PathVariable("screen") VPinScreen screen, @PathVariable("file") String filename) {
    try {
      Game game = frontendService.getOriginalGame(gameId);
      String suffix = FilenameUtils.getExtension(filename);
      File mediaFolder = frontendService.getMediaFolder(game, screen, suffix, false);
      File media = new File(mediaFolder, filename);
      if (media.exists()) {
        if (screen.equals(VPinScreen.Wheel)) {
          new WheelAugmenter(media).deAugment();
          new WheelIconDelete(media).delete();
        }
        return media.delete();
      }
      return false;
    }
    finally {
      gameLifecycleService.notifyGameAssetsChanged(gameId, AssetType.FRONTEND_MEDIA, null);
    }
  }


  @DeleteMapping("/media/{gameId}")
  public boolean deleteMedia(@PathVariable("gameId") int gameId) {
    try {
      Game game = frontendService.getOriginalGame(gameId);
      VPinScreen[] values = VPinScreen.values();
      for (VPinScreen screen : values) {
        FrontendMedia gameMedia = frontendService.getGameMedia(game);
        List<FrontendMediaItem> mediaItems = gameMedia.getMediaItems(screen);
        for (FrontendMediaItem mediaItem : mediaItems) {
          File file = mediaItem.getFile();
          if (screen.equals(VPinScreen.Wheel)) {
            new WheelAugmenter(file).deAugment();
            new WheelIconDelete(file).delete();
          }
          if (file.delete()) {
            LOG.info("Deleted game media: {}", file.getAbsolutePath());
          }
        }
      }
      return true;
    }
    finally {
      gameLifecycleService.notifyGameAssetsChanged(gameId, AssetType.FRONTEND_MEDIA, null);
    }
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
      if (data.containsKey("setDefault")) {
        return setDefaultAsset(gameId, screen, data.get("setDefault"));
      }
      if (data.containsKey("oldName")) {
        return renameAsset(gameId, screen, data.get("oldName"), data.get("newName"));
      }
      return true;
    }
    catch (Exception e) {
      LOG.error("Failed to execute media change request: " + e.getMessage(), e);
    }
    finally {
      gameLifecycleService.notifyGameAssetsChanged(gameId, AssetType.FRONTEND_MEDIA, null);
    }
    return false;
  }

  private boolean setDefaultAsset(int gameId, VPinScreen screen, String defaultName) {
    Game game = frontendService.getOriginalGame(gameId);
    List<File> mediaFiles = frontendService.getMediaFiles(game, screen);

    File torename = mediaFiles.stream().filter(f -> f.getName().equals(defaultName)).findFirst().orElse(null);
    if (torename == null) {
      LOG.info("Cannot set default asset as {} does not exist in the assets for game {} and screen {}", defaultName, gameId, screen);
      return false;
    }

    String extension = FilenameUtils.getExtension(defaultName);
    File temp = new File(torename.getParentFile(), "temp_" + defaultName);
    if (torename.renameTo(temp)) {
      for (File file : mediaFiles) {
        // find existing default files, mind there could be several with different extensions
        String fileext = FilenameUtils.getExtension(file.getName());
        if (FileUtils.isDefaultAsset(file.getName()) && StringUtils.equalsIgnoreCase(fileext, extension)) {
          File defaultFile = FileUtils.uniqueAsset(file);
          if (file.renameTo(defaultFile)) {
            LOG.info("Renamed \"{}\" to \"{}\"", file.getAbsolutePath(), defaultFile.getName());
          }
          else {
            LOG.warn("Cannot rename \"{}\" to \"{}\", state may be inconsistent", file.getAbsolutePath(), defaultFile.getName());
            return false;
          }
        }
      }
      // ends by renaming temp file to default
      File newFile = new File(torename.getParent(), FileUtils.baseUniqueAsset(defaultName) + "." + extension);
      if (temp.renameTo(newFile)) {
        LOG.info("New default asset set \"{} \"for game {} and screen{}", newFile.getAbsolutePath(), gameId, screen);
        return true;
      }
      else {
        LOG.warn("Cannot rename \"{}\" to \"{}\", state may be inconsistent", temp.getAbsolutePath(), newFile.getName());
      }

    }
    else {
      LOG.warn("Cannot rename \"{}\", set as default operation ignored", torename.getAbsolutePath());
    }
    return false;
  }

  private boolean renameAsset(int gameId, VPinScreen screen, String oldName, String newName) {
    Game game = frontendService.getOriginalGame(gameId);
    List<File> mediaFiles = frontendService.getMediaFiles(game, screen);
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
    Game game = frontendService.getOriginalGame(gameId);
    List<File> mediaFiles = frontendService.getMediaFiles(game, screen);
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
    Game game = frontendService.getOriginalGame(gameId);
    File target = gameMediaService.uniqueMediaAsset(game, screen);
    FileOutputStream out = new FileOutputStream(target);
    // copy base64 asset
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

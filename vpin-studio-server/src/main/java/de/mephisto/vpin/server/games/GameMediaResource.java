package de.mephisto.vpin.server.games;

import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.restclient.assets.AssetMetaData;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.frontend.*;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobDescriptorFactory;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.MimeTypeUtil;
import de.mephisto.vpin.server.assets.TableAssetSourcesService;
import de.mephisto.vpin.server.assets.TableAssetsService;
import de.mephisto.vpin.server.converter.MediaConverterService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.system.JCodec;
import de.mephisto.vpin.server.util.PngFrameCapture;
import de.mephisto.vpin.server.util.UploadUtil;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping(API_SEGMENT + "media")
public class GameMediaResource {
  private final static Logger LOG = LoggerFactory.getLogger(GameMediaResource.class);

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private GameMediaService gameMediaService;

  @Autowired
  private TableAssetsService tableAssetsService;

  @Autowired
  private MediaConverterService mediaConverterService;

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

    List<TableAsset> result = tableAssetsService.search(source, emulatorType, search.getScreen().getSegment(), game, search.getTerm());
    search.setResult(result);
    return search;
  }

  @PostMapping("/assets/download/{gameId}/{screen}/{append}")
  public boolean downloadTableAsset(@PathVariable("gameId") int gameId,
                                    @PathVariable("screen") VPinScreen screen,
                                    @PathVariable("append") boolean append,
                                    @RequestBody TableAsset asset) throws Exception {
    try {
      LOG.info("Starting download of " + asset.getName() + "(appending: " + append + ")");
      Game game = frontendService.getOriginalGame(gameId);
      File mediaFolder = frontendService.getMediaFolder(game, screen, asset.getFileSuffix(), false);
      File target = new File(mediaFolder, game.getGameName() + "." + asset.getFileSuffix());
      if (target.exists() && append) {
        target = FileUtils.uniqueAsset(target);
      }
      tableAssetsService.download(asset, target);

      // for PLayfield, if the tableAsset is in a different orientation than frontend, rotate the asset
      if (VPinScreen.PlayField.equals(screen) && frontendService.getFrontend().isPlayfieldMediaInverted() ^ asset.isPlayfieldMediaInverted()) {
        if (MimeTypeUtil.isImage(asset.getMimeType())) {
          mediaConverterService.rotateImage180(target);
        }
        else if (MimeTypeUtil.isVideo(asset.getMimeType())) {
          mediaConverterService.rotateVideo180(target);
        }
      }

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

  @GetMapping("/assets/d/{screenSegment}/{assetSourceId}/{gameId}/{url}")
  public void getTableAsset(HttpServletResponse response, HttpServletRequest request,
                            @PathVariable("screenSegment") String screenSegment,
                            @PathVariable("assetSourceId") String assetSourceId,
                            @PathVariable("gameId") int gameId,
                            @PathVariable("url") String url) throws Exception {
    Game game = gameService.getGame(gameId);
    EmulatorType emulatorType = game != null && game.getEmulator() != null ? game.getEmulator().getType() : EmulatorType.VisualPinball;

    String decode = URLDecoder.decode(url, StandardCharsets.UTF_8);
    String folder = decode.substring(0, decode.lastIndexOf("/"));
    String name = decode.substring(decode.lastIndexOf("/") + 1);

    TableAssetSource source = tableAssetSourcesService.getAssetSource(assetSourceId);
    Optional<TableAsset> result = tableAssetsService.get(source, emulatorType, screenSegment, game, folder, name);
    if (result.isEmpty()) {
      throw new ResponseStatusException(NOT_FOUND);
    }

    TableAsset tableAsset = result.get();

    long contentLength = tableAsset.getLength();
    String mimeType = tableAsset.getMimeType();

    // Process headers

    response.setContentType(mimeType);

    HttpStatus status = HttpStatus.OK;

    // optional range
    long start = -1, end = -1;
    String rangeHeader = request.getHeader("Range");
    if (rangeHeader != null) {
      List<HttpRange> ranges = HttpRange.parseRanges(rangeHeader);
      if (ranges.size() == 1) {
        HttpRange range = ranges.get(0);
        start = range.getRangeStart(contentLength);
        end = range.getRangeEnd(contentLength);

        if (start > 0 || end > 0) {
          status = HttpStatus.PARTIAL_CONTENT;
          response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + contentLength);
          contentLength = end - start + 1;
        } else {
          start = -1;
        }
      }
    }

    response.setContentLength((int) contentLength);
    response.setHeader("Access-Control-Allow-Origin", "*");
    response.setHeader("Access-Control-Expose-Headers", "origin, range");
    response.setHeader("Cache-Control", "public, max-age=36000");
    response.setHeader("Accept-Ranges", "bytes");

    if (start >= 0) {
      LOG.info("Processing {} method, Length={}, range {}-{}", request.getMethod(), contentLength, start, end);
    }
    else {
      LOG.info("Processing {} method, Length={}", request.getMethod(), contentLength);
    }

    // For HEAD, do not write the body
    if ("HEAD".equals(request.getMethod())) {
      response.setStatus(HttpStatus.OK.value());
      return;
    }
    // else normal download
    response.setStatus(status.value());

    try (ServletOutputStream outputStream = response.getOutputStream()) {
      tableAssetsService.download(outputStream, tableAsset, start, contentLength);
      response.flushBuffer();
    }
    catch (ClientAbortException cae) {
      LOG.info("Connection aborted while streaming media {} from {}", name, assetSourceId);
    }
    catch (IOException e) {
      LOG.error("Failed to stream media {} from {}: {}", name, assetSourceId, e.getMessage(), e);
      response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
    }
  }

  //---------------------------------

  @GetMapping("/{id}/{screen}/{name}")
  public void getMedia(HttpServletResponse response, HttpServletRequest request,
                       @PathVariable("id") int id,
                       @PathVariable("screen") VPinScreen screen,
                       @PathVariable("name") String name,
                       @RequestParam(value = "preview", required = false) boolean preview)
      throws IOException {
    if (screen == null) {
      LOG.error("Failed to resolve screen for value {}", screen);
    }
    Game game = frontendService.getOriginalGame(id);
    if (game == null) {
      throw new ResponseStatusException(NOT_FOUND);
    }

    FrontendMedia frontendMedia = frontendService.getGameMedia(game);
    final FrontendMediaItem frontendMediaItem;
    if (frontendMedia != null) {
      if (!StringUtils.isEmpty(name)) {
        //name = name.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
        //name = name.replaceAll("\\+", "%2B");
        name = URLDecoder.decode(name, Charset.defaultCharset());
        frontendMediaItem = frontendMedia.getMediaItem(screen, name);
      }
      else {
        frontendMediaItem = frontendMedia.getDefaultMediaItem(screen);
      }
    } else {
      frontendMediaItem = null;
    }

    downloadFrontendMediaItem(response, request, id, name, preview, frontendMediaItem);
  }

  private void downloadFrontendMediaItem(HttpServletResponse response, HttpServletRequest request, int id, String name,
      boolean preview, final FrontendMediaItem frontendMediaItem) throws IOException 
  {
    if (frontendMediaItem == null) {
      throw new ResponseStatusException(NOT_FOUND);
    }

    File file = frontendMediaItem.getFile();

    final boolean getPreview;
    long contentLength = -1;
    String mimeType = frontendMediaItem.getMimeType();
    if (preview && StringUtils.startsWithIgnoreCase(mimeType, "video/")) {
      // will return only a frame, content length cannot be calculated
      mimeType = "image/png";
      getPreview = true;
    }
    else {
      contentLength = frontendMediaItem.getSize();
      getPreview = false;
    }

    // Process headers

    response.setContentType(mimeType);

    HttpStatus status = HttpStatus.OK;

    // optional range
    long start = -1, end = -1;
    String rangeHeader = request.getHeader("Range");
    if (rangeHeader != null) {
      List<HttpRange> ranges = HttpRange.parseRanges(rangeHeader);
      if (ranges.size() == 1) {
        HttpRange range = ranges.get(0);
        start = range.getRangeStart(contentLength);
        end = range.getRangeEnd(contentLength);

        if (start > 0 || end > 0) {
          status = HttpStatus.PARTIAL_CONTENT;
          response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + contentLength);
          contentLength = end - start + 1;
        } else {
          start = -1;
        }
      }
    }

    response.setContentLength((int) contentLength);
    response.setHeader("Access-Control-Allow-Origin", "*");
    response.setHeader("Access-Control-Expose-Headers", "origin, range");
    response.setHeader("Cache-Control", "public, max-age=36000");
    response.setHeader("Accept-Ranges", "bytes");

    if (start >= 0) {
      LOG.info("Processing {} method, Length={}, range {}-{}", request.getMethod(), contentLength, start, end);
    }
    else {
      LOG.info("Processing {} method, Length={}", request.getMethod(), contentLength);
    }

    // For HEAD, do not write the body
    if ("HEAD".equals(request.getMethod())) {
      response.setStatus(HttpStatus.OK.value());
      return;
    }
    // else normal download
    try (ServletOutputStream outputStream = response.getOutputStream()) {

      response.setStatus(status.value());

      if (getPreview) {
        byte[] bytes = JCodec.grab(frontendMediaItem.getFile());
        if (bytes != null) {
          response.setContentLength(bytes.length);
          response.getOutputStream().write(bytes);
        }
        else {
          response.sendError(HttpStatus.NOT_FOUND.value());
          return;
        }
      }
      else {
        String ext = FilenameUtils.getExtension(file.getName());
        if (ext.equalsIgnoreCase("apng") && preview) {
          byte[] bytes = PngFrameCapture.captureFirstFrame(file);
          response.setContentLength((int) bytes.length);
          IOUtils.copy(new ByteArrayInputStream(bytes), outputStream);
        }
        else {
          try (FileInputStream in = new FileInputStream(file)) {
            if (start >= 0) {
              IOUtils.copyLarge(in, outputStream, start, contentLength);
            }
            else {
              IOUtils.copy(in, outputStream);
            }
          }
        }
      }
      response.flushBuffer();
    }
    catch (ClientAbortException cae) {
      LOG.info("Connection aborted while downloading {} for game {}", name, id);
    }
    catch (IOException e) {
      LOG.error("Failed to stream media {} for game {}: {}", name, id, e.getMessage(), e);
      response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
    }
  }

  @GetMapping("/{id}/{screen}")
  public void getMedia(HttpServletResponse response, HttpServletRequest request,
                       @PathVariable("id") int id,
                       @PathVariable("screen") VPinScreen screen,
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

      String suffix = FilenameUtils.getExtension(file.getOriginalFilename());
      File out = gameMediaService.uniqueMediaAsset(gameId, screen, suffix, true, append);
      if (out == null) {
        LOG.error("No game found for media upload.");
        return JobDescriptorFactory.error("No game found for media upload.");
      }

      LOG.info("Uploading " + out.getAbsolutePath());
      UploadUtil.upload(file, out);
      gameLifecycleService.notifyGameScreenAssetsChanged(gameId, screen, out);
      return JobDescriptorFactory.empty();
    }
    catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Media upload failed: " + e.getMessage());
    }
    finally {
      gameLifecycleService.notifyGameAssetsChanged(gameId, AssetType.FRONTEND_MEDIA, null);
    }
  }

  @GetMapping("/metadata/{gameId}/{screen}/{file}")
  public AssetMetaData metadata(@PathVariable("gameId") int gameId, @PathVariable("screen") VPinScreen screen, @PathVariable("file") String filename) {
    return gameMediaService.getMetadata(gameId, screen, filename);
  }

  @DeleteMapping("/media/{gameId}/{screen}/{file}")
  public boolean deleteMedia(@PathVariable("gameId") int gameId, @PathVariable("screen") VPinScreen screen, @PathVariable("file") String filename) {
    try {
      return gameMediaService.deleteMedia(gameId, screen, filename);
    }
    finally {
      gameLifecycleService.notifyGameAssetsChanged(gameId, AssetType.FRONTEND_MEDIA, null);
    }
  }

  @DeleteMapping("/media/{gameId}")
  public boolean deleteMedia(@PathVariable("gameId") int gameId) {
    try {
      return gameMediaService.deleteMedia(gameId);
    }
    finally {
      gameLifecycleService.notifyGameAssetsChanged(gameId, AssetType.FRONTEND_MEDIA, null);
    }
  }

  @PutMapping("/media/{gameId}/{screen}")
  public boolean doPut(@PathVariable("gameId") int gameId, @PathVariable("screen") VPinScreen screen, @RequestBody Map<String, String> data) throws Exception {
    try {
      if (data.containsKey("fullscreen")) {
        return gameMediaService.toFullscreenMedia(gameId, screen);
      }
      if (data.containsKey("blank")) {
        return gameMediaService.addBlank(gameId, screen);
      }
      if (data.containsKey("setDefault")) {
        return gameMediaService.setDefaultAsset(gameId, screen, data.get("setDefault"));
      }
      if (data.containsKey("oldName")) {
        return gameMediaService.renameAsset(gameId, screen, data.get("oldName"), data.get("newName"));
      }
      if (data.containsKey("copy")) {
        VPinScreen target = VPinScreen.valueOf(data.get("target"));
        return gameMediaService.copyAsset(gameId, screen, data.get("copy"), target);
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
}

package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.connectors.assets.EncryptDecrypt;
import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.connectors.assets.TableAssetsService;
import de.mephisto.vpin.popper.PopperAssetAdapter;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.UploadUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.*;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static de.mephisto.vpin.server.util.RequestUtil.CONTENT_LENGTH;
import static de.mephisto.vpin.server.util.RequestUtil.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping(API_SEGMENT + "poppermedia")
public class PopperMediaResource implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(PopperResource.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private SystemService systemService;

  private TableAssetsService tableAssetsService;

  @GetMapping("/{id}")
  public GameMedia getGameMedia(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game == null) {
      throw new ResponseStatusException(NOT_FOUND, "Not game found for id " + id);
    }
    return game.getGameMedia();
  }

  @GetMapping("/assets/search/{screen}/{term}")
  public List<TableAsset> searchTableAssets(@PathVariable("screen") String screen,
                                            @PathVariable("term") String term) {
    try {
      return tableAssetsService.search(EncryptDecrypt.SECRET_KEY_1, screen, term);
    } catch (Exception e) {
      LOG.error("Asset search failed: " + e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  @PostMapping("/assets/download/{gameId}/{screen}")
  public boolean downloadTableAsset(@PathVariable("gameId") int gameId,
                                    @PathVariable("screen") String screen,
                                    @RequestBody TableAsset asset) {
    Game game = gameService.getGame(gameId);
    PopperScreen s = PopperScreen.valueOf(screen);
    File pinpuSystemFolder = new File(systemService.getPinUPSystemFolder(), "POPMedia/" + systemService.getPupUpMediaFolderName(game) + "/" + s.name());
    File target = new File(pinpuSystemFolder, game.getGameDisplayName() + "." + asset.getMimeTypeSuffix());
    tableAssetsService.download(asset, target);
    return true;
  }

  @GetMapping("/{id}/{screen}/{name}")
  public ResponseEntity<Resource> handleRequestWithName(@PathVariable("id") int id, @PathVariable("screen") String screen, @PathVariable("name") String name) throws IOException {
    PopperScreen popperScreen = PopperScreen.valueOf(screen);
    Game game = gameService.getGame(id);
    if (game != null) {
      GameMedia gameMedia = game.getGameMedia();
      GameMediaItem gameMediaItem = gameMedia.getDefaultMediaItem(popperScreen);
      if (!StringUtils.isEmpty(name)) {
        name = URLDecoder.decode(name, Charset.defaultCharset());
        gameMediaItem = gameMedia.getMediaItem(popperScreen, name);
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

  @PostMapping("/upload/{screen}")
  public JobExecutionResult upload(@PathVariable("screen") PopperScreen popperScreen,
                                   @RequestParam(value = "file", required = false) MultipartFile file,
                                   @RequestParam(value = "uploadType", required = false) String uploadType,
                                   @RequestParam("objectId") Integer gameId) {
    try {
      if (file == null) {
        LOG.error("Upload request did not contain a file object.");
        return JobExecutionResultFactory.error("Upload request did not contain a file object.");
      }

      Game game = gameService.getGame(gameId);
      if (game == null) {
        LOG.error("No game found for popper media upload.");
        return JobExecutionResultFactory.error("No game found for PinUP Popper media upload.");
      }

      File pinUPMediaFolder = game.getPinUPMediaFolder(popperScreen);
      String filename = game.getGameDisplayName();
      String suffix = FilenameUtils.getExtension(file.getOriginalFilename());

      File out = new File(pinUPMediaFolder, filename + "." + suffix);
      if (out.exists()) {
        String nameIndex = "01";
        out = new File(pinUPMediaFolder, filename + nameIndex + "." + suffix);
      }

      int index = 1;
      while (out.exists()) {
        index++;
        String nameIndex = index <= 9 ? "0" + index : String.valueOf(index);
        out = new File(pinUPMediaFolder, filename + nameIndex + "." + suffix);
      }

      LOG.info("Uploading " + out.getAbsolutePath());
      UploadUtil.upload(file, out);

      return JobExecutionResultFactory.empty();
    } catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "ALT sound upload failed: " + e.getMessage());
    }
  }

  @DeleteMapping("/media/{gameId}/{screen}/{file}")
  public boolean deleteMedia(@PathVariable("gameId") int gameId, @PathVariable("screen") PopperScreen screen, @PathVariable("file") String filename) {
    Game game = gameService.getGame(gameId);
    File pinUPMediaFolder = game.getPinUPMediaFolder(screen);
    File media = new File(pinUPMediaFolder, filename);
    if (media.exists()) {
      return media.delete();
    }
    return false;
  }

  @PutMapping("/media/{gameId}/{screen}")
  public boolean doPut(@PathVariable("gameId") int gameId, @PathVariable("screen") PopperScreen screen, @RequestBody Map<String, String> data) throws Exception {
    if (data.containsKey("fullscreen")) {
      return toFullscreenMedia(gameId, screen);
    }

    return renameMedia(gameId, screen, data);
  }

  private boolean toFullscreenMedia(int gameId, PopperScreen screen) throws IOException {
    Game game = gameService.getGame(gameId);
    List<File> pinUPMedia = game.getPinUPMedia(screen);
    if (pinUPMedia.size() == 1) {
      File mediaFile = pinUPMedia.get(0);
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

      LOG.info("Copying blank video to " + target.getAbsolutePath());
      FileOutputStream out = new FileOutputStream(target);
      //copy base64 encoded 0s video
      byte[] bytesEncoded = Base64.getDecoder().decode("AAAAGGZ0eXBpc29tAAAAAGlzb21tcDQxAAAACGZyZWUAAAAmbWRhdCELUCh9wBQ+4cAhC1AAfcAAPuHAIQtQAH3AAD7hwAAAAlNtb292AAAAbG12aGQAAAAAxzFHd8cxR3cAAV+QAAAYfQABAAABAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADAAAAG2lvZHMAAAAAEA0AT////xX/DgQAAAACAAABxHRyYWsAAABcdGtoZAAAAAfHMUd3xzFHdwAAAAIAAAAAAAAYfQAAAAAAAAAAAAAAAAEAAAAAAQAAAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAEAAAAAAAAAAAAAAAAAAAWBtZGlhAAAAIG1kaGQAAAAAxzFHd8cxR3cAAKxEAAAL/xXHAAAAAAA0aGRscgAAAAAAAAAAc291bgAAAAAAAAAAAAAAAFNvdW5kIE1lZGlhIEhhbmRsZXIAAAABBG1pbmYAAAAQc21oZAAAAAAAAAAAAAAAJGRpbmYAAAAcZHJlZgAAAAAAAAABAAAADHVybCAAAAABAAAAyHN0YmwAAABkc3RzZAAAAAAAAAABAAAAVG1wNGEAAAAAAAAAAQAAAAAAAAAAAAIAEAAAAACsRAAAAAAAMGVzZHMAAAAAA4CAgB8AQBAEgICAFEAVAAYAAAANdQAADXUFgICAAhIQBgECAAAAGHN0dHMAAAAAAAAAAQAAAAMAAAQAAAAAHHN0c2MAAAAAAAAAAQAAAAEAAAADAAAAAQAAABRzdHN6AAAAAAAAAAoAAAADAAAAFHN0Y28AAAAAAAAAAQAAACg=");
      IOUtils.write(bytesEncoded, out);
      out.close();

      return true;
    }
    return false;
  }

  private boolean renameMedia(int gameId, PopperScreen screen, Map<String, String> data) {
    Game game = gameService.getGame(gameId);
    File pinUPMediaFolder = game.getPinUPMediaFolder(screen);

    String oldName = data.get("oldName");
    String newName = data.get("newName");

    File media = new File(pinUPMediaFolder, oldName);
    String gameBaseName = FilenameUtils.getBaseName(game.getGameFileName());
    if (media.exists() && newName.startsWith(gameBaseName)) {
      String suffix = FilenameUtils.getExtension(media.getName());
      if (!newName.endsWith("." + suffix)) {
        newName = newName + "." + suffix;
      }

      File target = new File(pinUPMediaFolder, newName);
      LOG.info("Renaming " + media.getAbsolutePath() + " to " + target.getAbsolutePath());
      return media.renameTo(target);
    }
    else {
      LOG.warn("Invalid target name '" + newName + "' should start with '" + gameBaseName + "'");
    }
    return false;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    tableAssetsService = new TableAssetsService();
    tableAssetsService.registerAdapter(new PopperAssetAdapter());
  }
}

package de.mephisto.vpin.server.playlists;

import de.mephisto.vpin.restclient.frontend.FrontendMedia;
import de.mephisto.vpin.restclient.frontend.FrontendMediaItem;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.util.UploadUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static de.mephisto.vpin.server.util.RequestUtil.CONTENT_LENGTH;
import static de.mephisto.vpin.server.util.RequestUtil.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping(API_SEGMENT + "playlistmedia")
public class PlaylistMediaResource {
  private final static Logger LOG = LoggerFactory.getLogger(PlaylistMediaResource.class);

  @Autowired
  private PlaylistMediaService playlistMediaService;

  @Autowired
  private PlaylistService playlistService;


  @DeleteMapping("/{playlistId}/{screen}/{file}")
  public boolean deleteMedia(@PathVariable("playlistId") int playlistId, @PathVariable("screen") VPinScreen screen, @PathVariable("file") String filename) {
    return playlistMediaService.deleteMedia(playlistId, screen, filename);
  }

  @GetMapping("/{id}/{screen}/{name}")
  public ResponseEntity<Resource> getMedia(@PathVariable("id") int id, @PathVariable("screen") String screen, @PathVariable("name") String name) throws IOException {
    VPinScreen vPinScreen = VPinScreen.valueOfSegment(screen);
    Playlist playlist = playlistService.getPlaylist(id);
    if (playlist != null) {
      FrontendMedia frontendMedia = playlist.getPlaylistMedia();
      FrontendMediaItem frontendMediaItem = frontendMedia.getDefaultMediaItem(vPinScreen);
      if (!StringUtils.isEmpty(name)) {
        name = name.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
        name = name.replaceAll("\\+", "%2B");
        name = URLDecoder.decode(name, Charset.defaultCharset());
        frontendMediaItem = frontendMedia.getMediaItem(vPinScreen, name);
      }

      if (frontendMediaItem != null) {
        File file = frontendMediaItem.getFile();
        FileInputStream in = new FileInputStream(file);
        byte[] bytes = IOUtils.toByteArray(in);
        ByteArrayResource bytesResource = new ByteArrayResource(bytes);
        in.close();

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(CONTENT_LENGTH, String.valueOf(file.length()));
        responseHeaders.set(CONTENT_TYPE, frontendMediaItem.getMimeType());
        responseHeaders.set("Access-Control-Allow-Origin", "*");
        responseHeaders.set("Access-Control-Expose-Headers", "origin, range");
        responseHeaders.set("Cache-Control", "public, max-age=3600");
        return ResponseEntity.ok().headers(responseHeaders).body(bytesResource);
      }
    }

    return ResponseEntity.notFound().build();
  }

  @PostMapping("/upload/{screen}/{append}")
  public JobExecutionResult upload(@PathVariable("screen") VPinScreen screen,
                                   @PathVariable("append") boolean append,
                                   @RequestParam(value = "file", required = false) MultipartFile file,
                                   @RequestParam("objectId") Integer playlistId) {
    try {
      if (file == null) {
        LOG.error("Upload request did not contain a file object.");
        return JobExecutionResultFactory.error("Upload request did not contain a file object.");
      }

      Playlist playlist = playlistService.getPlaylist(playlistId);
      if (playlist == null) {
        LOG.error("No playlist for media upload.");
        return JobExecutionResultFactory.error("No playlist found for media upload.");
      }

      String suffix = FilenameUtils.getExtension(file.getOriginalFilename());
      File out = playlistMediaService.buildMediaAsset(playlist, screen, suffix, append);
      LOG.info("Uploading " + out.getAbsolutePath());
      UploadUtil.upload(file, out);

      return JobExecutionResultFactory.empty();
    }
    catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Playlist media upload failed: " + e.getMessage());
    }
  }
}

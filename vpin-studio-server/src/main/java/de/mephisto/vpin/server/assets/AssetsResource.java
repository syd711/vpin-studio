package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.restclient.assets.AssetRequest;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.RequestUtil;
import de.mephisto.vpin.server.util.UploadUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 *
 */
@RestController
@RequestMapping(API_SEGMENT + "assets")
public class AssetsResource {
  private final static Logger LOG = LoggerFactory.getLogger(AssetsResource.class);

  @Autowired
  private AssetService assetService;

  @GetMapping
  public List<Asset> getAssets() {
    return assetService.getAssets();
  }

  @GetMapping("/maintenance")
  public ResponseEntity<byte[]> getMaintenanceBackground() {
    return serializeFile(new File("resources/maintenance.mp4"));
  }

  @GetMapping("/competition/{gameId}")
  public ResponseEntity<byte[]> getCompetitionBackground(@PathVariable("gameId") int gameId) {
    Asset competitionBackground = assetService.getCompetitionBackground(gameId);
    if (competitionBackground == null) {
      File defaultAsset = new File(SystemService.RESOURCES, "competition-bg-default.png");
      return serializeFile(defaultAsset);
    }
    return serializeAsset(competitionBackground);
  }

  @PostMapping("/metadata")
  public AssetRequest getMetaData(@RequestBody AssetRequest request) {
    return assetService.getMetadata(request);
  }

  @GetMapping("/defaultbackground/{id}")
  public ResponseEntity<byte[]> getRaw(@PathVariable("id") int id) throws Exception {
    byte[] raw = assetService.getRaw(id);
    return RequestUtil.serializeImage(raw, "background-" + id + ".png");
  }

  @GetMapping("/{id}")
  public Asset getById(@PathVariable("id") final int id) {
    return assetService.getById(id);
  }

  @GetMapping("/avatar")
  public ResponseEntity<byte[]> getAvatar() {
    Asset asset = assetService.getAvatar();
    if (asset != null) {
      return serializeAsset(asset);
    }
    throw new ResponseStatusException(NOT_FOUND, "Not avatar asset found");
  }

  @GetMapping("/data/{uuid}")
  public ResponseEntity<byte[]> get(@PathVariable("uuid") String uuid) {
    Asset asset = assetService.getByUuid(uuid);
    if (asset != null) {
      return serializeAsset(asset);
    }
    throw new ResponseStatusException(NOT_FOUND, "Not asset found for uuid " + uuid);
  }

  @DeleteMapping("/{id}")
  public boolean delete(@PathVariable("id") int id) {
    return assetService.resetGameAssets(id);
  }

  @DeleteMapping("/background/{gameId}")
  public boolean deleteDefaultBackground(@PathVariable("gameId") int gameId) {
    return assetService.deleteDefaultBackground(gameId);
  }

  @GetMapping("/index/exists")
  public boolean isMediaIndexAvailable() {
    return assetService.isMediaIndexAvailable();
  }

  @PostMapping("/save")
  public Asset save(@RequestBody Asset asset) {
    return assetService.save(asset);
  }

  @PostMapping("/{id}/upload/{max}")
  public Asset upload(@PathVariable("id") long id,
                      @PathVariable("max") int maxSize,
                      @RequestParam("assetType") String assetType,
                      @RequestParam("file") MultipartFile file) throws IOException {
    if (file == null) {
      LOG.error("Upload request did not contain a file object.");
      throw new ResponseStatusException(NOT_FOUND, "Upload request did not contain a file object.");
    }

    byte[] data = file.getBytes();
    if (maxSize > 0) {
      data = UploadUtil.resizeImageUpload(file, maxSize);
    }
    return assetService.saveOrUpdate(data, id, file.getOriginalFilename(), assetType, null);
  }

  @PostMapping("/background")
  public Boolean backgroundUpload(@RequestParam(value = "file", required = false) MultipartFile file,
                                  @RequestParam(value = "uploadType", required = false) String uploadType,
                                  @RequestParam("objectId") Integer gameId) {
    try {
      return assetService.backgroundUpload(file, gameId);
    }
    catch (Exception e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Background image upload failed: " + e.getMessage());
    }
  }

  public ResponseEntity<byte[]> serializeAsset(Asset asset) {
    return ResponseEntity.ok()
        .lastModified(asset.getUpdatedAt().getTime())
        .contentType(MediaType.parseMediaType(asset.getMimeType()))
        .contentLength(asset.getData().length)
        .cacheControl(CacheControl.maxAge(3600 * 24 * 7, TimeUnit.SECONDS).cachePublic())
        .body(asset.getData());
  }

  public ResponseEntity<byte[]> serializeFile(File file) {
    try {
      String suffix = FilenameUtils.getExtension(file.getName());
      String mimeType = "image/" + suffix;
      return ResponseEntity.ok()
          .lastModified(file.lastModified())
          .contentType(MediaType.parseMediaType(mimeType))
          .contentLength(file.length())
          .cacheControl(CacheControl.maxAge(3600 * 24 * 7, TimeUnit.SECONDS).cachePublic())
          .body(IOUtils.toByteArray(new FileInputStream(file)));
    }
    catch (IOException e) {
      LOG.error("Faild to serialize file " + file.getAbsolutePath() + ": " + e.getMessage(), e);
    }
    return null;
  }
}

package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.server.util.UploadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
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

  @GetMapping("/{id}")
  public Asset getById(@PathVariable("id") final int id) {
    return assetService.getById(id);
  }

  @GetMapping("/data/{uuid}")
  public ResponseEntity<byte[]> get(@PathVariable("uuid") String uuid) {
    Asset asset = assetService.getByUuid(uuid);
    if (asset != null) {
      return assetService.serializeAsset(asset);
    }
    throw new ResponseStatusException(NOT_FOUND, "Not asset found for uuid " + uuid);
  }

  @DeleteMapping("/{id}")
  public boolean delete(@PathVariable("id") int id) {
    return assetService.delete(id);
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
    return assetService.saveOrUpdate(data, id, file.getOriginalFilename(), assetType);
  }
}

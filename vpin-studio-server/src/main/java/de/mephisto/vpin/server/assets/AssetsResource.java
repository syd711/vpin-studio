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
import java.util.UUID;
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
  private AssetRepository assetRepository;

  @GetMapping
  public List<Asset> getAssets() {
    return assetRepository.findAll();
  }

  @GetMapping("/{id}")
  public Asset getById(@PathVariable("id") final String id) {
    Optional<Asset> asset = assetRepository.findById(Long.valueOf(id));
    return asset.orElse(null);
  }

  @GetMapping("/data/{uuid}")
  public ResponseEntity<byte[]> get(@PathVariable("uuid") String uuid) {
    try {
      Optional<Asset> assetOptional = assetRepository.findByUuid(uuid);
      if (assetOptional.isPresent()) {
        Asset asset = assetOptional.get();
        if (asset.getData() != null) {
          return ResponseEntity.ok()
              .lastModified(asset.getUpdatedAt().getTime())
              .contentType(MediaType.parseMediaType(asset.getMimeType()))
              .contentLength(asset.getData().length)
              .cacheControl(CacheControl.maxAge(3600 * 24 * 7, TimeUnit.SECONDS).cachePublic())
              .body(asset.getData());
        }
      }
      else {
        LOG.warn("Requested asset '" + uuid + "', but did not find and asset for it.");
      }
    } catch (Exception e) {
      LOG.error("Failed to load asset: " + e.getMessage(), e);
    }

    return null;
  }

  @DeleteMapping("/{id}")
  public boolean delete(@PathVariable("id") String id) {
    Asset asset = getById(id);
    assetRepository.delete(asset);
    return true;
  }

  @PostMapping("/save")
  public Asset save(@RequestBody Asset asset) {
    return assetRepository.save(asset);
  }

  @PostMapping("/{id}/upload/{max}")
  public Asset upload(@PathVariable("id") long id,
                      @PathVariable("max") int maxSize,
                      @RequestParam("file") MultipartFile file) throws IOException {
    if (file == null) {
      LOG.error("Upload request did not contain a file object.");
      throw new ResponseStatusException(NOT_FOUND, "Upload request did not contain a file object.");
    }

    byte[] data = file.getBytes();
    if (maxSize > 0) {
      data = UploadUtil.resizeImageUpload(file, maxSize);
    }

    String mimeType = "image/jpg";
    if(file.getOriginalFilename().toLowerCase().endsWith(".png")) {
      mimeType = "image/png";
    }

    Asset asset = new Asset();
    asset.setUuid(UUID.randomUUID().toString());
    if (id > 0) {
      Optional<Asset> byId = assetRepository.findById(id);
      if (byId.isPresent()) {
        asset = byId.get();
      }
    }
    asset.setData(data);
    asset.setMimeType(mimeType);
    Asset updated = assetRepository.saveAndFlush(asset);
    LOG.info("Saved " +updated);
    return updated;
  }
}

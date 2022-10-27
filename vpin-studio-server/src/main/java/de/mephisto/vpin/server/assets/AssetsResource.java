package de.mephisto.vpin.server.assets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@RestController
@RequestMapping("/asset")
public class AssetsResource {
  private final static Logger LOG = LoggerFactory.getLogger(AssetsResource.class);
  public final int MAX_PACKET_SIZE = 4194304;

  @Autowired
  private AssetRepository assetRepository;

  @GetMapping("/{id}")
  public Asset getById(@PathVariable("id") final String id) {
    Optional<Asset> asset = assetRepository.findById(Long.valueOf(id));
    if(asset.isPresent()) {
      return asset.get();
    }

    return null;
  }

  @GetMapping("/data/{uuid}")
  public ResponseEntity<byte[]> get(@PathVariable("uuid") String uuid) {
    try {
      String id = uuid;
      if(id.contains(".")) {
        id = id.substring(0, id.indexOf("."));
      }
      Optional<Asset> assetOptional= assetRepository.findByUuid(id);
      if(assetOptional.isPresent()) {
        Asset asset = assetOptional.get();
        if(asset.getData() != null) {
          return ResponseEntity.ok()
              .lastModified(asset.getUpdatedAt().getTime())
              .contentType(MediaType.parseMediaType(asset.getMimeType()))
              .contentLength(asset.getData().length)
              .cacheControl(CacheControl.maxAge(3600 * 24 * 7, TimeUnit.SECONDS).cachePublic())
              .body(asset.getData());
        }
      }
      else {
        LOG.warn("Requested asset '" + id + "', but did not find and asset for it.");
      }
    }
    catch (Exception e) {
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

  @PostMapping("/{id}/upload")
  public Asset upload(@PathVariable("id") String id,
                      @RequestParam("file") MultipartFile file) {
    byte[] bytes = new byte[0];
    try {
      bytes = file.getBytes();
      Asset asset = new Asset();
      if(id != null && !id.equals("null")) {
        asset = getById(id);
      }
      asset.setData(bytes);
      return assetRepository.save(asset);
    } catch (Exception e) {
      LOG.error("Failed to store asset: " + e.getMessage() + ", byte size was " + bytes.length, e);
    }
    return null;
  }
}

package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.restclient.AssetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AssetService {
  private final static Logger LOG = LoggerFactory.getLogger(AssetService.class);

  @Autowired
  private AssetRepository assetRepository;

  public Asset save(Asset asset) {
    return assetRepository.save(asset);
  }

  public Asset getById(long id) {
    Optional<Asset> asset = assetRepository.findById(id);
    return asset.orElse(null);
  }

  public Asset getCompetitionBackground(long gameId) {
    Optional<Asset> asset = assetRepository.findByExternalIdAndAssetType(String.valueOf(gameId), AssetType.COMPETITION.name());
    return asset.orElse(null);
  }

  public Asset getByUuid(String uuid) {
    Optional<Asset> asset = assetRepository.findByUuid(uuid);
    return asset.orElse(null);
  }


  public List<Asset> getAssets() {
    return assetRepository.findAll();
  }

  public boolean delete(long id) {
    Optional<Asset> byId = assetRepository.findById(id);
    if (byId.isPresent()) {
      assetRepository.delete(byId.get());
      return true;
    }
    return false;
  }

  public Asset saveOrUpdate(byte[] data, long id, String assetName, String assetType) {
    String mimeType = "image/jpg";
    if (assetName.toLowerCase().endsWith(".png")) {
      mimeType = "image/png";
    }

    Asset asset = new Asset();
    asset.setAssetType(assetType);

    asset.setUuid(UUID.randomUUID().toString());
    if (id >= 0) {
      Optional<Asset> byId = assetRepository.findById(id);
      if (byId.isPresent()) {
        asset = byId.get();
      }
    }
    asset.setData(data);
    asset.setMimeType(mimeType);
    Asset updated = assetRepository.saveAndFlush(asset);
    LOG.info("Saved " + updated);
    return updated;
  }

  public ResponseEntity serializeAsset(Asset asset) {
    return ResponseEntity.ok()
        .lastModified(asset.getUpdatedAt().getTime())
        .contentType(MediaType.parseMediaType(asset.getMimeType()))
        .contentLength(asset.getData().length)
        .cacheControl(CacheControl.maxAge(3600 * 24 * 7, TimeUnit.SECONDS).cachePublic())
        .body(asset.getData());
  }
}

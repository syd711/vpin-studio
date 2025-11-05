package de.mephisto.vpin.server.assets;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

  Optional<Asset> findByUuid(String uuid);

  Optional<Asset> findByExternalId(String externalId);

  Optional<Asset> findByExternalIdAndAssetType(String externalId, String assetType);

  @Transactional
  void deleteByExternalId(String gameId);

  @Transactional
  void deleteByUuid(String gameId);
}

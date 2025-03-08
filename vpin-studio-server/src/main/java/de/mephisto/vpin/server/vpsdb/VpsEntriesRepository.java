package de.mephisto.vpin.server.vpsdb;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VpsEntriesRepository extends JpaRepository<VpsDbEntry, Long> {
  VpsDbEntry findByVpsTableId(String id);
}

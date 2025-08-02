package de.mephisto.vpin.server.backups;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BackupSourceRepository extends JpaRepository<BackupSource, Long> {
}

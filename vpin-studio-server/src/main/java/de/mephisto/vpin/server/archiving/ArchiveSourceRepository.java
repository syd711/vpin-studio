package de.mephisto.vpin.server.archiving;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArchiveSourceRepository extends JpaRepository<ArchiveSource, Long> {
}

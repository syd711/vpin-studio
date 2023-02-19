package de.mephisto.vpin.server.vpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VpaSourceRepository extends JpaRepository<VpaSource, Long> {
}

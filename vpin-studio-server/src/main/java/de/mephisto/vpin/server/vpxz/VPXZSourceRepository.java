package de.mephisto.vpin.server.vpxz;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VPXZSourceRepository extends JpaRepository<VPXZSource, Long> {
}

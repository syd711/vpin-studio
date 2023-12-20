package de.mephisto.vpin.server.mania;

import de.mephisto.vpin.restclient.mania.ManiaAccountRepresentation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManiaAccountRepository extends JpaRepository<ManiaAccount, Long> {

  ManiaAccount findByCabinetId(String cabinetId);
}


package de.mephisto.vpin.server.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameDetailsRepository extends JpaRepository<GameDetails, Long> {

  GameDetails findByPupId(int pupId);
}


package de.mephisto.vpin.server.games;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameDetailsRepository extends JpaRepository<GameDetails, Long> {

  GameDetails findByPupId(int pupId);
}

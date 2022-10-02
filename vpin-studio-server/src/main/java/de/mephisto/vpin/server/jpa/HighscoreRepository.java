
package de.mephisto.vpin.server.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HighscoreRepository extends JpaRepository<Highscore, Long> {

  Highscore findByPupId(int pupId);
}

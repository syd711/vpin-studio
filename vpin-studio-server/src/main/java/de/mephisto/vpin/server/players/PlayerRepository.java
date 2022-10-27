
package de.mephisto.vpin.server.players;

import de.mephisto.vpin.server.highscores.Highscore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<Highscore, Long> {
}

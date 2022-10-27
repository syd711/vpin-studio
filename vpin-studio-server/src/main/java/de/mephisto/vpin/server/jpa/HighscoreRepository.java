
package de.mephisto.vpin.server.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HighscoreRepository extends JpaRepository<Highscore, Long> {

  @Query(value = "SELECT * FROM Highscores c WHERE c.gameId = ?1 ORDER BY createdAt LIMIT 1", nativeQuery = true)
  Highscore findByGameId(int gameId);
}

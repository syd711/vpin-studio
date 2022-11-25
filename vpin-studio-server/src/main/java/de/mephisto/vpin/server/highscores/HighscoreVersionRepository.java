
package de.mephisto.vpin.server.highscores;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HighscoreVersionRepository extends JpaRepository<Highscore, Long> {

  @Query(value = "SELECT * FROM HighscoreVersions c WHERE c.gameId = ?1 ORDER BY updatedAt", nativeQuery = true)
  List<HighscoreVersion> findByGameId(int gameId);
}

package de.mephisto.vpin.server.highscores;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface HighscoreRepository extends JpaRepository<Highscore, Long> {

  Optional<Highscore> findByGameId(int gameId);

  Optional<Highscore> findByGameIdAndCreatedAtBetween(int gameId, Date start, Date end);

  @Query(value = "SELECT * FROM Highscores h WHERE h.raw NOT NULL ORDER BY lastModified DESC", nativeQuery = true)
  List<Highscore> findRecent();

  List<Highscore> findAllByOrderByCreatedAtDesc();

  List<Highscore> findByRawIsNotNull();
}

package de.mephisto.vpin.server.highscores;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface HighscoreVersionRepository extends JpaRepository<HighscoreVersion, Long> {

  List<HighscoreVersion> findAllByOrderByCreatedAtDesc();

  List<HighscoreVersion> findByGameIdAndCreatedAtBetween(int gameId, Date startDate, Date endDate);

  List<HighscoreVersion> findByGameId(int gameId);

  Optional<HighscoreVersion> findByCreatedAt(Date createdAt);

}

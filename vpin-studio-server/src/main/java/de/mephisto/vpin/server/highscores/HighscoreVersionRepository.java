package de.mephisto.vpin.server.highscores;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface HighscoreVersionRepository extends JpaRepository<HighscoreVersion, Long> {

  List<HighscoreVersion> findAllByOrderByCreatedAtDesc();

  List<HighscoreVersion> findByGameIdAndCreatedAtBetween(int gameId, Date startDate, Date endDate);

}

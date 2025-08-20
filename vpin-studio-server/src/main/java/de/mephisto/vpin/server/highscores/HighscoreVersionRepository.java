package de.mephisto.vpin.server.highscores;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface HighscoreVersionRepository extends JpaRepository<HighscoreVersion, Long> {

  @Query(value = "SELECT * FROM HighscoreVersions h WHERE changedPosition > 0 ORDER BY createdAt DESC limit 200", nativeQuery = true)
  List<HighscoreVersion> findAllLimited();

  @Query(value = "SELECT * FROM HighscoreVersions h WHERE changedPosition > 0 AND newRaw LIKE '%' || ?1 || '%' ORDER BY createdAt DESC limit 50", nativeQuery = true)
  List<HighscoreVersion> findAllByInitials(@Param("initials") String initials);

  List<HighscoreVersion> findAllByOrderByCreatedAtDesc();

  List<HighscoreVersion> findByGameIdAndCreatedAtBetween(int gameId, Date startDate, Date endDate);

  //List<HighscoreVersion> findByGameId(int gameId);

  List<HighscoreVersion> findByGameIdOrderByCreatedAtDesc(int gameId);

  Optional<HighscoreVersion> findByCreatedAt(Date createdAt);

}

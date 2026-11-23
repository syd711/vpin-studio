package de.mephisto.vpin.server.achievements.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AchievementSessionRepository extends JpaRepository<AchievementSession, Long> {

  List<AchievementSession> findByStartedAtAfter(Instant since);

  List<AchievementSession> findByRom(String rom);
}

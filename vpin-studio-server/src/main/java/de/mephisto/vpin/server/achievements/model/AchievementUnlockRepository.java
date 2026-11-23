package de.mephisto.vpin.server.achievements.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementUnlockRepository extends JpaRepository<AchievementUnlock, Long> {

  List<AchievementUnlock> findByPlayerId(long playerId);

  List<AchievementUnlock> findByPlayerIdAndRom(long playerId, String rom);

  List<AchievementUnlock> findByPlayerIdAndSyncedAtIsNull(long playerId);

  void deleteByPlayerId(long playerId);
}

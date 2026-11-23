package de.mephisto.vpin.server.achievements.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AchievementPlayerStateRepository extends JpaRepository<AchievementPlayerState, Long> {
}

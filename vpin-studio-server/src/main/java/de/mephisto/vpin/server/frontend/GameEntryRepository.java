package de.mephisto.vpin.server.frontend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameEntryRepository extends JpaRepository<GameEntry, Integer> {
}

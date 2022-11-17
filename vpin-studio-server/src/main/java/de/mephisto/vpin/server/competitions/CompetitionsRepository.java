package de.mephisto.vpin.server.competitions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompetitionsRepository extends JpaRepository<Competition, Long> {

  @Query(value = "SELECT * FROM Competitions c WHERE c.active = \"true\" ORDER BY updatedAt LIMIT 1", nativeQuery = true)
  List<Competition> findActiveCompetitions();
}

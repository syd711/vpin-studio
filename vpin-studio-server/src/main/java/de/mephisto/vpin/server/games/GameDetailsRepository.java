package de.mephisto.vpin.server.games;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameDetailsRepository extends JpaRepository<GameDetails, Long> {

  GameDetails findByPupId(int pupId);

  @Query(value = "SELECT * FROM GameDetails g WHERE g.pupId = ?1 ORDER BY createdAt DESC", nativeQuery = true)
  List<GameDetails> findAllByPupId(int pupId);

  List<GameDetails> findByRomName(String rom);

  @Query(value = "SELECT g.pupId FROM GameDetails g WHERE g.pupId IS NOT NULL AND g.pupId > 0", nativeQuery = true)
  List<Integer> findAllPupIds();

  @Modifying
  @Query(value = "DELETE FROM GameDetails WHERE pupId IN (:ids)", nativeQuery = true)
  void deleteByPupId(@Param("ids") List<Long> ids);
}

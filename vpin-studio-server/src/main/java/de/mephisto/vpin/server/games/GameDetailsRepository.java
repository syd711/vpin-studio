package de.mephisto.vpin.server.games;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameDetailsRepository extends JpaRepository<GameDetails, Long> {

  GameDetails findByPupId(int pupId);

  List<GameDetails> findByRomName(String rom);

}

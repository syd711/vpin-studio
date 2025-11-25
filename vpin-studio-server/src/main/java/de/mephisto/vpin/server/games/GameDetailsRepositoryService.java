package de.mephisto.vpin.server.games;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class GameDetailsRepositoryService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private GameDetailsRepository gameDetailsRepository;


  public GameDetails findByPupId(int pupId) {
    return gameDetailsRepository.findByPupId(pupId);
  }

  public List<GameDetails> findByRomName(String rom) {
    return gameDetailsRepository.findByRomName(rom);
  }

  public void saveAndFlush(@NonNull GameDetails gameDetails) {
    gameDetailsRepository.saveAndFlush(gameDetails);
  }

  public void delete(@NonNull GameDetails byPupId) {
    gameDetailsRepository.delete(byPupId);
  }

  public List<GameDetails> findAll() {
    return gameDetailsRepository.findAll();
  }

  public void deleteAll() {
    gameDetailsRepository.deleteAll();
  }
}

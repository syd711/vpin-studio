package de.mephisto.vpin.server.games;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class GameDetailsRepositoryService {
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private GameDetailsRepository gameDetailsRepository;

  public GameDetails findByPupId(int pupId) {
    try {
      return gameDetailsRepository.findByPupId(pupId);
    }
    catch (IncorrectResultSizeDataAccessException e) {
      //this is a workaround for the missing non-uniqueness in the GameDetails entity.
      LOG.error("Failed to fetch game by PUP id: {}", e.getMessage());
      List<GameDetails> allByPupId = gameDetailsRepository.findAllByPupId(pupId);
      if (!allByPupId.isEmpty()) {
        delete(allByPupId.get(0));
        LOG.warn("Deleted duplicate GameDetails entry PUP id: {}", pupId);
        return findByPupId(pupId);
      }
    }
    return null;
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

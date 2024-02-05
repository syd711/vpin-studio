package de.mephisto.vpin.server.highscores.monitoring;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.HighscoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HighscoreMonitoringService {

  @Autowired
  private HighscoreService highscoreService;

  public void startMonitoring(Game game) {

  }

  public void stopMonitoring() {

  }
}

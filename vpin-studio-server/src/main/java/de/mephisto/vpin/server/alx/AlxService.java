package de.mephisto.vpin.server.alx;

import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.server.highscores.HighscoreVersion;
import de.mephisto.vpin.server.highscores.HighscoreVersionRepository;
import de.mephisto.vpin.server.popper.PinUPConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlxService {

  @Autowired
  private PinUPConnector pinUPConnector;

  @Autowired
  private HighscoreVersionRepository highscoreVersionRepository;

  public List<TableAlxEntry> getAlxEntries() {
    List<TableAlxEntry> alxData = pinUPConnector.getAlxData();
    for (TableAlxEntry entry : alxData) {
      List<HighscoreVersion> byGameId = highscoreVersionRepository.findByGameId(entry.getGameId());
      entry.setScores(byGameId.size());
    }
    return alxData;
  }
}

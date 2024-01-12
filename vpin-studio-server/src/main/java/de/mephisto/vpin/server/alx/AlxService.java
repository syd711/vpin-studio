package de.mephisto.vpin.server.alx;

import de.mephisto.vpin.restclient.alx.AlxSummary;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.server.highscores.HighscoreVersion;
import de.mephisto.vpin.server.highscores.HighscoreVersionRepository;
import de.mephisto.vpin.server.popper.PinUPConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlxService {

  @Autowired
  private PinUPConnector pinUPConnector;

  @Autowired
  private HighscoreVersionRepository highscoreVersionRepository;

  public AlxSummary getAlxSummary(int gameId) {
    AlxSummary summary = new AlxSummary();

    summary.setStartDate(pinUPConnector.getStartDate());

    List<TableAlxEntry> alxData = new ArrayList<>();
    if (gameId > 0) {
      alxData = pinUPConnector.getAlxData(gameId);
    } else {
      alxData = pinUPConnector.getAlxData();
    }

    for (TableAlxEntry entry : alxData) {
      List<HighscoreVersion> byGameId = highscoreVersionRepository.findByGameId(entry.getGameId());
      List<HighscoreVersion> collect = byGameId.stream().filter(score -> score.getChangedPosition() > 0).collect(Collectors.toList());
      List<HighscoreVersion> highscores = byGameId.stream().filter(score -> score.getChangedPosition() == 1).collect(Collectors.toList());
      entry.setScores(collect.size());
      entry.setHighscores(highscores.size());

      summary.getEntries().add(entry);
    }
    return summary;
  }

  public AlxSummary getAlxSummary() {
    return getAlxSummary(-1);
  }
}

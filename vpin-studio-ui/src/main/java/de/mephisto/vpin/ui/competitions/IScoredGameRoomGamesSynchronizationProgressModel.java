package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScoredGame;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class IScoredGameRoomGamesSynchronizationProgressModel extends ProgressModel<IScoredGame> {
  private final static Logger LOG = LoggerFactory.getLogger(IScoredGameRoomGamesSynchronizationProgressModel.class);

  private final Iterator<IScoredGame> iterator;
  private GameRoom gameRoom;
  private final List<IScoredGame> games;

  public IScoredGameRoomGamesSynchronizationProgressModel(@NonNull GameRoom gameRoom) {
    super("iScored Synchronization");
    this.gameRoom = gameRoom;
    this.games = gameRoom.getGames();
    this.iterator = games.iterator();
  }

  public IScoredGameRoomGamesSynchronizationProgressModel(GameRoom gameRoom, IScoredGame game) {
    super("iScored Synchronization");
    this.gameRoom = gameRoom;
    this.games = Arrays.asList(game);
    this.iterator = games.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public IScoredGame getNext() {
    return iterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return games.size() == 1;
  }

  @Override
  public String nextToString(IScoredGame c) {
    return "Synchronizing \"" + c.getName() + "\"";
  }

  @Override
  public int getMax() {
    return games.size();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, IScoredGame next) {
    try {
      CompetitionRepresentation competitionRepresentations = client.getCompetitionService().synchronizeIScoredGameRoomGame(gameRoom, next);
      progressResultModel.getResults().add(competitionRepresentations);
    }
    catch (Exception e) {
      LOG.error("Failed to sync competitions data: " + e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "iScored Synchronization Failed", "Result: " + e.getMessage());
      });
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}

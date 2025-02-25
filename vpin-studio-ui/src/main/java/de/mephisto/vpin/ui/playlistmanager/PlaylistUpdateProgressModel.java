package de.mephisto.vpin.ui.playlistmanager;

import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.playlists.PlaylistRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class PlaylistUpdateProgressModel extends ProgressModel<GameRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(PlaylistUpdateProgressModel.class);
  private final PlaylistRepresentation playlist;
  private final List<GameRepresentation> games;

  private final Iterator<GameRepresentation> gameIterator;
  private final boolean add;
  private int count = 0;

  public PlaylistUpdateProgressModel(String title, PlaylistRepresentation playlist, List<GameRepresentation> games, boolean add) {
    super(title);
    this.playlist = playlist;
    this.games = games;
    this.gameIterator = games.iterator();
    this.add = add;
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return games.size();
  }

  @Override
  public boolean isIndeterminate() {
    return games.size() == 1;
  }

  @Override
  public boolean hasNext() {
    return this.gameIterator.hasNext();
  }

  @Override
  public GameRepresentation getNext() {
    return gameIterator.next();
  }

  @Override
  public String nextToString(GameRepresentation game) {
    if (add) {
      return "Adding \"" + game.getGameDisplayName() + "\"";
    }
    return "Removing \"" + game.getGameDisplayName() + "\"";
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    super.finalizeModel(progressResultModel);

    if (count > UIDefaults.DEFAULT_MAX_REFRESH_COUNT) {
      EventManager.getInstance().notifyTablesChanged();
    }
    else {
      for (GameRepresentation game : games) {
        EventManager.getInstance().notifyTableChange(game.getId(), null);
      }
    }
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, GameRepresentation game) {
    try {
      if (add) {
        Studio.client.getPlaylistsService().addToPlaylist(playlist, game, false, false);
      }
      else {
        Studio.client.getPlaylistsService().removeFromPlaylist(playlist, game);
      }
      count++;
    }
    catch (Exception e) {
      LOG.error("Error updating playlist: {}", e.getMessage(), e);
    }
  }
}

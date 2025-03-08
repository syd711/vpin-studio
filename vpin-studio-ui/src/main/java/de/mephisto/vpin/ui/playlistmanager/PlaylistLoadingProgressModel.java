package de.mephisto.vpin.ui.playlistmanager;

import de.mephisto.vpin.restclient.frontend.PlaylistGame;
import de.mephisto.vpin.restclient.playlists.PlaylistRepresentation;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class PlaylistLoadingProgressModel extends ProgressModel<PlaylistGame> {
  private final static Logger LOG = LoggerFactory.getLogger(PlaylistLoadingProgressModel.class);
  private final List<PlaylistGame> games;

  private final Iterator<PlaylistGame> gameIterator;
  private int count = 0;

  public PlaylistLoadingProgressModel(PlaylistRepresentation playlist) {
    super("Loading Games for \"" + playlist.getName() + "\"");
    this.games = playlist.getGames();
    this.gameIterator = games.iterator();
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
  public PlaylistGame getNext() {
    return gameIterator.next();
  }

  @Override
  public String nextToString(PlaylistGame game) {
    return null;
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, PlaylistGame game) {
    try {
      count++;
      client.getGameService().getGameCached(game.getId());
    }
    catch (Exception e) {
      LOG.error("Error fetching game: {}", e.getMessage(), e);
    }
  }
}

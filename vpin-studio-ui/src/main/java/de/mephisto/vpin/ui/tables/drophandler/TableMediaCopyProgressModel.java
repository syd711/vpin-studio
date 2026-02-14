package de.mephisto.vpin.ui.tables.drophandler;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.playlists.PlaylistRepresentation;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class TableMediaCopyProgressModel extends ProgressModel<FrontendMediaItemRepresentation> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final int objectId;
  private final boolean playlistMode;
  private final Iterator<FrontendMediaItemRepresentation> iterator;
  private final VPinScreen targetScreen;
  private final List<FrontendMediaItemRepresentation> items = new ArrayList<>();

  public TableMediaCopyProgressModel(GameRepresentation game, VPinScreen targetScreen, FrontendMediaItemRepresentation mediaItem) {
    super("Copying " + mediaItem.getName());
    this.targetScreen = targetScreen;
    this.items.add(mediaItem);
    this.iterator = this.items.iterator();
    this.objectId = game.getId();
    this.playlistMode = false;
  }

  public TableMediaCopyProgressModel(PlaylistRepresentation playlist, VPinScreen targetScreen, FrontendMediaItemRepresentation mediaItem) {
    super("Copying " + mediaItem.getName());
    this.targetScreen = targetScreen;
    this.items.add(mediaItem);
    this.iterator = this.items.iterator();
    this.objectId = playlist.getId();
    this.playlistMode = true;
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return this.items.size();
  }

  @Override
  public FrontendMediaItemRepresentation getNext() {
    return iterator.next();
  }

  @Override
  public boolean isIndeterminate() {
    return this.items.size() == 1;
  }

  @Override
  public String nextToString(FrontendMediaItemRepresentation item) {
    return "Copying \"" + item.getName() + "\" to screen \"" + targetScreen.name() + "\"";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, FrontendMediaItemRepresentation media) {
    try {
      client.getGameMediaService().copyMedia(objectId, playlistMode, media.getScreen(), media.getName(), targetScreen);
    }
    catch (Exception e) {
      LOG.error("Copy failed: " + e.getMessage(), e);
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }
}

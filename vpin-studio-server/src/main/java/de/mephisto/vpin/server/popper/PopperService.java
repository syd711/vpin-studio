package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.restclient.PinUPControl;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.system.SystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PopperService {
  private final static Logger LOG = LoggerFactory.getLogger(PopperService.class);

  private final List<TableStatusChangeListener> listeners = new ArrayList<>();
  private final List<PopperLaunchListener> launchListeners = new ArrayList<>();

  @Autowired
  private SystemService systemService;

  @Autowired
  private PinUPConnector pinUPConnector;

  public PinUPControl getPinUPControlFor(PopperScreen screen) {
    return pinUPConnector.getPinUPControlFor(screen);
  }

  public List<Playlist> getPlaylists() {
    return pinUPConnector.getPlayLists();
  }

  public int importVPXGame(File file, boolean importToPopper, int playListId) {
    if(importToPopper) {
      int gameId = pinUPConnector.importGame(file);
      if(gameId >= 0 && playListId >= 0) {
        pinUPConnector.addToPlaylist(gameId, playListId);
      }
      return gameId;
    }
    return -1;
  }

  public void notifyTableStatusChange(final Game game, final boolean started) {
    TableStatusChangedEvent event = () -> game;
    for (TableStatusChangeListener listener : this.listeners) {
      if (started) {
        listener.tableLaunched(event);
      }
      else {
        listener.tableExited(event);
      }
    }
  }

  public boolean isPinUPRunning() {
    Optional<ProcessHandle> pinUP = ProcessHandle.allProcesses().filter(p -> p.info().command().isPresent() && p.info().command().get().contains("PinUP")).findFirst();
    return pinUP.isPresent();
  }

  public boolean terminate() {
    return systemService.killPopper();
  }

  @SuppressWarnings("unused")
  public void addPopperLaunchListener(PopperLaunchListener listener) {
    this.launchListeners.add(listener);
  }

  @SuppressWarnings("unused")
  public void addTableStatusChangeListener(TableStatusChangeListener listener) {
    this.listeners.add(listener);
  }

  @SuppressWarnings("unused")
  public void removeTableStatusChangeListener(TableStatusChangeListener listener) {
    this.listeners.remove(listener);
  }

  public void notifyPopperLaunch() {
    for (PopperLaunchListener launchListener : launchListeners) {
      launchListener.popperLaunched();
    }
  }

  public void augmentWheel(Game game, String badge) {
    GameMediaItem gameMediaItem = game.getGameMedia().get(PopperScreen.Wheel);
    if(gameMediaItem != null) {
      File wheelIcon = gameMediaItem.getFile();
      WheelAugmenter augmenter = new WheelAugmenter(wheelIcon);

      File badgeFile = systemService.getBagdeFile(badge);
      if(badgeFile.exists()) {
        augmenter.augment(badgeFile);
      }
    }
  }

  public void deAugmentWheel(Game game) {
    GameMediaItem gameMediaItem = game.getGameMedia().get(PopperScreen.Wheel);
    if(gameMediaItem != null) {
      File wheelIcon = gameMediaItem.getFile();
      WheelAugmenter augmenter = new WheelAugmenter(wheelIcon);
      augmenter.deAugment();
    }
  }
}

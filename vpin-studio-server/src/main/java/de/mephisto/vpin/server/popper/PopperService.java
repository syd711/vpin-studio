package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.commons.EmulatorType;
import de.mephisto.vpin.restclient.TableManagerSettings;
import de.mephisto.vpin.restclient.PinUPControl;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PopperService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(PopperService.class);

  private final List<PopperStatusChangeListener> listeners = new ArrayList<>();

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

  @SuppressWarnings("unused")
  public void addPopperStatusChangeListener(PopperStatusChangeListener listener) {
    this.listeners.add(listener);
  }

  public int importVPXGame(File file, boolean importToPopper, int playListId) {
    if (importToPopper) {
      int gameId = pinUPConnector.importGame(file);
      if (gameId >= 0 && playListId >= 0) {
        pinUPConnector.addToPlaylist(gameId, playListId);
      }
      return gameId;
    }
    return -1;
  }

  public void notifyTableStatusChange(final Game game, final boolean started) {
    TableStatusChangedEvent event = () -> game;
    for (PopperStatusChangeListener listener : this.listeners) {
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
    boolean b = systemService.killPopper();
    for (PopperStatusChangeListener listener : listeners) {
      listener.popperExited();
    }
    return b;
  }

  public void notifyPopperLaunch() {
    for (PopperStatusChangeListener listener : listeners) {
      listener.popperLaunched();
    }
  }

  public void notifyPopperRestart() {
    for (PopperStatusChangeListener listener : listeners) {
      listener.popperRestarted();
    }
  }

  public void notifyPopperExit() {
    for (PopperStatusChangeListener listener : listeners) {
      listener.popperExited();
    }
  }

  public void augmentWheel(Game game, String badge) {
    GameMediaItem gameMediaItem = game.getGameMedia().get(PopperScreen.Wheel);
    if (gameMediaItem != null) {
      File wheelIcon = gameMediaItem.getFile();
      WheelAugmenter augmenter = new WheelAugmenter(wheelIcon);

      File badgeFile = systemService.getBagdeFile(badge);
      if (badgeFile.exists()) {
        augmenter.augment(badgeFile);
      }
    }
  }

  public void deAugmentWheel(Game game) {
    GameMediaItem gameMediaItem = game.getGameMedia().get(PopperScreen.Wheel);
    if (gameMediaItem != null) {
      File wheelIcon = gameMediaItem.getFile();
      WheelAugmenter augmenter = new WheelAugmenter(wheelIcon);
      augmenter.deAugment();
    }
  }

  public boolean saveArchiveManager(TableManagerSettings archiveManagerDescriptor) {
    if (archiveManagerDescriptor.getPlaylistId() != -1) {
      pinUPConnector.enablePCGameEmulator();
      File file = systemService.getVPinStudioMenuExe();
      int newGameId = pinUPConnector.importGame(EmulatorType.PC_GAMES, SystemService.VPIN_STUDIO_MENU_NAME, file.getAbsolutePath(),
          SystemService.VPIN_STUDIO_MENU_NAME, SystemService.VPIN_STUDIO_MENU_NAME);
      pinUPConnector.addToPlaylist(newGameId, archiveManagerDescriptor.getPlaylistId());
    }
    else {
      File file = systemService.getVPinStudioMenuExe();
      pinUPConnector.deleteGame(file.getAbsolutePath());
    }
    return true;
  }

  @NonNull
  public TableManagerSettings getArchiveManagerDescriptor() {
    TableManagerSettings descriptor = new TableManagerSettings();
    File file = systemService.getVPinStudioMenuExe();
    Game game = pinUPConnector.getGameByFilename(file.getAbsolutePath());
    if (game != null) {
      descriptor.setPlaylistId(pinUPConnector.getPlayListForGame(game.getId()).getId());
    }
    return descriptor;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Thread shutdownHook = new Thread(this::notifyPopperExit);
    Runtime.getRuntime().addShutdownHook(shutdownHook);
  }
}

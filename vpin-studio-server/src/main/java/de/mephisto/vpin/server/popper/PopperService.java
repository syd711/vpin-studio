package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.commons.EmulatorType;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.restclient.*;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
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

  public PinUPControls getPinUPControls() {
    return pinUPConnector.getControls();
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

  public TableDetails getTableDetails(int gameId) {
    return pinUPConnector.getTableDetails(gameId);
  }

  public TableDetails saveTableDetails(TableDetails tableDetails, int gameId) {
    Game game = pinUPConnector.getGame(gameId);
    pinUPConnector.saveTableDetails(game, tableDetails);
    return tableDetails;
  }

  public boolean restart() {
    systemService.restartPopper();
    return true;
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
      int newGameId = pinUPConnector.importGame(EmulatorType.PC_GAMES, UIDefaults.MANAGER_TITLE, file.getAbsolutePath(),
          UIDefaults.MANAGER_TITLE, UIDefaults.MANAGER_TITLE);
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

  public void cloneGameMedia(Game original, Game clone) {
    PopperScreen[] values = PopperScreen.values();
    for (PopperScreen originalScreenValue : values) {
      try {
        GameMediaItem gameMediaItem = original.getGameMedia().get(originalScreenValue);
        if (gameMediaItem != null && gameMediaItem.getFile().exists()) {
          File mediaFile = gameMediaItem.getFile();
          String suffix = FilenameUtils.getExtension(mediaFile.getName());
          String name = FilenameUtils.getBaseName(clone.getGameFileName());

          File cloneTarget = new File(clone.getPinUPMediaFolder(originalScreenValue), name + "." + suffix);
          if (cloneTarget.exists()) {
            cloneTarget.delete();
          }

          FileUtils.copyFile(mediaFile, cloneTarget);
          LOG.info("Cloned PinUP Popper media: " + cloneTarget.getAbsolutePath());
        }
      } catch (IOException e) {
        LOG.info("Failed to clone popper media: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Thread shutdownHook = new Thread(this::notifyPopperExit);
    Runtime.getRuntime().addShutdownHook(shutdownHook);
  }
}

package de.mephisto.vpin.ui.dropins;

import de.mephisto.vpin.commons.utils.TransitionUtil;
import de.mephisto.vpin.commons.utils.localsettings.LocalSettingsChangeListener;
import de.mephisto.vpin.commons.utils.localsettings.LocalUISettings;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.tables.UploadAnalysisDispatcher;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.List;

public class DropInManager implements LocalSettingsChangeListener, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(DropInManager.class);

  private static DropInManager instance;

  private MenuButton dropInsBtn;

  private File dropinsFolder;
  private DropInMonitoringThread dropinsMonitor;

  private DropInManager() {
    LocalUISettings.addListener(this);
  }

  private GameRepresentation gameSelection;

  public static DropInManager getInstance() {
    if (instance == null) {
      instance = new DropInManager();
    }
    return instance;
  }

  public void init(MenuButton dropInsBtn) {
    this.dropInsBtn = dropInsBtn;
    this.dropInsBtn.getGraphic().setVisible(false);
    this.dropInsBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        dropInsBtn.getGraphic().setVisible(false);
      }
    });

    String dropInPath = LocalUISettings.getString(LocalUISettings.DROP_IN_FOLDER);
    if (dropInPath != null) {
      this.dropinsFolder = new File(dropInPath);
    }
    dropinsMonitor = new DropInMonitoringThread();
    dropinsMonitor.setDropInFolder(dropinsFolder);
    dropinsMonitor.startMonitoring();

    this.reload();

    EventManager.getInstance().addListener(this);
  }

  public void reload() {
    this.dropInsBtn.getItems().clear();

    if (dropinsFolder != null && dropinsFolder.exists() && dropinsFolder.isDirectory()) {
      File[] files = dropinsFolder.listFiles(pathname -> pathname.isFile());
      if (files != null) {
        for (File file : files) {
          try {
            FXMLLoader loader = new FXMLLoader(DropInContainerController.class.getResource("dropin-container.fxml"));
            BorderPane root = loader.load();
            root.getStyleClass().add("dropin-menu-item");
            DropInContainerController containerController = loader.getController();
            containerController.setData(dropInsBtn, file);
            CustomMenuItem item = new CustomMenuItem();
            item.setContent(root);
            dropInsBtn.getItems().add(item);
          }
          catch (IOException e) {
            LOG.error("Failed to load drop in container: " + e.getMessage(), e);
          }
        }
      }
    }
  }


  @Override
  public void localSettingsChanged(@NotNull String key, @Nullable String value) {
    if (key.equals(LocalUISettings.DROP_IN_FOLDER_ENABLED)) {
      if (value != null) {
        boolean enabled = Boolean.parseBoolean(value);
        LOG.info("Drop-in monitoring enabled: " + enabled);
        Platform.runLater(() -> {
          dropInsBtn.setVisible(enabled);
        });

        if (enabled) {
          dropinsMonitor.startMonitoring();
        }
        else {
          dropinsMonitor.stopMonitoring();
        }
      }
    }
    else if (key.equals(LocalUISettings.DROP_IN_FOLDER)) {
      if (value != null) {
        File dropFolder = new File(value);
        this.dropinsFolder = dropFolder;

        dropinsMonitor.setDropInFolder(dropFolder);
        reload();

        if (dropFolder.exists()) {
          LOG.info("Changed drop-in folder to \"" + dropFolder.getAbsolutePath() + "\"");
          Platform.runLater(() -> {
            dropInsBtn.setVisible(true);
          });
          return;
        }
      }

      Platform.runLater(() -> {
        dropInsBtn.setVisible(false);
      });
    }
  }

  @Override
  public void tablesSelected(List<GameRepresentation> games) {
    this.gameSelection = null;
    if (!games.isEmpty()) {
      this.gameSelection = games.get(0);
    }
  }

  public void notifyDropInUpdates(WatchEvent.Kind<Path> entry) {
    reload();
    boolean isNew = entry.equals(StandardWatchEventKinds.ENTRY_CREATE);
    dropInsBtn.getGraphic().setVisible(isNew);
  }

  public void install(File file) {
    if (gameSelection != null) {
      UploadAnalysisDispatcher.dispatch(file, gameSelection);
    }
  }
}

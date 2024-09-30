package de.mephisto.vpin.ui.dropins;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.FolderChangeListener;
import de.mephisto.vpin.commons.utils.FolderMonitoringThread;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.localsettings.LocalSettingsChangeListener;
import de.mephisto.vpin.commons.utils.localsettings.LocalUISettings;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.tables.UploadAnalysisDispatcher;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

import org.controlsfx.control.Notifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DropInManager implements LocalSettingsChangeListener, StudioEventListener, FolderChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(DropInManager.class);

  private static DropInManager instance;

  private MenuButton dropInsBtn;

  private File dropinsFolder;
  private FolderMonitoringThread dropinsMonitor;

  private GameRepresentation gameSelection;

  public static DropInManager getInstance() {
    if (instance == null) {
      instance = new DropInManager();
    }
    return instance;
  }

  private DropInManager() {
    // initialize monitoring thread once here
    String dropInPath = LocalUISettings.getString(LocalUISettings.DROP_IN_FOLDER);
    if (dropInPath != null) {
      this.dropinsFolder = new File(dropInPath);
    }
    dropinsMonitor = new FolderMonitoringThread(this, false);
    dropinsMonitor.setFolder(dropinsFolder);
    dropinsMonitor.startMonitoring();
  }

  public void init(MenuButton dropInsBtn) {
    // monitor changes
    LocalUISettings.addListener(this);
    EventManager.getInstance().addListener(this);

    this.dropInsBtn = dropInsBtn;
    this.dropInsBtn.getGraphic().setVisible(false);
    this.dropInsBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        dropInsBtn.getGraphic().setVisible(false);
      }
    });
    this.reload();
    localSettingsChanged(LocalUISettings.DROP_IN_FOLDER_ENABLED, LocalUISettings.getString(LocalUISettings.DROP_IN_FOLDER_ENABLED));
  }

  /**
   * Should run on JavaFX Thread !
   */
  private void reload() {
    this.dropInsBtn.getItems().clear();

    if (dropinsFolder != null && dropinsFolder.exists() && dropinsFolder.isDirectory()) {
      File[] files = dropinsFolder.listFiles(pathname -> pathname.isFile());
      if (files != null) {
        for (File file : files) {
          if (!FileUtils.isTempFile(file)) {
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

        dropinsMonitor.setFolder(dropFolder);
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

  public void install(File file) {
    if (gameSelection != null) {
      UploadAnalysisDispatcher.dispatch(file, gameSelection);
    }
  }

  @Override
  public void notifyFolderChange(@NonNull File folder, @Nullable File file) {
    if (FileUtils.isTempFile(file)) {
      return;
    }

    Platform.runLater(() -> {
      reload();
      if (file != null) {
        dropInsBtn.getGraphic().setVisible(true);
        Notifications.create()
            .title("New Drop-In Detected!")
            .text(file.getAbsolutePath())
            .graphic(WidgetFactory.createCheckboxIcon())
            .showInformation();
      }
    });
  }
}

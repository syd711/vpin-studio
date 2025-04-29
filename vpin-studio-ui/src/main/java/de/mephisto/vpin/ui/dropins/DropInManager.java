package de.mephisto.vpin.ui.dropins;

import de.mephisto.vpin.commons.utils.FolderChangeListener;
import de.mephisto.vpin.commons.utils.FolderMonitoringThread;
import de.mephisto.vpin.commons.utils.localsettings.LocalSettingsChangeListener;
import de.mephisto.vpin.commons.utils.localsettings.LocalUISettings;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.tables.UploadAnalysisDispatcher;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class DropInManager implements LocalSettingsChangeListener, StudioEventListener, FolderChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(DropInManager.class);

  private static DropInManager instance;

  private MenuButton dropInsBtn;

  private File dropinsFolder;
  private FolderMonitoringThread dropinsMonitor;

  private GameRepresentation gameSelection;
  private Stream<Path> stream;
  private int itemCount;
  private int itemLimit = 50;

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
    dropinsMonitor = new FolderMonitoringThread(this, false, true);
    dropinsMonitor.setFolder(dropinsFolder);
    dropinsMonitor.startMonitoring();
  }

  public void init(MenuButton dropInsBtn) {
    // monitor changes
    LocalUISettings.addListener(this);
    EventManager.getInstance().addListener(this);

    this.dropInsBtn = dropInsBtn;
    this.dropInsBtn.setVisible(false);
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
  public void reload() {
    this.dropInsBtn.getItems().clear();

    itemCount = 0;
    itemLimit = 100;

    //TODO skipped a "Load More Item..." button for now and simply limit the items
    itemCount = 0;
    itemLimit = 100;

    //TODO skipped a "Load More Item..." button for now and simply limit the items
    if (dropinsFolder != null && dropinsFolder.exists() && dropinsFolder.isDirectory()) {
      continueReading();
    }
  }

  private void continueReading() {
    try (Stream<Path> stream = Files.walk(dropinsFolder.toPath())) {
      stream.filter((p) -> Files.isRegularFile(p) && !FileUtils.isTempFile(p.toFile()))
          .forEach((p) -> {
            if (itemCount > itemLimit) {
              return;
            }

            List<MenuItem> items = dropInsBtn.getItems();
            for (MenuItem item : items) {
              if (item.getUserData().equals(p.toFile())) {
                return;
              }
            }
            try {
              FXMLLoader loader = new FXMLLoader(DropInContainerController.class.getResource("dropin-container.fxml"));
              BorderPane root = loader.load();
              root.getStyleClass().add("dropin-menu-item");
              DropInContainerController containerController = loader.getController();
              containerController.setData(dropInsBtn, p.toFile());
              CustomMenuItem item = new CustomMenuItem();
              item.setUserData(p.toFile());
              item.setContent(root);
              dropInsBtn.getItems().add(item);
              itemCount++;
            }
            catch (IOException e) {
              LOG.error("Failed to load drop in container: " + e.getMessage(), e);
            }
          });
    }
    catch (IOException e) {
      LOG.error("Failed to walk through drop in container: " + e.getMessage(), e);
    }
  }

  @Override
  public void localSettingsChanged(@NonNull String key, @Nullable String value) {
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
    UploadAnalysisDispatcher.dispatch(file, gameSelection, () -> {
      //TODO move to installed folder
    });
  }

  @Override
  public void notifyFolderChange(@NonNull File folder, @Nullable File file) {
    Platform.runLater(() -> {
      reload();
      dropInsBtn.getGraphic().setVisible(true);
    });
  }
}

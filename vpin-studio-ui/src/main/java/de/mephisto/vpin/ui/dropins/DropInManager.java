package de.mephisto.vpin.ui.dropins;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.*;
import de.mephisto.vpin.commons.utils.localsettings.LocalSettingsChangeListener;
import de.mephisto.vpin.commons.utils.localsettings.LocalUISettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.tables.UploadAnalysisDispatcher;
import de.mephisto.vpin.ui.util.StudioFolderChooser;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.mephisto.vpin.ui.Studio.client;

public class DropInManager implements LocalSettingsChangeListener, StudioEventListener, FolderChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(DropInManager.class);
  public Debouncer searchDebouncer = new Debouncer();

  private static DropInManager instance;

  private MenuButton dropInsBtn;

  private File dropinsFolder;
  private FolderMonitoringThread dropinsMonitor;

  private GameRepresentation gameSelection;
  private Stream<Path> stream;
  private int itemCount;
  private int itemLimit = 50;

  private boolean enabled = false;
  private TextField filter = new TextField();

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

    dropInsBtn.getStyleClass().add("base-component");

    filter.setPrefWidth(200);
    filter.setStyle("-fx-font-size: 14px;");
    filter.setPromptText("Filter for assets...");
    HBox.setMargin(filter, new Insets(0, 0, 3, 3));
    filter.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        searchDebouncer.debounce("dropinsearch", () -> {
          Platform.runLater(() -> {
            reload();
            Platform.runLater(() -> {
              filter.requestFocus();
            });
          });
        }, 100);
      }
    });

    Button resetBtn = new Button();
    HBox.setMargin(resetBtn, new Insets(0, 3, 3, 0));
    resetBtn.getStyleClass().add("ghost-icon-button");
    resetBtn.setTextFill(Paint.valueOf("#FFFFFF"));
    resetBtn.setGraphic(WidgetFactory.createIcon("mdi2c-close-thick", "#FFFFFF"));
    resetBtn.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        filter.setText("");
        event.consume();
      }
    });

    this.dropInsBtn = dropInsBtn;
    this.dropInsBtn.setVisible(false);
    this.dropInsBtn.getGraphic().setVisible(false);
    this.dropInsBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        dropInsBtn.getGraphic().setVisible(false);
      }
    });

    HBox box = new HBox(3);
    box.setAlignment(Pos.CENTER_RIGHT);
    Button btn = new Button("Delete All");
    HBox.setMargin(btn, new Insets(0, 3, 3, 3));
    btn.getStyleClass().add("default-text");
    btn.setTextFill(Paint.valueOf("#ff3333"));
    btn.setGraphic(WidgetFactory.createIcon("mdi2d-delete-outline", "#ff3333"));
    btn.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete all drop in files?", "All files will be moved to the trash.");
        if (result.isPresent() && result.get().equals(ButtonType.OK)) {
          List<MenuItem> items = dropInsBtn.getItems();
          for (MenuItem item : items) {
            File f = (File) item.getUserData();
            if (!TrashBin.moveTo(f)) {
              WidgetFactory.showAlert(Studio.stage, "Error", "Deletion failed, another process is blocking this file.");
            }
          }
        }
        reload();
      }
    });
    box.getChildren().add(btn);
    box.getChildren().add(new Separator(Orientation.VERTICAL));
    box.getChildren().add(filter);
    box.getChildren().add(resetBtn);

    CustomMenuItem item = new CustomMenuItem();
    item.setUserData("menu");
    item.setContent(box);
    this.dropInsBtn.getItems().addFirst(item);

    this.reload();
    localSettingsChanged(LocalUISettings.DROP_IN_FOLDER_ENABLED, LocalUISettings.getString(LocalUISettings.DROP_IN_FOLDER_ENABLED));
  }

  public boolean isDropInFile(@NonNull File file) {
    if (enabled && dropinsFolder != null && dropinsFolder.exists()) {
      String path = dropinsFolder.getAbsolutePath();
      return file.getAbsolutePath().startsWith(path);
    }
    return false;
  }

  /**
   * Should run on JavaFX Thread !
   */
  public void reload() {
    for (MenuItem item : new ArrayList<>(this.dropInsBtn.getItems())) {
      if (item.getUserData() != null && item.getUserData().equals("menu")) {
        continue;
      }
      this.dropInsBtn.getItems().remove(item);
    }

    itemCount = 0;
    itemLimit = 100;

    //TODO skipped a "Load More Item..." button for now and simply limit the items
    if (dropinsFolder != null && dropinsFolder.exists() && dropinsFolder.isDirectory()) {
      continueReading();
    }
  }

  private void continueReading() {
    try (Stream<Path> stream = Files.walk(dropinsFolder.toPath())) {
      stream.filter((p) -> Files.isRegularFile(p) && !p.toFile().isHidden() && !FileUtils.isTempFile(p.toFile()))
          .forEach((p) -> {
            if (itemCount > itemLimit) {
              return;
            }

            String filterText = filter.getText();
            if (!StringUtils.isEmpty(filterText)) {
              if (!p.toFile().getName().toLowerCase().contains(filterText.toLowerCase())) {
                return;
              }
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
        enabled = Boolean.parseBoolean(value);
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
      this.gameSelection = games.getFirst();
    }
  }

  public void install(File file) {
    UploadAnalysisDispatcher.dispatch(file, gameSelection, () -> {

      JFXFuture
          .supplyAsync(() -> client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class))
          .thenAcceptLater(uiSettings -> {
            int postAction = uiSettings.getDropinPostAction();
            switch (postAction) {
              case UISettings.DROP_IN_POSTACTION_DONOTHING:
                break;

              case UISettings.DROP_IN_POSTACTION_MOVETOFOLDER:
                moveFile(file, new File(uiSettings.getDropinPostTargetFolder()), null);
                break;

              case UISettings.DROP_IN_POSTACTION_MOVETOTABLEFOLDER:
                if (gameSelection != null) {
                  moveFile(file, new File(uiSettings.getDropinPostTargetFolder()), gameSelection.getGameDisplayName());
                }
                else {
                  WidgetFactory.showAlert(Studio.stage, "No game selected !", "No game selected so cannot determine target folder !");
                }
                break;

              case UISettings.DROP_IN_POSTACTION_MOVETO:
                StudioFolderChooser chooser = new StudioFolderChooser();
                chooser.setTitle("Select Target Folder");
                File targetFolder = chooser.showOpenDialog(Studio.stage);
                moveFile(file, targetFolder, null);
                break;

              case UISettings.DROP_IN_POSTACTION_MOVETOTRASH:
                Optional<ButtonType> result1 = WidgetFactory.showConfirmation(Studio.stage, "Delete file ?", "Delete \"" + file.getAbsolutePath() + "\"?", "The file will be moved to the trash bin.");
                boolean confirmed1 = result1.isPresent() && result1.get().equals(ButtonType.OK);
                if (confirmed1 && !Desktop.getDesktop().moveToTrash(file)) {
                  WidgetFactory.showAlert(Studio.stage, "Cannot move file to trash !", "The file \"" + file.getAbsolutePath() + "\" couldn't be moved to trash !");
                }
                break;

              case UISettings.DROP_IN_POSTACTION_DELETE:
                Optional<ButtonType> result2 = WidgetFactory.showConfirmation(Studio.stage, "Delete file ?", "Delete file \"" + file.getAbsolutePath() + "\"?", "The file cannot be recovered.");
                boolean confirmed2 = result2.isPresent() && result2.get().equals(ButtonType.OK);
                if (confirmed2 && !file.delete()) {
                  WidgetFactory.showAlert(Studio.stage, "Cannot delete file !", "The file \"" + file.getAbsolutePath() + "\" couldn't be deleted !");
                }
                break;
            }
          });
    });
  }

  private boolean moveFile(File file, File target, String subfolder) {
    if (!target.exists()) {
      WidgetFactory.showAlert(Studio.stage, "Target folder doesn't exist !", "The target folder \"" + target.getAbsolutePath() + "\" doesn't exist !");
      return false;
    }
    if (subfolder != null) {
      target = new File(target, subfolder);
      if (!target.exists() && !target.mkdirs()) {
        WidgetFactory.showAlert(Studio.stage, "Cannot create target table folder !", "The target table folder \"" + target.getAbsolutePath() + "\" couldn't be created !");
        return false;
      }
    }
    File targetFile = new File(target, file.getName());
    if (targetFile.exists()) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Overwrite file ?", "A file with same name \"" + file.getName() + "\" already exists in target folder !", "Do you want to overwrite it ?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        targetFile.delete();
      }
      else {
        return false;
      }
    }
    Platform.runLater(() -> {
      try {
        Files.copy(file.toPath(), targetFile.toPath());
        Files.delete(file.toPath());
      }
      catch (IOException ioe) {
        LOG.error("Cannot move file " + file.getName(), ioe);
        WidgetFactory.showAlert(Studio.stage, "File cannot be copied", "The file \"" + file.getName() + "\" couldn't be copied in target folder !");
      }
    });
    return false;
  }

  @Override
  public void notifyFolderChange(@NonNull File folder, @Nullable File file) {
    Platform.runLater(() -> {
      reload();
      dropInsBtn.getGraphic().setVisible(true);
    });
  }
}

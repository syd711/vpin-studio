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
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
import de.mephisto.vpin.commons.utils.i18n.Messages;

public class DropInManager implements LocalSettingsChangeListener, StudioEventListener, FolderChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(DropInManager.class);
  public Debouncer searchDebouncer = new Debouncer();

  private static DropInManager instance;

  private MenuButton dropInsBtn;

  private File dropinsFolder;
  private FolderMonitoringThread dropinsMonitor;

  private GameRepresentation gameSelection;
  private ListView<File> fileListView;

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

    HBox.setHgrow(filter, Priority.ALWAYS);
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
    this.dropInsBtn.showingProperty().addListener((obs, oldVal, showing) -> {
      if (showing) {
        Platform.runLater(() -> {
          ScrollBar hBar = (ScrollBar) fileListView.lookup(".scroll-bar:horizontal");
          if (hBar != null) {
            hBar.setVisible(false);
            hBar.setManaged(false);
          }
        });
      }
    });
    this.dropInsBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        dropInsBtn.getGraphic().setVisible(false);
      }
    });

    HBox box = new HBox(3);
    box.setAlignment(Pos.CENTER_RIGHT);
    Button btn = new Button("Delete All");
    HBox.setMargin(btn, new Insets(3, 3, 3, 3));
    btn.getStyleClass().add("default-text");
    btn.setTextFill(Paint.valueOf("#ff3333"));
    btn.setGraphic(WidgetFactory.createIcon("mdi2d-delete-outline", "#ff3333"));
    btn.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, Messages.get("dialog.delete_all_drop_in_files"), Messages.get("dialog.all_files_will_be_moved_to_the"));
        if (result.isPresent() && result.get().equals(ButtonType.OK)) {
          for (File f : new ArrayList<>(fileListView.getItems())) {
            if (!TrashBin.moveTo(f)) {
              WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("dialog.deletion_failed_another_process_is_blocking_this"));
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

    Label placeholder = new Label("No drop-in files found");
    placeholder.setStyle("-fx-text-fill: #AAAAAA; -fx-font-size: 13px;");
    fileListView = new ListView<>();
    fileListView.setPlaceholder(placeholder);
    fileListView.setPrefHeight(50);
    fileListView.setPrefWidth(500);
    fileListView.setMinWidth(400);
    fileListView.setFocusTraversable(false);
    fileListView.setCellFactory(lv -> new DropInListCell(dropInsBtn));
    CustomMenuItem listMenuItem = new CustomMenuItem(fileListView, false);
    listMenuItem.setUserData("list");
    this.dropInsBtn.getItems().add(listMenuItem);

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
    List<File> files = new ArrayList<>();
    if (dropinsFolder != null && dropinsFolder.exists() && dropinsFolder.isDirectory()) {
      try (Stream<Path> paths = Files.walk(dropinsFolder.toPath())) {
        String filterText = filter.getText();
        paths.filter(p -> Files.isRegularFile(p) && !p.toFile().isHidden() && !FileUtils.isTempFile(p.toFile()))
            .filter(p -> StringUtils.isEmpty(filterText) || p.toFile().getName().toLowerCase().contains(filterText.toLowerCase()))
            .forEach(p -> files.add(p.toFile()));
      }
      catch (IOException e) {
        LOG.error("Failed to walk drop in folder: " + e.getMessage(), e);
      }
    }
    if (fileListView != null) {
      fileListView.getItems().setAll(files);
      double maxHeight = 600.0;
      fileListView.setPrefHeight(files.isEmpty() ? 50 : Math.min(files.size() * 90.0, maxHeight));
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
              case UISettings.DROP_IN_POSTACTION_DONOTHING: {
                break;
              }
              case UISettings.DROP_IN_POSTACTION_MOVETOFOLDER: {
                moveFile(file, new File(uiSettings.getDropinPostTargetFolder()), null);
                break;
              }
              case UISettings.DROP_IN_POSTACTION_MOVETOTABLEFOLDER: {
                if (gameSelection != null) {
                  moveFile(file, new File(uiSettings.getDropinPostTargetFolder()), gameSelection.getGameDisplayName());
                }
                else {
                  WidgetFactory.showAlert(Studio.stage, Messages.get("dialog.no_game_selected"), Messages.get("dialog.no_game_selected_so_cannot_determine_target"));
                }
                break;
              }
              case UISettings.DROP_IN_POSTACTION_MOVETO: {
                StudioFolderChooser chooser = new StudioFolderChooser();
                chooser.setTitle("Select Target Folder");
                File targetFolder = chooser.showOpenDialog(Studio.stage);
                moveFile(file, targetFolder, null);
                break;
              }
              case UISettings.DROP_IN_POSTACTION_MOVETOTRASH: {
                boolean confirmed1 = true;
                if (uiSettings.isConfirmDropInMoveToTrash()) {
                  Optional<ButtonType> result1 = WidgetFactory.showConfirmation(Studio.stage, Messages.get("dialog.delete_file_2"), Messages.get("dialog.delete") + file.getAbsolutePath() + "\"?", Messages.get("dialog.the_file_will_be_moved_to_the"));
                  confirmed1 = result1.isPresent() && result1.get().equals(ButtonType.OK);
                }
                if (confirmed1 && !Desktop.getDesktop().moveToTrash(file)) {
                  WidgetFactory.showAlert(Studio.stage, Messages.get("dialog.cannot_move_file_to_trash"), Messages.get("dialog.the_file") + file.getAbsolutePath() + Messages.get("dialog.couldn_t_be_moved_to_trash"));
                }
                break;
              }
              case UISettings.DROP_IN_POSTACTION_DELETE: {
                Optional<ButtonType> result2 = WidgetFactory.showConfirmation(Studio.stage, Messages.get("dialog.delete_file_2"), Messages.get("dialog.delete_file_3") + file.getAbsolutePath() + "\"?", Messages.get("dialog.the_file_cannot_be_recovered"));
                boolean confirmed2 = result2.isPresent() && result2.get().equals(ButtonType.OK);
                if (confirmed2 && !file.delete()) {
                  WidgetFactory.showAlert(Studio.stage, Messages.get("dialog.cannot_delete_file"), Messages.get("dialog.the_file") + file.getAbsolutePath() + Messages.get("dialog.couldn_t_be_deleted"));
                }
                break;
              }
            }
          });
    });
  }

  private boolean moveFile(File file, File target, String subfolder) {
    if (!target.exists()) {
      WidgetFactory.showAlert(Studio.stage, Messages.get("dialog.target_folder_doesn_t_exist"), Messages.get("dialog.the_target_folder") + target.getAbsolutePath() + Messages.get("dialog.doesn_t_exist"));
      return false;
    }
    if (subfolder != null) {
      target = new File(target, subfolder);
      if (!target.exists() && !target.mkdirs()) {
        WidgetFactory.showAlert(Studio.stage, Messages.get("dialog.cannot_create_target_table_folder"), Messages.get("dialog.the_target_table_folder") + target.getAbsolutePath() + Messages.get("dialog.couldn_t_be_created"));
        return false;
      }
    }
    File targetFile = new File(target, file.getName());
    if (targetFile.exists()) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, Messages.get("dialog.overwrite_file"), Messages.get("dialog.a_file_with_same_name") + file.getName() + Messages.get("dialog.already_exists_in_target_folder"), Messages.get("dialog.do_you_want_to_overwrite_it"));
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
        WidgetFactory.showAlert(Studio.stage, Messages.get("dialog.file_cannot_be_copied"), Messages.get("dialog.the_file") + file.getName() + Messages.get("dialog.couldn_t_be_copied_in_target_folder"));
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

  private static class DropInListCell extends ListCell<File> {
    private final MenuButton menuButton;
    private BorderPane root;
    private DropInContainerController controller;

    DropInListCell(MenuButton menuButton) {
      this.menuButton = menuButton;
      setPadding(Insets.EMPTY);
    }

    @Override
    protected void updateItem(File file, boolean empty) {
      super.updateItem(file, empty);
      if (empty || file == null) {
        setGraphic(null);
        return;
      }
      if (root == null) {
        try {
          FXMLLoader loader = new FXMLLoader(DropInContainerController.class.getResource("dropin-container.fxml"));
          loader.setResources(Messages.getBundle());
          root = loader.load();
          root.getStyleClass().add("dropin-menu-item");
          controller = loader.getController();
        }
        catch (IOException e) {
          LOG.error("Failed to load drop in container: " + e.getMessage(), e);
          setGraphic(null);
          return;
        }
      }
      controller.setData(menuButton, file);
      setGraphic(root);
    }
  }
}

package de.mephisto.vpin.ui.backglassmanager;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.backglassmanager.dialogs.BackglassManagerDialogs;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tables.TablesSidebarDirectB2SController;
import de.mephisto.vpin.ui.tables.dialogs.FrontendMediaUploadProgressModel;
import de.mephisto.vpin.ui.tables.models.B2SGlowing;
import de.mephisto.vpin.ui.tables.models.B2SLedType;
import de.mephisto.vpin.ui.tables.models.B2SVisibility;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingColumn;
import de.mephisto.vpin.ui.tables.panels.BaseTableController;
import de.mephisto.vpin.ui.util.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

/**
 *
 */
public class BackglassManagerController extends BaseTableController<DirectB2S, DirectB2SModel>
    implements Initializable, StudioFXController, StudioEventListener {

  private final static Logger LOG = LoggerFactory.getLogger(BackglassManagerController.class);

  private final Debouncer debouncer = new Debouncer();
  public static final int DEBOUNCE_MS = 100;

  @FXML
  private BorderPane root;

  @FXML
  private Label nameLabel;

  @FXML
  private Label typeLabel;

  @FXML
  private Label authorLabel;

  @FXML
  private Label artworkLabel;

  @FXML
  private Label grillLabel;

  @FXML
  private Label b2sElementsLabel;

  @FXML
  private Label scoresLabel;

  @FXML
  private Label playersLabel;

  @FXML
  private Label bulbsLabel;

  @FXML
  private Label filesizeLabel;

  @FXML
  private Label resolutionLabel;

  @FXML
  private Label dmdResolutionLabel;

  @FXML
  private Label fullDmdLabel;

  @FXML
  private Label modificationDateLabel;

  @FXML
  private Pane loaderStackImages;

  @FXML
  private BorderPane thumbnailImagePane;

  @FXML
  private ImageView thumbnailImage;

  @FXML
  private BorderPane dmdThumbnailImagePane;

  @FXML
  private ImageView dmdThumbnailImage;

  @FXML
  private Button downloadBackglassBtn;
  @FXML
  private Button useAsMediaBackglassBtn;

  @FXML
  private Button uploadDMDBtn;

  @FXML
  private Button resBtn;
  @FXML
  private Button downloadDMDBtn;
  @FXML
  private Button useAsMediaDMDBtn;
  @FXML
  private Button deleteDMDBtn;

  //-- Editors

  @FXML
  private ComboBox<B2SVisibility> hideGrill;

  @FXML
  private CheckBox hideB2SDMD;

  @FXML
  private CheckBox hideB2SBackglass;

  @FXML
  private ComboBox<B2SVisibility> hideDMD;

  @FXML
  private Spinner<Integer> skipLampFrames;

  @FXML
  private Spinner<Integer> skipGIFrames;

  @FXML
  private Spinner<Integer> skipSolenoidFrames;

  @FXML
  private Spinner<Integer> skipLEDFrames;

  @FXML
  private CheckBox lightBulbOn;

  @FXML
  private CheckBox startAsExe;

  @FXML
  private CheckBox startAsExeServer;

  @FXML
  private ComboBox<B2SGlowing> glowing;

  @FXML
  private ComboBox<B2SLedType> usedLEDType;

  @FXML
  private CheckBox startBackground;

  @FXML
  private CheckBox bringBGFromTop;

  @FXML
  private Button renameBtn;

  @FXML
  private Button uploadBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button duplicateBtn;

  @FXML
  private Button reloadBackglassBtn;

  @FXML
  private Label gameLabel;

  @FXML
  private Label gameFilenameLabel;

  @FXML
  private Button dataManagerBtn;

  @FXML
  private Button openBtn;

  @FXML
  TableColumn<DirectB2SModel, DirectB2SModel> statusColumn;

  @FXML
  TableColumn<DirectB2SModel, DirectB2SModel> displayNameColumn;

  @FXML
  TableColumn<DirectB2SModel, DirectB2SModel> fullDmdColumn;

  @FXML
  TableColumn<DirectB2SModel, DirectB2SModel> grillColumn;


  @FXML
  TableColumn<DirectB2SModel, DirectB2SModel> scoreColumn;

//-------------

  private DirectB2SData tableData;
  private DirectB2STableSettings tableSettings;
  private boolean refreshing;

  private GameRepresentation game;
  private DirectB2ServerSettings serverSettings;
  private FileDragEventHandler fileDragEventHandler;

  @FXML
  private void onUpload(ActionEvent e) {
    if (game != null) {
      Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
      TableDialogs.directUpload(stage, AssetType.DIRECTB2S, game, null);
      // when done, force refresh
      refreshBackglass();
    }
  }

  @FXML
  private void onBackglassReload(ActionEvent e) {
    refreshBackglass();
  }

  @FXML
  private void onResEdit(ActionEvent e) {
    DirectB2S selection = getSelection();
    if(selection != null) {
      BackglassManagerDialogs.openResGenerator(selection);
    }
  }

  public void refreshBackglass() {
    refreshBackglass(getSelection());
  }

  public void refreshBackglass(DirectB2S directB2s) {
    if (directB2s != null) {
      try {
        DirectB2SModel model = getModel(directB2s);
        if (model != null) {
          model.setBean(directB2s);
          model.reload();

          // refresh views too if the directB2s is selected
          DirectB2S selected = getSelection();
          if (selected != null && model.sameBean(selected)) {
            refresh(model.getBacklass());
          }
        }
      }
      catch (Exception ex) {
        LOG.error("Refreshing backglass failed: " + ex.getMessage(), ex);
        WidgetFactory.showAlert(stage, "Error", "Refreshing backglass failed: " + ex.getMessage());
      }
    }
  }

  @FXML
  private void onBackglassDownload() {
    if (tableData.isBackgroundAvailable()) {
      try (InputStream in = client.getBackglassServiceClient().getDirectB2sBackground(tableData)) {
        export(in);
      }
      catch (IOException ioe) {
        LOG.error("Cannot download background image for game " + tableData.getGameId(), ioe);
      }
    }
  }

  @FXML
  private void onDMDUpload() {
    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select DMD Image");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg", "*.jpeg"));

    File selection = fileChooser.showOpenDialog(stage);
    if (selection != null) {
      updateDMDImage(selection);
    }
  }

  @FXML
  private void onDMDDownload() {
    if (tableData.isDmdImageAvailable()) {
      try (InputStream in = client.getBackglassServiceClient().getDirectB2sDmd(tableData)) {
        export(in);
      }
      catch (IOException ioe) {
        LOG.error("Cannot download DMD image for game " + tableData.getGameId(), ioe);
      }
    }
  }

  @FXML
  private void onBackglassUseAsMedia() {
    if (tableData.isBackgroundAvailable()) {
      try (InputStream in = client.getBackglassServiceClient().getDirectB2sBackground(tableData)) {
        Image img = new Image(in);
        if (tableData.getGrillHeight() > 0 && tableSettings != null && tableSettings.getHideGrill() == 1) {
          PixelReader reader = img.getPixelReader();
          img = new WritableImage(reader, 0, 0, (int) img.getWidth(), (int) (img.getHeight() - tableData.getGrillHeight()));
        }

        uploadImageAsMedia(VPinScreen.BackGlass, "Backglass", img);
      }
      catch (IOException ioe) {
        LOG.error("Cannot download backglass and set as backglass media image for game " + tableData.getGameId(), ioe);
      }
    }
  }

  @FXML
  private void onDMDUseAsMedia() {
    if (tableData.isDmdImageAvailable()) {
      try (InputStream in = client.getBackglassServiceClient().getDirectB2sDmd(tableData)) {
        Image img = new Image(in);
        uploadImageAsMedia(VPinScreen.Menu, "DMD", img);
      }
      catch (IOException ioe) {
        LOG.error("Cannot download DMD and set as DMD media image for game " + tableData.getGameId(), ioe);
      }
    }
  }

  private void uploadImageAsMedia(VPinScreen screen, String screenName, Image img) throws IOException {
    Path tmp = Files.createTempFile("tmp_" + screen, ".png");
    RenderedImage renderedImage = SwingFXUtils.fromFXImage(img, null);
    ImageIO.write(renderedImage, "png", tmp.toFile());

    FrontendMediaRepresentation medias = client.getGameMediaService().getGameMedia(game.getId());
    boolean append = false;

    Optional<FrontendMediaItemRepresentation> existingImage = medias.getMediaItems(screen).stream().filter(m -> m.getMimeType().contains("image")).findAny();
    if (existingImage.isPresent()) {
      Optional<ButtonType> buttonType = WidgetFactory.showConfirmationWithOption(Studio.stage, "Replace " + screenName + " Media ?",
          "A " + screenName + " media asset already exists.",
          "Append new asset or overwrite existing asset?", "Overwrite", "Append");
      if (buttonType.isPresent() && buttonType.get().equals(ButtonType.OK)) {
      }
      else if (buttonType.isPresent() && buttonType.get().equals(ButtonType.APPLY)) {
        append = true;
      }
      else {
        return;
      }
    }
    else {
      Optional<ButtonType> buttonType = WidgetFactory.showConfirmation(Studio.stage, "Copy in " + screenName + " Media ?",
          "Add the " + screenName + " image as media asset.", null, "Copy");
      if (buttonType.isPresent() && buttonType.get().equals(ButtonType.OK)) {
      }
      else {
        return;
      }
    }
    FrontendMediaUploadProgressModel model = new FrontendMediaUploadProgressModel(game,
        screenName + " Media Upload", Arrays.asList(tmp.toFile()), screen, append);
    ProgressDialog.createProgressDialog(model);
  }

  @FXML
  private void onDMDDelete() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete DMD Image",
        "Delete DMD image from backglass \"" + tableData.getFilename() + "\"?", null, "Delete");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      deleteDMDImage();
    }
  }

  private void updateDMDImage(File selection) {
    DirectB2S b2s = tableData.toDirectB2S();
    ProgressDialog.createProgressDialog(new BackglassManagerDmdUploadProgressModel("Set DMD Image", b2s, selection));
    refreshBackglass(b2s);
    if (game != null) {
      EventManager.getInstance().notifyTableChange(game.getId(), null);
    }
  }

  private void deleteDMDImage() {
    DirectB2S b2s = tableData.toDirectB2S();
    ProgressDialog.createProgressDialog(new BackglassManagerDmdUploadProgressModel("Delete DMD Image", b2s, null));
    refreshBackglass(b2s);
    if (game != null) {
      EventManager.getInstance().notifyTableChange(game.getId(), null);
    }
  }

  private void export(InputStream in) {
    if (in != null) {
      StudioFolderChooser chooser = new StudioFolderChooser();
      chooser.setTitle("Select Target Folder");
      File targetFolder = chooser.showOpenDialog(stage);

      if (targetFolder != null) {
        try {
          File targetFile = new File(targetFolder, this.tableData.getName() + ".png");
          targetFile = FileUtils.uniqueFile(targetFile);
          FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
          IOUtils.copy(in, fileOutputStream);
          fileOutputStream.close();

          WidgetFactory.showInformation(stage, "Export Finished", "Written \"" + targetFile.getName() + "\".");
        }
        catch (IOException e) {
          LOG.error("Failed to download backglass image: " + e.getMessage(), e);
          WidgetFactory.showAlert(stage, "Error", "Failed to download backglass image: " + e.getMessage());
        }
      }
    }
  }

  @FXML
  private void onTableDataManager(ActionEvent e) {
    if (game != null) {
      Platform.runLater(() -> {
        TableDialogs.openTableDataDialog(tablesController.getTableOverviewController(), this.game);
      });
    }
  }

  @FXML
  protected void onRename(Event e) {
    DirectB2SModel selection = getSelectedModel();
    if (selection != null) {
      //Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
      String newName = WidgetFactory.showInputDialog(Studio.stage, "Rename Backglass", "Enter new name for backglass file \"" + selection.getFileName() + "\"", null, null, selection.getName());
      if (newName != null) {
        if (!FileUtils.isValidFilename(newName)) {
          WidgetFactory.showAlert(stage, "Invalid Filename", "The specified file name contains invalid characters.");
          return;
        }

        try {
          if (!newName.endsWith(".directb2s")) {
            newName = newName + ".directb2s";
          }
          DirectB2S b2s = client.getBackglassServiceClient().renameBackglass(selection.getBacklass(), newName);
          if (b2s != null) {
            selection.setBean(b2s);
            selection.reload();

            applyFilter();

            // then notify changes
            if (game != null) {
              EventManager.getInstance().notifyTableChange(game.getId(), null);
            }
          }
        }
        catch (Exception ex) {
          WidgetFactory.showAlert(Studio.stage, "Error", "Failed to dupliate backglass: " + ex.getMessage());
        }
      }
    }
  }

  @FXML
  private void onDuplicate(ActionEvent e) {
    DirectB2SModel selection = getSelectedModel();
    if (selection != null) {
      //Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Duplicate Backglass", "Duplicate backglass file \"" + selection.getFileName() + "\"?", null, "Duplicate");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        try {
          DirectB2S b2s = client.getBackglassServiceClient().duplicateBackglass(selection.getBacklass());
          if (b2s != null) {
            int idx = models.indexOf(selection);
            models.add(idx + 1, toModel(b2s));

            applyFilter();
          }
        }
        catch (Exception ex) {
          WidgetFactory.showAlert(Studio.stage, "Error", "Failed to dupliate backglass: " + ex.getMessage());
        }
      }
    }
  }

  @Override
  protected void onDelete(Event e) {
    try {
      DirectB2SModel selection = getSelectedModel();
      if (selection != null) {
        //Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
        Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Backglass", "Delete backglass file \"" + selection.getFileName() + "\"?", null, "Delete");
        if (result.isPresent() && result.get().equals(ButtonType.OK)) {
          if (client.getBackglassServiceClient().deleteBackglass(selection.getBacklass())) {
            // remove from the list if successfully deleted
            models.remove(selection);

            applyFilter();

            // then notify changes
            if (game != null) {
              EventManager.getInstance().notifyTableChange(game.getId(), null);
            }
          }
        }
      }
    }
    catch (Exception ex) {
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to delete backglass file: " + ex.getMessage());
    }
  }

  @FXML
  private void onOpen() {
    DirectB2S selectedItem = getSelection();
    if (selectedItem != null) {
      File file = new File(selectedItem.getFileName());
      SystemUtil.openFile(file);
    }
  }

  @FXML
  private void onReload() {
    client.getBackglassServiceClient().clearCache();
    doReload();
  }

  public void doReload() {
    startReload("Loading Backglasses...");

    refreshPlaylists();

    JFXFuture.supplyAsync(() -> {
      return client.getBackglassServiceClient().getBackglasses();
    }).thenAcceptLater(data -> {
      setItems(data);
      endReload();
    });
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize("backglass", "backglasses", new BackglassManagerColumnSorter(this));

    EventManager.getInstance().addListener(this);

    serverSettings = client.getBackglassServiceClient().getServerSettings();

    this.clearBtn.setVisible(false);
    this.dataManagerBtn.setDisable(true);
    this.renameBtn.setDisable(true);
    this.uploadBtn.setDisable(true);
    this.duplicateBtn.setDisable(true);
    this.deleteBtn.setDisable(true);
    this.reloadBackglassBtn.setDisable(true);

    FrontendType frontendType = Studio.client.getFrontendService().getFrontendType();
    if (!frontendType.supportMedias()) {
      HBox bgtoolbar = (HBox) this.useAsMediaBackglassBtn.getParent();
      bgtoolbar.getChildren().remove(useAsMediaBackglassBtn);

      HBox dmdtoolbar = (HBox) this.useAsMediaDMDBtn.getParent();
      dmdtoolbar.getChildren().remove(useAsMediaDMDBtn);
    }

    this.openBtn.setVisible(false); //TODO

    bindTable();

    super.loadFilterPanel("scene-directb2s-admin-filter.fxml");

    super.loadPlaylistCombo();

    hideGrill.setItems(FXCollections.observableList(TablesSidebarDirectB2SController.VISIBILITIES));
    hideGrill.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      if (!refreshing && tableSettings != null) {
        save(() -> tableSettings.setHideGrill(t1.getId()));
      }
    });

    hideB2SDMD.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!refreshing && tableSettings != null) {
        save(() -> tableSettings.setHideB2SDMD(newValue));
      }
    });

    hideB2SBackglass.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!refreshing && tableSettings != null) {
        save(() -> tableSettings.setHideB2SBackglass(newValue));
      }
    });

    hideDMD.setItems(FXCollections.observableList(TablesSidebarDirectB2SController.VISIBILITIES));
    hideDMD.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      if (!refreshing && tableSettings != null) {
        save(() -> tableSettings.setHideDMD(t1.getId()));
      }
    });

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    skipLampFrames.setValueFactory(factory);
    skipLampFrames.valueProperty().addListener((observableValue, integer, t1) -> {
      if (!refreshing && tableSettings != null) {
        debounceAndSave("skipLampFrames", () -> tableSettings.setLampsSkipFrames(t1));
      }
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    skipGIFrames.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      if (!refreshing && tableSettings != null) {
        debounceAndSave("skipGIFrames", () -> tableSettings.setGiStringsSkipFrames(t1));
      }
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    skipSolenoidFrames.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      if (!refreshing && tableSettings != null) {
        debounceAndSave("skipSolenoidFrames", () -> tableSettings.setSolenoidsSkipFrames(t1));
      }
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    skipLEDFrames.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      if (!refreshing && tableSettings != null) {
        debounceAndSave("skipLEDFrames", () -> tableSettings.setLedsSkipFrames(t1));
      }
    });

    glowing.setItems(FXCollections.observableList(TablesSidebarDirectB2SController.GLOWINGS));
    glowing.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      if (!refreshing && tableSettings != null) {
        save(() -> tableSettings.setGlowIndex(t1.getId()));
      }
    });

    startAsExe.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!refreshing && tableSettings != null) {
        save(() -> tableSettings.setStartAsEXE(newValue ? newValue : null));
      }
    });

    lightBulbOn.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!refreshing && tableSettings != null) {
        save(() -> tableSettings.setGlowBulbOn(newValue));
      }
    });

    usedLEDType.setItems(FXCollections.observableList(TablesSidebarDirectB2SController.LED_TYPES));
    usedLEDType.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      if (!refreshing && tableSettings != null) {
        save(() -> {
          tableSettings.setUsedLEDType(t1 != null ? t1.getId() : 0);
          glowing.setDisable(t1 != null && t1.getId() == 1);
          lightBulbOn.setDisable(t1 != null && t1.getId() == 1);
          lightBulbOn.setSelected(false);
        });
      }
    });

    startBackground.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!refreshing && tableSettings != null) {
        save(() -> tableSettings.setStartBackground(newValue));
      }
    });

    bringBGFromTop.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!refreshing && tableSettings != null) {
        save(() -> tableSettings.setFormToFront(newValue));
      }
    });

    // Install the handler for backglass selection
    this.tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      refresh(newValue != null ? newValue.getBacklass() : null);
    });

    // add the overlay for drag and drop
    new BackglassManagerDragDropHandler(this, tableView, tableStack);

    // add the overlay for DMD image drag    
    fileDragEventHandler = FileDragEventHandler.install(loaderStackImages, dmdThumbnailImagePane, true, "png", "jpg", "jpeg")
        .setOnDragDropped(e -> {
          List<File> files = e.getDragboard().getFiles();
          if (files != null && files.size() == 1) {
            File selection = files.get(0);
            Platform.runLater(() -> {
              updateDMDImage(selection);
            });
          }
        });
  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    NavigationController.setBreadCrumb(Arrays.asList("Backglasses"));

    // first time activation 
    if (this.models == null) {
      doReload();

      if (this.tableView.getItems().isEmpty()) {
        clearSelection();
      }
      else {
        this.tableView.getSelectionModel().select(0);
      }
      refresh(null);
    }
    refreshBackglass();
  }

  private void bindTable() {

    BaseLoadingColumn.configureColumn(statusColumn, (value, model) -> {
      if (!model.isVpxAvailable()) {
        Label icon = new Label();
        icon.setTooltip(new Tooltip("The backglass file \"" + model.getName() + "\n has no matching VPX file."));
        icon.setGraphic(WidgetFactory.createExclamationIcon());
        return icon;
      }
      // else
      return WidgetFactory.createCheckIcon();
    }, true);

    BaseLoadingColumn.configureColumn(displayNameColumn, (value, model) -> {
      Label label = new Label(model.getName());
      label.getStyleClass().add("default-text");
      return label;
    }, true);

    BaseLoadingColumn.configureLoadingColumn(fullDmdColumn, cell -> new LoadingCheckTableCell() {
      @Override
      protected String getLoading(DirectB2SModel model) {
        return "loading...";
      }

      @Override
      protected int isChecked(DirectB2SModel model) {
        return model.hasDmd() ? (model.isFullDmd() ? 1 : 2) : 0;
      }

      @Override
      protected String getTooltip(DirectB2SModel model) {
        return (model.isFullDmd() ?
            "Full DMD backglass" : "DMD backglass present, but not Full-DMD Aspect Ratio")
            + ", resolution " + model.getDmdWidth() + "x" + model.getDmdHeight();
      }
    });

    BaseLoadingColumn.configureLoadingColumn(grillColumn, cell -> new LoadingCheckTableCell() {
      @Override
      protected String getLoading(DirectB2SModel model) {
        return "";
      }

      @Override
      protected int isChecked(DirectB2SModel model) {
        return model.getGrillHeight() > 0 ? 1 : 0;
      }

      @Override
      protected String getTooltip(DirectB2SModel model) {
        return "Grill Height set to " + model.getGrillHeight();
      }
    });

    BaseLoadingColumn.configureLoadingColumn(scoreColumn, cell -> new LoadingCheckTableCell() {
      @Override
      protected String getLoading(DirectB2SModel model) {
        return "";
      }

      @Override
      protected int isChecked(DirectB2SModel model) {
        return model.getNbScores() > 0 ? 1 : 0;
      }

      @Override
      protected String getTooltip(DirectB2SModel model) {
        return model.getNbScores() > 0 ? "Backglass contains " + model.getNbScores() + " scores" : "";
      }
    });
  }

  private void refresh(@Nullable DirectB2S newValue) {
    if (newValue != null) {
      fileDragEventHandler.setDisabled(false);
      NavigationController.setBreadCrumb(Arrays.asList("Backglasses", newValue.getName()));
    }
    else {
      fileDragEventHandler.setDisabled(true);
      NavigationController.setBreadCrumb(Arrays.asList("Backglasses"));
    }

    this.refreshing = true;

    //both these depend on the game selection
    this.uploadBtn.setDisable(true);
    this.dataManagerBtn.setDisable(true);

    this.openBtn.setDisable(true);
    this.renameBtn.setDisable(true);
    this.duplicateBtn.setDisable(true);
    this.deleteBtn.setDisable(true);
    this.reloadBackglassBtn.setDisable(true);

    nameLabel.setText("-");
    typeLabel.setText("-");
    authorLabel.setText("-");
    artworkLabel.setText("-");
    b2sElementsLabel.setText("-");
    scoresLabel.setText("-");
    grillLabel.setText("-");
    playersLabel.setText("-");
    bulbsLabel.setText("-");
    filesizeLabel.setText("-");
    modificationDateLabel.setText("-");
    thumbnailImage.setImage(new Image(Studio.class.getResourceAsStream("empty-preview.png")));
    dmdThumbnailImage.setImage(new Image(Studio.class.getResourceAsStream("empty-preview.png")));
    downloadBackglassBtn.setDisable(true);
    useAsMediaBackglassBtn.setDisable(true);
    uploadDMDBtn.setDisable(true);
    downloadDMDBtn.setDisable(true);
    useAsMediaDMDBtn.setDisable(true);
    deleteDMDBtn.setDisable(true);
    resBtn.setDisable(true);
    resolutionLabel.setText("");
    dmdResolutionLabel.setText("");
    fullDmdLabel.setText("");
    gameLabel.setText("-");
    gameFilenameLabel.setText("-");

    hideGrill.setDisable(true);
    hideB2SBackglass.setDisable(true);
    hideB2SDMD.setDisable(true);
    hideDMD.setDisable(true);
    startBackground.setDisable(true);
    bringBGFromTop.setDisable(true);
    skipGIFrames.setDisable(true);
    skipLampFrames.setDisable(true);
    skipSolenoidFrames.setDisable(true);
    skipLEDFrames.setDisable(true);
    usedLEDType.setDisable(true);
    lightBulbOn.setDisable(true);
    glowing.setDisable(true);
    startAsExe.setDisable(true);

    skipLampFrames.getValueFactory().valueProperty().set(0);
    skipGIFrames.getValueFactory().valueProperty().set(0);
    skipSolenoidFrames.getValueFactory().valueProperty().set(0);
    skipLEDFrames.getValueFactory().valueProperty().set(0);
    lightBulbOn.selectedProperty().setValue(false);
    bringBGFromTop.selectedProperty().setValue(false);
    startBackground.selectedProperty().setValue(false);

    this.refreshing = false;

    game = null;
    tableSettings = null;

    if (newValue != null) {
      thumbnailImagePane.setCenter(new ProgressIndicator());
      dmdThumbnailImagePane.setCenter(new ProgressIndicator());

      JFXFuture.runAsync(() -> {
        try {
          this.tableData = client.getBackglassServiceClient().getDirectB2SData(newValue);
          if (this.tableData.getGameId() > 0) {
            this.game = client.getGame(this.tableData.getGameId());
            this.tableSettings = client.getBackglassServiceClient().getTableSettings(this.tableData.getGameId());
            this.dataManagerBtn.setDisable(false);
            this.uploadBtn.setDisable(false);
          }
        }
        catch (Exception e) {
          this.tableData = new DirectB2SData();
        }

        loadImages();

      }).thenLater(() -> {

        this.refreshing = true;

        this.openBtn.setDisable(false);
        this.renameBtn.setDisable(false);
        this.duplicateBtn.setDisable(false);
        this.deleteBtn.setDisable(false);
        this.resBtn.setDisable(false);
        this.reloadBackglassBtn.setDisable(false);

        if (game != null) {
          gameLabel.setText(game.getGameDisplayName());
          gameFilenameLabel.setText(game.getGameFileName());
          dataManagerBtn.setDisable(false);
          this.uploadBtn.setDisable(false);
        }
        else {
          //VPX is not installed, but available!
          if (newValue.isVpxAvailable()) {
            gameLabel.setText("?");
            gameFilenameLabel.setText("(Available, but not installed)");
          }
        }

        nameLabel.setText(tableData.getName());
        typeLabel.setText(DirectB2SData.getTableType(tableData.getTableType()));
        authorLabel.setText(tableData.getAuthor());
        artworkLabel.setText(tableData.getArtwork());
        grillLabel.setText(String.valueOf(tableData.getGrillHeight()));
        b2sElementsLabel.setText(String.valueOf(tableData.getB2sElements()));
        scoresLabel.setText(String.valueOf(tableData.getScores()));
        playersLabel.setText(String.valueOf(tableData.getNumberOfPlayers()));
        filesizeLabel.setText(FileUtils.readableFileSize(tableData.getFilesize()));
        bulbsLabel.setText(String.valueOf(tableData.getIlluminations()));

        modificationDateLabel.setText(SimpleDateFormat.getDateTimeInstance().format(tableData.getModificationDate()));

        if (tableSettings != null) {
          boolean serverLaunchAsExe = serverSettings != null && serverSettings.getDefaultStartMode() == DirectB2ServerSettings.EXE_START_MODE;
          boolean tableLaunchAsExe = tableSettings.getStartAsEXE() != null && tableSettings.getStartAsEXE();
          startAsExe.setDisable(false);
          startAsExe.setSelected(tableLaunchAsExe);
          startAsExeServer.setSelected(serverLaunchAsExe);
          startAsExeServer.setDisable(true);

          hideGrill.setDisable(tableData.getGrillHeight() == 0);
          hideGrill.setValue(TablesSidebarDirectB2SController.VISIBILITIES.stream().filter(v -> v.getId() == tableSettings.getHideGrill()).findFirst().orElse(null));
          hideB2SBackglass.setDisable(false);
          hideB2SBackglass.selectedProperty().setValue(tableSettings.isHideB2SBackglass());
          hideB2SDMD.setDisable(false);
          hideB2SDMD.selectedProperty().setValue(tableSettings.isHideB2SDMD());
          hideDMD.setDisable(false);
          hideDMD.setValue(TablesSidebarDirectB2SController.VISIBILITIES.stream().filter(v -> v.getId() == tableSettings.getHideDMD()).findFirst().orElse(null));
          usedLEDType.setDisable(false);
          usedLEDType.setValue(TablesSidebarDirectB2SController.LED_TYPES.stream().filter(v -> v.getId() == tableSettings.getUsedLEDType()).findFirst().orElse(null));

          skipLampFrames.setDisable(tableData.getIlluminations() == 0);
          skipLampFrames.getValueFactory().valueProperty().set(tableSettings.getLampsSkipFrames());
          skipGIFrames.setDisable(tableData.getIlluminations() == 0);
          skipGIFrames.getValueFactory().valueProperty().set(tableSettings.getGiStringsSkipFrames());
          skipSolenoidFrames.setDisable(tableData.getIlluminations() == 0);
          skipSolenoidFrames.getValueFactory().valueProperty().set(tableSettings.getSolenoidsSkipFrames());
          skipLEDFrames.setDisable(tableData.getIlluminations() == 0);
          skipLEDFrames.getValueFactory().valueProperty().set(tableSettings.getLedsSkipFrames());
          lightBulbOn.setDisable(usedLEDType.getValue() != null && usedLEDType.getValue().getId() == 1);
          lightBulbOn.selectedProperty().setValue(tableSettings.isGlowBulbOn());
          glowing.setDisable(usedLEDType.getValue() != null && usedLEDType.getValue().getId() == 1);
          glowing.setValue(TablesSidebarDirectB2SController.GLOWINGS.stream().filter(v -> v.getId() == tableSettings.getGlowIndex()).findFirst().get());

          startBackground.setDisable(false);
          startBackground.selectedProperty().setValue(tableSettings.isStartBackground());
          bringBGFromTop.setDisable(false);
          bringBGFromTop.selectedProperty().setValue(tableSettings.isFormToFront());
        }

        this.refreshing = false;
      });
    }
  }

  private void loadImages() {
    Image thumbnail = null;
    String thumbnailError = null;
    if (tableData.isBackgroundAvailable()) {
      try (InputStream in = client.getBackglassServiceClient().getDirectB2sBackground(tableData)) {
        thumbnail = new Image(in);
        if (tableData.getGrillHeight() > 0 && tableSettings != null && tableSettings.getHideGrill() == 1) {
          PixelReader reader = thumbnail.getPixelReader();
          thumbnail = new WritableImage(reader, 0, 0, (int) thumbnail.getWidth(), (int) (thumbnail.getHeight() - tableData.getGrillHeight()));
        }
      }
      catch (IOException ioe) {
        LOG.error("Cannot download background image for game " + tableData.getGameId(), ioe);
        thumbnail = null;
        thumbnailError = "Failed to read image data.";
      }
    }
    else {
      thumbnailError = "No Image data available.";
    }
    final Image _thumbnail = thumbnail;
    final String _thumbnailError = thumbnailError;
    Platform.runLater(() -> {
      if (_thumbnail != null) {
        thumbnailImage.setImage(_thumbnail);
        thumbnailImagePane.setCenter(thumbnailImage);
        downloadBackglassBtn.setDisable(false);
        useAsMediaBackglassBtn.setDisable(game == null);
        resolutionLabel.setText("Resolution: " + (int) _thumbnail.getWidth() + " x " + (int) _thumbnail.getHeight());
      }
      else {
        thumbnailImage.setImage(null);
        thumbnailImagePane.setCenter(null);
        resolutionLabel.setText(_thumbnailError);
      }
    });

    Image dmdThumbnail = null;
    String dmdThumbnailError = null;
    if (tableData.isDmdImageAvailable()) {
      try (InputStream in = client.getBackglassServiceClient().getDirectB2sDmd(tableData)) {
        dmdThumbnail = new Image(in);
      }
      catch (IOException ioe) {
        LOG.error("Cannot download DMD image for game " + tableData.getGameId(), ioe);
        dmdThumbnailError = "Failed to read DMD image data.";
      }
    }
    else {
      dmdThumbnailError = "No DMD background available.";
    }
    final Image _dmdThumbnail = dmdThumbnail;
    final String _dmdThumbnailError = dmdThumbnailError;
    Platform.runLater(() -> {
      uploadDMDBtn.setDisable(false);
      if (_dmdThumbnail != null) {
        dmdThumbnailImage.setImage(_dmdThumbnail);
        dmdThumbnailImagePane.setCenter(dmdThumbnailImage);
        downloadDMDBtn.setDisable(false);
        useAsMediaDMDBtn.setDisable(game == null);
        deleteDMDBtn.setDisable(false);
        dmdResolutionLabel.setText("Resolution: " + (int) _dmdThumbnail.getWidth() + " x " + (int) _dmdThumbnail.getHeight());
        fullDmdLabel.setText(DirectB2SData.isFullDmd(_dmdThumbnail.getWidth(), _dmdThumbnail.getHeight()) ? "Yes" : "No");
      }
      else {
        dmdThumbnailImage.setImage(null);
        dmdThumbnailImagePane.setCenter(null);
        dmdResolutionLabel.setText(_dmdThumbnailError);
        fullDmdLabel.setText("No");
      }
    });
  }

  public void selectGame(@Nullable GameRepresentation game) {
    // try to select first backglass of the game, do the selection by name
    if (game != null) {
      String gameBaseName = FilenameUtils.getBaseName(game.getGameFileName());

      // at calling time, the list may not have been populated so register a listener in that case
      if (models != null) {
        selectGame(gameBaseName);
      }
      else {
        ChangeListener<ObservableList<DirectB2SModel>> listener = new ChangeListener<ObservableList<DirectB2SModel>>() {
          @Override
          public void changed(ObservableValue<? extends ObservableList<DirectB2SModel>> observable,
                              ObservableList<DirectB2SModel> oldValue, ObservableList<DirectB2SModel> newValue) {
            selectGame(gameBaseName);
            tableView.itemsProperty().removeListener(this);
          }
        };
        this.tableView.itemsProperty().addListener(listener);
      }
    }
  }

  private void selectGame(String gameBaseName) {
    for (DirectB2SModel backglass : models) {
      if (StringUtils.startsWithIgnoreCase(backglass.getFileName(), gameBaseName)) {
        tableView.scrollTo(backglass);
        tableView.getSelectionModel().select(backglass);
        break;
      }
    }
  }

  private void debounceAndSave(String debounceKey, Runnable r) {
    debouncer.debounce(debounceKey, () -> {
      save(r);
    }, DEBOUNCE_MS);
  }

  private void save(Runnable r) {
    if (this.game != null) {
      try {
        r.run();
        client.getBackglassServiceClient().saveTableSettings(game.getId(), this.tableSettings);
        //DirectB2SModel selectedItem = getSelection();
        //if (selectedItem != null) {
        Platform.runLater(() -> {
          //this.refresh(selectedItem.getBacklass());
          EventManager.getInstance().notifyTableChange(game.getId(), null);
        });
      }
      catch (Exception e) {
        LOG.error("Failed to save B2STableSettings.xml: " + e.getMessage());
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save B2STableSettings.xml: " + e.getMessage());
      }
    }
  }

  public GameRepresentation getGame() {
    return game;
  }


  //------------------------------------------------
  // Implementation of StudioEventListener

  @Override
  public void tableChanged(int id, String rom, String gameName) {
    DirectB2SModel selection = tableView.getSelectionModel().getSelectedItem();

    if (id > 0) {
      GameRepresentation refreshedGame = client.getGameService().getGame(id);
      reload(refreshedGame);
    }

    if (selection != null && selection.getGameId() == id) {
      refresh(selection.getBacklass());
    }
  }

  private void reload(GameRepresentation refreshedGame) {
    // tab should have been initiliazed to support reload
    if (refreshedGame != null && models != null) {
      DirectB2SData b2sdata = client.getBackglassServiceClient().getDirectB2SData(refreshedGame.getId());
      if (b2sdata != null) {
        DirectB2S b2s = b2sdata.toDirectB2S();
        // forced to true as this b2s comes from a game
        b2s.setVpxAvailable(true);

        DirectB2SModel model = getModel(b2s);
        if (model != null) {
          model.setBean(b2s);
        }
        else {
          model = toModel(b2s);
          models.add(model);
        }
        // simulate reload as we have data already
        model.load(b2sdata);
      }
    }
  }

  //------------------------------------------------

  protected DirectB2SModel toModel(DirectB2S b2s) {
    return new DirectB2SModel(b2s);
  }
}

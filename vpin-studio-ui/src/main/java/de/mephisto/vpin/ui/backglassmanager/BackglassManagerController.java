package de.mephisto.vpin.ui.backglassmanager;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.directb2s.DirectB2SAndVersions;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.ui.*;
import de.mephisto.vpin.ui.backglassmanager.dialogs.BackglassManagerDialogs;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.StudioEventListener;
import de.mephisto.vpin.ui.tables.TableDialogs;
import de.mephisto.vpin.ui.tables.TablesSidebarDirectB2SController;
import de.mephisto.vpin.ui.tables.dialogs.FrontendMediaUploadProgressModel;
import de.mephisto.vpin.ui.tables.models.B2SFormPosition;
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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
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

import javax.annotation.Nonnull;
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
public class BackglassManagerController extends BaseTableController<DirectB2SAndVersions, DirectB2SModel>
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
  private ComboBox<B2SVisibility> startBackground;

  @FXML
  private ComboBox<B2SFormPosition> formToPosition;

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
  private Label emulatorNameLabel;

  @FXML
  private Button directB2SSetDefaultBtn;

  @FXML
  private Button directB2SDisableBtn;

  @FXML
  private Label directB2SLabel;

  @FXML
  private ComboBox<String> directB2SCombo;

  @FXML
  private Button directB2SDeleteBtn;

  @FXML
  private Button dataManagerBtn;

  @FXML
  private Button openBtn;

  @FXML
  private Button vpsOpenBtn;

  @FXML
  private Button dmdPositionBtn;

  @FXML
  private Button tableNavigateBtn;

  @FXML
  TableColumn<DirectB2SModel, DirectB2SModel> statusColumn;

  @FXML
  TableColumn<DirectB2SModel, DirectB2SModel> displayNameColumn;

  @FXML
  TableColumn<DirectB2SModel, DirectB2SModel> numberDirectB2SColumn;

  @FXML
  TableColumn<DirectB2SModel, DirectB2SModel> fullDmdColumn;

  @FXML
  TableColumn<DirectB2SModel, DirectB2SModel> grillColumn;

  @FXML
  TableColumn<DirectB2SModel, DirectB2SModel> scoreColumn;

  @FXML
  TableColumn<DirectB2SModel, DirectB2SModel> resColumn;

  @FXML
  TableColumn<DirectB2SModel, DirectB2SModel> frameColumn;

//-------------

  private DirectB2SAndVersions directB2S;
  private GameRepresentation game;
  private DirectB2SData tableData;
  private DirectB2STableSettings tableSettings;
  private int refreshingCounter;

  private DirectB2ServerSettings serverSettings;
  private FileDragEventHandler fileDragEventHandler;

  private boolean activeView = false;

  @FXML
  private void onTableMouseClicked(MouseEvent mouseEvent) {
    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
      if (mouseEvent.getClickCount() == 2) {
        onDMDPosition();
      }
    }
  }

  @FXML
  private void onDMDPosition() {
    if (game != null) {
      TableDialogs.openDMDPositionDialog(game);
    }
  }

  @FXML
  private void onUpload(ActionEvent e) {
    if (game != null) {
      Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
      TableDialogs.directUpload(stage, AssetType.DIRECTB2S, game, () -> {
        // when done, force refresh
        unselectVersion();
        reloadSelection();
      });
    }
  }

  @FXML
  private void onOpenTable(ActionEvent e) {
    if (game != null) {
      NavigationController.navigateTo(NavigationItem.Tables, new NavigationOptions(game.getId()));
    }
  }

  @FXML
  private void onBackglassReload(ActionEvent e) {
    reloadSelection();
  }

  @FXML
  private void onResEdit(ActionEvent e) {
    DirectB2SAndVersions selection = getSelection();
    if (selection != null) {
      BackglassManagerDialogs.openResGenerator(selection.getEmulatorId(), selection.getFileName());
    }
  }

  @FXML
  private void onBackglassDownload() {
    if (tableData != null && tableData.isBackgroundAvailable()) {
      try (InputStream in = client.getBackglassServiceClient().getDirectB2sBackground(tableData)) {
        export(in);
      }
      catch (IOException ioe) {
        LOG.error("Cannot download background image for backglass " + tableData, ioe);
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
    if (tableData != null && tableData.isDmdImageAvailable()) {
      try (InputStream in = client.getBackglassServiceClient().getDirectB2sDmd(tableData)) {
        export(in);
      }
      catch (IOException ioe) {
        LOG.error("Cannot download DMD image for backglass " + tableData, ioe);
      }
    }
  }

  @FXML
  private void onBackglassUseAsMedia() {
    if (tableData != null && tableData.isBackgroundAvailable() && game != null) {
      try (InputStream in = client.getBackglassServiceClient().getDirectB2sBackground(tableData)) {
        Image img = new Image(in);
        if (tableData.getGrillHeight() > 0 && tableSettings != null && tableSettings.getHideGrill() == 1) {
          PixelReader reader = img.getPixelReader();
          img = new WritableImage(reader, 0, 0, (int) img.getWidth(), (int) (img.getHeight() - tableData.getGrillHeight()));
        }

        uploadImageAsMedia(game, VPinScreen.BackGlass, "Backglass", img);
      }
      catch (IOException ioe) {
        LOG.error("Cannot download backglass and set as backglass media image for backglass " + tableData, ioe);
      }
    }
  }

  @FXML
  private void onDMDUseAsMedia() {
    if (tableData != null && tableData.isDmdImageAvailable() && game != null) {
      try (InputStream in = client.getBackglassServiceClient().getDirectB2sDmd(tableData)) {
        Image img = new Image(in);
        uploadImageAsMedia(game, VPinScreen.Menu, "DMD", img);
      }
      catch (IOException ioe) {
        LOG.error("Cannot download DMD and set as DMD media image for backglass " + tableData, ioe);
      }
    }
  }

  private void uploadImageAsMedia(@Nonnull GameRepresentation game, VPinScreen screen, String screenName, Image img) throws IOException {
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
    ProgressDialog.createProgressDialog(new BackglassManagerDmdUploadProgressModel("Set DMD Image", directB2S.getEmulatorId(), directB2SCombo.getValue(), selection));

    // then refresh images and table
    reloadItem(directB2S);

    if (game != null) {
      EventManager.getInstance().notifyTableChange(game.getId(), null);
    }
  }

  private void deleteDMDImage() {
    ProgressDialog.createProgressDialog(new BackglassManagerDmdUploadProgressModel("Delete DMD Image", directB2S.getEmulatorId(), directB2SCombo.getValue(), null));

    // then refresh images and table
    reloadItem(directB2S);

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
          DirectB2SAndVersions b2s = client.getBackglassServiceClient().renameBackglass(selection.getEmulatorId(), selection.getFileName(), newName);
          if (b2s != null) {
            selection.setBean(b2s);
            unselectVersion();
            reloadSelection();
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
          DirectB2SAndVersions b2s = client.getBackglassServiceClient().duplicateBackglass(selection.getEmulatorId(), selection.getFileName());
          if (b2s != null) {
            reloadItem(b2s);
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
          if (client.getBackglassServiceClient().deleteBackglass(selection.getEmulatorId(), selection.getFileName())) {
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
    DirectB2SAndVersions selectedItem = getSelection();
    if (selectedItem != null && game != null) {
      GameEmulatorRepresentation emulatorRepresentation = client.getFrontendService().getGameEmulator(game.getEmulatorId());
      File folder = new File(emulatorRepresentation.getTablesDirectory());
      File file = new File(folder, selectedItem.getFileName());
      if (file.exists()) {
        SystemUtil.openFile(file);
      }
      else {
        SystemUtil.openFolder(file.getParentFile());
      }
    }
  }

  @FXML
  private void onDirectB2SDefault() {
    JFXFuture
        .supplyAsync(() -> client.getBackglassServiceClient().setBackglassAsDefault(directB2S.getEmulatorId(), directB2SCombo.getValue()))
        .thenAcceptLater((b2s) -> {
          // reload table and selected view
          unselectVersion();
          reloadItem(b2s);
        })
        .onErrorLater((e) -> WidgetFactory.showAlert(stage, "Error", "Cannot set " + directB2SCombo.getValue() + " as default", e.getMessage()));
  }

  @FXML
  private void onDirectB2SDisable() {
    JFXFuture
        .supplyAsync(() -> client.getBackglassServiceClient().disableBackglass(directB2S.getEmulatorId(), directB2S.getFileName()))
        .thenAcceptLater((b2s) -> {
          // reload table and selected view
          unselectVersion();
          reloadItem(b2s);
        })
        .onErrorLater((e) -> WidgetFactory.showAlert(stage, "Error", "Cannot disable backglass", e.getMessage()));
  }

  @FXML
  private void onDirectB2SDelete() {
    JFXFuture
      .supplyAsync(() -> client.getBackglassServiceClient().deleteBackglassVersion(directB2S.getEmulatorId(), directB2SCombo.getValue()))
      .thenAcceptLater((b2s) -> {
        // reload table and selected view
        unselectVersion();
        reloadItem(b2s);
      })
      .onErrorLater((e) -> WidgetFactory.showAlert(stage, "Error", "Cannot disable backglass", e.getMessage()));
  }

  @FXML
  private void onVpsOpen() {
    if (game != null) {
      VpsTable tableById = client.getVpsService().getTableById(game.getExtTableId());
      if (tableById != null) {
        Studio.browse(VPS.getVpsTableUrl(tableById.getId()));
      }
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
    resBtn.managedProperty().bindBidirectional(resBtn.visibleProperty());

    resBtn.setVisible(Features.RES_EDITOR);
    EventManager.getInstance().addListener(this);

    serverSettings = client.getBackglassServiceClient().getServerSettings();

    this.clearBtn.setVisible(false);

    FrontendType frontendType = Studio.client.getFrontendService().getFrontendType();
    if (!frontendType.supportMedias()) {
      HBox bgtoolbar = (HBox) this.useAsMediaBackglassBtn.getParent();
      bgtoolbar.getChildren().remove(useAsMediaBackglassBtn);

      HBox dmdtoolbar = (HBox) this.useAsMediaDMDBtn.getParent();
      dmdtoolbar.getChildren().remove(useAsMediaDMDBtn);
    }

    this.openBtn.setVisible(client.getSystemService().isLocal());

    bindTable();

    super.loadFilterPanel("scene-directb2s-admin-filter.fxml");

    super.loadPlaylistCombo();

    hideGrill.setItems(FXCollections.observableList(TablesSidebarDirectB2SController.VISIBILITIES));
    hideGrill.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      if (refreshingCounter == 0 && tableSettings != null) {
        save(() -> tableSettings.setHideGrill(t1.getId()));
      }
    });

    hideB2SDMD.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (refreshingCounter == 0 && tableSettings != null) {
        save(() -> tableSettings.setHideB2SDMD(newValue));
      }
    });

    hideB2SBackglass.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (refreshingCounter == 0 && tableSettings != null) {
        save(() -> tableSettings.setHideB2SBackglass(newValue));
      }
    });

    hideDMD.setItems(FXCollections.observableList(TablesSidebarDirectB2SController.VISIBILITIES));
    hideDMD.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      if (refreshingCounter == 0 && tableSettings != null) {
        save(() -> tableSettings.setHideDMD(t1.getId()));
      }
    });

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    skipLampFrames.setValueFactory(factory);
    skipLampFrames.valueProperty().addListener((observableValue, integer, t1) -> {
      if (refreshingCounter == 0 && tableSettings != null) {
        debounceAndSave("skipLampFrames", () -> tableSettings.setLampsSkipFrames(t1));
      }
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    skipGIFrames.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      if (refreshingCounter == 0 && tableSettings != null) {
        debounceAndSave("skipGIFrames", () -> tableSettings.setGiStringsSkipFrames(t1));
      }
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    skipSolenoidFrames.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      if (refreshingCounter == 0 && tableSettings != null) {
        debounceAndSave("skipSolenoidFrames", () -> tableSettings.setSolenoidsSkipFrames(t1));
      }
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0);
    skipLEDFrames.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      if (refreshingCounter == 0 && tableSettings != null) {
        debounceAndSave("skipLEDFrames", () -> tableSettings.setLedsSkipFrames(t1));
      }
    });

    glowing.setItems(FXCollections.observableList(TablesSidebarDirectB2SController.GLOWINGS));
    glowing.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      if (refreshingCounter == 0 && tableSettings != null) {
        save(() -> tableSettings.setGlowIndex(t1.getId()));
      }
    });

    startAsExe.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (refreshingCounter == 0 && tableSettings != null) {
        save(() -> tableSettings.setStartAsEXE(newValue ? newValue : null));
      }
    });

    lightBulbOn.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (refreshingCounter == 0 && tableSettings != null) {
        save(() -> tableSettings.setGlowBulbOn(newValue));
      }
    });

    usedLEDType.setItems(FXCollections.observableList(TablesSidebarDirectB2SController.LED_TYPES));
    usedLEDType.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      if (refreshingCounter == 0 && tableSettings != null) {
        save(() -> {
          tableSettings.setUsedLEDType(t1 != null ? t1.getId() : 0);
          glowing.setDisable(t1 != null && t1.getId() == 1);
          lightBulbOn.setDisable(t1 != null && t1.getId() == 1);
          lightBulbOn.setSelected(false);
        });
      }
    });

    startBackground.setItems(FXCollections.observableList(TablesSidebarDirectB2SController.VISIBILITIES));
    startBackground.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      if (refreshingCounter == 0 && tableSettings != null) {
        save(() -> tableSettings.setStartBackground(t1.getId()));
      }
    });

    formToPosition.setItems(FXCollections.observableList(TablesSidebarDirectB2SController.FORM_POSITIONS));
    formToPosition.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (refreshingCounter == 0 && tableSettings != null) {
        save(() -> tableSettings.setFormToPosition(newValue.getId()));
      }
    });

    directB2SCombo.valueProperty().addListener((obs, o, n) -> {
      if (refreshingCounter == 0) {
        refreshTableData(directB2S.getEmulatorId(), n);
      }
    });

    // Install the handler for backglass selection
    this.tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      refreshView(newValue != null ? newValue.getBacklass() : null);
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

    // deactivate all elemenys in the right sections
    refreshTableData(-1, null);
    refreshGame(-1);
    refreshTableSettings(-1);
  }

  @Override
  public void onViewDeactivated() {
    activeView = false;
  }

  @Override
  public void onViewActivated(NavigationOptions options) {
    activeView = true;
    NavigationController.setBreadCrumb(Arrays.asList("Backglasses"));

    // first time activation
    if (models == null || models.isEmpty()) {
      doReload();

      if (this.tableView.getItems().isEmpty()) {
        clearSelection();
      }
      else {
        this.tableView.getSelectionModel().select(0);
      }
      refreshView(null);
    }
    reloadSelection();
  }

  private void bindTable() {

    BaseLoadingColumn.configureColumn(statusColumn, (value, model) -> {
      if (!model.isGameAvailable()) {
        Label icon = new Label();
        icon.setTooltip(new Tooltip("The backglass file \"" + model.getName() + "\n has no matching game file."));
        icon.setGraphic(WidgetFactory.createExclamationIcon(getIconColor(value)));
        return icon;
      }
      // else
      return WidgetFactory.createCheckIcon(getIconColor(value));
    }, this, true);

    BaseLoadingColumn.configureColumn(displayNameColumn, (value, model) -> {
      Label label = new Label(model.getName());
      label.getStyleClass().add("default-text");
      label.setStyle(getLabelCss(value));
      return label;
    }, this, true);

    BaseLoadingColumn.configureColumn(numberDirectB2SColumn, (value, model) -> {
      int nbVersions = value.getNbVersions();
      String iconname = null;
      if (nbVersions > 9) {
        iconname = "mdi2n-numeric-9-plus-box-multiple-outline";
      }
      else if (nbVersions > 1) {
        iconname = "mdi2n-numeric-" + nbVersions + "-box-multiple-outline";
      }
      if (iconname != null) {
        return WidgetFactory.createIcon(iconname, value.isEnabled() ? null : WidgetFactory.DISABLED_COLOR);
      }
      else {
        return new Label("");
      }
    }, this, true);

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
      protected int isChecked(DirectB2SModel model) {
        return model.getNbScores() > 0 ? 1 : 0;
      }

      @Override
      protected String getTooltip(DirectB2SModel model) {
        return model.getNbScores() > 0 ? "Backglass contains " + model.getNbScores() + " scores" : "";
      }
    });

    BaseLoadingColumn.configureLoadingColumn(resColumn, cell -> new LoadingCheckTableCell() {
      @Override
      protected int isChecked(DirectB2SModel model) {
        return model.getResPath() != null ? 1 : 0;
      }

      @Override
      protected String getTooltip(DirectB2SModel model) {
        return model.getResPath() != null ? "Backglass uses a specific .res file: " + model.getResPath() : "";
      }
    });
    resColumn.setVisible(Features.RES_EDITOR);

    BaseLoadingColumn.configureLoadingColumn(frameColumn, cell -> new LoadingCheckTableCell() {
      @Override
      protected int isChecked(DirectB2SModel model) {
        return model.getFramePath() != null ? 1 : 0;
      }

      @Override
      protected String getTooltip(DirectB2SModel model) {
        return model.getFramePath() != null ? "Backglass uses a background frame: " + model.getFramePath() : "";
      }
    });
    frameColumn.setVisible(Features.RES_EDITOR);
  }

  @Override
  protected void refreshView(@Nullable DirectB2SAndVersions newValue) {
    if (newValue != null) {
      fileDragEventHandler.setDisabled(false);
      NavigationController.setBreadCrumb(Arrays.asList("Backglasses", newValue.getName()));
    }
    else {
      fileDragEventHandler.setDisabled(true);
      NavigationController.setBreadCrumb(Arrays.asList("Backglasses"));
    }

    this.refreshingCounter++;

    this.directB2S = newValue;

    int emulatorId = -1;
    int gameId = -1;
    if (newValue != null) {
      List<String> versions = newValue.getVersions();
      String prevSelected = directB2SCombo.getValue();

      directB2SCombo.setItems(FXCollections.observableList(versions));
      setVersioningDisabled(versions.size() <= 1);

      // reselect previously added
      if (prevSelected != null && versions.contains(prevSelected)) {
        directB2SCombo.getSelectionModel().select(prevSelected);
      }
      else {
        directB2SCombo.getSelectionModel().selectFirst();
      }

      // determine associated game and emulator
      emulatorId = newValue.getEmulatorId();
      gameId = client.getBackglassServiceClient().getGameId(newValue.getEmulatorId(), newValue.getFileName());
    }
    else {
      setVersioningDisabled(true);
    }

    // now refresh other sections
    refreshTableData(emulatorId, directB2SCombo.getValue());
    refreshGame(gameId);
    refreshTableSettings(gameId);

    this.refreshingCounter--;
  }

  private void setVersioningDisabled(boolean b) {
    directB2SCombo.setDisable(b);
    directB2SSetDefaultBtn.setDisable(b);
    directB2SDisableBtn.setDisable(b);
    directB2SDeleteBtn.setDisable(b);
    if (b) {
      directB2SCombo.setItems(null);
    }
  }

  protected void refreshTableData(int emulatorId, String directb2sFilename) {
    this.tableData = null;

    nameLabel.setText("-");
    typeLabel.setText("-");
    authorLabel.setText("-");
    artworkLabel.setText("-");
    grillLabel.setText("-");
    b2sElementsLabel.setText("-");
    scoresLabel.setText("-");
    playersLabel.setText("-");
    filesizeLabel.setText("-");
    bulbsLabel.setText("-");

    modificationDateLabel.setText("-");

    skipLampFrames.setDisable(true);
    skipGIFrames.setDisable(true);
    skipSolenoidFrames.setDisable(true);
    skipLEDFrames.setDisable(true);

    this.renameBtn.setDisable(true);
    this.duplicateBtn.setDisable(true);
    this.deleteBtn.setDisable(true);
    this.reloadBackglassBtn.setDisable(true);

    downloadBackglassBtn.setDisable(true);
    uploadDMDBtn.setDisable(true);
    downloadDMDBtn.setDisable(true);
    deleteDMDBtn.setDisable(true);

    thumbnailImage.setImage(new Image(Studio.class.getResourceAsStream("empty-preview.png")));
    dmdThumbnailImage.setImage(new Image(Studio.class.getResourceAsStream("empty-preview.png")));
    resolutionLabel.setText("");
    dmdResolutionLabel.setText("");
    fullDmdLabel.setText("");

    if (directb2sFilename != null) {

      thumbnailImagePane.setCenter(new ProgressIndicator());
      dmdThumbnailImagePane.setCenter(new ProgressIndicator());

      JFXFuture.supplyAsync(() -> client.getBackglassServiceClient().getDirectB2SData(emulatorId, directb2sFilename))
          .thenAcceptLater((tableData) -> {
            this.refreshingCounter++;

            this.tableData = tableData;

            loadImages(emulatorId, directb2sFilename);

            nameLabel.setText(tableData.getName());
            typeLabel.setText(DirectB2SData.getTableType(tableData.getTableType()));
            authorLabel.setText(tableData.getAuthor());
            artworkLabel.setText(tableData.getArtwork());
            grillLabel.setText(String.valueOf(tableData.getGrillHeight()));
            b2sElementsLabel.setText(String.valueOf(tableData.getB2sElements()));
            b2sElementsLabel.setTooltip(new Tooltip(tableData.getB2sElements() + " variants exist for this backglass"));
            scoresLabel.setText(String.valueOf(tableData.getScores()));
            playersLabel.setText(String.valueOf(tableData.getNumberOfPlayers()));
            filesizeLabel.setText(FileUtils.readableFileSize(tableData.getFilesize()));
            bulbsLabel.setText(String.valueOf(tableData.getIlluminations()));

            disableCombosFrames();

            modificationDateLabel.setText(SimpleDateFormat.getDateTimeInstance().format(tableData.getModificationDate()));

            this.renameBtn.setDisable(false);
            this.duplicateBtn.setDisable(false);
            this.deleteBtn.setDisable(false);
            this.reloadBackglassBtn.setDisable(false);

            this.refreshingCounter--;
          });
    }
  }

  protected void refreshGame(int gameId) {
    // refresh Game and Emulator
    this.game = null;
    emulatorNameLabel.setText("-");
    gameLabel.setText("-");
    gameFilenameLabel.setText("-");
    // depend on the game selection
    this.dataManagerBtn.setDisable(true);
    this.tableNavigateBtn.setDisable(true);
    this.dmdPositionBtn.setDisable(true);
    this.resBtn.setDisable(true);
    this.uploadBtn.setDisable(true);

    useAsMediaBackglassBtn.setDisable(true);
    useAsMediaDMDBtn.setDisable(true);

    this.openBtn.setDisable(true);
    this.vpsOpenBtn.setDisable(true);

    if (gameId > 0) {
      JFXFuture.supplyAsync(() -> {
            this.game = client.getGame(gameId);
            int emuId = game != null ? game.getEmulatorId() : directB2S.getEmulatorId();
            return client.getFrontendService().getGameEmulator(emuId);
          })
          .thenAcceptLater(emu -> {
            emulatorNameLabel.setText(emu != null ? emu.getName() : "?");

            if (game != null) {
              gameLabel.setText(game.getGameDisplayName());
              gameFilenameLabel.setText(game.getGameFileName());

              useAsMediaBackglassBtn.setDisable(false);
              useAsMediaDMDBtn.setDisable(false);
          
              openBtn.setDisable(false);
              vpsOpenBtn.setDisable(client.getVpsService().getTableById(game.getExtTableId()) == null);

              dataManagerBtn.setDisable(false);
              tableNavigateBtn.setDisable(false);
              dmdPositionBtn.setDisable(false);
              resBtn.setDisable(false);
              uploadBtn.setDisable(false);
            }
            else {
              //VPX is not installed, but available!
              if (directB2S.isGameAvailable()) {
                gameLabel.setText("?");
                gameFilenameLabel.setText("(Available, but not installed)");
              }
            }
          });
    }
  }

  protected void refreshTableSettings(int gameId) {
    this.tableSettings = null;

    usedLEDType.setDisable(true);
    lightBulbOn.setDisable(true);
    glowing.setDisable(true);
    startAsExe.setDisable(true);

    formToPosition.setDisable(true);

    skipLampFrames.getValueFactory().valueProperty().set(0);
    skipGIFrames.getValueFactory().valueProperty().set(0);
    skipSolenoidFrames.getValueFactory().valueProperty().set(0);
    skipLEDFrames.getValueFactory().valueProperty().set(0);
    lightBulbOn.selectedProperty().setValue(false);

    hideGrill.setDisable(true);
    hideGrill.setValue(null);
    hideB2SBackglass.setDisable(true);
    hideB2SBackglass.setSelected(false);
    hideB2SDMD.setDisable(true);
    hideB2SDMD.setSelected(false);
    hideDMD.setDisable(true);
    hideDMD.setValue(null);
    startBackground.setDisable(true);
    formToPosition.setDisable(true);

    if (gameId > 0) {
      JFXFuture.supplyAsync(() -> client.getBackglassServiceClient().getTableSettings(gameId))
          .thenAcceptLater((tableSettings) -> {
            this.refreshingCounter++;

            this.tableSettings = tableSettings;

            boolean serverLaunchAsExe = serverSettings != null && serverSettings.getDefaultStartMode() == DirectB2ServerSettings.EXE_START_MODE;
            boolean tableLaunchAsExe = tableSettings.getStartAsEXE() != null && tableSettings.getStartAsEXE();
            startAsExe.setDisable(false);
            startAsExe.setSelected(tableLaunchAsExe);
            startAsExeServer.setSelected(serverLaunchAsExe);
            startAsExeServer.setDisable(true);

            hideB2SBackglass.setDisable(false);
            hideB2SBackglass.setSelected(tableSettings.isHideB2SBackglass());
            hideB2SDMD.setDisable(false);
            hideB2SDMD.setSelected(tableSettings.isHideB2SDMD());
            hideDMD.setDisable(false);
            hideDMD.setValue(TablesSidebarDirectB2SController.VISIBILITIES.stream().filter(v -> v.getId() == tableSettings.getHideDMD()).findFirst().orElse(null));
            usedLEDType.setDisable(false);
            usedLEDType.setValue(TablesSidebarDirectB2SController.LED_TYPES.stream().filter(v -> v.getId() == tableSettings.getUsedLEDType()).findFirst().orElse(null));
            hideGrill.setDisable(false);
            hideGrill.setValue(TablesSidebarDirectB2SController.VISIBILITIES.stream().filter(v -> v.getId() == tableSettings.getHideGrill()).findFirst().orElse(null));

            disableCombosFrames();

            skipLampFrames.getValueFactory().valueProperty().set(tableSettings.getLampsSkipFrames());
            skipGIFrames.getValueFactory().valueProperty().set(tableSettings.getGiStringsSkipFrames());
            skipSolenoidFrames.getValueFactory().valueProperty().set(tableSettings.getSolenoidsSkipFrames());
            skipLEDFrames.getValueFactory().valueProperty().set(tableSettings.getLedsSkipFrames());
            lightBulbOn.setDisable(usedLEDType.getValue() != null && usedLEDType.getValue().getId() == 1);
            lightBulbOn.selectedProperty().setValue(tableSettings.isGlowBulbOn());
            glowing.setDisable(usedLEDType.getValue() != null && usedLEDType.getValue().getId() == 1);
            glowing.setValue(TablesSidebarDirectB2SController.GLOWINGS.stream().filter(v -> v.getId() == tableSettings.getGlowIndex()).findFirst().get());

            startBackground.setDisable(false);
            startBackground.setValue(TablesSidebarDirectB2SController.VISIBILITIES.stream().filter(v -> v.getId() == tableSettings.getStartBackground()).findFirst().orElse(null));

            formToPosition.setDisable(false);
            formToPosition.setValue(TablesSidebarDirectB2SController.FORM_POSITIONS.stream().filter(v -> v.getId() == tableSettings.getFormToPosition()).findFirst().orElse(null));

            this.refreshingCounter--;
          });
    }
  }

  protected void disableCombosFrames() {
    skipLampFrames.setDisable(tableSettings != null && tableData != null && tableData.getIlluminations() == 0);
    skipGIFrames.setDisable(tableSettings != null && tableData != null && tableData.getIlluminations() == 0);
    skipSolenoidFrames.setDisable(tableSettings != null && tableData != null && tableData.getIlluminations() == 0);
    skipLEDFrames.setDisable(tableSettings != null && tableData != null && tableData.getIlluminations() == 0);
  }

  private void loadImages(int emulatorId, String fileName) {
    Image thumbnail = null;
    String thumbnailError = null;
    if (tableData != null && tableData.isBackgroundAvailable()) {
      String url = client.getBackglassServiceClient().getDirectB2sPreviewBackgroundUrl(emulatorId, fileName, true);
      thumbnail = new Image(url);
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
    if (tableData != null && tableData.isDmdImageAvailable()) {
      String url = client.getBackglassServiceClient().getDirectB2sDmdUrl(emulatorId, fileName);
      dmdThumbnail = new Image(url);
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

  /**
   * Prior to reload, when user is modifying versions, we don't maintain version selection
   */
  private void unselectVersion() {
    directB2SCombo.getSelectionModel().clearSelection();
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
    if (!activeView) {
      return;
    }

    DirectB2SModel selection = tableView.getSelectionModel().getSelectedItem();

    if (id > 0) {
      GameRepresentation refreshedGame = client.getGameService().getGame(id);

      // When a game is updated or added, the associated backglass in table + view should be updated
      // If it is a new game, this backglas sis discovered and added into th etable as a new row

      // tab should have been initiliazed to support reload
      if (refreshedGame != null && models != null) {
        DirectB2SAndVersions b2s = client.getBackglassServiceClient().getDirectB2S(refreshedGame.getId());
        reloadItem(b2s);
      }
    }

    if (selection != null && selection.getGameId() == id) {
      refreshView(selection.getBacklass());
    }
  }

  //------------------------------------------------

  private String getIconColor(DirectB2SAndVersions value) {
    return value.isEnabled() ? null : WidgetFactory.DISABLED_COLOR;
  }

  private String getLabelCss(DirectB2SAndVersions value) {
    return value.isEnabled() ? "" : WidgetFactory.DISABLED_TEXT_STYLE;
  }

  @Override
  protected DirectB2SModel toModel(DirectB2SAndVersions b2s) {
    return new DirectB2SModel(b2s);
  }

  protected DirectB2SModel getModel(int emulatorId, String fileName) {
    return models.stream().filter(m -> m.sameBean(emulatorId, fileName)).findFirst().orElse(null);
  }
}

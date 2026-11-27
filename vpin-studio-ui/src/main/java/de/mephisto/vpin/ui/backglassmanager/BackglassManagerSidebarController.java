package de.mephisto.vpin.ui.backglassmanager;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.validation.BackglassValidationCode;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.backglassmanager.dialogs.BackglassBulkImportType;
import de.mephisto.vpin.ui.backglassmanager.dialogs.BackglassManagerDialogs;
import de.mephisto.vpin.ui.tables.TablesSidebarDirectB2SController;
import de.mephisto.vpin.ui.tables.dialogs.FrontendMediaUploadProgressModel;
import de.mephisto.vpin.ui.tables.models.*;
import de.mephisto.vpin.ui.tables.panels.BaseSideBarController;
import de.mephisto.vpin.ui.tables.panels.PlayButtonController;
import de.mephisto.vpin.ui.tables.validation.BackglassValidationTexts;
import de.mephisto.vpin.ui.util.FileDragEventHandler;
import de.mephisto.vpin.ui.util.JFXHelper;
import de.mephisto.vpin.ui.util.LocalizedValidation;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import de.mephisto.vpin.ui.util.StudioFolderChooser;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.stage;

/**
 *
 */
public class BackglassManagerSidebarController extends BaseSideBarController<DirectB2S> implements Initializable {

  private final static Logger LOG = LoggerFactory.getLogger(BackglassManagerController.class);

  private final Debouncer debouncer = new Debouncer();
  public static final int DEBOUNCE_MS = 100;

  @FXML
  private Label titleLabel;

  @FXML
  private Button openBtn;

  @FXML
  private Button vpsOpenBtn;

  @FXML
  private HBox validationError;
  @FXML
  private Label validationErrorLabel;
  @FXML
  private Label validationErrorText;
  @FXML
  private Button validationUploadDmdBtn;

  @FXML
  private Label nameLabel;

  @FXML
  private Label filenameLabel;

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
  private StackPane thumbnailImagePane;

  @FXML
  private ImageView thumbnailImage;

  @FXML
  private Label thumbnailEmptyLabel;

  @FXML
  private ProgressIndicator thumbnailProgress;

  @FXML
  private StackPane dmdThumbnailImagePane;

  @FXML
  private ImageView dmdThumbnailImage;

  @FXML
  private VBox dmdEmptyBox;

  @FXML
  private Button uploadDMDEmptyBtn;

  @FXML
  private ProgressIndicator dmdThumbnailProgress;

  @FXML
  private Pane noneActiveInfo;

  @FXML
  private Button downloadBackglassBtn;
  @FXML
  private SplitMenuButton useAsMediaBackglassBtn;

  @FXML
  private Button uploadDMDBtn;
  @FXML
  private Button downloadDMDBtn;
  @FXML
  private SplitMenuButton useAsMediaDMDBtn;
  @FXML
  private Button deleteDMDBtn;

  @FXML
  private VBox versionPane;

  @FXML
  private CheckBox activateCheckbox;

  @FXML
  private ComboBox<String> directB2SCombo;
  @FXML
  private Label directB2SLabel;

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
  private ComboBox<B2SStartAsExe> startAsExe;

  @FXML
  private Label startAsExeServerLabel;

  @FXML
  private ComboBox<B2SGlowing> glowing;

  @FXML
  private ComboBox<B2SLedType> usedLEDType;

  @FXML
  private ComboBox<B2SDualMode> dualModes;

  @FXML
  private ComboBox<B2SVisibility> startBackground;

  @FXML
  private ComboBox<B2SFormPosition> formToPosition;

  @FXML
  private CheckBox formNoFocus;

  @FXML
  private Button reloadBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Label gameLabel;

  @FXML
  private Label gameFilenameLabel;

  @FXML
  private Label gameIdLabel;

  @FXML
  private Label emulatorNameLabel;

  @FXML
  private Button dataManagerBtn;

  @FXML
  private Button tableNavigateBtn;

  @FXML
  private Button gameLaunchBtn;

  //-------------
  private GameRepresentation game;

  private DirectB2S directb2s;
  private DirectB2SData tableData;
  private DirectB2STableSettings tableSettings;
  private int refreshingCounter;
  private String latestSelection;

  private BackglassManagerController backglassManagerController;

  private ChangeListener<Boolean> activationChangeListener = (observable, oldValue, newValue) -> {
    latestSelection = null;
    if (newValue) {
      setDirectB2SDefault();
    }
    else {
      setDirectB2SDisabled();
    }
  };

  //------------------------------------------------ OPERATIONS ON BACKGLASS ---

  @FXML
  void onBackglassDelete(Event e) {
    try {
      DirectB2SModel selection = backglassManagerController.getSelectedModel();
      String selectedVersion = getSelectedVersion();
      if (selection != null && selectedVersion != null) {
        Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, Messages.get("dialog.delete_backglass"), Messages.get("msg.delete_backglass_file", String.valueOf(selectedVersion)), null, Messages.get("common.delete"));
        if (result.isPresent() && result.get().equals(ButtonType.OK)) {
          client.getBackglassServiceClient().deleteBackglassVersion(getEmulatorId(), selectedVersion);
          BackglassManagerControllerUtils.notifyChange(getEmulatorId(), selectedVersion, game);
        }
      }
    }
    catch (Exception ex) {
      WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("msg.failed_to_delete_backglass_file", ex.getMessage()));
    }
  }

  @FXML
  void onBackglassDownload() {
    if (tableData != null && tableData.isBackgroundAvailable()) {
      try (InputStream in = client.getBackglassServiceClient().getDirectB2sPreviewBackground(tableData, true)) {
        export(in);
      }
      catch (IOException ioe) {
        LOG.error("Cannot download background image for backglass " + tableData, ioe);
      }
    }
  }

  @FXML
  void onDMDUpload() {
    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle(Messages.get("dialog.select_dmd_image"));
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter(Messages.get("dialog.filter_image"), "*.png", "*.jpg", "*.jpeg"));

    File selection = fileChooser.showOpenDialog(stage);
    if (selection != null) {
      BackglassManagerControllerUtils.updateDMDImage(getEmulatorId(), getSelectedVersion(), game, selection);
    }
  }

  @FXML
  void onDMDDownload() {
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
  void onBackglassUseAsMedia() {
    if (tableData != null && tableData.isBackgroundAvailable() && game != null) {
      try (InputStream in = client.getBackglassServiceClient().getDirectB2sPreviewBackground(tableData, false)) {
        Image img = new Image(in);
        uploadImageAsMedia(game, VPinScreen.BackGlass, Messages.get("dialog.screen_backglass"), img);
      }
      catch (IOException ioe) {
        LOG.error("Cannot download backglass and set as backglass media image for backglass " + tableData, ioe);
      }
    }
  }

  @FXML
  void onBackglassBulkImport() {
    BackglassManagerDialogs.openBulkImport(getEmulatorId(), BackglassBulkImportType.BACKGLASS);
  }

  @FXML
  void onDMDBulkImport() {
    BackglassManagerDialogs.openBulkImport(getEmulatorId(), BackglassBulkImportType.DMD);
  }

  @FXML
  void onDMDUseAsMedia() {
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

  private void uploadImageAsMedia(@NonNull GameRepresentation game, VPinScreen screen, String screenName, Image img) throws IOException {
    Path tmp = Files.createTempFile("tmp_" + screen, ".png");
    RenderedImage renderedImage = SwingFXUtils.fromFXImage(img, null);
    ImageIO.write(renderedImage, "png", tmp.toFile());

    FrontendMediaRepresentation medias = client.getGameMediaService().getGameMedia(game.getId());
    boolean append = false;

    Optional<FrontendMediaItemRepresentation> existingImage = medias.getMediaItems(screen).stream().filter(m -> m.getMimeType().contains("image")).findAny();
    if (existingImage.isPresent()) {
      Optional<ButtonType> buttonType = WidgetFactory.showConfirmationWithOption(Studio.stage, Messages.get("msg.replace_media", screenName),
          Messages.get("msg.a_media_asset_already_exists", screenName),
          Messages.get("dialog.append_new_asset_or_overwrite_existing_asset"), Messages.get("dialog.overwrite"), Messages.get("dialog.append"));
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
      Optional<ButtonType> buttonType = WidgetFactory.showConfirmation(Studio.stage, Messages.get("msg.copy_in_media", screenName),
          Messages.get("msg.add_the_image_as_media_asset", screenName), null, Messages.get("dialog.copy"));
      if (buttonType.isPresent() && buttonType.get().equals(ButtonType.OK)) {
      }
      else {
        return;
      }
    }
    FrontendMediaUploadProgressModel model = new FrontendMediaUploadProgressModel(game,
        Messages.get("dialog.screen_media_upload", screenName), List.of(tmp.toFile()), screen, append, null);
    ProgressDialog.createProgressDialog(model);
  }

  @FXML
  void onDMDDelete() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, Messages.get("dialog.delete_dmd_image"),
        Messages.get("msg.delete_dmd_image_from_backglass", tableData.getFilename()), null, Messages.get("common.delete"));
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      BackglassManagerControllerUtils.deleteDMDImage(getEmulatorId(), getSelectedVersion(), game);
    }
  }

  @FXML
  void onGameLaunch() {
    if (game != null) {
      PlayButtonController.onPlay(game, null, null);
    }
  }

  private void setDirectB2SDefault() {
    DirectB2S selectedItem = backglassManagerController.getSelection();
    String selectedVersion = getSelectedVersion();
    if (selectedItem != null && selectedVersion != null) {
      JFXFuture
          .supplyAsync(() -> client.getBackglassServiceClient().setBackglassAsDefault(selectedItem.getEmulatorId(), selectedVersion))
          .thenAcceptLater((b2s) -> {
            BackglassManagerControllerUtils.notifyChange(b2s);
          })
          .onErrorLater((e) -> WidgetFactory.showAlert(stage, Messages.get("common.error"), Messages.get("msg.cannot_set_as_default", String.valueOf(selectedVersion)), e.getMessage()));
    }
  }

  private void setDirectB2SDisabled() {
    DirectB2S selectedItem = backglassManagerController.getSelection();
    String selectedVersion = getSelectedVersion();
    if (selectedItem != null && selectedVersion != null) {
      JFXFuture
          .supplyAsync(() -> client.getBackglassServiceClient().disableBackglass(selectedItem.getEmulatorId(), selectedVersion))
          .thenAcceptLater((b2s) -> {
            BackglassManagerControllerUtils.notifyChange(b2s);
          })
          .onErrorLater((e) -> WidgetFactory.showAlert(stage, Messages.get("common.error"), Messages.get("msg.cannot_set_as_default", String.valueOf(selectedVersion)), e.getMessage()));
    }
  }

  private void export(InputStream in) {
    if (in != null) {
      StudioFolderChooser chooser = new StudioFolderChooser();
      chooser.setTitle(Messages.get("dialog.select_target_folder"));
      File targetFolder = chooser.showOpenDialog(stage);

      if (targetFolder != null) {
        try {
          File targetFile = new File(targetFolder, this.tableData.getName() + ".png");
          targetFile = FileUtils.uniqueFile(targetFile);
          FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
          IOUtils.copy(in, fileOutputStream);
          fileOutputStream.close();

          WidgetFactory.showInformation(stage, Messages.get("dialog.export_finished"), Messages.get("msg.written", targetFile.getName()));
        }
        catch (IOException e) {
          LOG.error("Failed to download backglass image: " + e.getMessage(), e);
          WidgetFactory.showAlert(stage, Messages.get("common.error"), Messages.get("msg.failed_to_download_backglass_image", e.getMessage()));
        }
      }
    }
  }

  //------------------------------------------------ CONTEXT MENU STATE ---
  // the context menu of the backglass list offers the actions of this sidebar with the same enabled state

  boolean isActivationAvailable() {
    return activateCheckbox.isVisible() && !activateCheckbox.isDisable();
  }

  boolean isActiveVersion() {
    return activateCheckbox.isSelected();
  }

  void toggleActivation() {
    activateCheckbox.setSelected(!activateCheckbox.isSelected());
  }

  boolean isBackglassImageAvailable() {
    return !downloadBackglassBtn.isDisable();
  }

  boolean isDmdUploadAvailable() {
    return !uploadDMDBtn.isDisable();
  }

  boolean isDmdImageAvailable() {
    return !downloadDMDBtn.isDisable();
  }

  boolean isGameAvailable() {
    return game != null;
  }

  boolean isVpsAvailable() {
    return !vpsOpenBtn.isDisable();
  }

  boolean isFileOpenAvailable() {
    return openBtn.isVisible() && !openBtn.isDisable();
  }

  //------------------------------------------------ EVENT CASCADING ---

  @FXML
  private void onOpenTable(ActionEvent e) {
    getBackglassManagerController().onOpenTable(e);
  }

  @FXML
  private void onTableDataManager(ActionEvent e) {
    getBackglassManagerController().onTableDataManager(e);
  }

  @FXML
  protected void onReload(ActionEvent e) {
    getBackglassManagerController().onBackglassReload(e);
  }

  @FXML
  private void onOpen() {
    getBackglassManagerController().onOpen();
  }

  @FXML
  private void onVpsOpen() {
    getBackglassManagerController().onVpsOpen();
  }

  @FXML
  private void onValidationSettings() {
    PreferencesController.open("validators_backglass");
  }

  //------------------------------------------------ EVENT CASCADING ---

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    versionPane.managedProperty().bindBidirectional(versionPane.visibleProperty());
    directB2SCombo.managedProperty().bindBidirectional(directB2SCombo.visibleProperty());
    directB2SLabel.managedProperty().bindBidirectional(directB2SLabel.visibleProperty());
    noneActiveInfo.managedProperty().bindBidirectional(noneActiveInfo.visibleProperty());
    validationError.managedProperty().bindBidirectional(validationError.visibleProperty());
    validationUploadDmdBtn.managedProperty().bindBidirectional(validationUploadDmdBtn.visibleProperty());
    openBtn.managedProperty().bindBidirectional(openBtn.visibleProperty());
    activateCheckbox.managedProperty().bindBidirectional(activateCheckbox.visibleProperty());
    openBtn.setVisible(client.getSystemService().isLocal());
    validationError.setVisible(false);

    // the images follow the width of their tiles (two tiles share the sidebar width)
    thumbnailImage.fitWidthProperty().bind(thumbnailImagePane.widthProperty().subtract(16));
    dmdThumbnailImage.fitWidthProperty().bind(dmdThumbnailImagePane.widthProperty().subtract(16));

    if (!Features.MEDIA_ENABLED) {
      useAsMediaBackglassBtn.setVisible(false);
      useAsMediaBackglassBtn.setManaged(false);
      useAsMediaDMDBtn.setVisible(false);
      useAsMediaDMDBtn.setManaged(false);
    }

    activateCheckbox.selectedProperty().addListener(activationChangeListener);
    directB2SCombo.valueProperty().addListener((obs, o, n) -> {
      if (refreshingCounter > 0) {
        return;
      }
      latestSelection = n;
      String selectedVersion = getSelectedVersion();
      if (selectedVersion != null) {
        tableData = client.getBackglassServiceClient().getDirectB2SData(getEmulatorId(), selectedVersion);
        refreshTableData(tableData);
      }
      refreshStatusCheckbox();
    });

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

    startAsExe.setItems(FXCollections.observableList(TablesSidebarDirectB2SController.START_AS_EXE));
    startAsExe.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (refreshingCounter == 0 && tableSettings != null) {
        save(() -> tableSettings.setStartAsEXE(newValue.getId()));
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

    dualModes.setItems(FXCollections.observableList(TablesSidebarDirectB2SController.DUAL_MODES));
    dualModes.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      if (refreshingCounter == 0 && tableSettings != null) {
        save(() -> {
          tableSettings.setDualMode(t1 != null ? t1.getId() : 0);
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

    formNoFocus.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (refreshingCounter == 0 && tableSettings != null) {
        save(() -> tableSettings.setFormNoFocus(newValue));
      }
    });


    // add the overlay for DMD image drag    
    FileDragEventHandler.install(loaderStackImages, dmdThumbnailImagePane, true, "png", "jpg", "jpeg")
        .setOnDragDropped(e -> {
          List<File> files = e.getDragboard().getFiles();
          if (files != null && files.size() == 1) {
            File selection = files.getFirst();
            Platform.runLater(() -> {
              BackglassManagerControllerUtils.updateDMDImage(getEmulatorId(), getSelectedVersion(), game, selection);
            });
          }
        });

    // deactivate all elements in the right sections
    resetGame();
    setData(null);
  }

  private void refreshStatusCheckbox() {
    activateCheckbox.selectedProperty().removeListener(activationChangeListener);
    activateCheckbox.setSelected(false);
    // no game associated to the backglass file -> activation is not possible
    activateCheckbox.setDisable(directb2s == null || directb2s.getGameId() <= 0);
    String selectedVersion = getSelectedVersion();
    if (selectedVersion != null && directb2s != null) {
      // compare full names, a .directb2s and a .B2SPro version may share the same base name
      boolean selected = directb2s.isEnabled() && Strings.CI.equals(FilenameUtils.getName(selectedVersion), FilenameUtils.getName(directb2s.getFileName()));
      activateCheckbox.setSelected(selected);
    }
    activateCheckbox.selectedProperty().addListener(activationChangeListener);
  }

//
//  private void refreshView(int emulatorId, String selectedVersion, int gameId) {
//    if (selectedVersion != null) {
//      fileDragEventHandler.setDisabled(false);
//    }
//    else {
//      fileDragEventHandler.setDisabled(true);
//    }
//
//    // start refresh and reset counter
//    this.refreshingCounter = 1;
//
//    // now refresh other sections
//    this.emulatorId = emulatorId;
//    this.selectedVersion = selectedVersion;
//
//    refreshTableData(emulatorId, selectedVersion);
//    refreshTableSettings(gameId);
//    setGame(null, false);
//
//    this.refreshingCounter = 0;
//  }

  protected void refreshTableData(@Nullable DirectB2SData directB2SData) {
    nameLabel.setText("-");
    filenameLabel.setText("-");
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

    downloadBackglassBtn.setDisable(true);
    uploadDMDBtn.setDisable(true);
    uploadDMDEmptyBtn.setDisable(true);
    downloadDMDBtn.setDisable(true);
    deleteDMDBtn.setDisable(true);

    showBackgroundImage(null, false);
    showDmdImage(null, false);
    resolutionLabel.setText("");
    dmdResolutionLabel.setText("");
    fullDmdLabel.setText("");

    reloadBtn.setDisable(true);
    deleteBtn.setDisable(true);

    if (directB2SData != null) {
      showBackgroundImage(null, true);
      showDmdImage(null, true);

      loadImages(directB2SData.getEmulatorId(), directB2SData.getFilename());

      setLabelAndTooltip(filenameLabel, directB2SData.getFilename());
      setLabelAndTooltip(nameLabel, directB2SData.getName());
      typeLabel.setText(DirectB2SData.getTableType(directB2SData.getTableType()));
      authorLabel.setText(directB2SData.getAuthor());
      artworkLabel.setText(directB2SData.getArtwork());
      grillLabel.setText(String.valueOf(directB2SData.getGrillHeight()));
      b2sElementsLabel.setText(String.valueOf(directB2SData.getB2sElements()));
      b2sElementsLabel.setTooltip(new Tooltip(Messages.get("dialog.variants_exist_for_backglass", directB2SData.getB2sElements())));
      scoresLabel.setText(String.valueOf(directB2SData.getNbScores()));
      playersLabel.setText(String.valueOf(directB2SData.getNumberOfPlayers()));
      filesizeLabel.setText(FileUtils.readableFileSize(directB2SData.getFilesize()));
      bulbsLabel.setText(String.valueOf(directB2SData.getIlluminations()));

      disableCombosFrames();

      modificationDateLabel.setText(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault()).format(directB2SData.getModificationDate()));

      reloadBtn.setDisable(false);
      deleteBtn.setDisable(false);
    }
  }

  public int getEmulatorId() {
    return directb2s.getEmulatorId();
  }

  public String getSelectedVersion() {
    DirectB2S selection = backglassManagerController.getSelection();
    String selectedVersion = null;
    if (selection != null && selection.getVersions().size() > 1) {
      selectedVersion = directB2SCombo.getValue();   // alternative check:  versionSelector.isVisible()
    }
    if (selectedVersion == null && selection != null && selection.getVersions().size() > 0) {
      selectedVersion = selection.getVersion(0);
    }
    return selectedVersion;
  }

  protected void resetGame() {
      // set emulator name
      emulatorNameLabel.setText("-");
      gameLabel.setText("-");
      gameFilenameLabel.setText("-");
      gameIdLabel.setText("-");

      // depend on the game selection
      vpsOpenBtn.setDisable(true);
      useAsMediaBackglassBtn.setDisable(true);
      useAsMediaDMDBtn.setDisable(true);
  
      this.dataManagerBtn.setDisable(true);
      this.tableNavigateBtn.setDisable(true);
      this.gameLaunchBtn.setDisable(true);
  }

  protected void setGame(@Nullable GameRepresentation game, boolean gameAvailable) {
    this.game = game;

    if (game != null) {
      JFXFuture.supplyAsync(() -> {
            return client.getEmulatorService().getGameEmulator(game.getEmulatorId());
          })
          .thenAcceptLater(emu -> {
            if (emu != null) {
              emulatorNameLabel.setText(emu.getName());
            }
          });

      setLabelAndTooltip(gameLabel, game.getGameDisplayName());
      setLabelAndTooltip(gameFilenameLabel, game.getGameFileName());
      setLabelAndTooltip(gameIdLabel, String.valueOf(game.getId()));

      useAsMediaBackglassBtn.setDisable(false);
      useAsMediaDMDBtn.setDisable(false);
      vpsOpenBtn.setDisable(client.getVpsService().getTableById(game.getExtTableId()) == null);

      dataManagerBtn.setDisable(false);
      tableNavigateBtn.setDisable(false);
      gameLaunchBtn.setDisable(false);
    }
    else if (gameAvailable) {
      gameFilenameLabel.setText(Messages.get("backglass.directb2s_sidebar.available_but_not_installed"));
    }
  }

  protected void setLabelAndTooltip(Label lbl, String txt) {
    lbl.setText(txt);
    lbl.setTooltip(new Tooltip(txt));
  }

  protected void setData(@Nullable DirectB2S model) {
    this.directb2s = model;
    this.refreshingCounter = 1;

    titleLabel.setText(model != null ? model.getName() : "-");
    openBtn.setDisable(model == null);

    // maintain current selection if possible
    directB2SCombo.getItems().clear();
    if (model != null) {
      // TODO why is this required? The versions of the model that is passed here are always "0" on second select
      //DirectB2SAndVersions directB2SVersions = client.getBackglassServiceClient().getDirectB2S(model.getEmulatorId(), model.getFileName());
      //List<String> versions = directB2SVersions.getVersions();
      List<String> versions = model.getVersions();

      versionPane.setVisible(true);
      noneActiveInfo.setVisible(!model.isEnabled());
      // choosing the active version only makes sense when there is more than one
      activateCheckbox.setVisible(versions.size() > 1);

      if (versions.size() > 1) {
        directB2SCombo.setVisible(true);
        directB2SLabel.setVisible(false);

        directB2SCombo.getItems().addAll(versions);
        // re-select previously one else first in the list
        if (latestSelection != null && versions.contains(latestSelection)) {
          directB2SCombo.getSelectionModel().select(latestSelection);
        }
        else {
          directB2SCombo.getSelectionModel().selectFirst();
        }
      }
      else if (versions.size() == 1) {
        directB2SCombo.setVisible(false);
        directB2SLabel.setVisible(true);

        directB2SLabel.setText(versions.getFirst());
      }  
    }
    else {
      versionPane.setVisible(false);
      activateCheckbox.setVisible(false);
    }

    refreshTableData(null);

    if (directb2s != null && getSelectedVersion() != null) {
      refreshingCounter++;
      JFXFuture.supplyAsync(() -> client.getBackglassServiceClient().getDirectB2SData(getEmulatorId(), getSelectedVersion()))
        .thenAcceptLater(data -> {
          // Ignore this old answer as a new backglass is now selected
          if (getEmulatorId() != data.getEmulatorId() || !getSelectedVersion().equals(data.getFilename())) {
            return;
          }

          this.tableData = data;
          refreshTableData(tableData);
          refreshingCounter--;
        });

      refreshStatusCheckbox();
    }  
    refreshTableSettings(model != null ? model.getGameId() : -1);
    this.refreshingCounter--;
  }

  protected void refreshTableSettings(int gameId) {
    this.tableSettings = null;

    dualModes.setDisable(true);
    usedLEDType.setDisable(true);
    glowing.setDisable(true);
    startAsExe.setDisable(true);

    formToPosition.setDisable(true);

    skipLampFrames.getValueFactory().valueProperty().set(0);
    skipGIFrames.getValueFactory().valueProperty().set(0);
    skipSolenoidFrames.getValueFactory().valueProperty().set(0);
    skipLEDFrames.getValueFactory().valueProperty().set(0);
    lightBulbOn.setDisable(true);
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
    formNoFocus.setDisable(true);
    formNoFocus.setSelected(false);
    startAsExeServerLabel.setText("");

    if (gameId > 0) {
      JFXFuture.supplyAllAsync(
              () -> client.getBackglassServiceClient().getTableSettings(gameId),
              () -> client.getBackglassServiceClient().getServerSettings())
          .thenAcceptLater((objs) -> {
            this.refreshingCounter++;

            this.tableSettings = (DirectB2STableSettings) objs[0];
            DirectB2ServerSettings serverSettings = (DirectB2ServerSettings) objs[1];

            boolean serverLaunchAsExe = serverSettings != null && serverSettings.getDefaultStartMode() == DirectB2ServerSettings.EXE_START_MODE;
            startAsExeServerLabel.setText(Messages.get(serverLaunchAsExe ? "backglass.sidebar.server_default_exe_on" : "backglass.sidebar.server_default_exe_off"));

            startAsExe.setDisable(false);
            startAsExe.setValue(TablesSidebarDirectB2SController.START_AS_EXE.stream().filter(v -> v.getId() == tableSettings.getStartAsEXE()).findFirst().get());

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

            dualModes.setValue(TablesSidebarDirectB2SController.DUAL_MODES.stream().filter(v -> v.getId() == tableSettings.getDualMode()).findFirst().orElse(null));

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
            formNoFocus.setDisable(false);
            formNoFocus.selectedProperty().setValue(tableSettings.isFormNoFocus());

            this.refreshingCounter--;
          });
    }
  }

  protected void disableCombosFrames() {
    skipLampFrames.setDisable(tableSettings == null || tableData == null || tableData.getIlluminations() == 0);
    skipGIFrames.setDisable(tableSettings == null || tableData == null || tableData.getIlluminations() == 0);
    skipSolenoidFrames.setDisable(tableSettings == null || tableData == null || tableData.getIlluminations() == 0);
    skipLEDFrames.setDisable(tableSettings == null || tableData == null || tableData.getIlluminations() == 0);

    dualModes.setDisable(tableSettings == null || tableData == null || tableData.getDualBackglass() == 0);
  }

  private void loadImages(int emulatorId, String fileName) {
    JFXFuture.supplyAsync(() -> {
          if (tableData != null && tableData.isBackgroundAvailable()) {
            String url = client.getBackglassServiceClient().getDirectB2sPreviewBackgroundUrl(emulatorId, fileName, true);
            return new Image(url);
          }
          return null;
        })
        .thenAcceptLater(_thumbnail -> {
          // Ignore old answer when a new backglass has been selected
          if (getEmulatorId() != emulatorId || !getSelectedVersion().equals(fileName)) {
            return;
          }

          showBackgroundImage(_thumbnail, false);
          if (_thumbnail != null) {
            downloadBackglassBtn.setDisable(false);
            if (tableSettings != null && tableSettings.isHideB2SBackglass()) {
              resolutionLabel.setText(Messages.get("backglass.directb2s_sidebar.backglass_hidden_2"));
              JFXHelper.setImageDisabled(thumbnailImage, true);
            }
            else {
              resolutionLabel.setText(Messages.get("backglass.sidebar.resolution", (int) _thumbnail.getWidth(), (int) _thumbnail.getHeight()));
            }
          }
        });

    JFXFuture.supplyAsync(() -> {
          if (tableData != null && tableData.isDmdImageAvailable()) {
            String url = client.getBackglassServiceClient().getDirectB2sDmdUrl(emulatorId, fileName);
            return new Image(url);
          }
          return null;
        })
        .thenAcceptLater(_dmdThumbnail -> {
          // Ignore old answer when a new backglass has been selected
          if (getEmulatorId() != emulatorId || !getSelectedVersion().equals(fileName)) {
            return;
          }

          uploadDMDBtn.setDisable(false);
          uploadDMDEmptyBtn.setDisable(false);
          showDmdImage(_dmdThumbnail, false);
          if (_dmdThumbnail != null) {
            downloadDMDBtn.setDisable(false);
            deleteDMDBtn.setDisable(false);
            if (tableSettings != null && tableSettings.isHideB2SDMD()) {
              dmdResolutionLabel.setText(Messages.get("backglass.directb2s_admin_filter.b2s_dmd_hidden"));
              JFXHelper.setImageDisabled(dmdThumbnailImage, true);
            } else {
              dmdResolutionLabel.setText(Messages.get("backglass.sidebar.resolution", (int) _dmdThumbnail.getWidth(), (int) _dmdThumbnail.getHeight()));
            }
            fullDmdLabel.setText(DirectB2SData.isFullDmd(_dmdThumbnail.getWidth(), _dmdThumbnail.getHeight()) ? Messages.get("common.yes") : Messages.get("common.no"));
          }
          else {
            dmdResolutionLabel.setText("");
            fullDmdLabel.setText(Messages.get("common.no"));
          }
        });
  }

  /**
   * Shows the background image, the loading indicator or the "no image" hint
   */
  private void showBackgroundImage(@Nullable Image image, boolean loading) {
    thumbnailImage.setImage(image);
    JFXHelper.setImageDisabled(thumbnailImage, false);
    thumbnailImage.setVisible(image != null);
    thumbnailProgress.setVisible(loading);
    thumbnailEmptyLabel.setVisible(image == null && !loading);
  }

  /**
   * Shows the DMD image, the loading indicator or the empty state with its upload button
   */
  private void showDmdImage(@Nullable Image image, boolean loading) {
    dmdThumbnailImage.setImage(image);
    JFXHelper.setImageDisabled(dmdThumbnailImage, false);
    dmdThumbnailImage.setVisible(image != null);
    dmdThumbnailProgress.setVisible(loading);
    dmdEmptyBox.setVisible(image == null && !loading);
  }

  /**
   * Shows the first validation error of the selected backglass, with an upload action for missing / wrong DMD images
   */
  protected void setValidation(int validationCode, @Nullable DirectB2SModel model) {
    if (validationCode > 0 && model != null) {
      LocalizedValidation validationMessage = BackglassValidationTexts.validate(validationCode, model);
      validationErrorLabel.setText(validationMessage.getLabel());
      validationErrorText.setText(validationMessage.getText());
      validationUploadDmdBtn.setVisible(validationCode == BackglassValidationCode.CODE_NO_FULLDMD
          || validationCode == BackglassValidationCode.CODE_WRONG_FULLDMD_RATIO);
      validationError.setVisible(true);
    }
    else {
      validationErrorLabel.setText("");
      validationErrorText.setText("");
      validationError.setVisible(false);
    }
  }

  public void setVisible(boolean visible) {
  }

  private void debounceAndSave(String debounceKey, Runnable modifications) {
    debouncer.debounce(debounceKey, () -> {
      save(modifications);
    }, DEBOUNCE_MS);
  }

  private void save(Runnable modifications) {
    if (this.game != null) {
      modifications.run();
      JFXFuture.runAsync(() -> client.getBackglassServiceClient().saveTableSettings(game.getId(), this.tableSettings))
        .thenLater(() -> BackglassManagerControllerUtils.notifyChange(getEmulatorId(), getSelectedVersion(), game))
        .onErrorLater(e -> {
          LOG.error("Failed to save B2STableSettings.xml: " + e.getMessage());
          WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("msg.failed_to_save_b2stablesettings_xml", e.getMessage()));
        });
    }
  }

  //---------------------------------------------

  public void setBackglassManagerController(BackglassManagerController backglassManagerController) {
    this.backglassManagerController = backglassManagerController;
  }

  private BackglassManagerController getBackglassManagerController() {
    return backglassManagerController;
  }
}

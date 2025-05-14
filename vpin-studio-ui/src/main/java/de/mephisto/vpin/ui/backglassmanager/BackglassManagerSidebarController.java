package de.mephisto.vpin.ui.backglassmanager;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.tables.TablesSidebarDirectB2SController;
import de.mephisto.vpin.ui.tables.dialogs.FrontendMediaUploadProgressModel;
import de.mephisto.vpin.ui.tables.models.*;
import de.mephisto.vpin.ui.tables.panels.BaseSideBarController;
import de.mephisto.vpin.ui.tables.panels.PlayButtonController;
import de.mephisto.vpin.ui.util.FileDragEventHandler;
import de.mephisto.vpin.ui.util.JFXHelper;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import de.mephisto.vpin.ui.util.StudioFolderChooser;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
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
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import org.apache.commons.io.IOUtils;
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
public class BackglassManagerSidebarController extends BaseSideBarController<DirectB2S> implements Initializable {

  private final static Logger LOG = LoggerFactory.getLogger(BackglassManagerController.class);

  private final Debouncer debouncer = new Debouncer();
  public static final int DEBOUNCE_MS = 100;

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
  private BorderPane thumbnailImagePane;

  @FXML
  private ImageView thumbnailImage;

  @FXML
  private BorderPane dmdThumbnailImagePane;

  @FXML
  private ImageView dmdThumbnailImage;

  @FXML
  private Pane versionSelector;

  @FXML
  private Pane noneActiveInfo;

  @FXML
  private Button downloadBackglassBtn;
  @FXML
  private Button useAsMediaBackglassBtn;

  @FXML
  private Button uploadDMDBtn;
  @FXML
  private Button downloadDMDBtn;
  @FXML
  private Button useAsMediaDMDBtn;
  @FXML
  private Button deleteDMDBtn;

  @FXML
  private CheckBox activateCheckbox;

  @FXML
  private ComboBox<String> directB2SCombo;

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
  private Button reloadBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Label gameLabel;

  @FXML
  private Label gameFilenameLabel;

  @FXML
  private Label emulatorNameLabel;

  @FXML
  private Button dataManagerBtn;

  @FXML
  private Button tableNavigateBtn;


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
  private void onBackglassDelete(Event e) {
//    try {
//      DirectB2SModel selection = backglassManagerController.getSelectedModel();
//      if (selection != null) {
//        //Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
//        Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Backglass", "Delete backglass file \"" + selection.getFileName() + "\"?", "This also deletes all version of this backglass.", "Delete");
//        if (result.isPresent() && result.get().equals(ButtonType.OK)) {
//          if (client.getBackglassServiceClient().deleteBackglass(selection.getEmulatorId(), selection.getFileName())) {
//            backglassManagerController.delete(selection);
//          }
//        }
//      }
//    }
//    catch (Exception ex) {
//      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to delete backglass file: " + ex.getMessage());
//    }
    try {
      DirectB2SModel selection = backglassManagerController.getSelectedModel();
      String selectedVersion = getSelectedVersion();
      if (selection != null && selectedVersion != null) {
        Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Backglass", "Delete backglass file \"" + selectedVersion + "\"?", null, "Delete");
        if (result.isPresent() && result.get().equals(ButtonType.OK)) {
          client.getBackglassServiceClient().deleteBackglassVersion(selection.getEmulatorId(), selectedVersion);
          backglassManagerController.delete(selection);
        }
      }
    }
    catch (Exception ex) {
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to delete backglass file: " + ex.getMessage());
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
      BackglassManagerControllerUtils.updateDMDImage(getEmulatorId(), getSelectedVersion(), game, selection);
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

  private void uploadImageAsMedia(@NonNull GameRepresentation game, VPinScreen screen, String screenName, Image img) throws IOException {
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
      BackglassManagerControllerUtils.deleteDMDImage(getEmulatorId(), getSelectedVersion(), game);
    }
  }

  @FXML
  private void onGameLaunch() {
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
            backglassManagerController.reloadItem(b2s);
          })
          .onErrorLater((e) -> WidgetFactory.showAlert(stage, "Error", "Cannot set " + selectedVersion + " as default", e.getMessage()));
    }
  }

  private void setDirectB2SDisabled() {
    DirectB2S selectedItem = backglassManagerController.getSelection();
    String selectedVersion = getSelectedVersion();
    if (selectedItem != null && selectedVersion != null) {
      JFXFuture
          .supplyAsync(() -> client.getBackglassServiceClient().disableBackglass(selectedItem.getEmulatorId(), selectedVersion))
          .thenAcceptLater((b2s) -> {
            backglassManagerController.reloadItem(b2s);
          })
          .onErrorLater((e) -> WidgetFactory.showAlert(stage, "Error", "Cannot set " + selectedVersion + " as default", e.getMessage()));
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

  //------------------------------------------------ EVENT CASCADING ---

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    versionSelector.managedProperty().bindBidirectional(versionSelector.visibleProperty());
    noneActiveInfo.managedProperty().bindBidirectional(noneActiveInfo.visibleProperty());

    FrontendType frontendType = Studio.client.getFrontendService().getFrontendType();
    if (!frontendType.supportMedias()) {
      HBox bgtoolbar = (HBox) this.useAsMediaBackglassBtn.getParent();
      bgtoolbar.getChildren().remove(useAsMediaBackglassBtn);

      HBox dmdtoolbar = (HBox) this.useAsMediaDMDBtn.getParent();
      dmdtoolbar.getChildren().remove(useAsMediaDMDBtn);
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

    // add the overlay for DMD image drag    
    FileDragEventHandler.install(loaderStackImages, dmdThumbnailImagePane, true, "png", "jpg", "jpeg")
        .setOnDragDropped(e -> {
          List<File> files = e.getDragboard().getFiles();
          if (files != null && files.size() == 1) {
            File selection = files.get(0);
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
    String selectedVersion = directB2SCombo.getValue();
    if (selectedVersion != null && directb2s != null) {
      boolean selected = FileUtils.baseNameMatches(selectedVersion, directb2s.getFileName());
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
    downloadDMDBtn.setDisable(true);
    deleteDMDBtn.setDisable(true);

    thumbnailImage.setImage(new Image(Studio.class.getResourceAsStream("empty-preview.png")));
    JFXHelper.setImageDisabled(thumbnailImage, false);
    dmdThumbnailImage.setImage(new Image(Studio.class.getResourceAsStream("empty-preview.png")));
    JFXHelper.setImageDisabled(dmdThumbnailImage, false);
    resolutionLabel.setText("");
    dmdResolutionLabel.setText("");
    fullDmdLabel.setText("");

    reloadBtn.setDisable(true);
    deleteBtn.setDisable(true);

    if (directB2SData != null) {
      thumbnailImagePane.setCenter(new ProgressIndicator());
      dmdThumbnailImagePane.setCenter(new ProgressIndicator());

      loadImages(directB2SData.getEmulatorId(), directB2SData.getFilename());

      setLabelandTooltip(filenameLabel, directB2SData.getFilename());
      setLabelandTooltip(nameLabel, directB2SData.getName());
      typeLabel.setText(DirectB2SData.getTableType(directB2SData.getTableType()));
      authorLabel.setText(directB2SData.getAuthor());
      artworkLabel.setText(directB2SData.getArtwork());
      grillLabel.setText(String.valueOf(directB2SData.getGrillHeight()));
      b2sElementsLabel.setText(String.valueOf(directB2SData.getB2sElements()));
      b2sElementsLabel.setTooltip(new Tooltip(directB2SData.getB2sElements() + " variants exist for this backglass"));
      scoresLabel.setText(String.valueOf(directB2SData.getNbScores()));
      playersLabel.setText(String.valueOf(directB2SData.getNumberOfPlayers()));
      filesizeLabel.setText(FileUtils.readableFileSize(directB2SData.getFilesize()));
      bulbsLabel.setText(String.valueOf(directB2SData.getIlluminations()));

      disableCombosFrames();

      modificationDateLabel.setText(SimpleDateFormat.getDateTimeInstance().format(directB2SData.getModificationDate()));

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
    if (selection != null && selection.getNbVersions() > 1) {
      selectedVersion = directB2SCombo.getValue();   // alternative check:  versionSelector.isVisible()
    }
    if (selectedVersion == null && selection != null && selection.getNbVersions() > 0) {
      selectedVersion = selection.getVersion(0);
    }
    return selectedVersion;
  }

  protected void resetGame() {
      // set emulator name
      emulatorNameLabel.setText("-");
      gameLabel.setText("-");
      gameFilenameLabel.setText("-");
    
      // depend on the game selection
      useAsMediaBackglassBtn.setDisable(true);
      useAsMediaDMDBtn.setDisable(true);
  
      this.dataManagerBtn.setDisable(true);
      this.tableNavigateBtn.setDisable(true);
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

      setLabelandTooltip(gameLabel, game.getGameDisplayName());
      setLabelandTooltip(gameFilenameLabel, game.getGameFileName());

      useAsMediaBackglassBtn.setDisable(false);
      useAsMediaDMDBtn.setDisable(false);

      dataManagerBtn.setDisable(false);
      tableNavigateBtn.setDisable(false);
    }
    else if (gameAvailable) {
      gameFilenameLabel.setText("(Available, but not installed)");
    }
  }

  protected void setLabelandTooltip(Label lbl, String txt) {
    lbl.setText(txt);
    lbl.setTooltip(new Tooltip(txt));
  }

  protected void setData(@Nullable DirectB2S model) {
    this.directb2s = model;

    // maintain current selection if possible
    directB2SCombo.getItems().clear();
    noneActiveInfo.setVisible(false);
    if (model != null) {
      // TODO why is this required? The versions of the model that is passed here are always "0" on second select
      //DirectB2SAndVersions directB2SVersions = client.getBackglassServiceClient().getDirectB2S(model.getEmulatorId(), model.getFileName());
      //List<String> versions = directB2SVersions.getVersions();
      List<String> versions = model.getVersions();

      versionSelector.setVisible(versions.size() > 1);

      if (versions.size() > 1) {
        directB2SCombo.getItems().addAll(versions);
        // re-select previously one else first in the list
        if (latestSelection != null && versions.contains(latestSelection)) {
          directB2SCombo.getSelectionModel().select(latestSelection);
        }
        else {
          directB2SCombo.getSelectionModel().selectFirst();
        }
      }
      noneActiveInfo.setVisible(!model.isEnabled());
    }
    else {
      versionSelector.setVisible(false);
    }

    refreshTableData(null);

    if (directb2s != null && getSelectedVersion() != null) {
      refreshingCounter++;
      JFXFuture.supplyAsync(() -> client.getBackglassServiceClient().getDirectB2SData(getEmulatorId(), getSelectedVersion()))
        .thenAcceptLater(data -> {
          this.tableData = data;
          refreshTableData(tableData);
          refreshingCounter--;
        });

      refreshStatusCheckbox();
    }  
    refreshTableSettings(model != null ? model.getGameId() : -1);
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

            DirectB2ServerSettings serverSettings = client.getBackglassServiceClient().getServerSettings();
            boolean serverLaunchAsExe = serverSettings != null && serverSettings.getDefaultStartMode() == DirectB2ServerSettings.EXE_START_MODE;
            startAsExeServer.setSelected(serverLaunchAsExe);
            startAsExeServer.setDisable(true);

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
    JFXFuture.supplyAsync(() -> {
          if (tableData != null && tableData.isBackgroundAvailable()) {
            String url = client.getBackglassServiceClient().getDirectB2sPreviewBackgroundUrl(emulatorId, fileName, true);
            return new Image(url);
          }
          return null;
        })
        .thenAcceptLater(_thumbnail -> {
          if (_thumbnail != null) {
            thumbnailImage.setImage(_thumbnail);
            thumbnailImagePane.setCenter(thumbnailImage);
            downloadBackglassBtn.setDisable(false);
            if (tableSettings.isHideB2SBackglass()) {
              resolutionLabel.setText("Backglass Hidden.");
              JFXHelper.setImageDisabled(thumbnailImage, true);
            }
            else {
              resolutionLabel.setText("Resolution: " + (int) _thumbnail.getWidth() + " x " + (int) _thumbnail.getHeight());
            }
          }
          else {
            thumbnailImage.setImage(null);
            thumbnailImagePane.setCenter(null);
            resolutionLabel.setText("No Image data available.");
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
          uploadDMDBtn.setDisable(false);
          if (_dmdThumbnail != null) {
            dmdThumbnailImage.setImage(_dmdThumbnail);
            dmdThumbnailImagePane.setCenter(dmdThumbnailImage);
            downloadDMDBtn.setDisable(false);
            deleteDMDBtn.setDisable(false);
            if (tableSettings.isHideB2SDMD()) {
              dmdResolutionLabel.setText("B2S DMD Hidden");
              JFXHelper.setImageDisabled(dmdThumbnailImage, true);
            } else {
              dmdResolutionLabel.setText("Resolution: " + (int) _dmdThumbnail.getWidth() + " x " + (int) _dmdThumbnail.getHeight());
            }
            fullDmdLabel.setText(DirectB2SData.isFullDmd(_dmdThumbnail.getWidth(), _dmdThumbnail.getHeight()) ? "Yes" : "No");
          }
          else {
            dmdThumbnailImage.setImage(null);
            dmdThumbnailImagePane.setCenter(null);
            dmdResolutionLabel.setText("No DMD background available.");
            fullDmdLabel.setText("No");
          }
        });
  }

  public void setVisible(boolean visible) {
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

  //---------------------------------------------

  public void setBackglassManagerController(BackglassManagerController backglassManagerController) {
    this.backglassManagerController = backglassManagerController;
  }

  private BackglassManagerController getBackglassManagerController() {
    return backglassManagerController;
  }
}

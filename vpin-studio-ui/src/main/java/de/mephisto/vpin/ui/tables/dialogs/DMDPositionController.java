package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2sScreenRes;
import de.mephisto.vpin.restclient.dmd.DMDAspectRatio;
import de.mephisto.vpin.restclient.dmd.DMDInfo;
import de.mephisto.vpin.restclient.dmd.DMDInfoZone;
import de.mephisto.vpin.restclient.dmd.DMDType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.backglassmanager.BackglassManagerController;
import de.mephisto.vpin.ui.backglassmanager.BackglassManagerControllerUtils;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.FileDragEventHandler;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.fx.DialogHeaderController;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.BoundingBox;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;

import static de.mephisto.vpin.ui.Studio.client;

public class DMDPositionController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(DMDPositionController.class);

  @FXML
  protected DialogHeaderController headerController;  //fxml magic! Not unused -> id + "Controller"

  private BackglassManagerController backglassMgrController;

  @FXML
  private Label titleLabel;
  @FXML
  private Button prevButton;
  @FXML
  private Button nextButton;
  @FXML
  private CheckBox autosaveCheckbox;

  @FXML
  private TabPane tabPane;
  @FXML
  private Tab playfieldTab;
  @FXML
  private Tab backglassTab;
  @FXML
  private Tab dmdTab;

  @FXML
  private Label romLabel;
  @FXML
  private Label tablePositionLabel;
  @FXML
  private ComboBox<DMDType> DMDTypeCombo;

  @FXML
  private RadioButton ratioOff;
  @FXML
  private RadioButton ratio3;
  @FXML
  private RadioButton ratio4;
  @FXML
  private RadioButton ratio8;

  @FXML
  private RadioButton radioOnBackglass;
  @FXML
  private RadioButton radioOnB2sDMD;
  @FXML
  private RadioButton radioOnPlayfield;

  @FXML
  private HBox disablePane;
  @FXML
  private CheckBox disableViaMameCheckbox;
  @FXML
  private CheckBox disableViaIniCheckbox;

  @FXML
  private VBox backglassScorePane;
  @FXML
  private CheckBox disableBGScoreCheckbox;
  @FXML
  private Button resetToScoresBtn;

  @FXML
  private HBox romPane;
  @FXML
  private GridPane spinnersPane;
  @FXML
  private VBox radioOnPane;
  @FXML
  private VBox ratiosPane;
  @FXML
  private GridPane autoPositionPane;

  @FXML
  private Spinner<Integer> xSpinner;
  @FXML
  private Spinner<Integer> ySpinner;
  @FXML
  private Spinner<Integer> widthSpinner;
  @FXML
  private Spinner<Integer> heightSpinner;

  @FXML
  private Spinner<Integer> marginSpinner;

  @FXML
  private Button autoPositionBtn;
  @FXML
  private Button centerXBtn;

  @FXML
  private Button saveLocallyBtn;
  @FXML
  private Button saveCloseLocallyBtn;
  @FXML
  private Button saveGloballyBtn;

  @FXML
  private BorderPane parentpane;
  @FXML
  private Pane imagepane;

  private VPinScreen loadedVpinScreen; 

  private ProgressIndicator progressIndicator = new ProgressIndicator();

  @FXML
  private ImageView fullDMDImage;
  /** The zoom factor of the image from screenres to ImageView */
  private double zoom = 1.0;
  /** The bounding box of the image */
  private BoundingBox bounds;

  @FXML
  private VBox noFullDMDPane;
  @FXML
  private VBox uploadFullDMDImagePane;
  @FXML
  private VBox activateFullDMDImagePane;
  @FXML
  private VBox useFullDMDMediaPane;

  @FXML
  private Rectangle emptyImage;

  private GameRepresentation game;

  private DirectB2sScreenRes screenres;

  private List<DMDPositionResizer> dragBoxes = new ArrayList<>();

  private DMDInfo dmdinfo;

  private DMDInfoZone selectedZone;

  private ToggleGroup ratioRadioGroup;
  
 
  @FXML
  protected void onPrevious(ActionEvent e) {
    onAutosave(() -> {
      clearGame();
      switchGame(backglassMgrController.selectPreviousGame());  
    });
  }

  @FXML
  protected void onNext(ActionEvent e) {
    onAutosave(() -> {
      clearGame();
      switchGame(backglassMgrController.selectNextGame());
    });
  }

 protected void onAutosave(@NonNull Runnable onSuccess) {
    if (autosaveCheckbox.isSelected()) {
      onSaveClick(true, onSuccess);
    }
    else if (headerController.isDirty()) {
      Optional<ButtonType> result = WidgetFactory.showYesNoConfirmation(Studio.stage, "You have unsaved changes.", "Do you want to save them ?");
      if (result.isPresent() && result.get().equals(ButtonType.YES)) {
        onSaveClick(true, onSuccess);
        return;
      }
      else if (result.isPresent() && result.get().equals(ButtonType.NO)) {
        onSuccess.run();
      }
      else {
        // click cancel, stay on the table
        return;
      }
    }
    else {
      onSuccess.run();
    }
  }

  /**
   * Start th espinning indicator OR Stop spinning and display the image back
   */
  public void setWait(boolean on) {
    parentpane.setCenter(on ? progressIndicator : imagepane);
  }

  private void clearGame() {
    headerController.setDirty(false);
    titleLabel.setText("loading...");
    romLabel.setText("--");
    tablePositionLabel.setText("");
    DMDTypeCombo.setValue(null);
    disableAll();
    loadImage(null, false);
    loadDragBoxes(null, false);
  }

  private void switchGame(int gameId) {
    JFXFuture.supplyAsync(() -> client.getGame(gameId))
    .thenAcceptLater(game -> {
      if (game != null) {
        setGame(game, backglassMgrController);
      }
    });
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSaveGloballyClick(ActionEvent e) {
    onSaveClick(false, null);
  }

  @FXML
  private void onSaveLocallyClick(ActionEvent e) {
    onSaveClick(true, null);
  }

  @FXML
  private void onSaveCloseLocallyClick(ActionEvent e) {
    onSaveClick(true, () -> {
      Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
      stage.close();
    });
  }

  private void onSaveClick(boolean locally, @Nullable Runnable onSuccess) {
    JFXFuture.runAsync(() -> {
          DMDInfo dmdinfo = fillDmdInfo();
          dmdinfo.setLocallySaved(locally);
          LOG.info("Saving dmdinfo for game {} : {}", game.getGameFileName(), dmdinfo);
          client.getDmdPositionService().saveDMDInfo(dmdinfo);
        })
        .thenLater(() -> {
          headerController.setDirty(false);
          if (onSuccess != null) {
            onSuccess.run();
          }
        })
        .onErrorLater(e -> {
          WidgetFactory.showAlert(headerController.getStage(), "Cannot save DMD Position", e.getMessage());
        });
  }

  @Override
  public void onKeyPressed(KeyEvent ke) {
    for (DMDPositionResizer dragBox : dragBoxes) {
      if (dragBox.keyPressed(ke)) {
        ke.consume();
        return;
      }
    }
    // else process dialog keys
    KeyCode code = ke.getCode();
    switch (code) {
      case PAGE_DOWN: {
        onNext(null); 
        return;
      }
      case PAGE_UP: {
        onPrevious(null); 
        return;
      }
      case S: {
        if (ke.isControlDown()) {
          onSaveLocallyClick(null);
          return;
        }
      }
    }
  }

  @FXML
  private void onAutoPosition() {
    if (selectedZone != null) {
      LOG.info("Autoposition dmdinfo for game {}", game.getGameFileName());
      // set the margin for auto positioning
      selectedZone.setMargin(marginSpinner.getValue());
      selectedZone.setOnScreen(loadedVpinScreen);

      disableAll();
      JFXFuture.supplyAsync(() -> client.getDmdPositionService().autoPositionDMDInfo(dmdinfo.getGameId(), selectedZone))
          .thenAcceptLater(newZone -> setDmdInfoZone(selectedZone, newZone));
    }
  }

  @FXML
  private void onResetToScores() {
    DMDInfo movedDmdinfo = fillDmdInfo();
    LOG.info("Reseting Zones to Scores positionsfor game {}", game.getGameFileName());
    disableAll();
    JFXFuture.supplyAsync(() -> client.getDmdPositionService().resetToScores(movedDmdinfo))
        .thenAcceptLater(dmd -> setDmdInfo(dmd, false, true));
  }

  @FXML
  private void onCenterX() {
    for (DMDPositionResizer dragBox : dragBoxes) {
      if (dragBox.isSelected()) {
        dragBox.centerHorizontally();
      }
    }
  }

  @FXML
  private void onFullDMDFileSelect(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select DMD Image");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg", "*.jpeg"));

    File selection = fileChooser.showOpenDialog(stage);
    if (selection != null) {
      updateDMDImage(selection);
    }
  }

  private void updateDMDImage(File selection) {
    String baseName = FilenameUtils.getBaseName(game.getGameFileName());
    File folder = new File(game.getGameFileName()).getParentFile();
    String directB2SFilename = new File(folder, baseName + ".directb2s").toString();
    BackglassManagerControllerUtils.updateDMDImage(game.getEmulatorId(), directB2SFilename, game, selection);
    loadImage(loadedVpinScreen, true);
  }

  @FXML
  private void onActivateDMDImageSelect(ActionEvent event) {
    doPerformImageAction(dmd -> {
        DirectB2STableSettings tableSettings = client.getBackglassServiceClient().getTableSettings(dmd.getGameId());
        tableSettings.setHideB2SDMD(false);
        client.getBackglassServiceClient().saveTableSettings(dmd.getGameId(), tableSettings);
        return dmd;
      }, true);
  }

  @FXML
  private void onFullDMDMediaUse() {
    doPerformImageAction(dmd -> client.getDmdPositionService().useFrontendFullDMDMedia(dmd), false);
  }

  @FXML
  private void onFullDMDMediaGrab() {
    doPerformImageAction(dmd -> client.getDmdPositionService().grabFrontendFullDMDMedia(dmd), true);
  }

  private void doPerformImageAction(Function<DMDInfo, DMDInfo> action, boolean notifyTableChange) {
    DMDInfo movedDmdinfo = fillDmdInfo();
    disableAll();
    setWait(true);
    JFXFuture.supplyAsync(() -> action.apply(movedDmdinfo))
        .thenAcceptLater(dmd -> {
          setWait(false);
          setDmdInfo(dmd, false, false);
          loadImage(loadedVpinScreen, true);

          // also notify other components
          if (notifyTableChange) {
            EventManager.getInstance().notifyTableChange(dmd.getGameId(), null);
          }
        });
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    // listener on tab selection
    this.tabPane.getSelectionModel().selectedItemProperty().addListener((ov, oldT, newT) -> {
      VPinScreen selectedScreen = tabToScreen(newT);
      selectTab(selectedScreen, false);
    });

    saveLocallyBtn.managedProperty().bindBidirectional(saveLocallyBtn.visibleProperty());
    saveCloseLocallyBtn.managedProperty().bindBidirectional(saveCloseLocallyBtn.visibleProperty());
    saveGloballyBtn.managedProperty().bindBidirectional(saveGloballyBtn.visibleProperty());
    saveLocallyBtn.setVisible(false);
    saveCloseLocallyBtn.setVisible(false);
    saveGloballyBtn.setVisible(false);

    // create a toggle group
    ratioRadioGroup = new ToggleGroup();
    ratioOff.setToggleGroup(ratioRadioGroup);
    ratioOff.setUserData(DMDAspectRatio.ratioOff);
    ratio3.setToggleGroup(ratioRadioGroup);
    ratio3.setUserData(DMDAspectRatio.ratio3x1);
    ratio4.setToggleGroup(ratioRadioGroup);
    ratio4.setUserData(DMDAspectRatio.ratio4x1);
    ratio8.setToggleGroup(ratioRadioGroup);
    ratio8.setUserData(DMDAspectRatio.ratio8x1);

    ratioOff.setSelected(true);
    ratioRadioGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
      @Override
      public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
        if(newValue != null) {
          DMDAspectRatio aspectRatio = (DMDAspectRatio) newValue.getUserData();
          for (DMDPositionResizer dragBox : dragBoxes) {
            dragBox.setAspectRatio(aspectRatio.getValue());
          }
        }
        headerController.setDirty(true);
      }
    });

    // add a selector in the pane to draw a rectangle.
    //new DMDPositionSelection(imagepane, area, ratioProperty, color,
      // called on drag start, hide the lime dragbox
      //() -> {
        //selectedBox.setVisible(false);
      //},
      // called once dragged, show the dragbox back and reposition/resize it
      //rect -> {
        //dragBox.setVisible(true);
        //dragBox.select();

        //dragBox.setX(rect.getMinX());
        //dragBox.setY(rect.getMinY());
        //dragBox.setWidth(rect.getWidth());
        //dragBox.setHeight(rect.getHeight());
      //});

    emptyImage = new Rectangle();
    emptyImage.setFill(Color.grayRgb(30));
    emptyImage.setStroke(Color.BLACK);

    // create a toggle group 
    ToggleGroup tg = new ToggleGroup();
    radioOnPlayfield.setToggleGroup(tg);
    radioOnPlayfield.setUserData(VPinScreen.PlayField);
    radioOnBackglass.setToggleGroup(tg);
    radioOnBackglass.setUserData(VPinScreen.BackGlass);
    radioOnB2sDMD.setToggleGroup(tg);
    radioOnB2sDMD.setUserData(VPinScreen.Menu);

    tg.selectedToggleProperty().addListener((obs, o, n) -> {
      // make sure the image of the screen is loaded
      VPinScreen selectedScreen = n != null? (VPinScreen) n.getUserData() : null;
      //loadImage(selectedScreen);
      
      if (selectedZone != null) {

        // prevent a double load of image at dialog opening
        if (selectedScreen != null && !selectedScreen.equals(selectedZone.getOnScreen())) {
          DMDInfo movedDmdinfo = fillDmdInfo();

          LOG.info("Moving dmdinfo for game {} : {}", game.getGameFileName(), movedDmdinfo);
          disableAll();
          JFXFuture.supplyAsync(() -> client.getDmdPositionService().moveDMDInfo(movedDmdinfo.getGameId(), selectedZone, selectedScreen))
              .thenAcceptLater(newZone -> setDmdInfoZone(selectedZone, newZone));
        }
      }
    });

    ratioRadioGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
      @Override
      public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
        DMDInfo dmdinfo = fillDmdInfo();
        loadDmdInfo(dmdinfo);
        headerController.setDirty(true);
      }
    });

    // by default do not use alphanumeric
    DMDTypeCombo.setItems(FXCollections.observableArrayList(DMDType.NoDMD, DMDType.VirtualDMD, DMDType.VpinMAMEDMD));
    DMDTypeCombo.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      if (t1 != null && !dmdinfo.getDMDType().equals(t1)) {
        DMDInfo movedDmdinfo = fillDmdInfo();

        LOG.info("Swicthing dmdinfo for game {} to {}", game.getGameFileName(), t1);
        disableAll();
        JFXFuture.supplyAsync(() -> client.getDmdPositionService().switchDMDInfo(movedDmdinfo, t1))
            .thenAcceptLater(dmd -> setDmdInfo(dmd, false, true));
      }
    });

    disableViaMameCheckbox.selectedProperty().addListener((obs, oldV, newV) -> {
      if (!newV) {
        disableViaIniCheckbox.setSelected(true);
      }
      headerController.setDirty(true);
    });
    disableViaIniCheckbox.selectedProperty().addListener((obs, oldV, newV) -> {
      if (!newV) {
        disableViaMameCheckbox.setSelected(true);
      }
      headerController.setDirty(true);
    });

    // by default hide the noFullDMD Pane
    noFullDMDPane.setVisible(false);

    // add the overlay for DMD image drag    
    FileDragEventHandler.install(imagepane, fullDMDImage, true, "png", "jpg", "jpeg")
        .setOnDragFilter(files -> {
          return VPinScreen.Menu.equals(this.loadedVpinScreen);
        })
        .setOnDragDropped(e -> {
          List<File> files = e.getDragboard().getFiles();
          if (files != null && files.size() == 1) {
            File selection = files.get(0);
            Platform.runLater(() -> updateDMDImage(selection));
          }
        });

    // Load user preferences
    try {
      UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS);
      autosaveCheckbox.setSelected(uiSettings.isAutoSaveDmdPosition());
      autosaveCheckbox.selectedProperty().addListener((obs, o, v) -> {
        UISettings modifiedUISettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS);
        modifiedUISettings.setAutoSaveDmdPosition(v);
        client.getPreferenceService().setJsonPreference(modifiedUISettings);
      });
    }
    catch (Exception e) {
      LOG.error("Cannot set UI preference, exception ignored : " + e.getMessage());
    }
  }

  private void configureSpinner(Spinner<Integer> spinner, ObjectProperty<Integer> property,
                                ReadOnlyObjectProperty<Integer> minProperty, ReadOnlyObjectProperty<Integer> maxProperty) {

    IntegerSpinnerValueFactory factory = new IntegerSpinnerValueFactory(minProperty.get(), maxProperty.get());
    spinner.setValueFactory(factory);
    spinner.setEditable(true);
    factory.valueProperty().bindBidirectional(property);
    factory.minProperty().bind(minProperty);
    factory.maxProperty().bind(maxProperty);
    factory.valueProperty().addListener((obs, o, v) -> headerController.setDirty(true));
  }

  @Override
  public void onDialogCancel() {
  }

  public void setGame(GameRepresentation gameRepresentation, @Nullable BackglassManagerController backglassMgrController) {
    this.game = gameRepresentation;
    this.backglassMgrController = backglassMgrController;
    titleLabel.setText(game.getGameDisplayName());

    saveLocallyBtn.setVisible(backglassMgrController != null);
    prevButton.setVisible(backglassMgrController != null);
    nextButton.setVisible(backglassMgrController != null);

    disableAll();
    setWait(true);
    JFXFuture.supplyAllAsync(
      () -> client.getBackglassServiceClient().getScreenRes(game.getEmulatorId(), game.getGameFileName(), false),
      () -> client.getDmdPositionService().getDMDInfo(game.getId()))
    .thenAcceptLater((objs) -> {
      // stop spinning and display the image back
      setWait(false);

      this.screenres = (DirectB2sScreenRes) objs[0];
      setDmdInfo((DMDInfo) objs[1], true, false);
    })
    .onErrorLater(ex -> setDmdError(ex));
  }

  private void setDmdError(Throwable e) {
    LOG.error("Error getting dmd information, set an empty one");
    this.dmdinfo = new DMDInfo();
    WidgetFactory.showAlert(Studio.stage, "Getting DMD information failed", 
      "Please check your screenres.txt or <tablename>.res file exists, else check the server logs");
  }

  private void disableAll() {
    _disableButtons(true);
    _disableSidebar(true);
    disableForZone(true);
    tabPane.setDisable(true);
  }
  private void enableDmd() {
    _disableButtons(false);
    _disableSidebar(false);
    tabPane.setDisable(false);
  }
  private void _disableButtons(boolean disabled) {
    saveLocallyBtn.setDisable(disabled);
    saveCloseLocallyBtn.setDisable(disabled);
    saveGloballyBtn.setDisable(disabled);
  }
  private void _disableSidebar(boolean disabled) {
    DMDTypeCombo.setDisable(disabled);
    disablePane.setDisable(disabled);
    backglassScorePane.setDisable(disabled);
    //romPane.setDisable(disabled);
  } 

  private void disableForZone(boolean disabled) {
    radioOnPane.setDisable(disabled);
    spinnersPane.setDisable(disabled);
    ratiosPane.setDisable(disabled);
    autoPositionPane.setDisable(disabled);
  }

  private void setDmdInfoZone(DMDInfoZone oldZone, DMDInfoZone zone) {
    // re-enable sidebar, zone elements will be enabled by selectTab
    enableDmd();

    int pos = dmdinfo.getZones().indexOf(oldZone);
    if (pos >= 0) {
      dmdinfo.getZones().remove(pos);
      dmdinfo.getZones().add(pos, zone);
      this.selectedZone = zone;
      selectTab(zone.getOnScreen(), false);
    }
    // method called either after autoposition or move, so DMD is modified
    headerController.setDirty(true);
  }

  /**
   * To be called on a Thread, from a Future, as it loads the image synchronously
   * in order to calculate the bounds of the image
   */
  private void setDmdInfo(DMDInfo dmdinfo, boolean forceRefresh, boolean dirty) {
    LOG.info("Received dmdinfo for game {} : {}", game.getGameFileName(), dmdinfo);
    this.dmdinfo = dmdinfo;

    // re-enable buttons
    enableDmd();

    String storename = StringUtils.defaultString(dmdinfo.getDmdStoreName(), dmdinfo.getGameRom());

    romLabel.setText(StringUtils.defaultIfEmpty(storename, "--"));

    List<DMDType> types = dmdinfo.isSupportAlphaNumericDmd() ? 
      Arrays.asList(DMDType.NoDMD, DMDType.VirtualDMD, DMDType.AlphaNumericDMD, DMDType.VpinMAMEDMD) :
      Arrays.asList(DMDType.NoDMD, DMDType.VirtualDMD, DMDType.VpinMAMEDMD);
    DMDTypeCombo.setItems(FXCollections.observableArrayList(types));
    DMDTypeCombo.setValue(dmdinfo.getDMDType());

    boolean canSelectHowToDisable = dmdinfo.isDisabled(); //&& dmdinfo.isUseFreezy()
    disablePane.setManaged(canSelectHowToDisable);
    disablePane.setVisible(canSelectHowToDisable);

    tablePositionLabel.setText((dmdinfo.isLocallySaved() ? "Locally" : "Globally") + " in " 
        + (dmdinfo.isUseRegistry() ? "registry" : "dmddevice.ini"));

    boolean isAlpha = DMDType.AlphaNumericDMD.equals(dmdinfo.getDMDType());
    backglassScorePane.setManaged(isAlpha);
    backglassScorePane.setVisible(isAlpha);

    // no local save if no rom
    if (storename != null) {

      if (backglassMgrController != null) {
        saveLocallyBtn.setText("Save for " + storename);
        saveLocallyBtn.setVisible(true);

        saveCloseLocallyBtn.setText("Save & Close for " + storename);
      }
      else {
        saveCloseLocallyBtn.setText("Save for " + storename);
      }
      saveCloseLocallyBtn.setVisible(true);
    }

    // no global save when using registry
    //if (!dmdinfo.isUseRegistry()) {
    //  saveGloballyBtn.setVisible(true);
    //}

    // when dmd is auto positioned on grill, size can be 0x0 so turn option off 
    boolean hasDMD = dmdinfo.isSupportFullDmd();
    radioOnB2sDMD.setManaged(hasDMD);
    radioOnB2sDMD.setVisible(hasDMD);

    if (hasDMD && !tabPane.getTabs().contains(dmdTab)) {
      tabPane.getTabs().add(dmdTab);
    }
    else if (!hasDMD && tabPane.getTabs().contains(dmdTab)) {
      tabPane.getTabs().remove(dmdTab);
    }

    // select first zone
    VPinScreen selectedScreen = loadedVpinScreen;
    if (forceRefresh || selectedScreen == null) {
      selectedScreen = dmdinfo.getZones().size() > 0 ? 
        dmdinfo.getZones().get(0).getOnScreen() : VPinScreen.BackGlass;
    }
    selectTab(selectedScreen, forceRefresh);

    disableViaIniCheckbox.setSelected(dmdinfo.isDisableViaIni());
    disableViaMameCheckbox.setSelected(dmdinfo.isDisableInVpinMame());
    disableBGScoreCheckbox.setSelected(dmdinfo.isDisableBackglassScores());

    loadDmdInfo(dmdinfo);
    headerController.setDirty(dirty);
  }

  private void loadImage(VPinScreen onScreen, boolean forceRefresh) {
  
    // resize the preview proportionnaly to the real screen size, 
    // determine if this is fitWidth or fitHeight that must be adjusted 
    double screenWidth = onScreen != null? screenres.getScreenWidth(onScreen) : 1024.0;
    double screenHeight = onScreen != null? screenres.getScreenHeight(onScreen) : 768.0;
    boolean adjustWidth = screenWidth / screenHeight < 1024.0 / 768.0;
    final double fitWidth = adjustWidth ? 768.0 * screenWidth / screenHeight : 1024.0;
    final double fitHeight = adjustWidth ? 768.0 : 1024.0 * screenHeight / screenWidth;

    if (onScreen == null) {
      fullDMDImage.setImage(null);
      // add an empty background to simulate an image 
      imagepane.getChildren().remove(emptyImage);
      emptyImage.setWidth(fitWidth);
      emptyImage.setHeight(fitHeight);
      imagepane.getChildren().add(0, emptyImage);
      parentpane.setCenter(imagepane);
      loadedVpinScreen = null;
    }
    else if (forceRefresh || !onScreen.equals(loadedVpinScreen)) {
      setWait(true);

      noFullDMDPane.setVisible(false);

      JFXFuture.supplyAsync(() -> {
        ByteArrayInputStream is =  client.getDmdPositionService().getPicture(dmdinfo.getGameId(), onScreen);
        return is != null ? new Image(is) : null;
      })
      .thenAcceptLater(image -> {
        // check we are always on the tab the image was requested for (can happen user quickly switches tab)
        //if (!onScreen.equals(getSelectedTab())) {
        //  parentpane.setCenter(imagepane);
        //  return;
        //}

        // stop spinning and display the image back
        setWait(false);

        if (image == null || image.isError()) {
          fullDMDImage.setImage(null);

          // add an empty background to simulate an image 
          imagepane.getChildren().remove(emptyImage);
          emptyImage.setWidth(fitWidth);
          emptyImage.setHeight(fitHeight);
          imagepane.getChildren().add(0, emptyImage);

          if (VPinScreen.Menu.equals(onScreen)) {
            noFullDMDPane.setVisible(true);

            // check presence of image in backglass
            JFXFuture.supplyAsync(() -> client.getBackglassServiceClient().getDirectB2SData(dmdinfo.getGameId()))
              .thenAcceptLater(data -> {
                activateFullDMDImagePane.setVisible(data.isDmdImageAvailable());
                activateFullDMDImagePane.setManaged(data.isDmdImageAvailable());

                uploadFullDMDImagePane.setVisible(!data.isDmdImageAvailable());
                uploadFullDMDImagePane.setManaged(!data.isDmdImageAvailable());
              });

            // check presence of media asset
            JFXFuture.supplyAsync(() -> client.getFrontendService().getDefaultFrontendMediaItem(dmdinfo.getGameId(), VPinScreen.Menu))
              .thenAcceptLater(item -> {
                useFullDMDMediaPane.setVisible(item != null);
              });
          }
        }
        else {
          fullDMDImage.setFitWidth(fitWidth);
          fullDMDImage.setFitHeight(fitHeight);
          fullDMDImage.setImage(image);
          //FIXME  fullDMDImage.setPreserveRatio(zone.isImageCentered());
          LOG.info("Image resized to {} x {}", fitWidth, fitHeight);

          // allow direct interraction by key on dragboxes
          imagepane.requestFocus();
        }

        // memorize loaded image
        loadedVpinScreen = onScreen;
      });
    }

    // calculate our zoom
      this.zoom = fitWidth / screenWidth;
      this.bounds = new BoundingBox(0.0, 0.0, (fitWidth + 0.0) / zoom, fitHeight / zoom);
  }

  private void loadDragBoxes(VPinScreen onScreen, boolean forceRefresh) {

    // first delete previous boxes
    for (DMDPositionResizer dragBox : dragBoxes) {
      dragBox.removeFromPane(imagepane);
    }
    dragBoxes.clear();

    if (!dmdinfo.isDisabled()) {

      DMDPositionResizer selectedBox = null;
      // now create new ones
      for (DMDInfoZone zone : dmdinfo.getZones()) {
        if (zone.getOnScreen().equals(onScreen)) {

          // The lime box that is used to position the DMD
          DMDPositionResizer dragBox = new DMDPositionResizer();
          dragBox.addToPane(imagepane);
          dragBoxes.add(dragBox);

          dragBox.setZoom(zoom);
          dragBox.setWidth(zone.getWidth());
          dragBox.setHeight(zone.getHeight());
          dragBox.setX(zone.getX());
          dragBox.setY(zone.getY());
          dragBox.setBounds(bounds);

          Toggle toggleValue = ratioRadioGroup.getSelectedToggle();
          Double aspectRatio = toggleValue != null ? ((DMDAspectRatio) toggleValue.getUserData()).getValue() : null;
          dragBox.setAspectRatio(aspectRatio);

          dragBox.setUserData(zone);
          if (zone == selectedZone) {
            selectedBox = dragBox;
          }

          // setup linkages between spinner and our dragbox
          dragBox.selectProperty().addListener((obs, oldV, newV) -> {
            if (newV) {
              configureSpinner(xSpinner, dragBox.xProperty(), dragBox.xMinProperty(), dragBox.xMaxProperty());
              configureSpinner(ySpinner, dragBox.yProperty(), dragBox.yMinProperty(), dragBox.yMaxProperty());
              configureSpinner(widthSpinner, dragBox.widthProperty(), dragBox.widthMinProperty(), dragBox.widthMaxProperty());
              configureSpinner(heightSpinner, dragBox.heightProperty(), dragBox.heightMinProperty(), dragBox.heightMaxProperty());

              selectedZone = (DMDInfoZone) dragBox.getUserData();
              // select the radio where the zone is positioned onto
              if (VPinScreen.BackGlass.equals(selectedZone.getOnScreen())) {
                radioOnBackglass.setSelected(true);
              }
              else if (VPinScreen.Menu.equals(selectedZone.getOnScreen())) {
                radioOnB2sDMD.setSelected(true);
              }
              else if (VPinScreen.PlayField.equals(selectedZone.getOnScreen())) {
                radioOnPlayfield.setSelected(true);
              }
            } else {
              selectedZone = null;

              // on deselection, update associated zone
              DMDInfoZone dragZone = (DMDInfoZone) dragBox.getUserData();
              dragZone.setX(dragBox.getX());
              dragZone.setY(dragBox.getY());
              dragZone.setWidth(dragBox.getWidth());
              dragZone.setHeight(dragBox.getHeight());
            }
            disableForZone(!newV);
          });
        }
      }
      // no selected box, pick first
      if (selectedBox == null && dragBoxes.size() > 0) {
        selectedBox = dragBoxes.get(0);
      }
      if (selectedBox != null) {
        selectedBox.select();
      }
      // no box on this screen, change screen and use the one of the first
      else if (forceRefresh) {
        VPinScreen selectedScreen = dmdinfo.getZones().size() > 0 ? 
        dmdinfo.getZones().get(0).getOnScreen() : VPinScreen.BackGlass;
        selectTab(selectedScreen, false);
      }
    }   
  }

  private void loadDmdInfo(DMDInfo dmdinfo) {
    // aspect ratio forced in dmddevice.ini, force it there too and disable
    DMDAspectRatio aspectRatio = dmdinfo.getAspectRatio();
    aspectRatio = aspectRatio == null ? DMDAspectRatio.ratioOff : aspectRatio;

    ratioOff.setSelected(aspectRatio.equals(DMDAspectRatio.ratioOff));
    ratioOff.setDisable(dmdinfo.isForceAspectRatio());
    ratio3.setSelected(aspectRatio.equals(DMDAspectRatio.ratio3x1));
    ratio3.setDisable(dmdinfo.isForceAspectRatio());
    ratio4.setSelected(aspectRatio.equals(DMDAspectRatio.ratio4x1));
    ratio4.setDisable(dmdinfo.isForceAspectRatio());
    ratio8.setSelected(aspectRatio.equals(DMDAspectRatio.ratio8x1));
    ratio8.setDisable(dmdinfo.isForceAspectRatio());
  }

  private DMDInfo fillDmdInfo() {
    for (DMDPositionResizer dragBox : dragBoxes) {
      DMDInfoZone zone = (DMDInfoZone) dragBox.getUserData();
      zone.setX(dragBox.getX());
      zone.setY(dragBox.getY());
      zone.setWidth(dragBox.getWidth());
      zone.setHeight(dragBox.getHeight());
    }

    Toggle selectedToggle = ratioRadioGroup.getSelectedToggle();
    DMDAspectRatio userData = selectedToggle != null ? (DMDAspectRatio) selectedToggle.getUserData() : DMDAspectRatio.ratioOff;
    dmdinfo.setAspectRatio(userData);

    dmdinfo.setDisableViaIni(disableViaIniCheckbox.isSelected());
    dmdinfo.setDisableInVpinMame(disableViaMameCheckbox.isSelected());
    dmdinfo.setDisableBackglassScores(disableBGScoreCheckbox.isSelected());

    return dmdinfo;
  }


  private void selectTab(VPinScreen onScreen, boolean forceRefresh) {
    Tab selectedTab = screenToTab(onScreen);
    if (tabPane.getSelectionModel().getSelectedItem() == selectedTab) {
      // already selected, just refresh images and drag boxes
      //disablePanes(true);
      // forceRefresh is true when a new game is set. 
      // Even if we are already on this tab, the image must be refreshed form the new game
      loadImage(onScreen, forceRefresh);
      // forceRefresh is to force the selection of a dragBox, possibly trigering tab change for new game
      loadDragBoxes(onScreen, forceRefresh);
    }
    else {
      tabPane.getSelectionModel().select(selectedTab);
    }
  }

  private Tab screenToTab(VPinScreen onScreen) {
    return VPinScreen.BackGlass.equals(onScreen) ? backglassTab :
          VPinScreen.Menu.equals(onScreen) ? dmdTab : 
          VPinScreen.PlayField.equals(onScreen) ? playfieldTab :
          null;
  }
  private VPinScreen tabToScreen(Tab tab) {
    return tab == backglassTab ? VPinScreen.BackGlass :
          tab == dmdTab ? VPinScreen.Menu : 
          tab == playfieldTab ? VPinScreen.PlayField :
          null;
  }
}

package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.directb2s.DirectB2sScreenRes;
import de.mephisto.vpin.restclient.dmd.DMDAspectRatio;
import de.mephisto.vpin.restclient.dmd.DMDInfo;
import de.mephisto.vpin.restclient.dmd.DMDInfoZone;
import de.mephisto.vpin.restclient.dmd.DMDType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.backglassmanager.BackglassManagerController;
import edu.umd.cs.findbugs.annotations.Nullable;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class DMDPositionController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(DMDPositionController.class);

  private BackglassManagerController backglassMgrController;

  @FXML
  private Label titleLabel;
  @FXML
  private Button prevButton;
  @FXML
  private Button nextButton;

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
  private CheckBox disableViaMameCheckbox;
  @FXML
  private CheckBox disableViaIniCheckbox;

  @FXML
  private HBox disablePane;
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
  private Spinner<Double> xSpinner;
  @FXML
  private Spinner<Double> ySpinner;
  @FXML
  private Spinner<Double> widthSpinner;
  @FXML
  private Spinner<Double> heightSpinner;

  @FXML
  private Spinner<Integer> marginSpinner;

  @FXML
  private Button autoPositionBtn;
  @FXML
  private Button centerXBtn;
  @FXML
  private Button resetToScoresBtn;

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

  @FXML
  private Rectangle emptyImage;

  private GameRepresentation game;

  private DirectB2sScreenRes screenres;

  private List<DMDPositionResizer> dragBoxes = new ArrayList<>();

  private DMDInfo dmdinfo;

  private DMDInfoZone selectedZone;

  // The image bounds
  private ObjectProperty<Bounds> area = new SimpleObjectProperty<>();

  private ObjectProperty<Color> color = new SimpleObjectProperty<>(Color.LIME);
  
  private SimpleObjectProperty<DMDAspectRatio> ratioProperty;

  /**
   * The converter for displaying numbers in spinners
   */
  private final DecimalFormat df = new DecimalFormat("#.##");

  /**
   * The zoom factor : <screen coordinates> x zoom = <resizer pixels>
   */
  private DoubleProperty zoom = new SimpleDoubleProperty(1);

  private ToggleGroup ratioRadioGroup;


  @FXML
  private void onNext(ActionEvent e) {
    clearGame();
    switchGame(backglassMgrController.selectNextGame());
  }

  @FXML
  private void onPrevious(ActionEvent e) {
    clearGame();
    switchGame(backglassMgrController.selectPreviousGame());
  }

  private void clearGame() {
    titleLabel.setText("loading...");
    romLabel.setText("--");
    tablePositionLabel.setText("");
    resetAll();
    loadImage(null, false);
    loadDragBoxes(null, false);
  }

  private void switchGame(int gameId) {
    JFXFuture.supplyAsync(() -> client.getGame(gameId))
    .thenAcceptLater(game -> {
      if (game != null && !game.equals(this.game)) {
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
    onSaveClick(e, false, true);
  }

  @FXML
  private void onSaveLocallyClick(ActionEvent e) {
    onSaveClick(e, true, false);
  }

  @FXML
  private void onSaveCloseLocallyClick(ActionEvent e) {
    onSaveClick(e, true, true);
  }

  private void onSaveClick(ActionEvent e, boolean locally, boolean close) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    JFXFuture.runAsync(() -> {
          DMDInfo dmdinfo = fillDmdInfo();
          dmdinfo.setLocallySaved(locally);
          LOG.info("Saving dmdinfo for game {} : {}", game.getGameFileName(), dmdinfo);
          client.getDmdPositionService().saveDMDInfo(dmdinfo);
        })
        .thenLater(() -> {
          if (close) {
            stage.close();
          }
        });
  }

  @Override
  public void onKeyPressed(KeyEvent ke) {
    for (DMDPositionResizer dragBox : dragBoxes) {
      if (dragBox.keyPressed(ke)) {
        ke.consume();
      }
    }
  }

  @FXML
  private void onAutoPosition() {
    disableButtons();
    if (selectedZone != null) {
      LOG.info("Autoposition dmdinfo for game {}", game.getGameFileName());
      // set the margin for auto positioning
      selectedZone.setMargin(marginSpinner.getValue());

      JFXFuture.supplyAsync(() -> client.getDmdPositionService().autoPositionDMDInfo(dmdinfo.getGameId(), selectedZone))
          .thenAcceptLater(newZone -> setDmdInfoZone(selectedZone, newZone));
    }
  }

  @FXML
  private void onResetToScores() {
    DMDInfo movedDmdinfo = fillDmdInfo();
    LOG.info("Reseting Zones to Scores positionsfor game {}", game.getGameFileName());
    resetAll();
    JFXFuture.supplyAsync(() -> client.getDmdPositionService().resetToScores(movedDmdinfo))
        .thenAcceptLater(dmd -> setDmdInfo(dmd, false));
  }

  @FXML
  private void onCenterX() {
    for (DMDPositionResizer dragBox : dragBoxes) {
      if (dragBox.isSelected()) {
        dragBox.centerHorizontally();
      }
    }
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

    ratioProperty = new SimpleObjectProperty<>(DMDAspectRatio.ratioOff);

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
          ratioProperty.setValue((DMDAspectRatio) newValue.getUserData());

        }
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

    // now set the existing bounds, avoid null values
    Bounds bounds = fullDMDImage.getLayoutBounds();
    area.set(bounds);

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
          resetAll();
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
      }
    });

    // by default do not alphanumeric
    DMDTypeCombo.setItems(FXCollections.observableArrayList(DMDType.NoDMD, DMDType.VirtualDMD, DMDType.VpinMAMEDMD));
    DMDTypeCombo.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      if (t1 != null && !dmdinfo.getDMDType().equals(t1)) {
        DMDInfo movedDmdinfo = fillDmdInfo();

        LOG.info("Swicthing dmdinfo for game {} to {}", game.getGameFileName(), t1);
        resetAll();
        JFXFuture.supplyAsync(() -> client.getDmdPositionService().switchDMDInfo(movedDmdinfo, t1))
            .thenAcceptLater(dmd -> setDmdInfo(dmd, false));
      }
    });

    disableViaMameCheckbox.selectedProperty().addListener((obs, oldV, newV) -> {
      if (!newV) {
        disableViaIniCheckbox.setSelected(true);
      }
    });
    disableViaIniCheckbox.selectedProperty().addListener((obs, oldV, newV) -> {
      if (!newV) {
        disableViaMameCheckbox.setSelected(true);
      }
    });
  }

  private void configureSpinner(Spinner<Double> spinner, ObjectProperty<Double> property,
                                ReadOnlyObjectProperty<Double> minProperty, ReadOnlyObjectProperty<Double> maxProperty) {

    SpinnerValueFactory.DoubleSpinnerValueFactory factory =
        new SpinnerValueFactory.DoubleSpinnerValueFactory(minProperty.get(), maxProperty.get());

    // install a converter that convert pixels into screen coordinates using the zoom factor
    factory.setConverter(new StringConverter<Double>() {
      @Override
      public String toString(Double value) {
        if (value == null) {
          return "";
        }
        Double screenValue = value / zoom.get();
        return df.format(screenValue);
      }

      @Override
      public Double fromString(String value) {
        if (StringUtils.isBlank(value)) {
          return null;
        }
        try {
          double screenValue = df.parse(value).doubleValue();
          return screenValue * zoom.get();
        }
        catch (ParseException ex) {
          throw new RuntimeException(ex);
        }
      }
    });

    spinner.setValueFactory(factory);
    spinner.setEditable(true);

    factory.valueProperty().bindBidirectional(property);
    factory.minProperty().bind(minProperty);
    factory.maxProperty().bind(maxProperty);
  }

  @Override
  public void onDialogCancel() {
  }

  private void resetAll() {
    DMDTypeCombo.getSelectionModel().clearSelection();
    disableButtons();
    disablePanes(true);
    DMDTypeCombo.setDisable(true);
    disablePane.setDisable(true);
    romPane.setDisable(true);
    //loadedVpinScreen = null;
  }

  public void setGame(GameRepresentation gameRepresentation, @Nullable BackglassManagerController backglassMgrController) {
    this.game = gameRepresentation;
    this.backglassMgrController = backglassMgrController;
    this.titleLabel.setText(game.getGameDisplayName());

    prevButton.setVisible(backglassMgrController != null);
    nextButton.setVisible(backglassMgrController != null);

    resetAll();
    JFXFuture.supplyAllAsync(
      () -> client.getBackglassServiceClient().getScreenRes(game.getEmulatorId(), game.getGameFileName(), false),
      () -> client.getDmdPositionService().getDMDInfo(game.getId()))
    .thenAcceptLater((objs) -> {
      this.screenres = (DirectB2sScreenRes) objs[0];
      setDmdInfo((DMDInfo) objs[1], true);
    })
    .onErrorLater(ex -> setDmdError(ex));
  }

  private void setDmdError(Throwable e) {
    LOG.error("Error getting dmd information, set an empty one");
    this.dmdinfo = new DMDInfo();
    WidgetFactory.showAlert(Studio.stage, "Getting DMD information failed", 
      "Please check your screenres.txt or <tablename>.res file exists, else check the server logs");
  }

  private void disableButtons() {
    autoPositionBtn.setDisable(true);
    resetToScoresBtn.setDisable(true);
    saveLocallyBtn.setDisable(true);
    saveCloseLocallyBtn.setDisable(true);
    saveGloballyBtn.setDisable(true);
  }

  private void disablePanes(boolean disabled) {
    spinnersPane.setDisable(disabled);
    radioOnPane.setDisable(disabled);
    ratiosPane.setDisable(disabled);
    autoPositionPane.setDisable(disabled);
  } 

  private void setDmdInfoZone(DMDInfoZone oldZone, DMDInfoZone zone) {
    int pos = dmdinfo.getZones().indexOf(oldZone);
    if (pos >= 0) {
      dmdinfo.getZones().remove(pos);
      dmdinfo.getZones().add(pos, zone);
      this.selectedZone = zone;
      selectTab(zone.getOnScreen(), false);

      // re-enable buttons
      resetToScoresBtn.setDisable(false);
      saveLocallyBtn.setDisable(dmdinfo.getGameRom() == null);  
      saveCloseLocallyBtn.setDisable(dmdinfo.getGameRom() == null);  
    }
  }

  /**
   * To be called on a Thread, from a Future, as it loads the image synchronously
   * in order to calculate the bounds of the image
   */
  private void setDmdInfo(DMDInfo dmdinfo, boolean forceRefresh) {
    LOG.info("Received dmdinfo for game {} : {}", game.getGameFileName(), dmdinfo);
    this.dmdinfo = dmdinfo;

    romPane.setDisable(false);
    romLabel.setText(StringUtils.defaultIfEmpty(dmdinfo.getGameRom(), "--"));

    List<DMDType> types = dmdinfo.isSupportAlphaNumericDmd() ? 
      Arrays.asList(DMDType.NoDMD, DMDType.VirtualDMD, DMDType.AlphaNumericDMD, DMDType.VpinMAMEDMD) :
      Arrays.asList(DMDType.NoDMD, DMDType.VirtualDMD, DMDType.VpinMAMEDMD);
    DMDTypeCombo.setItems(FXCollections.observableArrayList(types));
    DMDTypeCombo.setValue(dmdinfo.getDMDType());
    DMDTypeCombo.setDisable(false);

    boolean canSelectHowToDisable = dmdinfo.isDisabled(); //&& dmdinfo.isUseFreezy()
    disablePane.setManaged(canSelectHowToDisable);
    disablePane.setVisible(canSelectHowToDisable);
    disablePane.setDisable(false);

    tablePositionLabel.setText((dmdinfo.isLocallySaved() ? "Locally" : "Globally") + " in " 
        + (dmdinfo.isUseRegistry() ? "registry" : "dmddevice.ini"));

    boolean isAlpha = DMDType.AlphaNumericDMD.equals(dmdinfo.getDMDType());
    resetToScoresBtn.setVisible(isAlpha);
    resetToScoresBtn.setManaged(isAlpha);
    resetToScoresBtn.setDisable(false);

    // no local save if no rom
    if (dmdinfo.getGameRom() != null) {
      saveLocallyBtn.setText("Save for " + dmdinfo.getGameRom());
      saveLocallyBtn.setDisable(false);
      saveLocallyBtn.setVisible(true);

      saveCloseLocallyBtn.setText("Save & Close for " + dmdinfo.getGameRom());
      saveCloseLocallyBtn.setDisable(false);
      saveCloseLocallyBtn.setVisible(true);
    }

    // no global save when using registry
    //if (!dmdinfo.isUseRegistry()) {
    //  saveGloballyBtn.setDisable(false);
    //  saveGloballyBtn.setVisible(true);
    //}

    // when dmd is auto positioned on grill, size can be 0x0 so turn option off 
    boolean hasDMD = dmdinfo.isSupportFullDmd();
    radioOnB2sDMD.setManaged(hasDMD);
    radioOnB2sDMD.setVisible(hasDMD);

    // select first zone
    VPinScreen selectedScreen = loadedVpinScreen;
    if (forceRefresh || selectedScreen == null) {
      selectedScreen = dmdinfo.getZones().size() > 0 ? 
        dmdinfo.getZones().get(0).getOnScreen() : VPinScreen.BackGlass;
    }
    selectTab(selectedScreen, forceRefresh);

    disableViaIniCheckbox.setSelected(dmdinfo.isDisableViaIni());
    disableViaMameCheckbox.setSelected(dmdinfo.isDisableInVpinMame());

    loadDmdInfo(dmdinfo);
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

      imagepane.getChildren().remove(emptyImage);
      emptyImage.setWidth(fitWidth);
      emptyImage.setHeight(fitHeight);
      imagepane.getChildren().add(0, emptyImage);

       loadedVpinScreen = null;
    }
    else if (forceRefresh || !onScreen.equals(loadedVpinScreen)) {
      parentpane.setCenter(progressIndicator);
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

        fullDMDImage.setFitWidth(fitWidth);
        fullDMDImage.setFitHeight(fitHeight);
        fullDMDImage.setImage(image);
        //FIXME  fullDMDImage.setPreserveRatio(zone.isImageCentered());
        LOG.info("Image resized to {} x {}", fitWidth, fitHeight);

        imagepane.getChildren().remove(emptyImage);
        if (image == null || image.isError()) {
          emptyImage.setWidth(fitWidth);
          emptyImage.setHeight(fitHeight);
          imagepane.getChildren().add(0, emptyImage);
        }
        parentpane.setCenter(imagepane);

        // allow direct interraction by key on dragboxes
        imagepane.requestFocus();

        // memorize loaded image
        loadedVpinScreen = onScreen;
      });
    }

    // calculate our zoom
    zoom.set(fitWidth / screenWidth);
    // configure min / max of spinners 
    Bounds bounds = new BoundingBox(0, 0, fitWidth, fitHeight);
    area.set(bounds);    
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
          DMDPositionResizer dragBox = new DMDPositionResizer(area, zoom, ratioProperty, color);
          dragBox.addToPane(imagepane);
          dragBoxes.add(dragBox);

          dragBox.setWidth(zone.getWidth());
          dragBox.setHeight(zone.getHeight());
          dragBox.setX(zone.getX());
          dragBox.setY(zone.getY());
      
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
            autoPositionBtn.setDisable(!newV);
            disablePanes(!newV);
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

//-----

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

    return dmdinfo;
  }


  private void selectTab(VPinScreen onScreen, boolean forceRefresh) {
    Tab selectedTab = screenToTab(onScreen);
    if (tabPane.getSelectionModel().getSelectedItem() == selectedTab) {
      // already selected, just refresh images and drag boxes
      disablePanes(true);
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

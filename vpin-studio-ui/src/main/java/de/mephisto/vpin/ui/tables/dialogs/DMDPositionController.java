package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.dmd.DMDAspectRatio;
import de.mephisto.vpin.restclient.dmd.DMDInfo;
import de.mephisto.vpin.restclient.dmd.DMDType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class DMDPositionController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(DMDPositionController.class);

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
  private Button saveLocallyBtn;
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

  private DMDPositionResizer dragBox;

  private DMDInfo dmdinfo;

  // The image bounds
  private ObjectProperty<Bounds> area = new SimpleObjectProperty<>();

  private ObjectProperty<Color> color = new SimpleObjectProperty<>(Color.LIME);

  /**
   * The converter for displaying numbers in spinners
   */
  private final DecimalFormat df = new DecimalFormat("#.##");

  /**
   * The zoom factor : <screen coordinates> x zoom = <resizer pixels>
   */
  private DoubleProperty zoom = new SimpleDoubleProperty(1);
  private ToggleGroup radioGroup;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSaveGloballyClick(ActionEvent e) {
    onSaveClick(e, false);
  }

  @FXML
  private void onSaveLocallyClick(ActionEvent e) {
    onSaveClick(e, true);
  }

  private void onSaveClick(ActionEvent e, boolean locally) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    JFXFuture.runAsync(() -> {
          DMDInfo dmdinfo = fillDmdInfo();
          dmdinfo.setLocallySaved(locally);
          LOG.info("Saving dmdinfo for game {} : {}", game.getGameFileName(), dmdinfo);
          client.getDmdPositionService().saveDMDInfo(dmdinfo);
        })
        .thenLater(() -> {
          stage.close();
        });
  }

  @Override
  public void onKeyPressed(KeyEvent ke) {
    if (dragBox != null && dragBox.keyPressed(ke)) {
      ke.consume();
    }
  }

  @FXML
  private void onAutoPosition() {
    disableButtons();
    DMDInfo movedDmdinfo = fillDmdInfo();
    LOG.info("Autoposition dmdinfo for game {}", game.getGameFileName());
    JFXFuture.supplyAsync(() -> client.getDmdPositionService().autoPositionDMDInfo(movedDmdinfo))
        .thenAcceptLater(dmd -> setDmdInfo(dmd));
  }

  @FXML
  private void onCenterX() {
    dragBox.centerHorizontally();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    // create a toggle group
    radioGroup = new ToggleGroup();
    ratioOff.setToggleGroup(radioGroup);
    ratioOff.setUserData(DMDAspectRatio.ratioOff);
    ratio3.setToggleGroup(radioGroup);
    ratio3.setUserData(DMDAspectRatio.ratio3x1);
    ratio4.setToggleGroup(radioGroup);
    ratio4.setUserData(DMDAspectRatio.ratio4x1);
    ratio8.setToggleGroup(radioGroup);
    ratio8.setUserData(DMDAspectRatio.ratio8x1);

    ratioOff.setSelected(true);

    saveLocallyBtn.managedProperty().bindBidirectional(saveLocallyBtn.visibleProperty());
    saveGloballyBtn.managedProperty().bindBidirectional(saveGloballyBtn.visibleProperty());
    saveLocallyBtn.setVisible(false);
    saveGloballyBtn.setVisible(false);

    SimpleObjectProperty<DMDAspectRatio> ratioProperty = new SimpleObjectProperty<>(DMDAspectRatio.ratioOff);
    radioGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
      @Override
      public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
        if(newValue != null) {
          ratioProperty.setValue((DMDAspectRatio) newValue.getUserData());
        }
      }
    });

    // The lime box that is used to position the DMD
    dragBox = new DMDPositionResizer(area, ratioProperty, color);
    dragBox.addToPane(imagepane);

    // setup linkages between spinner and our dragbox
    configureSpinner(xSpinner, dragBox.xProperty(), dragBox.xMinProperty(), dragBox.xMaxProperty());
    configureSpinner(ySpinner, dragBox.yProperty(), dragBox.yMinProperty(), dragBox.yMaxProperty());
    configureSpinner(widthSpinner, dragBox.widthProperty(), dragBox.widthMinProperty(), dragBox.widthMaxProperty());
    configureSpinner(heightSpinner, dragBox.heightProperty(), dragBox.heightMinProperty(), dragBox.heightMaxProperty());

    // add a selector in the pane to draw a rectangle.
    new DMDPositionSelection(imagepane, area, ratioProperty, color,
        // called on drag start, hide the lime dragbox
        () -> {
          dragBox.setVisible(false);
        },
        // called once dragged, show th edragbox back and reposition/resize it
        rect -> {
          dragBox.setVisible(true);
          dragBox.select();

          dragBox.setX(rect.getMinX());
          dragBox.setY(rect.getMinY());
          dragBox.setWidth(rect.getWidth());
          dragBox.setHeight(rect.getHeight());
        });

    // now set the existing bounds
    Bounds bounds = fullDMDImage.getLayoutBounds();
    area.set(bounds);

    emptyImage = new Rectangle();
    emptyImage.setFill(Color.grayRgb(30));
    emptyImage.setStroke(Color.BLACK);

    // create a toggle group 
    ToggleGroup tg = new ToggleGroup();
    radioOnPlayfield.setToggleGroup(tg);
    radioOnBackglass.setToggleGroup(tg);
    radioOnB2sDMD.setToggleGroup(tg);

    tg.selectedToggleProperty().addListener((obs, o, n) -> {
      VPinScreen selectedScreen = 
        radioOnBackglass.isSelected() ? VPinScreen.BackGlass :
        radioOnB2sDMD.isSelected() ? VPinScreen.Menu : 
        radioOnPlayfield.isSelected() ? VPinScreen.PlayField :
        null;

      // prevent a double load of image at dialog opening
      if (selectedScreen != null && !selectedScreen.equals(dmdinfo.getOnScreen())) {
        disableButtons();

        DMDInfo movedDmdinfo = fillDmdInfo();

        LOG.info("Moving dmdinfo for game {} : {}", game.getGameFileName(), movedDmdinfo);
        JFXFuture.supplyAsync(() -> client.getDmdPositionService().moveDMDInfo(movedDmdinfo, selectedScreen))
            .thenAcceptLater(dmd -> setDmdInfo(dmd));
      }
    });
    radioGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
      @Override
      public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
        DMDInfo dmdinfo = fillDmdInfo();
        dmdinfo.adjustAspectRatio();
        loadDmdInfo(dmdinfo);
      }
    });

    DMDTypeCombo.setItems(FXCollections.observableArrayList(DMDType.NoDMD, DMDType.VirtualDMD, DMDType.VpinMAMEDMD));
    DMDTypeCombo.valueProperty().addListener((observableValue, aBoolean, t1) -> {
      dmdinfo.setDMDType(t1);
      
      disablePanes(dmdinfo.isDisabled());

      // load image if not already loaded
      loadImage();

      boolean canSelectHowToDisable = dmdinfo.isDisabled(); //&& dmdinfo.isUseFreezy()
      disablePane.setManaged(canSelectHowToDisable);
      disablePane.setVisible(canSelectHowToDisable);
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

  public void setGame(GameRepresentation gameRepresentation) {
    this.game = gameRepresentation;

    JFXFuture.supplyAsync(() -> client.getDmdPositionService().getDMDInfo(game.getId()))
        .thenAcceptLater(dmd -> setDmdInfo(dmd))
        .onErrorLater(ex -> {
          setDmdError(ex);
        });
    }

  private void setDmdError(Throwable e) {
    LOG.error("Error getting dmd information, set an empty one");
    this.dmdinfo = new DMDInfo();
    WidgetFactory.showAlert(Studio.stage, "Getting DMD information failed", 
      "Please check your screenres.txt or <tablename>.res file exists, else check the server logs");
  }

  private void disableButtons() {
    autoPositionBtn.setDisable(true);
    saveLocallyBtn.setDisable(true);
    saveGloballyBtn.setDisable(true);
  }

  private void disablePanes(boolean disabled) {
    spinnersPane.setDisable(disabled);
    radioOnPane.setDisable(disabled);
    ratiosPane.setDisable(disabled);
    autoPositionPane.setDisable(disabled);
  } 

  /**
   * To be called on a Thread, from a Future, as it loads the image synchronously
   * in order to calculate the bounds of the image
   */
  private void setDmdInfo(DMDInfo dmdinfo) {
    LOG.info("Received dmdinfo for game {} : {}", game.getGameFileName(), dmdinfo);
    this.dmdinfo = dmdinfo; 

    // load image if not previously loaded
    loadImage();

    romLabel.setText(StringUtils.defaultIfEmpty(dmdinfo.getGameRom(), "--"));

    // setting the type, will load the image as well
    DMDTypeCombo.setValue(dmdinfo.getDMDType());

    tablePositionLabel.setText((dmdinfo.isLocallySaved() ? "Locally" : "Globally") + " in " 
        + (dmdinfo.isUseRegistry() ? "registry" : "dmddevice.ini"));

    autoPositionBtn.setDisable(false);

    // no local save if no rom
    if (dmdinfo.getGameRom() != null) {
      saveLocallyBtn.setText("Save for " + dmdinfo.getGameRom());
      saveLocallyBtn.setDisable(false);
      saveLocallyBtn.setVisible(true);
    }

    // no global save when using registry
    if (!dmdinfo.isUseRegistry()) {
      saveGloballyBtn.setDisable(false);
      saveGloballyBtn.setVisible(dmdinfo.isUseRegistry());
    }

    // when dmd is auto positioned on grill, size can be 0x0 so turn option off 
    boolean hasDMD = dmdinfo.isDmdScreenSet();
    radioOnB2sDMD.setManaged(hasDMD);
    radioOnB2sDMD.setVisible(hasDMD);

    if (VPinScreen.BackGlass.equals(dmdinfo.getOnScreen())) {
      radioOnBackglass.setSelected(true);
    }
    else if (VPinScreen.Menu.equals(dmdinfo.getOnScreen())) {
      radioOnB2sDMD.setSelected(true);
    }
    else if (VPinScreen.PlayField.equals(dmdinfo.getOnScreen())) {
      radioOnPlayfield.setSelected(true);
    }
    // The bounds for the DMD resizer
    Bounds bounds = fullDMDImage.getLayoutBounds();
    // calculate our zoom
    zoom.set(bounds.getWidth() / dmdinfo.getScreenWidth());
    // configure min / max of spinners 
    area.set(bounds);

    disableViaIniCheckbox.setSelected(dmdinfo.isDisableViaIni());
    disableViaMameCheckbox.setSelected(dmdinfo.isDisableInVpinMame());

    // Finally position our box, maybe resized, moved or rescaled keeping in account above constraints
    loadDmdInfo(dmdinfo);
  }

  private void loadImage() {
    // resize the preview proportionnaly to the real screen size, 
    // determine if this is fitWidth or fitHeight that must be adjusted 
    boolean adjustWidth = dmdinfo.getScreenWidth() / dmdinfo.getScreenHeight() < 1024.0 / 768.0;
    final double fitWidth = adjustWidth ? 768.0 * dmdinfo.getScreenWidth() / dmdinfo.getScreenHeight() : 1024.0;
    final double fitHeight = adjustWidth ? 768.0 : 1024.0 * dmdinfo.getScreenHeight() / dmdinfo.getScreenWidth();

    dragBox.setVisible(!dmdinfo.isDisabled());

    if (dmdinfo.isDisabled()) {
      fullDMDImage.setImage(null);

      imagepane.getChildren().remove(emptyImage);
      emptyImage.setWidth(fitWidth);
      emptyImage.setHeight(fitHeight);
      imagepane.getChildren().add(0, emptyImage);

       loadedVpinScreen = null;
    }
    else if (!dmdinfo.getOnScreen().equals(loadedVpinScreen)) {
      parentpane.setCenter(progressIndicator);
      JFXFuture.supplyAsync(() -> {
        ByteArrayInputStream is =  client.getDmdPositionService().getPicture(dmdinfo);
        return is != null ? new Image(is) : null;
      })
      .thenAcceptLater(image -> {
        fullDMDImage.setFitWidth(fitWidth);
        fullDMDImage.setFitHeight(fitHeight);
        fullDMDImage.setImage(image);
        fullDMDImage.setPreserveRatio(dmdinfo.isImageCentered());
        LOG.info("Image resized to {} x {}", fitWidth, fitHeight);

        imagepane.getChildren().remove(emptyImage);
        if (image == null || image.isError()) {
          emptyImage.setWidth(fitWidth);
          emptyImage.setHeight(fitHeight);
          imagepane.getChildren().add(0, emptyImage);
        }
        parentpane.setCenter(imagepane);

        dragBox.setVisible(true);

        // memorize loaded image
        loadedVpinScreen = dmdinfo.getOnScreen();
      });
    }
  }

  private void loadDmdInfo(DMDInfo dmdinfo) {
    dragBox.setWidth(dmdinfo.getWidth() * zoom.get());
    dragBox.setHeight(dmdinfo.getHeight() * zoom.get());
    dragBox.setX(dmdinfo.getX() * zoom.get());
    dragBox.setY(dmdinfo.getY() * zoom.get());
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
    dmdinfo.setX(dragBox.getX() / zoom.get());
    dmdinfo.setY(dragBox.getY() / zoom.get());
    dmdinfo.setWidth(dragBox.getWidth() / zoom.get());
    dmdinfo.setHeight(dragBox.getHeight() / zoom.get());

    dmdinfo.setMargin(marginSpinner.getValue());

    Toggle selectedToggle = radioGroup.getSelectedToggle();
    if (selectedToggle == null) {
      dmdinfo.setAspectRatio(DMDAspectRatio.ratioOff);
    }
    else {
      DMDAspectRatio userData = (DMDAspectRatio) selectedToggle.getUserData();
      dmdinfo.setAspectRatio(userData);
    }

    dmdinfo.setDisableViaIni(disableViaIniCheckbox.isSelected());
    dmdinfo.setDisableInVpinMame(disableViaMameCheckbox.isSelected());

    return dmdinfo;
  }
}

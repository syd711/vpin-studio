package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.dmd.DMDInfo;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.util.JFXFuture;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.mephisto.vpin.ui.Studio.client;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class DMDPositionController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(DMDPositionController.class);

  @FXML
  private Label romLabel;

  @FXML
  private Label tablePositionLabel;

  @FXML
  private CheckBox aspectRatioCheckbox;

  @FXML
  private RadioButton radioOnBackglass;
  @FXML
  private RadioButton radioOnB2sDMD;
  @FXML
  private RadioButton radioOnPlayfield;

  @FXML
  private Spinner<Double> xSpinner;
  @FXML
  private Spinner<Double> ySpinner;
  @FXML
  private Spinner<Double> widthSpinner;
  @FXML
  private Spinner<Double> heightSpinner;

  @FXML
  private Button saveLocallyBtn;

  @FXML
  private Pane imagepane;

  @FXML
  private ImageView fullDMDImage;

  private GameRepresentation game;

  private DMDPositionResizer dragBox;

  private DMDInfo dmdinfo;

  // The image bounds
  private ObjectProperty<Bounds> area = new SimpleObjectProperty<>();

  private ObjectProperty<Color> color = new SimpleObjectProperty<>(Color.LIME);

  /** The converter for displaying numbers in spinners */
  private final DecimalFormat df = new DecimalFormat("#.##");

  /** The zoom factor : <screen coordinates> x zoom = <resizer pixels> */
  private DoubleProperty zoom = new SimpleDoubleProperty(1);

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

  @FXML
  private void onAutoPosition() {
    DMDInfo movedDmdinfo = fillDmdInfo();
    LOG.info("Autoposition dmdinfo for game {}", game.getGameFileName());
    CompletableFuture
      .supplyAsync(() -> client.getDmdPositionService().autoPositionDMDInfo(movedDmdinfo))
      .thenAccept(dmd -> setDmdInfo(dmd));
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    // The lime box that is used to position the DMD
    dragBox = new DMDPositionResizer(area, aspectRatioCheckbox.selectedProperty(), color);
    dragBox.addToPane(imagepane);
   
    // setup linkages between spinner and our dragbox
    configureSpinner(xSpinner, dragBox.xProperty(),dragBox.xMinProperty(), dragBox.xMaxProperty());
    configureSpinner(ySpinner, dragBox.yProperty(), dragBox.yMinProperty(), dragBox.yMaxProperty());
    configureSpinner(widthSpinner, dragBox.widthProperty(), dragBox.widthMinProperty(), dragBox.widthMaxProperty());
    configureSpinner(heightSpinner, dragBox.heightProperty(), dragBox.heightMinProperty(), dragBox.heightMaxProperty());

    // add a selector in the pane to draw a rectangle.
    new DMDPositionSelection(imagepane, area, aspectRatioCheckbox.selectedProperty(), color, 
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

    // create a toggle group 
    ToggleGroup tg = new ToggleGroup(); 
    radioOnBackglass.setToggleGroup(tg);
    radioOnB2sDMD.setToggleGroup(tg);
    radioOnPlayfield.setToggleGroup(tg);
    tg.selectedToggleProperty().addListener((obs, o, n) -> {
      VPinScreen selectedScreen = getSelectedScreen();
      // prevent a double load of image at dialog opening
      if (!selectedScreen.equals(dmdinfo.getOnScreen())) {
        DMDInfo movedDmdinfo = fillDmdInfo();
        LOG.info("Moving dmdinfo for game {} : {}", game.getGameFileName(), movedDmdinfo);
        CompletableFuture
          .supplyAsync(() -> client.getDmdPositionService().moveDMDInfo(movedDmdinfo, selectedScreen))
          .thenAccept(dmd -> setDmdInfo(dmd));
      }
    });
  }

  private void configureSpinner(Spinner<Double> spinner, ObjectProperty<Double> property, 
        ReadOnlyObjectProperty<Double> minProperty, ReadOnlyObjectProperty<Double> maxProperty) {

    SpinnerValueFactory.DoubleSpinnerValueFactory factory = 
      new SpinnerValueFactory.DoubleSpinnerValueFactory(minProperty.get(), maxProperty.get());

      // install a converter that convert pixels into screen coordinates using the zoom factor
    factory.setConverter(new StringConverter<Double>() {
			@Override public String toString(Double value) {
        if (value == null) {
          return "";
        }
        Double screenValue = value / zoom.get();
        return df.format(screenValue);
			}
      @Override public Double fromString(String value) {
        if (StringUtils.isBlank(value)) {
          return null;
        }
        try {
          double screenValue = df.parse(value).doubleValue();
          return screenValue * zoom.get();
        } catch (ParseException ex) {
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
    CompletableFuture
      .supplyAsync(() -> client.getDmdPositionService().getDMDInfo(game.getId()))
      .thenAccept(dmd -> setDmdInfo(dmd));
  }

  /**
   * To be called on a Thread, from a Future, as it loads the image synchronously
   * in order to calculate the bounds of the image
   */
  private void setDmdInfo(DMDInfo dmdinfo) {
    LOG.info("Received dmdinfo for game {} : {}", game.getGameFileName(), dmdinfo);
    this.dmdinfo = dmdinfo;

    String backgroundUrl = 
      dmdinfo.isOnBackglass() ? client.getBackglassServiceClient().getDirectB2sPreviewBackgroundUrl(dmdinfo.getGameId(), true) :
      dmdinfo.isOnDMD() ? client.getBackglassServiceClient().getDirectB2sDmdUrl(dmdinfo.getGameId()) :
      null; 

    Image image = backgroundUrl != null ? new Image(backgroundUrl) : null;
    // when loaded, fill now the screen
    Platform.runLater(() -> {
      // resize the preview proportionnaly to the real screen size, 
      // determine if this is fitWidth or fitHeight that must be adjusted 
      double fitWidth=1024.0;
      double fitHeight=768.0;
      if (dmdinfo.getScreenWidth() / dmdinfo.getScreenHeight() < fitWidth / fitHeight) {
        fitWidth = fitHeight * dmdinfo.getScreenWidth() / dmdinfo.getScreenHeight();
      }
      else {
        fitHeight = fitWidth * dmdinfo.getScreenHeight() / dmdinfo.getScreenWidth();
      }
      fullDMDImage.setFitWidth(fitWidth);
      fullDMDImage.setFitHeight(fitHeight);
      fullDMDImage.setImage(image);
      fullDMDImage.setPreserveRatio(dmdinfo.isImageCentered());

      romLabel.setText(dmdinfo.getGameRom());
      tablePositionLabel.setText((dmdinfo.isLocallySaved() ? "Locally" : "Globally") + " in " +
        (dmdinfo.isUseRegistry() ? "registry" : "dmddevice.ini"));
      saveLocallyBtn.setText("Save for " + dmdinfo.getGameRom());

      if (dmdinfo.isOnBackglass()) {
        radioOnBackglass.setSelected(true);
      }
      else if (dmdinfo.isOnDMD()) {
        radioOnB2sDMD.setSelected(true);
      }
      else {
        radioOnPlayfield.setSelected(true);
      }

      // The bounds for the DMD resizer
      Bounds bounds = fullDMDImage.getLayoutBounds();

      // calculate our zoom
      double zoomX = bounds.getWidth() / dmdinfo.getScreenWidth();
      double zoomY = bounds.getHeight() / dmdinfo.getScreenHeight();
      zoom.set(zoomX);

      // configure min / max of spinners 
      area.set(bounds);

      // aspect ratio forced in dmddevice.ini, force it there too and disable 
      if (dmdinfo.isKeepAspectRatio()) {
        aspectRatioCheckbox.setSelected(true);
        aspectRatioCheckbox.setDisable(true);
      }
      else {
        // if existing dmd size ratio is close to 4:1, activate the checkbox
        double ratio = dmdinfo.getWidth() / dmdinfo.getHeight();
        boolean aspectRatio = (Math.abs(ratio - 4) < 0.01);
        aspectRatioCheckbox.setSelected(aspectRatio);
      }

      // Finally position our box, maybe resized, moved or rescaled keeping in account above constraints
      dragBox.setX(dmdinfo.getX() * zoomX);
      dragBox.setY(dmdinfo.getY() * zoomX);
      dragBox.setWidth(dmdinfo.getWidth() * zoomX);
      dragBox.setHeight(dmdinfo.getHeight() * zoomX);
    });
  }

  private VPinScreen getSelectedScreen() {
    return radioOnBackglass.isSelected() ? VPinScreen.BackGlass:
      radioOnB2sDMD.isSelected() ? VPinScreen.DMD: VPinScreen.PlayField;
  }

  private DMDInfo fillDmdInfo() {
    dmdinfo.setX(dragBox.getX() / zoom.get());
    dmdinfo.setY(dragBox.getY() / zoom.get());
    dmdinfo.setWidth(dragBox.getWidth() / zoom.get());
    dmdinfo.setHeight(dragBox.getHeight() / zoom.get());
    return dmdinfo;
  }
}

package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.dmd.DMDInfo;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.util.JFXFuture;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.mephisto.vpin.ui.Studio.client;

import java.net.URL;
import java.util.ResourceBundle;

public class DMDPositionController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(DMDPositionController.class);

  @FXML
  private CheckBox aspectRatioCheckbox;

  @FXML
  private RadioButton radioOnBackglass;
  @FXML
  private RadioButton radioOnB2sDMD;

  @FXML
  private Spinner<Double> xSpinner;
  @FXML
  private Spinner<Double> ySpinner;
  @FXML
  private Spinner<Double> widthSpinner;
  @FXML
  private Spinner<Double> heightSpinner;

  @FXML
  private Pane imagepane;

  @FXML
  private ImageView fullDMDImage;

  private GameRepresentation game;

  private DMDPositionResizer dragBox;

  // The image bounds
  private ObjectProperty<Bounds> area = new SimpleObjectProperty<>();

  private ObjectProperty<Color> color = new SimpleObjectProperty<>(Color.LIME);


  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSaveClick(ActionEvent e) {

  }

  @FXML
  private void onAutoPosition() {

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
    tg.selectedToggleProperty().addListener((obs, o, n) -> {
      //TODO     
    });
  }

  private void configureSpinner(Spinner<Double> spinner, ObjectProperty<Double> property, 
        ReadOnlyObjectProperty<Double> minProperty, ReadOnlyObjectProperty<Double> maxProperty) {
    SpinnerValueFactory.DoubleSpinnerValueFactory factory = 
      new SpinnerValueFactory.DoubleSpinnerValueFactory(minProperty.get(), maxProperty.get());
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
    JFXFuture
      .supplyAsync(() -> client.getDmdPositionService().getDMDInfo(game.getId()))
      .thenAcceptLater(dmdinfo -> setDmdInfo(dmdinfo));
  }

  private void setDmdInfo(DMDInfo dmdinfo) {
    Image image = new Image(client.getRestClient().getBaseUrl() + VPinStudioClientService.API + dmdinfo.getBackgroundUrl());
    fullDMDImage.setImage(image);
    fullDMDImage.setPreserveRatio(true);

    if (VPinScreen.BackGlass.equals(dmdinfo.getOnScreen())) {
      radioOnBackglass.setSelected(true);
    }
    else if (VPinScreen.DMD.equals(dmdinfo.getOnScreen())) {
      radioOnB2sDMD.setSelected(true);
    }

    // The bounds for the DMD resizer 
    Bounds bounds = fullDMDImage.getLayoutBounds();
    area.set(bounds);

    // Position our box
    dragBox.setX(dmdinfo.getX());
    dragBox.setY(dmdinfo.getY());
    dragBox.setWidth(dmdinfo.getWidth());
    dragBox.setHeight(dmdinfo.getHeight());

    // if existing dmd size ratio is close to 4:1, activate the checkbox
    if (dmdinfo != null) {
      double ratio = dmdinfo.getWidth() / dmdinfo.getHeight();
      if (Math.abs(ratio - 4) < 0.01) {
        aspectRatioCheckbox.setSelected(true);
      }
    }
  }

}

package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.util.JFXFuture;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
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
import java.util.function.Consumer;

public class DMDPositionController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(DMDPositionController.class);

  @FXML
  private CheckBox aspectRatioCheckbox;

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
  }

  @Override
  public void onDialogCancel() {

  }

  public void setGame(GameRepresentation gameRepresentation) {
    this.game = gameRepresentation;

    JFXFuture.supplyAsync(() -> {
      return client.getDmdPositionService().getDMDInfo(game.getId());
    })
    .thenAcceptLater(dmdinfo -> {

      Image image = new Image(client.getRestClient().getBaseUrl() + VPinStudioClientService.API + dmdinfo.getBackgroundUrl());
      fullDMDImage.setImage(image);
      fullDMDImage.setPreserveRatio(true);

      // The bounds for the DMD resizer 
      Bounds area = fullDMDImage.getLayoutBounds();
      // The lime box that is used to position the DMD
      dragBox = new DMDPositionResizer(imagepane, area, aspectRatioCheckbox.selectedProperty(), Color.LIME);

      configureSpinner(dragBox.xProperty(), xSpinner, dmdinfo.getX(), 0, image.getWidth(), null);
      configureSpinner(dragBox.yProperty(), ySpinner, dmdinfo.getY(), 0, image.getHeight(), null);
      configureSpinner(dragBox.widthProperty(), widthSpinner, dmdinfo.getWidth(), 0, image.getWidth(), (w) -> {
        if (aspectRatioCheckbox.isSelected()) {
          heightSpinner.getValueFactory().valueProperty().setValue(w / 4);
        }
      });
      configureSpinner(dragBox.heightProperty(), heightSpinner, dmdinfo.getHeight(), 0, image.getHeight(), (h) -> {
        if (aspectRatioCheckbox.isSelected()) {
          widthSpinner.getValueFactory().setValue(h * 4);
        }
      });

    // add a selector in the pane to draw a rectangle.
    new DMDPositionSelection(imagepane, area, aspectRatioCheckbox.selectedProperty(), Color.LIME, 
      // called on drag start, hide the lime dragbox 
      () -> {
        dragBox.setVisible(false);
      }, 
      // called once dragged, show th edragbox back and reposition/resize it
      rect -> {
        dragBox.setVisible(true);
        dragBox.select();

        xSpinner.getValueFactory().setValue(rect.getMinX());
        ySpinner.getValueFactory().setValue(rect.getMinY());
        widthSpinner.getValueFactory().setValue(rect.getWidth());
        heightSpinner.getValueFactory().setValue(rect.getHeight());
      });
      
      // if existing dmd size ratio is close to 4:1, activate the checkbox
      if (dmdinfo != null) {
        double ratio = dmdinfo.getWidth() / dmdinfo.getHeight();
        if (Math.abs(ratio - 4) < 0.01) {
          aspectRatioCheckbox.setSelected(true);
        }
      }
    });
  }
  private void configureSpinner(DoubleProperty property, Spinner<Double> spinner, double value, double min, double max, Consumer<Double> spinnerHook) {
    SpinnerValueFactory.DoubleSpinnerValueFactory factory = new SpinnerValueFactory.DoubleSpinnerValueFactory(min, max, 0);
    spinner.setValueFactory(factory);
    spinner.setEditable(true);

    // Set the bidirectionnal binding between the spinner and a property of the resizer
    spinner.getValueFactory().valueProperty().addListener((x, o, n) -> {
      // hook must run before setting the value so that when we set it up, 
      // it has already the good value and does not trigger a new change
      if (spinnerHook != null) {
        spinnerHook.accept(n);
      }
      property.set(n);
    });
    property.addListener((x, o, n) -> {
      spinner.getValueFactory().setValue((Double) n);
    });

    // now that propeties are bound, sets the value, that also changes the associated property
    spinner.getValueFactory().setValue(value);
  }
}

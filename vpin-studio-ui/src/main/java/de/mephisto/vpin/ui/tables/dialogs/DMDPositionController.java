package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
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

import java.net.URL;
import java.util.ResourceBundle;

public class DMDPositionController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(DMDPositionController.class);

  @FXML
  private CheckBox aspectRationCheckbox;

  @FXML
  private Spinner<Integer> xSpinner;
  @FXML
  private Spinner<Integer> ySpinner;
  @FXML
  private Spinner<Integer> widthSpinner;
  @FXML
  private Spinner<Integer> heightSpinner;

  @FXML
  private Pane stackpane;

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

//    DMDInfo dmdInfo = client.getDmdPositionService().getDMDInfo(this.game.getId());
    Image image = new Image("http://localhost:8089/api/v1/dmdposition/background/-1"); //TODO
    fullDMDImage.setImage(image);
    fullDMDImage.setPreserveRatio(true);

    // The bounds for the DMD handler 
    Rectangle2D area = new Rectangle2D(0, 0, fullDMDImage.getFitWidth(), fullDMDImage.getFitHeight());

    dragBox = new DMDPositionResizer(stackpane, area, Color.LIME);

    configureSpinner(dragBox.layoutXProperty(), xSpinner, 20, 0, image.getWidth());
    configureSpinner(dragBox.layoutYProperty(), ySpinner, 20, 0, image.getHeight());
    configureSpinner(dragBox.widthProperty(), widthSpinner, 400, 0, image.getWidth());
    configureSpinner(dragBox.heightProperty(), heightSpinner, 100, 0, image.getHeight());


   // add a selector in the pane to draw a 
   new DMDPositionSelection(stackpane, area, Color.LIME, 
     () -> {
      dragBox.setVisible(false);
     }, 
     rect -> {
      dragBox.setVisible(true);

      xSpinner.getValueFactory().setValue((int) rect.getMinX());
      ySpinner.getValueFactory().setValue((int) rect.getMinY());
      widthSpinner.getValueFactory().setValue((int) rect.getWidth());
      heightSpinner.getValueFactory().setValue((int) rect.getHeight());
     });  
  }
  private void configureSpinner(DoubleProperty property, Spinner<Integer> spinner, int value, int min, double max) {
    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(min, (int) max, 0);
    spinner.setValueFactory(factory);
    spinner.setEditable(true);

    spinner.valueProperty().addListener((x, o, n) -> {
      property.setValue(n);
    });
    property.addListener((x, o, n) -> {
      spinner.getValueFactory().setValue(n.intValue());
    });

    // now set the value, that also change the associated property
    spinner.getValueFactory().setValue(value);
  }
}

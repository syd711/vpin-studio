package de.mephisto.vpin.commons.fx;

import de.mephisto.vpin.commons.utils.localsettings.LocalUISettings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class DialogHeaderController implements Initializable {
  private final Debouncer debouncer = new Debouncer();

  private double xOffset;
  private double yOffset;

  @FXML
  private BorderPane header;

  @FXML
  private Label titleLabel;

  private Stage stage;
  private DialogController dialogController;
  private String id;

  @FXML
  private void onCloseClick() {
    Object userData = stage.getUserData();
    if(userData instanceof DialogController) {
      ((DialogController)userData).onDialogCancel();
    }
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    header.setUserData(this);
    header.setOnMousePressed(event -> {
      xOffset = stage.getX() - event.getScreenX();
      yOffset = stage.getY() - event.getScreenY();
    });
    header.setOnMouseDragged(event -> {
      stage.setX(event.getScreenX() + xOffset);
      stage.setY(event.getScreenY() + yOffset);
    });
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  public void setTitle(String title) {
    titleLabel.setText(title);
  }

  public void enableStateListener(Stage stage, DialogController dialogController, String id) {
    this.dialogController = dialogController;
    this.id = id;
    stage.xProperty().addListener((observable, oldValue, newValue) -> onDragDone());
    stage.yProperty().addListener((observable, oldValue, newValue) -> onDragDone());
    stage.widthProperty().addListener((observable, oldValue, newValue) -> onDragDone());
    stage.heightProperty().addListener((observable, oldValue, newValue) -> onDragDone());
  }

  @FXML
  public void onDragDone() {
    debouncer.debounce("position", () -> {
      int y = (int) stage.getY();
      int x = (int) stage.getX();
      int width = (int) stage.getWidth();
      int height = (int) stage.getHeight();
      LocalUISettings.saveLocation(id, x, y, width, height);

      dialogController.onResized(x, y, width, height);
    }, 500);
  }
}

package de.mephisto.vpin.commons.fx;

import de.mephisto.vpin.commons.utils.FXResizeHelper;
import de.mephisto.vpin.commons.utils.LocalUISettings;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class DialogHeaderResizeableController implements Initializable {
  private final Debouncer debouncer = new Debouncer();

  @FXML
  private Button maximizeBtn;

  @FXML
  private Button minimizeBtn;

  @FXML
  private Label titleLabel;

  @FXML
  private BorderPane header;

  private static MouseEvent event;

  public void setStateId(String stateId) {
    this.stageId = stateId;
  }

  private String stageId;

  @FXML
  private void onMouseClick(MouseEvent e) {
    if (e.getClickCount() == 2) {
      FXResizeHelper helper = (FXResizeHelper) getStage().getUserData();
      helper.switchWindowedMode(e);
    }
  }

  private Stage getStage() {
    return (Stage) header.getScene().getWindow();
  }

  @FXML
  private void onCloseClick() {
    getStage().close();
  }

  @FXML
  private void onDragDone() {
    if (titleLabel.getText() != null) {
      debouncer.debounce("position", () -> {
        int y = (int) getStage().getY();
        int x = (int) getStage().getX();
        int width = (int) getStage().getWidth();
        int height = (int) getStage().getHeight();
        if (width > 0 && height > 0) {
          LocalUISettings.saveLocation(stageId, x, y, width, height);
        }
      }, 500);
    }
  }

  @FXML
  private void onMaximize() {
    FXResizeHelper helper = (FXResizeHelper) getStage().getUserData();
    helper.switchWindowedMode(event);
  }

  @FXML
  private void onHideClick() {
    getStage().setIconified(true);
  }

  public void setTitle(String title) {
    titleLabel.setText(title);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    header.setUserData(this);

    Platform.runLater(() -> {
      getStage().xProperty().addListener((observable, oldValue, newValue) -> onDragDone());
      getStage().yProperty().addListener((observable, oldValue, newValue) -> onDragDone());
      getStage().widthProperty().addListener((observable, oldValue, newValue) -> onDragDone());
      getStage().heightProperty().addListener((observable, oldValue, newValue) -> onDragDone());

      header.setOnMouseMoved(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
          DialogHeaderResizeableController.event = event;
        }
      });
    });

  }
}
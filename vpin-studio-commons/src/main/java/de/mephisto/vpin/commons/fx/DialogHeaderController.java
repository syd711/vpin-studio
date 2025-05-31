package de.mephisto.vpin.commons.fx;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.localsettings.LocalUISettings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;

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

  /** The dirty indicator */
  private BooleanProperty dirty = new SimpleBooleanProperty(false);

  @FXML
  private Button modalBtn;

  private Stage stage;
  private DialogController dialogController;
  private String id;

  private boolean modal = true;

  @FXML
  private void onCloseClick() {
    Object userData = stage.getUserData();
    if (userData instanceof DialogController) {
      ((DialogController) userData).onDialogCancel();
    }
    stage.close();
  }

  @FXML
  private void onModalToggle() {
    modal = !modal;
    Object userData = stage.getUserData();
    if (userData instanceof DialogController) {
      ((DialogController) userData).setModality(modal);
    }
  }

  public void setModal(boolean b) {
    modal = b;
    if (modalBtn != null) {
      if (b) {
        FontIcon icon = WidgetFactory.createIcon("mdi2p-pin");
        icon.setIconSize(16);
        modalBtn.setGraphic(icon);
      }
      else {
        FontIcon icon = WidgetFactory.createIcon("mdi2p-pin-off-outline");
        icon.setIconSize(16);
        modalBtn.setGraphic(icon);
      }
    }
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
    // update title when the dirty flag changes
    dirty.addListener((pbs, o, v) -> updateTitle());
  }

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  public Stage getStage() {
    return stage;
  }

  public void setTitle(String title) {
    titleLabel.setText(title);
  }

  //---------------------------------------------
  // Dirty management

  private static final String dirtySuffix = " (*)";

  public boolean isDirty() {
    return this.dirty.get();
  }

  public void setDirty(boolean dirty) {
    this.dirty.set(dirty);
  }

  public BooleanProperty dirtyProperty() {
    return dirty;
  }
  
  protected void updateTitle() {
    String title = titleLabel.getText();
    if (dirty.get() && !title.endsWith(dirtySuffix)) {
      titleLabel.setText(title + dirtySuffix);
    }
    else if (!dirty.get() && title.endsWith(dirtySuffix)) {
      titleLabel.setText(StringUtils.removeEnd(title, dirtySuffix));
    }
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

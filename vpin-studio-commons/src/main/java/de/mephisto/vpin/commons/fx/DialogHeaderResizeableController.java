package de.mephisto.vpin.commons.fx;

import de.mephisto.vpin.commons.utils.FXResizeHelper;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

import org.kordamp.ikonli.javafx.FontIcon;

public class DialogHeaderResizeableController extends DialogHeaderController {

  @FXML
  private Button maximizeBtn;

  @FXML
  private Button minimizeBtn;

  // memorize last MouseEvent for maximize click
  private MouseEvent event;

  @FXML
  private void onMouseClick(MouseEvent e) {
    if (e.getClickCount() == 2) {
      FXResizeHelper helper = (FXResizeHelper) getStage().getUserData();
      boolean isMaximize = helper.switchWindowedMode(e);
      refreshWindowMaximizedState(isMaximize);
    }
  }

  public void setMaximizeable(boolean b) {
    this.maximizeBtn.setVisible(b);
  }

  @FXML
  private void onMaximize() {
    FXResizeHelper helper = (FXResizeHelper) getStage().getUserData();
    boolean isMaximize = helper.switchWindowedMode(event);
    refreshWindowMaximizedState(isMaximize);
  }

  @FXML
  private void onHideClick() {
    getStage().setIconified(true);
  }

  private void refreshWindowMaximizedState(boolean mIsMaximized) {
    FontIcon icon = WidgetFactory.createIcon(mIsMaximized? "mdi2w-window-restore" : "mdi2w-window-maximize");
    icon.setIconSize(16);
    maximizeBtn.setGraphic(icon);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  	super.initialize(url, resourceBundle);
    minimizeBtn.setVisible(false);
    header.setOnMouseMoved(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        DialogHeaderResizeableController.this.event = event;
      }
    });
  }
}

package de.mephisto.vpin.ui;

import de.mephisto.vpin.ui.util.Dialogs;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.mephisto.vpin.ui.Studio.client;

public class StudioKeyEventHandler implements EventHandler<KeyEvent> {
  private final static Logger LOG = LoggerFactory.getLogger(StudioKeyEventHandler.class);
  public static final double SCALING_DIFF = 0.05;
  private static double scaling = 1;

  private final Stage stage;

  public StudioKeyEventHandler(Stage stage) {
    this.stage = stage;
  }

  @Override
  public void handle(KeyEvent ke) {
    if (ke.getCode() == KeyCode.U && ke.isAltDown() && ke.isControlDown()) {
      Dialogs.openUpdateInfoDialog(client.getSystemService().getVersion(), true);
      ke.consume();
    }
    if (ke.getCode() == KeyCode.MINUS && ke.isAltDown() && ke.isControlDown()) {
      Parent r = stage.getScene().getRoot();
      if (scaling < SCALING_DIFF) {
        return;
      }
      scaling = scaling - SCALING_DIFF;
      stage.setWidth(stage.getWidth() * scaling);
      stage.setHeight(stage.getHeight() * scaling);

      r.setScaleX(scaling);
      r.setScaleY(scaling);
      ke.consume();
    }
    if (ke.getCode() == KeyCode.PLUS && ke.isAltDown() && ke.isControlDown()) {
      Parent r = stage.getScene().getRoot();
      if (scaling > (1 - SCALING_DIFF)) {
        return;
      }
      scaling = scaling + SCALING_DIFF;
      r.setScaleX(scaling);
      r.setScaleY(scaling);
      stage.setWidth(stage.getWidth() * scaling);
      stage.setHeight(stage.getHeight() * scaling);
      ke.consume();
    }
    if (ke.getCode() == KeyCode.H && ke.isAltDown() && ke.isControlDown()) {
      Parent r = stage.getScene().getRoot();
      scaling = 1;
      stage.setWidth(1920);
      stage.setHeight(1080);
      r.setScaleX(1);
      r.setScaleY(1);
      ke.consume();
    }
    if (ke.getCode() == KeyCode.S && ke.isAltDown() && ke.isControlDown()) {
      Parent r = stage.getScene().getRoot();
      scaling = 1;
      stage.setWidth(1280);
      stage.setHeight(720);
      r.setScaleX(1);
      r.setScaleY(1);
      ke.consume();
    }
    if (ke.getCode() == KeyCode.W && ke.isAltDown() && ke.isControlDown()) {
      Parent r = stage.getScene().getRoot();
      scaling = 1;
      stage.setWidth(2560);
      stage.setHeight(1440);
      r.setScaleX(1);
      r.setScaleY(1);
      ke.consume();
    }
    if (ke.getCode() == KeyCode.C && ke.isAltDown() && ke.isControlDown()) {
      Parent r = stage.getScene().getRoot();
      scaling = 1;
      stage.setWidth(1024);
      stage.setHeight(600);
      r.setScaleX(1);
      r.setScaleY(1);
      ke.consume();
    }

    if (!ke.isConsumed()) {
      StudioFXController controller = NavigationController.getActiveNavigationController();
      if (controller != null) {
        controller.onKeyEvent(ke);
      }
    }
  }
}

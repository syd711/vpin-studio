package de.mephisto.vpin.commons.utils;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.awt.*;
import java.util.HashMap;

/**
 * @author Simon Reinisch
 * @version 0.0.2
 */
public class FXResizeHelper {

  private final HashMap<Cursor, EventHandler<MouseEvent>> LISTENER = new HashMap<>();
  private final Stage STAGE;
  private final Scene SCENE;
  private final int TR;
  private final int TM;
  private final double SCREEN_WIDTH, SCREEN_HEIGHT;

  private double mPresSceneX, mPresSceneY;
  private double mPresScreeX, mPresScreeY;
  private double mPresStageW, mPresStageH;

  private double mWidthStore, mHeightStore, mXStore, mYStore;
  private boolean verticalOnly;

  private Object userData;

  public FXResizeHelper(Stage stage, int dt, int rt) {
    this(stage, dt, rt, false);
  }

  /**
   * Create an FXResizeHelper for undecoreated JavaFX Stages.
   * The only wich is your job is to create an padding for the Stage so the user can resize it.
   *
   * @param stage - The JavaFX Stage.
   * @param dt    - The area (in px) where the user can drag the window.
   * @param rt    - The area (in px) where the user can resize the window.
   */
  public FXResizeHelper(Stage stage, int dt, int rt, boolean verticalOnly) {
    this.verticalOnly = verticalOnly;
    this.TR = rt;
    this.TM = dt + rt;
    this.STAGE = stage;
    this.SCENE = stage.getScene();

    this.SCREEN_HEIGHT = Screen.getPrimary().getVisualBounds().getHeight();
    this.SCREEN_WIDTH = Screen.getPrimary().getVisualBounds().getWidth();

    createListener();
    launch();
  }

  public Object getUserData() {
    return userData;
  }

  public void setUserData(Object userData) {
    this.userData = userData;
  }

  /**
   * Minimize the stage.
   */
  public void minimize() {
    STAGE.setIconified(true);
  }

  /**
   * If the stage is maximized, it will be restored to the last postition
   * with heigth and width. Otherwise it will be maximized to fullscreen.
   */
  public void switchWindowedMode(MouseEvent e) {
    ObservableList<Screen> screensForRectangle = Screen.getScreensForRectangle(e.getScreenX(), e.getScreenY(), 1, 1);
    Screen screen = screensForRectangle.get(0);

    boolean mIsMaximized = STAGE.getX() == 0 && STAGE.getY() == 0;

    if (mIsMaximized) {
      STAGE.setX(mXStore > 0  ? mXStore : 50);
      STAGE.setY(mYStore > 0  ? mYStore : 50);
      STAGE.setWidth(mWidthStore > 0  ? mWidthStore : STAGE.getWidth() - 100);
      STAGE.setHeight(mHeightStore > 0 ? mHeightStore : STAGE.getHeight() - 100);
    }
    else {
      mXStore = STAGE.getX();
      mYStore = STAGE.getY();
      mWidthStore = STAGE.getWidth();
      mHeightStore = STAGE.getHeight();

      if (screen.equals(Screen.getPrimary())) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice defaultScreenDevice = ge.getDefaultScreenDevice();
        GraphicsConfiguration defaultConfiguration = defaultScreenDevice.getDefaultConfiguration();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(defaultConfiguration);
        STAGE.setY(screenInsets.top);
        STAGE.setX(screenInsets.left);
      }
      else {
        STAGE.setX(screen.getBounds().getMinX());
        STAGE.setY(screen.getBounds().getMinY());
      }


      STAGE.setWidth(screen.getVisualBounds().getWidth());
      STAGE.setHeight(screen.getVisualBounds().getHeight());
    }
  }

  private void createListener() {
    if (!verticalOnly) {
      LISTENER.put(Cursor.NW_RESIZE, event -> {
        double newWidth = mPresStageW - (event.getScreenX() - mPresScreeX);
        double newHeight = mPresStageH - (event.getScreenY() - mPresScreeY);
        if (newHeight > STAGE.getMinHeight()) {
          STAGE.setY(event.getScreenY() - mPresSceneY);
          STAGE.setHeight(newHeight);
        }
        if (newWidth > STAGE.getMinWidth()) {
          STAGE.setX(event.getScreenX() - mPresSceneX);
          STAGE.setWidth(newWidth);
        }
      });

      LISTENER.put(Cursor.NE_RESIZE, event -> {
        double newWidth = mPresStageW - (event.getScreenX() - mPresScreeX);
        double newHeight = mPresStageH + (event.getScreenY() - mPresScreeY);
        if (newHeight > STAGE.getMinHeight()) STAGE.setHeight(newHeight);
        if (newWidth > STAGE.getMinWidth()) {
          STAGE.setX(event.getScreenX() - mPresSceneX);
          STAGE.setWidth(newWidth);
        }
      });

      LISTENER.put(Cursor.SW_RESIZE, event -> {
        double newWidth = mPresStageW + (event.getScreenX() - mPresScreeX);
        double newHeight = mPresStageH - (event.getScreenY() - mPresScreeY);
        if (newHeight > STAGE.getMinHeight()) {
          STAGE.setHeight(newHeight);
          STAGE.setY(event.getScreenY() - mPresSceneY);
        }
        if (newWidth > STAGE.getMinWidth()) {
          STAGE.setWidth(newWidth);
        }
      });

      LISTENER.put(Cursor.SE_RESIZE, event -> {
        double newWidth = mPresStageW + (event.getScreenX() - mPresScreeX);
        double newHeight = mPresStageH + (event.getScreenY() - mPresScreeY);
        if (newHeight > STAGE.getMinHeight()) STAGE.setHeight(newHeight);
        if (newWidth > STAGE.getMinWidth()) STAGE.setWidth(newWidth);
      });
    }

    if (!verticalOnly) {
      LISTENER.put(Cursor.E_RESIZE, event -> {
        double newWidth = mPresStageW - (event.getScreenX() - mPresScreeX);
        if (newWidth > STAGE.getMinWidth()) {
          STAGE.setX(event.getScreenX() - mPresSceneX);
          STAGE.setWidth(newWidth);
        }
      });

      LISTENER.put(Cursor.W_RESIZE, event -> {
        double newWidth = mPresStageW + (event.getScreenX() - mPresScreeX);
        if (newWidth > STAGE.getMinWidth()) STAGE.setWidth(newWidth);
      });
    }


    LISTENER.put(Cursor.N_RESIZE, event -> {
      double newHeight = mPresStageH - (event.getScreenY() - mPresScreeY);
      if (newHeight > STAGE.getMinHeight()) {
        STAGE.setY(event.getScreenY() - mPresSceneY);
        STAGE.setHeight(newHeight);
      }
    });

    LISTENER.put(Cursor.S_RESIZE, event -> {
      double newHeight = mPresStageH + (event.getScreenY() - mPresScreeY);
      if (newHeight > STAGE.getMinHeight()) STAGE.setHeight(newHeight);
    });

    LISTENER.put(Cursor.OPEN_HAND, event -> {
      STAGE.setX(event.getScreenX() - mPresSceneX);
      STAGE.setY(event.getScreenY() - mPresSceneY);
    });
  }

  private void launch() {

    SCENE.setOnMousePressed(event -> {
      mPresSceneX = event.getSceneX();
      mPresSceneY = event.getSceneY();

      mPresScreeX = event.getScreenX();
      mPresScreeY = event.getScreenY();

      mPresStageW = STAGE.getWidth();
      mPresStageH = STAGE.getHeight();
    });

    SCENE.setOnMouseMoved(event -> {
      double sx = event.getSceneX();
      double sy = event.getSceneY();

      boolean l_trigger = sx > 0 && sx < TR;
      boolean r_trigger = sx < SCENE.getWidth() && sx > SCENE.getWidth() - TR;
      boolean u_trigger = sy < SCENE.getHeight() && sy > SCENE.getHeight() - TR;
      boolean d_trigger = sy > 0 && sy < TR;

      if (l_trigger && d_trigger && !verticalOnly) {
        fireAction(Cursor.NW_RESIZE);
      }
      else if (l_trigger && u_trigger && !verticalOnly) {
        fireAction(Cursor.NE_RESIZE);
      }
      else if (r_trigger && d_trigger && !verticalOnly) {
        fireAction(Cursor.SW_RESIZE);
      }
      else if (r_trigger && u_trigger && !verticalOnly) {
        fireAction(Cursor.SE_RESIZE);
      }
      else if (l_trigger && !verticalOnly) {
        fireAction(Cursor.E_RESIZE);
      }
      else if (r_trigger && !verticalOnly) {
        fireAction(Cursor.W_RESIZE);
      }
      else if (d_trigger) {
        fireAction(Cursor.N_RESIZE);
      }
      else if (sy < TM && !u_trigger) {
        fireAction(Cursor.OPEN_HAND);
      }
      else if (u_trigger) {
        fireAction(Cursor.S_RESIZE);
      }
      else {
        fireAction(Cursor.DEFAULT);
      }
    });
  }

  private void fireAction(Cursor c) {
    SCENE.setCursor(c);
    if (c != Cursor.DEFAULT) SCENE.setOnMouseDragged(LISTENER.get(c));
    else SCENE.setOnMouseDragged(null);
  }

  public void setVerticalOnly() {
    verticalOnly = true;
  }
}
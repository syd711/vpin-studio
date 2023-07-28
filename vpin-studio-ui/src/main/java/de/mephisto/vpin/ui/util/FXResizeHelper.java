package de.mephisto.vpin.ui.util;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;

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

  private boolean mIsMaximized = false;
  private double mWidthStore, mHeightStore, mXStore, mYStore;

  /**
   * Create an FXResizeHelper for undecoreated JavaFX Stages.
   * The only wich is your job is to create an padding for the Stage so the user can resize it.
   *
   * @param stage - The JavaFX Stage.
   * @param dt    - The area (in px) where the user can drag the window.
   * @param rt    - The area (in px) where the user can resize the window.
   */
  public FXResizeHelper(Stage stage, int dt, int rt) {
    this.TR = rt;
    this.TM = dt + rt;
    this.STAGE = stage;
    this.SCENE = stage.getScene();

    this.SCREEN_HEIGHT = Screen.getPrimary().getVisualBounds().getHeight();
    this.SCREEN_WIDTH = Screen.getPrimary().getVisualBounds().getWidth();

    createListener();
    launch();
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
  public void switchWindowedMode() {
    if (mIsMaximized) {
      STAGE.setY(mYStore);
      STAGE.setX(mXStore);
      STAGE.setWidth(mWidthStore);
      STAGE.setHeight(mHeightStore);
    } else {
      mXStore = STAGE.getX();
      mYStore = STAGE.getY();
      mWidthStore = STAGE.getWidth();
      mHeightStore = STAGE.getHeight();

      STAGE.setY(0);
      STAGE.setX(0);
      STAGE.setWidth(SCREEN_WIDTH);
      STAGE.setHeight(SCREEN_HEIGHT);
    }
    mIsMaximized = !mIsMaximized;
  }

  private void createListener() {
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

      if (l_trigger && d_trigger) fireAction(Cursor.NW_RESIZE);
      else if (l_trigger && u_trigger) fireAction(Cursor.NE_RESIZE);
      else if (r_trigger && d_trigger) fireAction(Cursor.SW_RESIZE);
      else if (r_trigger && u_trigger) fireAction(Cursor.SE_RESIZE);
      else if (l_trigger) fireAction(Cursor.E_RESIZE);
      else if (r_trigger) fireAction(Cursor.W_RESIZE);
      else if (d_trigger) fireAction(Cursor.N_RESIZE);
      else if (sy < TM && !u_trigger) fireAction(Cursor.OPEN_HAND);
      else if (u_trigger) fireAction(Cursor.S_RESIZE);
      else fireAction(Cursor.DEFAULT);
    });
  }

  private void fireAction(Cursor c) {
    SCENE.setCursor(c);
    if (c != Cursor.DEFAULT) SCENE.setOnMouseDragged(LISTENER.get(c));
    else SCENE.setOnMouseDragged(null);
  }

}
package de.mephisto.vpin.commons.utils;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.awt.*;
import java.util.HashMap;

/**
 * Adds drag-to-move and edge-drag-to-resize behavior to an undecorated (StageStyle.TRANSPARENT)
 * JavaFX Stage.
 */
public class FXResizeHelper {

  private final HashMap<Cursor, EventHandler<MouseEvent>> LISTENER = new HashMap<>();
  private final Stage STAGE;
  private final Scene SCENE;
  private final int TR;
  private final int TM;
  // distance (px) between the Scene's edges and the window's actual visible border, e.g. the
  // padding a dialog reserves around its content for a drop shadow; edge/drag hit-zones below
  // are measured from the visible border, not the raw Scene edge, so they stay aligned with it
  private final int MARGIN;

  private double mPresSceneX, mPresSceneY;
  private double mPresScreeX, mPresScreeY;
  private double mPresStageW, mPresStageH;

  private double mWidthStore, mHeightStore, mXStore, mYStore;
  private boolean verticalOnly;

  // the resize/drag mode chosen at MOUSE_PRESSED; stays fixed until the button is released
  private Cursor activeCursor = Cursor.DEFAULT;

  private Object userData;

  // tracks how the stage's geometry was last set, so keyboard/mouse maximize, snap, and
  // restore actions agree on where "restore" should return the stage to
  private enum WindowState { NORMAL, MAXIMIZED }

  private WindowState windowState = WindowState.NORMAL;


  public static void install(Stage stage, int dt, int rt) {
    new FXResizeHelper(stage, dt, rt, false, 0);
  }

  public static void install(Stage stage, int dt, int rt, boolean verticalOnly) {
    new FXResizeHelper(stage, dt, rt, verticalOnly, 0);
  }

  /**
   * Same as {@link #install(Stage, int, int)}, but for a Scene whose root reserves {@code margin}
   * px of padding around the visible window content (e.g. for a drop shadow), so edge/drag
   * hit-zones are measured from the visible border instead of the raw Scene edge.
   */
  public static void install(Stage stage, int dt, int rt, int margin) {
    new FXResizeHelper(stage, dt, rt, false, margin);
  }

  /**
   * Create an FXResizeHelper for undecoreated JavaFX Stages.
   * The only which is your job is to create an padding for the Stage so the user can resize it.
   *
   * @param stage - The JavaFX Stage.
   * @param dt    - The area (in px) where the user can drag the window.
   * @param rt    - The area (in px) where the user can resize the window.
   * @param margin - px of padding between the Scene's edges and the visible window border.
   */
  private FXResizeHelper(Stage stage, int dt, int rt, boolean verticalOnly, int margin) {
    this.verticalOnly = verticalOnly;
    this.TR = rt;
    this.TM = dt + rt;
    this.MARGIN = margin;
    this.STAGE = stage;
    this.SCENE = stage.getScene();

    // replace the Stage userData, normally a DialogController
    this.userData = stage.getUserData();
    stage.setUserData(this);

    createListener();
    launch();
  }

  public Object getUserData() {
    return userData;
  }

  /**
   * Minimize the stage.
   */
  public void minimize() {
    STAGE.setIconified(true);
  }

  // tolerance for comparing stage size against screen bounds, to absorb DPI-scaling rounding
  private static final double MAXIMIZED_EPSILON = 1.0;

  private static Screen getScreen(Stage stage) {
    double centerX = stage.getX() + stage.getWidth() / 2;
    double centerY = stage.getY() + stage.getHeight() / 2;

    ObservableList<Screen> screensForRectangle = Screen.getScreensForRectangle(centerX, centerY, 1, 1);
    if (screensForRectangle.isEmpty()) {
      return Screen.getPrimary();
    }
    return screensForRectangle.getFirst();
  }

  private static boolean isMaximized(Stage stage, Screen screen) {
    return Math.abs(stage.getWidth() - screen.getVisualBounds().getWidth()) < MAXIMIZED_EPSILON
        && Math.abs(stage.getHeight() - screen.getVisualBounds().getHeight()) < MAXIMIZED_EPSILON;
  }

  public static boolean isMaximized(Stage stage) {
    return isMaximized(stage, getScreen(stage));
  }

  /**
   * If the stage is maximized, it will be restored to the last postition
   * with heigth and width. Otherwise it will be maximized to fullscreen.
   */
  public boolean switchWindowedMode(MouseEvent e) {
    Screen screen = getScreen(STAGE);

    boolean mIsMaximized = isMaximized(STAGE, screen);

    if (mIsMaximized) {
      STAGE.setX(mXStore);
      STAGE.setY(mYStore);

      double width = mWidthStore > 0 ? mWidthStore : STAGE.getWidth() - 100;
      double height = mHeightStore > 0 ? mHeightStore : STAGE.getHeight() - 100;

      if (width > 0) {
        STAGE.setWidth(width);
      }
      if (height > 0) {
        STAGE.setHeight(height);
      }
      windowState = WindowState.NORMAL;
    }
    else {
      storeNormalGeometry();

      if (screen.equals(Screen.getPrimary())) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice defaultScreenDevice = ge.getDefaultScreenDevice();
        GraphicsConfiguration defaultConfiguration = defaultScreenDevice.getDefaultConfiguration();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(defaultConfiguration);
        STAGE.setY(screenInsets.top);
        STAGE.setX(screenInsets.left);
      }
      else {
        STAGE.setX(screen.getVisualBounds().getMinX());
        STAGE.setY(screen.getVisualBounds().getMinY());
      }


      if (screen.getVisualBounds().getWidth() > 0) {
        STAGE.setWidth(screen.getVisualBounds().getWidth());
      }

      if (screen.getVisualBounds().getHeight() > 0) {
        STAGE.setHeight(screen.getVisualBounds().getHeight());
      }
      windowState = WindowState.MAXIMIZED;
    }
    return !mIsMaximized;
  }

  /**
   * Maximizes the stage to fill the current screen's visual bounds, mirroring the OS Win+Up
   * shortcut. No-op if already maximized.
   */
  public void maximize() {
    if (windowState == WindowState.MAXIMIZED) {
      return;
    }
    storeNormalGeometry();

    Rectangle2D bounds = getScreen(STAGE).getVisualBounds();
    STAGE.setX(bounds.getMinX());
    STAGE.setY(bounds.getMinY());
    STAGE.setWidth(bounds.getWidth());
    STAGE.setHeight(bounds.getHeight());
    windowState = WindowState.MAXIMIZED;
  }

  /**
   * Mirrors the OS Win+Down shortcut: restores a maximized/snapped stage to its previous bounds,
   * or minimizes it if it's already at its normal (non-maximized, non-snapped) size.
   */
  public void restoreOrMinimize() {
    if (windowState == WindowState.NORMAL) {
      minimize();
      return;
    }
    STAGE.setX(mXStore);
    STAGE.setY(mYStore);
    STAGE.setWidth(mWidthStore);
    STAGE.setHeight(mHeightStore);
    windowState = WindowState.NORMAL;
  }

  private void storeNormalGeometry() {
    if (windowState == WindowState.NORMAL) {
      mXStore = STAGE.getX();
      mYStore = STAGE.getY();
      mWidthStore = STAGE.getWidth();
      mHeightStore = STAGE.getHeight();
    }
  }

  private void createListener() {
    if (!verticalOnly) {
      LISTENER.put(Cursor.NW_RESIZE, event -> {
        double newWidth = mPresStageW - (event.getScreenX() - mPresScreeX);
        double newHeight = mPresStageH - (event.getScreenY() - mPresScreeY);
        if (newHeight > STAGE.getMinHeight() && newHeight > 0) {
          STAGE.setY(event.getScreenY() - mPresSceneY);
          STAGE.setHeight(newHeight);
        }
        if (newWidth > STAGE.getMinWidth() && newWidth > 0) {
          STAGE.setX(event.getScreenX() - mPresSceneX);
          STAGE.setWidth(newWidth);
        }
      });

      LISTENER.put(Cursor.NE_RESIZE, event -> {
        double newWidth = mPresStageW + (event.getScreenX() - mPresScreeX);
        double newHeight = mPresStageH - (event.getScreenY() - mPresScreeY);
        if (newHeight > STAGE.getMinHeight() && newHeight > 0) {
          STAGE.setY(event.getScreenY() - mPresSceneY);
          STAGE.setHeight(newHeight);
        }
        if (newWidth > STAGE.getMinWidth() && newWidth > 0) {
          STAGE.setWidth(newWidth);
        }
      });

      LISTENER.put(Cursor.SW_RESIZE, event -> {
        double newWidth = mPresStageW - (event.getScreenX() - mPresScreeX);
        double newHeight = mPresStageH + (event.getScreenY() - mPresScreeY);
        if (newHeight > STAGE.getMinHeight() && newHeight > 0) {
          STAGE.setHeight(newHeight);
        }
        if (newWidth > STAGE.getMinWidth() && newWidth > 0) {
          STAGE.setX(event.getScreenX() - mPresSceneX);
          STAGE.setWidth(newWidth);
        }
      });

      LISTENER.put(Cursor.SE_RESIZE, event -> {
        double newWidth = mPresStageW + (event.getScreenX() - mPresScreeX);
        double newHeight = mPresStageH + (event.getScreenY() - mPresScreeY);
        if (newHeight > STAGE.getMinHeight() && newHeight > 0) {
          STAGE.setHeight(newHeight);
        }
        if (newWidth > STAGE.getMinWidth() && newWidth > 0) {
          STAGE.setWidth(newWidth);
        }
      });
    }

    if (!verticalOnly) {
      LISTENER.put(Cursor.W_RESIZE, event -> {
        double newWidth = mPresStageW - (event.getScreenX() - mPresScreeX);
        if (newWidth > STAGE.getMinWidth() && newWidth > 0) {
          STAGE.setX(event.getScreenX() - mPresSceneX);
          STAGE.setWidth(newWidth);
        }
      });

      LISTENER.put(Cursor.E_RESIZE, event -> {
        double newWidth = mPresStageW + (event.getScreenX() - mPresScreeX);
        if (newWidth > STAGE.getMinWidth() && newWidth > 0) {
          STAGE.setWidth(newWidth);
        }
      });
    }


    LISTENER.put(Cursor.N_RESIZE, event -> {
      double newHeight = mPresStageH - (event.getScreenY() - mPresScreeY);
      if (newHeight > STAGE.getMinHeight() && newHeight > 0) {
        STAGE.setY(event.getScreenY() - mPresSceneY);
        STAGE.setHeight(newHeight);
      }
    });

    LISTENER.put(Cursor.S_RESIZE, event -> {
      double newHeight = mPresStageH + (event.getScreenY() - mPresScreeY);
      if (newHeight > STAGE.getMinHeight() && newHeight > 0) {
        STAGE.setHeight(newHeight);
      }
    });

    LISTENER.put(Cursor.OPEN_HAND, event -> {
      STAGE.setX(event.getScreenX() - mPresSceneX);
      STAGE.setY(event.getScreenY() - mPresSceneY);
    });
  }

  private void launch() {

    // Registered as capturing-phase filters (not bubble-phase handlers) so the resize/drag
    // gesture is captured before a descendant control - e.g. a ToolBar, which is known to
    // swallow MOUSE_PRESSED/MOUSE_DRAGGED even over its empty background - can consume it first.
    SCENE.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
      // Decide the gesture from the press position, not SCENE.getCursor(): the scene cursor is
      // only refreshed on MOUSE_MOVED and can be stale (e.g. a SplitPane divider sets its own
      // cursor on its node), which used to resize the window while dragging an inner splitter.
      activeCursor = event.isPrimaryButtonDown() ? cursorAt(event.getSceneX(), event.getSceneY()) : Cursor.DEFAULT;

      mPresSceneX = event.getSceneX();
      mPresSceneY = event.getSceneY();

      mPresScreeX = event.getScreenX();
      mPresScreeY = event.getScreenY();

      mPresStageW = STAGE.getWidth();
      mPresStageH = STAGE.getHeight();
    });

    SCENE.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
      EventHandler<MouseEvent> handler = LISTENER.get(activeCursor);
      if (handler != null) {
        // a manual move/resize makes the geometry user-defined again, so a later restore
        // shouldn't jump back to a stale maximized/snapped position
        windowState = WindowState.NORMAL;
        handler.handle(event);
      }
    });

    SCENE.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> activeCursor = Cursor.DEFAULT);

    SCENE.setOnMouseMoved(event -> fireAction(cursorAt(event.getSceneX(), event.getSceneY())));

    // Once the pointer leaves the window, no further MOUSE_MOVED events arrive on this scene,
    // so a cursor set while hovering the drag/resize zones (e.g. OPEN_HAND) would otherwise stay
    // stuck. Skip the reset while a drag/resize is in progress (button held) to avoid glitching it.
    SCENE.setOnMouseExited(event -> {
      if (!event.isPrimaryButtonDown()) {
        fireAction(Cursor.DEFAULT);
      }
    });
  }

  /**
   * Maps a Scene position to the resize/drag cursor of the hit-zone it falls into
   * ({@link Cursor#DEFAULT} if it is in none).
   */
  private Cursor cursorAt(double sx, double sy) {
    double left = MARGIN;
    double top = MARGIN;
    double right = SCENE.getWidth() - MARGIN;
    double bottom = SCENE.getHeight() - MARGIN;

    boolean l_trigger = sx > left - TR && sx < left + TR;
    boolean r_trigger = sx < right + TR && sx > right - TR;
    boolean u_trigger = sy < bottom + TR && sy > bottom - TR;
    boolean d_trigger = sy > top - TR && sy < top + TR;

    if (l_trigger && d_trigger && !verticalOnly) {
      return Cursor.NW_RESIZE;
    }
    else if (l_trigger && u_trigger && !verticalOnly) {
      return Cursor.SW_RESIZE;
    }
    else if (r_trigger && d_trigger && !verticalOnly) {
      return Cursor.NE_RESIZE;
    }
    else if (r_trigger && u_trigger && !verticalOnly) {
      return Cursor.SE_RESIZE;
    }
    else if (l_trigger && !verticalOnly) {
      return Cursor.W_RESIZE;
    }
    else if (r_trigger && !verticalOnly) {
      return Cursor.E_RESIZE;
    }
    else if (d_trigger) {
      return Cursor.N_RESIZE;
    }
    else if (sy < top + TM && !u_trigger) {
      return Cursor.OPEN_HAND;
    }
    else if (u_trigger) {
      return Cursor.S_RESIZE;
    }
    return Cursor.DEFAULT;
  }

  private void fireAction(Cursor c) {
    SCENE.setCursor(c);
  }

  public void setVerticalOnly() {
    verticalOnly = true;
  }
}

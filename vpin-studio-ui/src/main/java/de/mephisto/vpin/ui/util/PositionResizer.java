package de.mephisto.vpin.ui.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

public class PositionResizer {

  private static int handleSize = 6, handleSize2 = handleSize / 2;
  private static int minSize = 8;

  /** The color of the box */
  private ObjectProperty<Color> colorProperty = new SimpleObjectProperty<>(Color.LIME);

  // Display elements, oi screen coordinates 
  private Group overlay;
  private Rectangle srFill, srBnd;
  private Rectangle[][] handles;

  private int sX, sY, centerX, centerY, sWidth, sHeight;
  private int moveX = 0;
  private int moveY = 0;
  private boolean resizeCentered = false;

  private int _areaMinX = -1, areaMaxX = -1, areaMinY = -1, areaMaxY = -1;
  private boolean acceptOutsidePart = true;

  private ObjectProperty<Integer>  xProperty, xMinProperty, xMaxProperty;
  private ObjectProperty<Integer>  yProperty, yMinProperty, yMaxProperty;
  private ObjectProperty<Integer>  widthProperty, widthMinProperty, widthMaxProperty;
  private ObjectProperty<Integer>  heightProperty, heightMinProperty, heightMaxProperty;

  /** The internal zoom to transform the coordinate of the box into screen coordinates */
  private double zoomX = 1.0;
  private double zoomY = 1.0;
  /** The aspect ratio between x/y that must be maintained. none if null */
  private Double aspectRatio = null;


  private Object userData;

  boolean eventTriggered = false;
  private EventHandler<MouseEvent> eventHandler = (e) -> {
    if (!eventTriggered) {
      overlay.setVisible(false);
    }
    eventTriggered = false;
  };

  public PositionResizer() {
    initOverlay();

    xProperty = new SimpleObjectProperty<Integer>(0);
    xMinProperty = new SimpleObjectProperty<Integer>(0);
    xMaxProperty = new SimpleObjectProperty<Integer>(0);
    xProperty.addListener((v, o, x) -> {
      if (areaMaxX >= 0) {
        if (acceptOutsidePart) {

        } else {
          widthMaxProperty.set(areaMaxX - x.intValue());
        }
      }
      srFill.setX(x.intValue() * zoomX);
      srBnd.setX(x.intValue() * zoomX);
      for (int i = 0; i <= 2; i++) {
        for (int j = 0; j <= 2; j++) {
          handles[i][j].setX((x.intValue() + i * widthProperty.get() / 2.0) * zoomX - i * handleSize2);
        }
      }
    });
    yProperty = new SimpleObjectProperty<Integer>(0);
    yMinProperty = new SimpleObjectProperty<Integer>(0);
    yMaxProperty = new SimpleObjectProperty<Integer>(0);
    yProperty.addListener((v, o, y) -> {
      if (areaMaxY >= 0 && !acceptOutsidePart) {
        heightMaxProperty.set(areaMaxY - y.intValue());
      }
      srFill.setY(y.intValue() * zoomY);
      srBnd.setY(y.intValue() * zoomY);
      for (int i = 0; i <= 2; i++) {
        for (int j = 0; j <= 2; j++) {
          handles[i][j].setY((y.intValue() + j * heightProperty.get() / 2.0) * zoomY - j * handleSize2);
        }
      }
    });
    widthProperty = new SimpleObjectProperty<Integer>(0);
    widthMinProperty = new SimpleObjectProperty<Integer>(0);
    widthMaxProperty = new SimpleObjectProperty<Integer>(0);
    widthProperty.addListener((v, o, width) -> {
      if (!processing) {
        processing = true;
        if (areaMaxX >= 0 && xProperty.get() + width.intValue() > areaMaxX) {
          width = areaMaxX - xProperty.get();
        }
        double newwidth = checkWidth(width.intValue());
        widthProperty.set((int) newwidth);
        ensureHeightRatio(newwidth);
        processing = false;
      }
      if (areaMaxX >= 0) {
        xMaxProperty.set(areaMaxX - width.intValue());
      }
      srFill.setWidth(width.intValue() * zoomX);
      srBnd.setWidth(width.intValue() * zoomX);
      for (int i = 1; i <= 2; i++) {
        for (int j = 0; j <= 2; j++) {
          handles[i][j].setX((xProperty.get() + i * width.intValue() / 2.0) * zoomX - i * handleSize2);
        }
      }
    });
    heightProperty = new SimpleObjectProperty<Integer>(0);
    heightMinProperty = new SimpleObjectProperty<Integer>(0);
    heightMaxProperty = new SimpleObjectProperty<Integer>(0);
    heightProperty.addListener((v, o, height) -> {
      if (!processing) {
        processing = true;
        if (areaMaxY >= 0 && yProperty.get() + height.intValue() > areaMaxY) {
          height = areaMaxY - yProperty.get();
        }
        double newheight = checkHeight(height.intValue());
        heightProperty.set((int) newheight);
        ensureWidthRatio(newheight);
        processing = false;
      }
      if (areaMaxY >= 0) {
        yMaxProperty.set(areaMaxY - height.intValue());
      }
      srFill.setHeight(height.intValue() * zoomY);
      srBnd.setHeight(height.intValue() * zoomY);
      for (int i = 0; i <= 2; i++) {
        for (int j = 1; j <= 2; j++) {
          handles[i][j].setY((yProperty.get() + j * height.intValue()   / 2.0) * zoomY - j * handleSize2);
        }
      }
    });
  }

  boolean processing = false;

  public void addToPane(Pane pane) {
    pane.getChildren().add(srFill);
    pane.getChildren().add(overlay);
    pane.addEventFilter(MouseEvent.MOUSE_PRESSED, eventHandler);
  }

  public void removeFromPane(Pane pane) {
    pane.getChildren().remove(srFill);
    pane.getChildren().remove(overlay);
    pane.removeEventFilter(MouseEvent.MOUSE_PRESSED, eventHandler);
  }

  private boolean isResizeCentered(int move) {
    return /*move == 0 ||*/ resizeCentered;
  }

  //-------------------

  public ObjectProperty<Integer> xProperty() {
    return xProperty;
  }

  public ObjectProperty<Integer> yProperty() {
    return yProperty;
  }

  public ObjectProperty<Integer> widthProperty() {
    return widthProperty;
  }

  public ObjectProperty<Integer> heightProperty() {
    return heightProperty;
  }

  public ReadOnlyObjectProperty<Integer> xMinProperty() {
    return xMinProperty;
  }

  public ReadOnlyObjectProperty<Integer> xMaxProperty() {
    return xMaxProperty;
  }

  public ReadOnlyObjectProperty<Integer> yMinProperty() {
    return yMinProperty;
  }

  public ReadOnlyObjectProperty<Integer> yMaxProperty() {
    return yMaxProperty;
  }

  public ReadOnlyObjectProperty<Integer> widthMinProperty() {
    return widthMinProperty;
  }

  public ReadOnlyObjectProperty<Integer> widthMaxProperty() {
    return widthMaxProperty;
  }

  public ReadOnlyObjectProperty<Integer> heightMinProperty() {
    return heightMinProperty;
  }

  public ReadOnlyObjectProperty<Integer> heightMaxProperty() {
    return heightMaxProperty;
  }

  public int getX() {
    return xProperty.get();
  }

  public void setX(int newX) {
    setInternalX(newX);
  }

  protected void setInternalX(int newX) {
    int bufferX = acceptOutsidePart ? widthProperty.get() - minSize : 0;
    if (_areaMinX >= 0 && newX + bufferX < _areaMinX) {
      newX = _areaMinX - bufferX;
    }
    if (areaMaxX >= 0 && newX + widthProperty.get() > areaMaxX) {
      newX = areaMaxX - widthProperty.get();
    }
    xProperty.set(newX);
  }

  public int getY() {
    return yProperty.get();
  }

  public void setY(int newY) {
    setInternalY(newY);
  }

  protected void setInternalY(int y) {
    if (areaMinY >= 0 && y < areaMinY) {
      y = areaMinY;
    }
    if (areaMaxY >= 0 && y + heightProperty.get() > areaMaxY) {
      y = areaMaxY - heightProperty.get();
    }
    yProperty.set(y);
  }

  public int getWidth() {
    return widthProperty.get();
  }

  public void setWidth(int width) {
    widthProperty.set(width);
  }

  public int getHeight() {
    return heightProperty.get();
  }

  public void setHeight(int height) {
    heightProperty.set(height);
  }

  public void setAspectRatio(Double aspectRatio) {
    this.aspectRatio = aspectRatio;
    if (aspectRatio != null && isSelected()) {
      ensureWidthRatio(heightProperty.get());
    }
  }

  public void setZoom(double zoom) {
    this.zoomX = zoom;
    this.zoomY = zoom;
  }
  public void setZoomX(double zoomX) {
    this.zoomX = zoomX;
  }
  public void setZoomY(double zoomY) {
    this.zoomY = zoomY;
  }

  public Color getColor() {
    return colorProperty.get();
  }
  public void setColor(Color color) {
    colorProperty.set(color);
  }
  public ObjectProperty<Color> colorProperty() {
    return colorProperty;
  }

  public Bounds getBounds() {
    return new BoundingBox(getX(), getY(), getWidth(), getHeight());
  }

  public Object getUserData() {
    return userData;
  }

  public void setUserData(Object userData) {
    this.userData = userData;
  } 

  public void setBounds(Bounds area) {
    if (area != null) {
      setBounds((int) area.getMinX(), (int) area.getMinY(), (int) area.getMaxX(), (int) area.getMaxY());
    }
    else {
      setBounds(-1, -1, -1, -1);
    }
  }

  public void setBounds(int minX, int minY, int maxX, int maxY) {
    _areaMinX = minX;
    areaMinY = minY;
    areaMaxX = maxX;
    areaMaxY = maxY;
    //this.acceptOutsidePart = acceptOutsidePart;

    int bufferX = acceptOutsidePart ? widthProperty.get() - minSize : 0;

    // set max then min to avoid error
    xMaxProperty.set(areaMaxX - widthProperty.get() < _areaMinX - bufferX ? _areaMinX - bufferX : areaMaxX - widthProperty.get());
    xMinProperty.set(_areaMinX - bufferX);
    yMaxProperty.set(areaMaxY - heightProperty.get() < areaMinY ? areaMinY : areaMaxY - heightProperty.get());
    yMinProperty.set(areaMinY);
    widthMaxProperty.set(areaMaxX - xProperty.get() < minSize ? minSize : areaMaxX - xProperty.get());
    widthMinProperty.set(minSize);
    heightMaxProperty.set(areaMaxY - yProperty.get() < minSize ? minSize : areaMaxY - yProperty.get());
    heightMinProperty.set(minSize);
  }

  //-------------------

  public boolean keyPressed(KeyEvent event) {
    if (overlay.isVisible()) {
      KeyCode code = event.getCode();
      switch (code) {
        case LEFT:
          if (event.isControlDown() || event.isShiftDown()) {
            moveFrom(-1, 0, event.isShiftDown());
            moveXPos(xProperty.get() - 1);
          }
          else if (event.isAltDown()) {
            moveFrom(+1, 0, false);
            moveXPos(xProperty.get() + widthProperty.get() - 1);
          }
          else {
            relocateInArea(xProperty.get() - 1, yProperty.get());
          }
          return true;
        case RIGHT:
          if (event.isControlDown() || event.isShiftDown()) {
            moveFrom(-1, 0, event.isShiftDown());
            moveXPos(xProperty.get() + 1);
          }
          else if (event.isAltDown()) {
            moveFrom(+1, 0, false);
            moveXPos(xProperty.get() + widthProperty.get() + 1);
          }
          else {
            relocateInArea(xProperty.get() + 1, yProperty.get());
          }
          return true;
        case UP:
          if (event.isControlDown() || event.isShiftDown()) {
            moveFrom(0, -1, event.isShiftDown());
            moveYPos(yProperty.get() - 1);
          }
          else if (event.isAltDown()) {
            moveFrom(0, +1, false);
            moveYPos(yProperty.get() + heightProperty.get() - 1);
          }
          else {
            relocateInArea(xProperty.get(), yProperty.get() - 1);
          }
          return true;
        case DOWN:
          if (event.isControlDown() || event.isShiftDown()) {
            moveFrom(0, -1, event.isShiftDown());
            moveYPos(yProperty.get() + 1);
          }
          else if (event.isAltDown()) {
            moveFrom(0, +1, false);
            moveYPos(yProperty.get() + heightProperty.get() + 1);
          }
          else {
            relocateInArea(xProperty.get(), yProperty.get() + 1);
          }
          return true;
        default:
      }
    }
    return false;
  }

  public void setVisible(boolean b) {
    srFill.setVisible(b);
    overlay.setVisible(b);
  }

  public boolean isSelected() {
    return overlay.isVisible();
  }

  public BooleanProperty selectProperty() {
    return overlay.visibleProperty();
  }

  public void select() {
    srFill.toFront();
    overlay.toFront();
    overlay.setVisible(true);
  }

  @Override
  public String toString() {
    return "[" + xProperty.get() + ", " + yProperty.get() + ", " + widthProperty.get() + ", " + heightProperty.get() + "]";
  }

  private void initOverlay() {
    srFill = new Rectangle();
    srFill.fillProperty().bind(colorProperty);
    srFill.setOpacity(0.3);
    srFill.setOnMousePressed(e -> {
      select();
      eventTriggered = true;
      srBnd.fireEvent(e);
      e.consume();
    });
    srFill.setOnMouseDragged(e -> {
      srBnd.fireEvent(e);
      e.consume();
    });
    srFill.setOnMouseReleased(e -> {
      srBnd.fireEvent(e);
      e.consume();
    });

    overlay = new Group();
    srBnd = new Rectangle();
    srBnd.strokeProperty().bind(colorProperty);
    srBnd.setStrokeType(StrokeType.INSIDE);
    srBnd.setStrokeWidth(1);
    srBnd.getStrokeDashArray().addAll(2d, 4d);
    srBnd.setFill(Color.TRANSPARENT);
    handleMouse(srBnd);
    overlay.getChildren().add(srBnd);

    this.handles = new Rectangle[3][3];
    for (int i = 0; i <= 2; i++) {
      for (int j = 0; j <= 2; j++) {
        String name = (j==0 ? "N" : j==1 ? "" : "S")
          + (i==0 ? "W" : i==1 ? "" : "E")
          + "_RESIZE";
        if (i != 1 || j != 1) {
          handles[i][j] = srCreate(Cursor.cursor(name));
          overlay.getChildren().add(handles[i][j]);
        }
        else {
          // dummy one to avoid NPE
          handles[i][j] = new Rectangle();
        }
      }
    }

    // unselect when focus is lost
    overlay.focusedProperty().addListener((obs, oldVal, newVal) -> {
      if (!newVal) {
        overlay.setVisible(false);
      }
    });

    overlay.setVisible(false);
  }

  private Rectangle srCreate(Cursor cursor) {
    Rectangle rectangle = new Rectangle(handleSize, handleSize);
    rectangle.fillProperty().bind(colorProperty);
    rectangle.setCursor(cursor);
    handleMouse(rectangle);
    return rectangle;
  }

  private void handleMouse(Rectangle node) {
    node.setOnMousePressed(me -> mousePressed(me));
    node.setOnMouseDragged(me -> mouseDragged(me));
    node.setOnMouseReleased(me -> mouseReleased(me));
  }

  private void mousePressed(MouseEvent me) {
    overlay.setVisible(true);
    sX = (int) (xProperty.get() * zoomX - me.getX());
    sY = (int) (yProperty.get() * zoomY - me.getY());

    Object source = me.getSource();
    for (int i = 0; i <= 2; i++) {
      for (int j = 0; j <= 2; j++) {
        if (source == handles[i][j]) {
          moveFrom(i-1, j-1, me.isShiftDown());
        }
      }
    }
    me.consume();
  }

  private void mouseDragged(MouseEvent me) {
    int x = (int) ((me.getX() + sX) / zoomX);
    int y = (int) ((me.getY() + sY) / zoomY);
    Object source = me.getSource();

    if (source == srBnd) {
      relocateInArea(x, y);
    }
    else {
      // could have been pressed during the drag
      if (this.resizeCentered != me.isShiftDown()) {
        mousePressed(me);
      }

      for (int i = 0; i <= 2; i++) {
        for (int j = 0; j <= 2; j++) {
          if (source == handles[i][j]) {
            if (i == 0) {
              moveXPos(x);
            }
            else if (i == 2) {
              moveXPos(x + sWidth);
            }

            if (j == 0 && (i == 1 || aspectRatio == null)) {
              moveYPos(y);
            }
            else if (j == 2 && (i == 1 || aspectRatio == null)) {
              moveYPos(y + sHeight);
            }
          }
        }
      }  
    } 

    me.consume();
  }

  private void mouseReleased(MouseEvent me) {
    me.consume();
  }

  //-----------------------------------------

  private void moveFrom(int moveX, int moveY, boolean resizeCentered) {
    this.moveX = moveX;
    this.moveY = moveY;
    this.resizeCentered = resizeCentered;

    this.sWidth = widthProperty.get();
    this.sHeight = aspectRatio != null ? (int) (sWidth / aspectRatio) : heightProperty.get();
    this.centerX = xProperty.get() + sWidth / 2;
    this.centerY = yProperty.get() + sHeight / 2;
  }

  private void moveXPos(int newX) {
    int x1 = 0, x2 = 0;
    boolean resizeCentered = isResizeCentered(aspectRatio != null ? moveY : -1);

    int bufferX = acceptOutsidePart ? widthProperty.get() - minSize : 0;

    if (moveX < 0) {
      x1 = newX; 
      if (_areaMinX >= 0 && x1 < _areaMinX - bufferX) {
        x1 = _areaMinX - bufferX;
      }
      x2 = resizeCentered ? centerX + (centerX - x1) : xProperty.get() + widthProperty.get();
      if (resizeCentered && areaMaxX >= 0 && x2 > areaMaxX) {
        x1 = centerX + (centerX - areaMaxX);
        x2 = areaMaxX;
      }
    }
    else if (moveX > 0) {
      x2 = newX;
      if (_areaMinX >= 0 && x2 < _areaMinX + minSize) {
        x2 = _areaMinX + minSize;
      }
      if (areaMaxX >= 0 && x2 > areaMaxX) {
        x2 = areaMaxX;
      }
      x1 = resizeCentered ? centerX - (x2 - centerX) : xProperty.get();
      if (resizeCentered && _areaMinX >= 0 && x1 < _areaMinX - bufferX) {
        x1 = _areaMinX - bufferX;
        x2 = centerX + (centerX - _areaMinX + bufferX);
      }
    }
    
    if (moveX != 0) {
      double width = x2 - x1;
      if (width < minSize) {
        width = minSize;
      }
      width = checkWidth(width);

      if (moveX < 0 || resizeCentered) {
        x1 = (int) (resizeCentered ? centerX - width / 2 : x2 - width);
        xProperty.set(x1);
      }
      widthProperty.set((int) width);
      ensureHeightRatio(width);
    }
  }

  private void moveYPos(int newY) {
    int y1 = 0, y2 = 0;
    boolean resizeCentered = isResizeCentered(aspectRatio != null ? moveX : -1);

    if (moveY < 0) {
      y1 = newY; 
      if (areaMinY >= 0 && y1 < areaMinY) {
        y1 = areaMinY;
      }
      y2 = resizeCentered ? centerY + (centerY - y1) : yProperty.get() + heightProperty.get();
      if (resizeCentered && areaMaxY >= 0 && y2 > areaMaxY) {
        y1 = centerY + (centerY - areaMaxY);
        y2 = areaMaxY;
      }
    }
    else if (moveY > 0) {
      y2 = newY;
      if (areaMaxY >= 0 && y2 > areaMaxY) {
        y2 = areaMaxY;
      }
      y1 = resizeCentered ? centerY - (y2 - centerY) : yProperty.get();
      if (resizeCentered && areaMinY >= 0 && y1 < areaMinY) {
        y1 = areaMinY;
        y2 = centerY + (centerY - areaMinY);
      }
    }
    
    if (moveY != 0) {
      double height = y2 - y1;
      if (height < minSize) {
        height = minSize;
      }
      height = checkHeight(height);
/*
      if (moveY < 0) {
        y1 = (int) (y2 - height);
        yProperty.set(y1);
      }
      else if (moveY > 0 && resizeCentered) {
        y1 = (int) (centerY - height / 2);
        yProperty.set(y1);
      }
      if (resizeCentered) {
        int x = centerX - widthProperty.get() / 2;
        xProperty.set(x);
      }
*/

    if (moveY < 0 || resizeCentered) {
        y1 = (int) (resizeCentered ? centerY - height / 2 : y2 - height);
        yProperty.set(y1);
      }
      heightProperty.set((int) height);
      ensureWidthRatio(height);
    }
  }

  private double checkWidth(double width) {
    if (aspectRatio != null) {
      boolean resizeCentered = isResizeCentered(moveX);
      double newHeight = width / aspectRatio;

      if (moveY < 0 || resizeCentered) {
        double y1 = resizeCentered ? centerY - newHeight / 2 : yProperty.get() + heightProperty.get() - newHeight;
        if (areaMinY >= 0 && y1 < areaMinY) {
          double h = resizeCentered ? 2 * (centerY - areaMinY) :
            yProperty.get() + heightProperty.get() - areaMinY;
          return h *  aspectRatio;
        }
      } 
      if (moveY > 0 || resizeCentered) {
        double y2 = resizeCentered ? centerY + newHeight / 2 : yProperty.get() + newHeight;
        if (areaMaxY >= 0 && y2 > areaMaxY) {
          double h = resizeCentered ? 2 * (areaMaxY - centerY) :
            areaMaxY - yProperty.get();
          return h * aspectRatio;
        }
      }
    }
    return width;
  }

  private void ensureHeightRatio(double width) {
    if (aspectRatio != null) {
      double height = width / aspectRatio;
      if (heightProperty.get() != height) {
        if (isResizeCentered(moveX)) {
          yProperty.set(centerY - (int) (height / 2));
        }
        else if (moveY < 0) {
          yProperty.set(yProperty.get() + heightProperty.get() - (int) height);
        }
        heightProperty.set((int) height);
      }
    }
  }

  private double checkHeight(double height) {
    if (aspectRatio != null) {
      boolean resizeCentered = isResizeCentered(moveX);
      double newWidth = height * aspectRatio;

      if (moveY < 0 || resizeCentered) {
        int bufferX = acceptOutsidePart ? widthProperty.get() - minSize : 0;
      	double x1 = resizeCentered ? centerX - newWidth / 2 : xProperty.get() + widthProperty.get() - newWidth;
        if (_areaMinX >=0 && x1 < _areaMinX - bufferX) {
          double w = resizeCentered ? 2 * (centerX - _areaMinX + bufferX) :
            xProperty.get() + widthProperty.get() - _areaMinX + bufferX;
          return w /  aspectRatio;
		}
      }
      if (moveY > 0 || resizeCentered) {
      	double x2 = resizeCentered ? centerX + newWidth / 2 : xProperty.get() + newWidth;
        if (areaMaxX >= 0 && x2 > areaMaxX) {
          double w = resizeCentered ? 2 * (areaMaxX - centerX) :
            areaMaxX - xProperty.get();
          return w / aspectRatio;
        }
      }
    }
    return height;
  }

  private void ensureWidthRatio(double height) {
    if (aspectRatio != null) {
      double width = height * aspectRatio;
      if (widthProperty.get() != width) {
        if (isResizeCentered(moveY)) {
          xProperty.set(centerX - (int) (width / 2));
        }
        else if (moveX < 0) {
          xProperty.set(xProperty.get() + widthProperty.get() - (int) width);
        }
        widthProperty.set((int) width);
      }
    }
  }

  void relocateInArea(int x, int y) {
    setInternalX(x);
    setInternalY(y);
  }

  public void centerHorizontally() {
    int bufferX = acceptOutsidePart ? widthProperty.get() - minSize : 0;

    if (_areaMinX >= 0 && areaMaxX > _areaMinX - bufferX) {
      double offset = (areaMaxX - _areaMinX + bufferX - widthProperty.get()) / 2;
      if (offset < 0) {
        offset = 0;
      }
      setInternalX(_areaMinX - bufferX + (int) offset);
    }
  }

  public void centerVertically() {
    if (areaMinY >= 0 && areaMaxY > areaMinY) {
      double offset = (areaMaxY - areaMinY - heightProperty.get()) / 2;
      if (offset < 0) {
        offset = 0;
      }
      setInternalY(areaMinY + (int) offset);
    }
  }

}

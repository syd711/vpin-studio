package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.dmd.DMDAspectRatio;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
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

public class DMDPositionResizer {

  private static double handleSize = 6, handleSize2 = handleSize / 2;
  private static double minSize = 8;

  private ObservableValue<Color> colorProperty;

  // Overlay elements
  private Group overlay;
  private Rectangle srFill, srBnd;
  private Rectangle[][] handles;

  private double sX, sY, centerX, centerY, sWidth, sHeight;
  private int moveX = 0;
  private int moveY = 0;
  private boolean resizeCentered = false;

  private ObservableValue<Bounds> areaProperty;

  private ObjectProperty<Double> xProperty, xMinProperty, xMaxProperty;
  private ObjectProperty<Double> yProperty, yMinProperty, yMaxProperty;
  private ObjectProperty<Double> widthProperty, widthMinProperty, widthMaxProperty;
  private ObjectProperty<Double> heightProperty, heightMinProperty, heightMaxProperty;

  private Object userData;

  boolean eventTriggered = false;
  private EventHandler<MouseEvent> eventHandler = (e) -> {
    if (!eventTriggered) {
      overlay.setVisible(false);
    }
    eventTriggered = false;
  };

  private DoubleProperty zoomProperty;

  private ObservableValue<DMDAspectRatio> aspectRatioProperty;

  DMDPositionResizer(ObjectProperty<Bounds> areaProperty, DoubleProperty zoomProperty, ObservableValue<DMDAspectRatio> aspectRatio, ObjectProperty<Color> colorProperty) {
    this.areaProperty = areaProperty;
    this.zoomProperty = zoomProperty;
    this.aspectRatioProperty = aspectRatio;
    this.colorProperty = colorProperty;

    initOverlay();

    xProperty = new SimpleObjectProperty<>(0d);
    xMinProperty = new SimpleObjectProperty<>(0d);
    xMaxProperty = new SimpleObjectProperty<>(0d);
    xProperty.addListener((v, o, x) -> {
      widthMaxProperty.set(areaProperty.get().getMaxX() - x);
      srFill.setX(x);
      srBnd.setX(x);
      for (int i = 0; i <= 2; i++) {
        for (int j = 0; j <= 2; j++) {
          handles[i][j].setX((x + i * widthProperty.get() / 2) - i * handleSize2);
        }
      }
    });
    yProperty = new SimpleObjectProperty<>(0d);
    yMinProperty = new SimpleObjectProperty<>(0d);
    yMaxProperty = new SimpleObjectProperty<>(0d);
    yProperty.addListener((v, o, y) -> {
      heightMaxProperty.set(areaProperty.get().getMaxY() - y);
      srFill.setY(y);
      srBnd.setY(y);
      for (int i = 0; i <= 2; i++) {
        for (int j = 0; j <= 2; j++) {
          handles[i][j].setY((y + j * heightProperty.get() / 2) - j * handleSize2);
        }
      }
    });
    widthProperty = new SimpleObjectProperty<>(0d);
    widthMinProperty = new SimpleObjectProperty<>(0d);
    widthMaxProperty = new SimpleObjectProperty<>(0d);
    widthProperty.addListener((v, o, width) -> {
      Bounds area = areaProperty.get();
      if (!processing) {
        processing = true;
        if (xProperty.get() + width > area.getMaxX()) {
          width = area.getMaxX() - xProperty.get();
        }
        width = checkWidth(width);
        widthProperty.set(width);
        ensureHeightRatio(width);
        processing = false;
      }
      xMaxProperty.set(area.getMaxX() - width);
      srFill.setWidth(width);
      srBnd.setWidth(width);
      for (int i = 1; i <= 2; i++) {
        for (int j = 0; j <= 2; j++) {
          handles[i][j].setX((xProperty.get() + i * width / 2) - i * handleSize2);
        }
      }
    });
    heightProperty = new SimpleObjectProperty<>(0d);
    heightMinProperty = new SimpleObjectProperty<>(0d);
    heightMaxProperty = new SimpleObjectProperty<>(0d);
    heightProperty.addListener((v, o, height) -> {
      Bounds area = areaProperty.get();
      if (!processing) {
        processing = true;
        if (yProperty.get() + height > area.getMaxY()) {
          height = area.getMaxY() - yProperty.get();
        }
        height = checkHeight(height);
        heightProperty.set(height);
        ensureWidthRatio(height);
        processing = false;
      }
      yMaxProperty.set(areaProperty.get().getMaxY() - height);
      srFill.setHeight(height);
      srBnd.setHeight(height);
      for (int i = 0; i <= 2; i++) {
        for (int j = 1; j <= 2; j++) {
          handles[i][j].setY((yProperty.get() + j * height / 2) - j * handleSize2);
        }
      }
    });

    aspectRatioProperty.addListener((obs, old, newRatio) -> {
      if (isKeepRatio() && isSelected()) {
        ensureWidthRatio(heightProperty.get());
      }
    });

    areaProperty.addListener((v, o, area) -> {
      // set max then min to avoid error
      xMaxProperty.set(area.getMaxX() - widthProperty.get() < area.getMinX() ? area.getMinX() : area.getMaxX() - widthProperty.get());
      xMinProperty.set(area.getMinX());
      yMaxProperty.set(area.getMaxY() - heightProperty.get() < area.getMinY() ? area.getMinY() : area.getMaxY() - heightProperty.get());
      yMinProperty.set(area.getMinY());
      widthMaxProperty.set(area.getMaxX() - xProperty.get() < minSize ? minSize : area.getMaxX() - xProperty.get());
      widthMinProperty.set(minSize);
      heightMaxProperty.set(area.getMaxY() - yProperty.get() < minSize ? minSize : area.getMaxY() - yProperty.get());
      heightMinProperty.set(minSize);
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

  private boolean isKeepRatio() {
    return aspectRatioProperty != null? aspectRatioProperty.getValue().isKeepRatio() : false;
  }

  private double getRatio() {
    return aspectRatioProperty != null? aspectRatioProperty.getValue().getValue() : 0.0;
  }

  private boolean isResizeCentered() {
    return resizeCentered;
  }

  //-------------------

  public ObjectProperty<Double> xProperty() {
    return xProperty;
  }

  public ObjectProperty<Double> yProperty() {
    return yProperty;
  }

  public ObjectProperty<Double> widthProperty() {
    return widthProperty;
  }

  public ObjectProperty<Double> heightProperty() {
    return heightProperty;
  }

  public ReadOnlyObjectProperty<Double> xMinProperty() {
    return xMinProperty;
  }

  public ReadOnlyObjectProperty<Double> xMaxProperty() {
    return xMaxProperty;
  }

  public ReadOnlyObjectProperty<Double> yMinProperty() {
    return yMinProperty;
  }

  public ReadOnlyObjectProperty<Double> yMaxProperty() {
    return yMaxProperty;
  }

  public ReadOnlyObjectProperty<Double> widthMinProperty() {
    return widthMinProperty;
  }

  public ReadOnlyObjectProperty<Double> widthMaxProperty() {
    return widthMaxProperty;
  }

  public ReadOnlyObjectProperty<Double> heightMinProperty() {
    return heightMinProperty;
  }

  public ReadOnlyObjectProperty<Double> heightMaxProperty() {
    return heightMaxProperty;
  }

  public double getX() {
    return xProperty.get() / zoomProperty.get();
  }

  public void setX(double newX) {
    setInternalX(newX * zoomProperty.get());
  }

  public void setInternalX(double newX) {
    Bounds area = areaProperty.getValue();
    if (newX < area.getMinX()) {
      newX = area.getMinX();
    }
    if (newX + widthProperty.get() > area.getMaxX()) {
      newX = area.getMaxX() - widthProperty.get();
    }
    xProperty.set(newX);
  }

  public double getY() {
    return yProperty.get() / zoomProperty.get();
  }

  public void setY(double newY) {
    setInternalY(newY * zoomProperty.get());
  }

  public void setInternalY(double y) {
    Bounds area = areaProperty.getValue();
    if (y < area.getMinY()) {
      y = area.getMinY();
    }
    if (y + heightProperty.get() > area.getMaxY()) {
      y = area.getMaxY() - heightProperty.get();
    }
    yProperty.set(y);
  }

  public double getWidth() {
    return widthProperty.get() / zoomProperty.get();
  }

  public void setWidth(double width) {
    widthProperty.set(width * zoomProperty.get());
  }

  public double getHeight() {
    return heightProperty.get() / zoomProperty.get();
  }

  public void setHeight(double height) {
    heightProperty.set(height * zoomProperty.get());
  }

  public Object getUserData() {
    return userData;
  }

  public void setUserData(Object userData) {
    this.userData = userData;
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
    sX = xProperty.get() - me.getX();
    sY = yProperty.get() - me.getY();

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
    double x = me.getX() + sX;
    double y = me.getY() + sY;
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

            if (j == 0 && (i == 1 || !isKeepRatio())) {
              moveYPos(y);
            }
            else if (j == 2 && (i == 1 || !isKeepRatio())) {
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
    this.sHeight = isKeepRatio() ? sWidth / getRatio() : heightProperty.get();
    this.centerX = xProperty.get() + sWidth / 2;
    this.centerY = yProperty.get() + sHeight / 2;
  }

  private void moveXPos(double newX) {
    double x1 = 0, x2 = 0;
    Bounds area = areaProperty.getValue();
    boolean resizeCentered = isResizeCentered();

    if (moveX < 0) {
      x1 = newX; 
      if (x1 < area.getMinX()) {
        x1 = area.getMinX();
      }
      x2 = resizeCentered ? centerX + (centerX - x1) : xProperty.get() + widthProperty.get();
      if (resizeCentered && x2 > area.getMaxX()) {
        x1 = centerX + (centerX - area.getMaxX());
        x2 = area.getMaxX();
      }
    }
    else if (moveX > 0) {
      x2 = newX;
      if (x2 > area.getMaxX()) {
        x2 = area.getMaxX();
      }
      x1 = resizeCentered ? centerX - (x2 - centerX) : xProperty.get();
      if (resizeCentered && x1 < area.getMinX()) {
        x1 = area.getMinX();
        x2 = centerX + (centerX - area.getMinX());
      }
    }
    
    if (moveX != 0) {
      double width = x2 - x1;
      if (width < minSize) {
        width = minSize;
      }
      width = checkWidth(width);

      if (moveX < 0 || resizeCentered) {
        x1 = resizeCentered ? centerX - width / 2 : x2 - width;
        xProperty.set(x1);
      }
      widthProperty.set(width);
      ensureHeightRatio(width);
    }
  }

  private void moveYPos(double newY) {
    double y1 = 0, y2 = 0;
    Bounds area = areaProperty.getValue();
    boolean resizeCentered = isResizeCentered();

    if (moveY < 0) {
      y1 = newY; 
      if (y1 < area.getMinY()) {
        y1 = area.getMinY();
      }
      y2 = resizeCentered ? centerY + (centerY - y1) : yProperty.get() + heightProperty.get();
      if (resizeCentered && y2 > area.getMaxY()) {
        y1 = centerY + (centerY - area.getMaxY());
        y2 = area.getMaxY();
      }
    }
    else if (moveY > 0) {
      y2 = newY;
      if (y2 > area.getMaxY()) {
        y2 = area.getMaxY();
      }
      y1 = resizeCentered ? centerY - (y2 - centerY) : yProperty.get();
      if (resizeCentered && y1 < area.getMinY()) {
        y1 = area.getMinY();
        y2 = centerY + (centerY - area.getMinY());
      }
    }
    
    if (moveY != 0) {
      double height = y2 - y1;
      if (height < minSize) {
        height = minSize;
      }
      height = checkHeight(height);

      if (moveY < 0 || resizeCentered) {
        y1 = resizeCentered ? centerY - height / 2 : y2 - height;
        yProperty.set(y1);
      }
      heightProperty.set(height);
      ensureWidthRatio(height);
    }
  }

  private double checkWidth(double width) {
    if (isKeepRatio()) {
      Bounds area = areaProperty.getValue();
      boolean resizeCentered = isResizeCentered();
      double newHeight = width / getRatio();

      if (moveY < 0 || resizeCentered) {
        double y1 = resizeCentered ? centerY - newHeight / 2 : yProperty.get() + heightProperty.get() - newHeight;
        if (y1 < area.getMinY()) {
          double h = resizeCentered ? 2 * (centerY - area.getMinY()) :
            yProperty.get() + heightProperty.get() - area.getMinY();
          return h *  getRatio();
        }
      } 
      if (moveY > 0 || resizeCentered) {
        double y2 = resizeCentered ? centerY + newHeight / 2 : yProperty.get() + newHeight;
        if (y2 > area.getMaxY()) {
          double h = resizeCentered ? 2 * (area.getMaxY() - centerY) :
            area.getMaxY() - yProperty.get();
          return h * getRatio();
        }
      }
    }
    return width;
  }

  private void ensureHeightRatio(double width) {
    if (isKeepRatio()) {
      double height = width / getRatio();
      if (heightProperty.get() != height) {
        if (isResizeCentered()) {
          yProperty.set(centerY - height / 2);
        }
        else if (moveY < 0) {
          yProperty.set(yProperty.get() + heightProperty.get() - height);
        }
        heightProperty.set(height);
      }
    }
  }

  private double checkHeight(double height) {
    if (isKeepRatio()) {
      Bounds area = areaProperty.getValue();
      boolean resizeCentered = isResizeCentered();
      double newWidth = height * getRatio();

      if (moveX < 0 || resizeCentered) {
      	double x1 = resizeCentered ? centerX - newWidth / 2 : xProperty.get() + widthProperty.get() - newWidth;
        if (x1 < area.getMinX()) {
          double w = resizeCentered ? 2 * (centerX - area.getMinX()) :
            xProperty.get() + widthProperty.get() - area.getMinX();
          return w /  getRatio();
		}
      }
      if (moveX > 0 || resizeCentered) {
      	double x2 = resizeCentered ? centerX + newWidth / 2 : xProperty.get() + newWidth;
        if (x2 > area.getMaxX()) {
          double w = resizeCentered ? 2 * (area.getMaxX() - centerX) :
            area.getMaxX() - xProperty.get();
          return w / getRatio();
        }
      }
    }
    return height;
  }

  private void ensureWidthRatio(double height) {
    if (isKeepRatio()) {
      double width = height * getRatio();
      if (widthProperty.get() != width) {
        if (isResizeCentered()) {
          xProperty.set(centerX - width / 2);
        }
        else if (moveX < 0) {
          xProperty.set(xProperty.get() + widthProperty.get() - width);
        }
        widthProperty.set(width);
      }
    }
  }

  void relocateInArea(double x, double y) {
    setInternalX(x);
    setInternalY(y);
  }

  public void centerHorizontally() {
    Bounds area = areaProperty.getValue();
    double offset = (area.getMaxX() - area.getMinX() - widthProperty.get()) / 2;
    if (offset < 0) {
      offset = 0;
    }
    setInternalX(area.getMinX() + offset);
  }

  public void centerVertically() {
    Bounds area = areaProperty.getValue();
    double offset = (area.getMaxY() - area.getMinY() - heightProperty.get()) / 2;
    if (offset < 0) {
      offset = 0;
    }
    setInternalY(area.getMinY() + offset);
  }

}

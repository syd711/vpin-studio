package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.dmd.DMDAspectRatio;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Toggle;
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

  private ObjectProperty<Color> colorProperty;

  // Overlay elements
  private Group overlay;
  private Rectangle srFill, srBnd, srNW, srN, srNE, srE, srSE, srS, srSW, srW;

  private double sX, sY, sWidth, sHeight;
  private boolean moveWest = false;
  private boolean moveNorth = false;

  private ObjectProperty<Bounds> areaProperty;

  private ObjectProperty<Double> xProperty, xMinProperty, xMaxProperty;
  private ObjectProperty<Double> yProperty, yMinProperty, yMaxProperty;
  private ObjectProperty<Double> widthProperty, widthMinProperty, widthMaxProperty;
  private ObjectProperty<Double> heightProperty, heightMinProperty, heightMaxProperty;


  boolean eventTriggered = false;
  private EventHandler<MouseEvent> eventHandler = (e) -> {
    if (!eventTriggered) {
      overlay.setVisible(false);
    }
    eventTriggered = false;
  };

  private ReadOnlyObjectProperty<Toggle> aspectRatio;
  private BooleanProperty snapCenter;

  DMDPositionResizer(ObjectProperty<Bounds> areaProperty, ReadOnlyObjectProperty<Toggle> aspectRatio, BooleanProperty snapCenter, ObjectProperty<Color> colorProperty) {
    this.areaProperty = areaProperty;
    this.aspectRatio = aspectRatio;
    this.snapCenter = snapCenter;
    this.colorProperty = colorProperty;

    initOverlay();

    xProperty = new SimpleObjectProperty<>(0d);
    xMinProperty = new SimpleObjectProperty<>(0d);
    xMaxProperty = new SimpleObjectProperty<>(0d);
    xProperty.addListener((v, o, x) -> {
      widthMaxProperty.set(areaProperty.get().getMaxX() - x);
      srFill.setX(x);
      srBnd.setX(x);
      srNW.setX(x);
      srN.setX((x + getWidth() / 2) - handleSize2);
      srNE.setX((x + getWidth()) - handleSize);
      srE.setX((x + getWidth()) - handleSize);
      srSE.setX((x + getWidth()) - handleSize);
      srS.setX((x + getWidth() / 2) - handleSize2);
      srSW.setX(x);
      srW.setX(x);
    });
    yProperty = new SimpleObjectProperty<>(0d);
    yMinProperty = new SimpleObjectProperty<>(0d);
    yMaxProperty = new SimpleObjectProperty<>(0d);
    yProperty.addListener((v, o, y) -> {
      heightMaxProperty.set(areaProperty.get().getMaxY() - y);
      srFill.setY(y);
      srBnd.setY(y);
      srNW.setY(y);
      srN.setY(y);
      srNE.setY(y);
      srE.setY((y + getHeight() / 2) - handleSize2);
      srSE.setY((y + getHeight()) - handleSize);
      srS.setY((y + getHeight()) - handleSize);
      srSW.setY((y + getHeight()) - handleSize);
      srW.setY((y + getHeight() / 2) - handleSize2);
    });
    widthProperty = new SimpleObjectProperty<>(0d);
    widthMinProperty = new SimpleObjectProperty<>(0d);
    widthMaxProperty = new SimpleObjectProperty<>(0d);
    widthProperty.addListener((v, o, width) -> {
      Bounds area = areaProperty.get();
      if (!processing) {
        processing = true;
        if (getX() + width > area.getMaxX()) {
          width = area.getMaxX() - getX();
        }
        width = checkWidth(width);
        widthProperty.set(width);
        ensureHeightRatio(width);
        processing = false;
      }
      xMaxProperty.set(area.getMaxX() - width);
      srFill.setWidth(width);
      srBnd.setWidth(width);
      srN.setX((getX() + width / 2) - handleSize2);
      srNE.setX((getX() + width) - handleSize);
      srE.setX((getX() + width) - handleSize);
      srSE.setX((getX() + width) - handleSize);
      srS.setX((getX() + width / 2) - handleSize2);
    });
    heightProperty = new SimpleObjectProperty<>(0d);
    heightMinProperty = new SimpleObjectProperty<>(0d);
    heightMaxProperty = new SimpleObjectProperty<>(0d);
    heightProperty.addListener((v, o, height) -> {
      Bounds area = areaProperty.get();
      if (!processing) {
        processing = true;
        if (getY() + height > area.getMaxY()) {
          height = area.getMaxY() - getY();
        }
        height = checkHeight(height);
        heightProperty.set(height);
        ensureWidthRatio(height);
        processing = false;
      }
      yMaxProperty.set(areaProperty.get().getMaxY() - height);
      srFill.setHeight(height);
      srBnd.setHeight(height);
      srE.setY((getY() + height / 2) - handleSize2);
      srSE.setY((getY() + height) - handleSize);
      srS.setY((getY() + height) - handleSize);
      srSW.setY((getY() + height) - handleSize);
      srW.setY((getY() + height / 2) - handleSize2);
    });

    areaProperty.addListener((v, o, area) -> {
      // set max then min to avoid error
      xMaxProperty.set(area.getMaxX() - getWidth() < area.getMinX() ? area.getMinX() : area.getMaxX() - getWidth());
      xMinProperty.set(area.getMinX());
      yMaxProperty.set(area.getMaxY() - getHeight() < area.getMinY() ? area.getMinY() : area.getMaxY() - getHeight());
      yMinProperty.set(area.getMinY());
      widthMaxProperty.set(area.getMaxX() - getX() < minSize ? minSize : area.getMaxX() - getX());
      widthMinProperty.set(minSize);
      heightMaxProperty.set(area.getMaxY() - getY() < minSize ? minSize : area.getMaxY() - getY());
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

  private int getRatio() {
    DMDAspectRatio ratio = (DMDAspectRatio) aspectRatio.getValue().getUserData();
    return ratio.getWidth();
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
    return xProperty.get();
  }

  public void setX(double newX) {
    Bounds area = areaProperty.get();
    if (newX < area.getMinX()) {
      newX = area.getMinX();
    }
    if (newX + getWidth() > area.getMaxX()) {
      newX = area.getMaxX() - getWidth();
    }
    xProperty.set(newX);
  }

  public double getY() {
    return yProperty.get();
  }

  public void setY(double y) {
    Bounds area = areaProperty.get();
    if (y < area.getMinY()) {
      y = area.getMinY();
    }
    if (y + getHeight() > area.getMaxY()) {
      y = area.getMaxY() - getHeight();
    }
    yProperty.set(y);
  }

  public double getWidth() {
    return widthProperty.get();
  }

  public void setWidth(double width) {
    widthProperty.set(width);
  }

  public double getHeight() {
    return heightProperty.get();
  }

  public void setHeight(double height) {
    heightProperty.set(height);
  }

  //-------------------

  public boolean keyPressed(KeyEvent event) {
    if (overlay.isVisible()) {
      //if (Keys.isSpecial(event)) {
      KeyCode code = event.getCode();
      switch (code) {
        case LEFT:
          if (event.isControlDown()) {
            if (event.isShiftDown()) {
              moveFrom(false, false);
              moveXPos(getX() + getWidth() - 1);
            }
            else {
              moveFrom(true, false);
              moveXPos(getX() - 1);
            }
          }
          else {
            relocateInArea(getX() - 1, getY());
          }
          return true;
        case RIGHT:
          if (event.isControlDown()) {
            if (event.isShiftDown()) {
              moveFrom(true, false);
              moveXPos(getX() + 1);
            }
            else {
              moveFrom(false, false);
              moveXPos(getX() + getWidth() + 1);
            }
          }
          else {
            relocateInArea(getX() + 1, getY());
          }
          return true;
        case UP:
          if (event.isControlDown()) {
            if (event.isShiftDown()) {
              moveFrom(false, false);
              moveYPos(getY() + getHeight() - 1);
            }
            else {
              moveFrom(false, true);
              moveYPos(getY() - 1);
            }
          }
          else {
            relocateInArea(getX(), getY() - 1);
          }
          return true;
        case DOWN:
          if (event.isControlDown()) {
            if (event.isShiftDown()) {
              moveFrom(false, true);
              moveYPos(getY() + 1);
            }
            else {
              moveFrom(false, false);
              moveYPos(getY() + getHeight() + 1);
            }
          }
          else {
            relocateInArea(getX(), getY() + 1);
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


  public void select() {
    srFill.toFront();
    overlay.toFront();
    overlay.setVisible(true);
  }

  @Override
  public String toString() {
    return "[" + getX() + ", " + getY() + ", " + getWidth() + ", " + getHeight() + "]";
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
    srNW = srCreate(Cursor.NW_RESIZE);
    srN = srCreate(Cursor.N_RESIZE);
    srNE = srCreate(Cursor.NE_RESIZE);
    srE = srCreate(Cursor.E_RESIZE);
    srSE = srCreate(Cursor.SE_RESIZE);
    srS = srCreate(Cursor.S_RESIZE);
    srSW = srCreate(Cursor.SW_RESIZE);
    srW = srCreate(Cursor.W_RESIZE);
    overlay.getChildren().addAll(srBnd, srNW, srN, srNE, srE, srSE, srS, srSW, srW);
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
    sX = getX() - me.getX();
    sY = getY() - me.getY();
    sWidth = getWidth();
    sHeight = sWidth / getRatio();
    me.consume();
  }

  private void mouseDragged(MouseEvent me) {
    double x = me.getX() + sX;
    double y = me.getY() + sY;
    Object source = me.getSource();
    if (source == srBnd) {
      relocateInArea(x, y);
    }
    else if (source == srNW) {
      moveFrom(true, true);
      moveXPos(x);
      if (getRatio() == 1) {
        moveYPos(y);
      }
    }
    else if (source == srN) {
      moveFrom(false, true);
      moveYPos(y);
    }
    else if (source == srNE) {
      moveFrom(false, true);
      moveXPos(x + sWidth);
      if (getRatio() == 1) {
        moveYPos(y);
      }
    }
    else if (source == srE) {
      moveFrom(false, false);
      moveXPos(x + sWidth);
    }
    else if (source == srSE) {
      moveFrom(false, false);
      moveXPos(x + sWidth);
      if (getRatio() == 1) {
        moveYPos(y + sHeight);
      }
    }
    else if (source == srS) {
      moveFrom(false, false);
      moveYPos(y + sHeight);
    }
    else if (source == srSW) {
      moveFrom(true, false);
      moveXPos(x);
      if (getRatio() == 1) {
        moveYPos(y + sHeight);
      }
    }
    else if (source == srW) {
      moveFrom(true, false);
      moveXPos(x);
    }

    //add snapping
    if (snapCenter.get()) {
      double areaMid = areaProperty.get().getWidth() / 2;
      double snapX = x + srBnd.getWidth() / 2;

      if (Math.abs(areaMid - snapX) < 20) {
        relocateInArea(areaMid - srBnd.getWidth() / 2, y);
      }
    }

    me.consume();
  }

  private void mouseReleased(MouseEvent me) {
    me.consume();
  }

  //-----------------------------------------

  private void moveFrom(boolean moveWest, boolean moveNorth) {
    this.moveWest = moveWest;
    this.moveNorth = moveNorth;
  }

  private void moveXPos(double newX) {
    double x = getX(), w = getWidth(), width;
    Bounds area = areaProperty.get();

    if (moveWest) {
      if (newX < area.getMinX()) {
        newX = area.getMinX();
      }
      width = x + w - newX;
      if (width < minSize) {
        width = minSize;
      }
      width = checkWidth(width);
      newX = x + w - width;
      xProperty.set(newX);
    }
    else {
      if (newX > area.getMaxX()) {
        newX = area.getMaxX();
      }
      width = newX - x;
      if (width < minSize) {
        width = minSize;
      }
      width = checkWidth(width);
    }
    widthProperty.set(width);
    ensureHeightRatio(width);
  }

  private void moveYPos(double newY) {
    double y = getY(), h = getHeight(), height;
    Bounds area = areaProperty.get();

    if (moveNorth) {
      if (newY < area.getMinY()) {
        newY = area.getMinY();
      }
      height = y + h - newY;
      if (height < minSize) {
        height = minSize;
      }
      height = checkHeight(height);
      newY = y + h - height;
      yProperty.set(newY);
    }
    else {
      if (newY > area.getMaxY()) {
        newY = area.getMaxY();
      }
      height = newY - y;
      if (height < minSize) {
        height = minSize;
      }
      height = checkHeight(height);
    }
    heightProperty.set(height);
    ensureWidthRatio(height);
  }

  private double checkWidth(double width) {
    Bounds area = areaProperty.get();
    if (getRatio() != 1 && moveNorth && (getY() + getHeight() - width / getRatio() < area.getMinY())) {
      width = (getY() + getHeight() - area.getMinY()) * getRatio();
    }
    if (getRatio() != 1 && !moveNorth && (getY() + width / getRatio() > area.getMaxY())) {
      width = (area.getMaxY() - getY()) * 4;
    }
    return width;
  }

  private double checkHeight(double height) {
    Bounds area = areaProperty.get();
    if (getRatio() != 1 && moveWest && (getX() + getWidth() - height * getRatio() < area.getMinX())) {
      height = (getX() + getWidth() - area.getMinX()) / getRatio();
    }
    if (getRatio() != 1 && !moveWest && (getX() + height * getRatio() > area.getMaxX())) {
      height = (area.getMaxX() - getX()) / getRatio();
    }
    return height;
  }

  private void ensureWidthRatio(double height) {
    double width = height * getRatio();
    if (getRatio() != 1 && getWidth() != width) {
      if (moveWest) {
        xProperty.set(getX() + getWidth() - width);
      }
      widthProperty.set(width);
    }
  }

  private void ensureHeightRatio(double width) {
    double height = width / getRatio();
    if (getRatio() != 1 && getHeight() != height) {
      if (moveNorth) {
        yProperty.set(getY() + getHeight() - height);
      }
      heightProperty.set(height);
    }
  }

  void relocateInArea(double x, double y) {
    setX(x);
    setY(y);
  }

}

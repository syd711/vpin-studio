package de.mephisto.vpin.ui.tables.dialogs;

import javafx.beans.property.BooleanProperty;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

public class DMDPositionResizer extends Rectangle {

  private static double handleSize = 6, handleSize2 = handleSize / 2;
  private static double minSize = 8;

  private Color color = Color.LIME;

  // Overlay elements
  private Group overlay;
  private Rectangle srBnd, srNW, srN, srNE, srE, srSE, srS, srSW, srW;

  private double sX, sY, sWidth, sHeight;

  private Bounds area;

  boolean eventTriggered = false;

  private BooleanProperty aspectRatio;

  DMDPositionResizer(Pane pane, Bounds area, BooleanProperty aspectRatio, Color fill) {
    this.area = area;
    this.aspectRatio = aspectRatio;
    this.color = fill;

    setFill(color);
    setOpacity(0.3);

    pane.getChildren().add(this);

    initOverlay();
    pane.getChildren().add(overlay);

    pane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
      if (!eventTriggered) {
        overlay.setVisible(false);
      }
      eventTriggered = false;
    });
    setOnMousePressed(e -> {
      select();
      eventTriggered = true;
      srBnd.fireEvent(e);
      e.consume();
    });
    setOnMouseDragged(e -> {
      srBnd.fireEvent(e);
      e.consume();
    });
    setOnMouseReleased(e -> {
      srBnd.fireEvent(e);
      e.consume();
    });

    boundsInParentProperty().addListener((v, o, n) -> updateOverlay());
  }

  public void select() {
    this.toFront();
    overlay.toFront();
    updateOverlay();
    overlay.setVisible(true);
  }

  @Override
  public String toString() {
    return "[" + getX() + ", " + getY() + ", " + getWidth() + ", " + getHeight() + "]";
  }

  private void initOverlay() {
    overlay = new Group();
    srBnd = new Rectangle();
    srBnd.setStroke(color);
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

  private void updateOverlay() {
    srBnd.setX(getX());
    srBnd.setY(getY());
    srBnd.setWidth(getWidth());
    srBnd.setHeight(getHeight());
    srNW.setX(getX());
    srNW.setY(getY());
    srN.setX((getX() + getWidth() / 2) - handleSize2);
    srN.setY(getY());
    srNE.setX((getX() + getWidth()) - handleSize);
    srNE.setY(getY());
    srE.setX((getX() + getWidth()) - handleSize);
    srE.setY((getY() + getHeight() / 2) - handleSize2);
    srSE.setX((getX() + getWidth()) - handleSize);
    srSE.setY((getY() + getHeight()) - handleSize);
    srS.setX((getX() + getWidth() / 2) - handleSize2);
    srS.setY((getY() + getHeight()) - handleSize);
    srSW.setX(getX());
    srSW.setY((getY() + getHeight()) - handleSize);
    srW.setX(getX());
    srW.setY((getY() + getHeight() / 2) - handleSize2);
  }

  private Rectangle srCreate(Cursor cursor) {
    Rectangle rectangle = new Rectangle(handleSize, handleSize, color);
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
    sHeight = checkAspectRatio(sWidth, getHeight());
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
      moveXPos(x, true);
      moveYPos(y, true);
      checkAspectRatio(true, true, true);
    } 
    else if (source == srN) {
      moveYPos(y, true);
      checkAspectRatio(false, false, true);
    } 
    else if (source == srNE) {
      moveXPos(x + sWidth, false);
      moveYPos(y, true);
      checkAspectRatio(true, false, true);
    } 
    else if (source == srE) {
      moveXPos(x + sWidth, false);
      checkAspectRatio(true, false, false);
    }
    else if (source == srSE) {
      moveXPos(x + sWidth, false);
      moveYPos(y + sHeight, false);
      checkAspectRatio(true, false, false);
    } 
    else if (source == srS) {
      moveYPos(y + sHeight, false);
      checkAspectRatio(false, false, false);
    }
    else if (source == srSW) {
      moveXPos(x, true);
      moveYPos(y + sHeight, false);
      checkAspectRatio(true, true, false);
    } 
    else if (source == srW) {
      moveXPos(x, true);
      checkAspectRatio(true, false, false);
    }
    me.consume();
  }

  private void mouseReleased(MouseEvent me) {
    me.consume();
  }

  private double checkAspectRatio(double width, double height) {
    return aspectRatio != null && aspectRatio.getValue() ? width / 4 : height;
  }

  private boolean moveXPos(double newX, boolean moveWest) {
    double x = getX(), w = getWidth(), width;
    boolean adjusted = false;

    if (moveWest) {
      if (newX < area.getMinX()) {
        newX = area.getMinX();
        adjusted = true;
      } 
      width = x + w - newX;
      if (width < minSize) {
        width = minSize;
        newX = x + w - minSize;
        adjusted = true;
      }
      setX(newX);
    } 
    else {
      if (newX > area.getMaxX()) {
        newX = area.getMaxX();
        adjusted = true;
      }
      width = newX - x;
      if (width < minSize) {
        width = minSize;
        adjusted = true;
      }
    }
    setWidth(width);
    return adjusted;
  }

  private boolean moveYPos(double newY, boolean moveNorth) {
    double y = getY(), h = getHeight(), height;
    boolean adjusted = false;

    if (moveNorth) {
      if (newY < area.getMinY()) {
        newY =  area.getMinY();
        adjusted = true;
      } 
      height = y + h - newY;
      if (height < minSize) {
        height = minSize;
        newY = y + h - minSize;
        adjusted = true;
      }
      setY(newY);
    } 
    else {
      if (newY > area.getMaxY()) {
        newY = area.getMaxY();
        adjusted = true;
      }
      height = newY - y;
      if (height < minSize) {
        height = minSize;
        adjusted = true;
      }
    }
    setHeight(height);
    return adjusted;
  }

  private void checkAspectRatio(boolean adjustHeight, boolean moveWest, boolean moveNorth) {
    if (aspectRatio != null && aspectRatio.getValue()) {
      double w = getWidth(), h = getHeight();
      if (adjustHeight) {
        h = w / 4;
        if (moveYPos(moveNorth ? getY() + getHeight() - h : getY() + h, moveNorth)) {
          moveXPos(moveWest ? getX() + getWidth() - getHeight() * 4 : getX() + getHeight() * 4, moveWest);
        }
      } 
      else {
        w = h * 4;
        if (moveXPos(moveWest ?  getX() + getWidth() - w : getX() + w, moveWest)) {
          moveYPos(moveNorth ? getY() + getHeight() - getWidth() / 4 : getY() + getWidth() / 4, moveNorth);
        }
      }
    }
  }

  void relocateInArea(double x, double y) {
    double maxX = area.getMaxX() - getWidth();
    double maxY = area.getMaxY() - getHeight();
    if (x < area.getMinX()) {
      x = area.getMinX();
    }
    if (y < area.getMinY()) {
      y = area.getMinY();
    }
    if (x > maxX) {
      x = maxX;
    }
    if (y > maxY) {
      y = maxY;
    }
    setX(x);
    setY(y);
  }
}

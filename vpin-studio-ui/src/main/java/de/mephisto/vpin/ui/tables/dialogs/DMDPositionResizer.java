package de.mephisto.vpin.ui.tables.dialogs;

import javafx.geometry.Rectangle2D;
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

  private Rectangle2D area;

  boolean eventTriggered = false;

  DMDPositionResizer(Pane pane, Rectangle2D area, Color fill) {
    this.area = area;
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
      this.toFront();
      overlay.toFront();
      updateOverlay();
      overlay.setVisible(true);
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

  @Override public String toString() { return "[" + getLayoutX() + ", " + getLayoutY() + ", " + getWidth() + ", " + getHeight() + "]"; }

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
    srBnd.setX(getLayoutX());
    srBnd.setY(getLayoutY());
    srBnd.setWidth(getWidth());
    srBnd.setHeight(getHeight());
    srNW.setX(getLayoutX());
    srNW.setY(getLayoutY());
    srN.setX((getLayoutX() + getWidth() / 2) - handleSize2);
    srN.setY(getLayoutY());
    srNE.setX((getLayoutX() + getWidth()) - handleSize);
    srNE.setY(getLayoutY());
    srE.setX((getLayoutX() + getWidth()) - handleSize);
    srE.setY((getLayoutY() + getHeight() / 2) - handleSize2);
    srSE.setX((getLayoutX() + getWidth()) - handleSize);
    srSE.setY((getLayoutY() + getHeight()) - handleSize);
    srS.setX((getLayoutX() + getWidth() / 2) - handleSize2);
    srS.setY((getLayoutY() + getHeight()) - handleSize);
    srSW.setX(getLayoutX());
    srSW.setY((getLayoutY() + getHeight()) - handleSize);
    srW.setX(getLayoutX());
    srW.setY((getLayoutY() + getHeight() / 2) - handleSize2);
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
    sX = getLayoutX() - me.getX();
    sY = getLayoutY() - me.getY();
    sWidth = getWidth();
    sHeight = getHeight();
    me.consume();
  }

  private void mouseDragged(MouseEvent me) {
    double x = me.getX() + sX;
    double y = me.getY() + sY;
    Object source = me.getSource();
    if (source == srBnd) relocateInArea(x, y);
    else if (source == srNW) { setHSize(x, true); setVSize(y, true); }
    else if (source == srN) setVSize(y, true);
    else if (source == srNE) { setHSize(x + sWidth, false); setVSize(y, true); }
    else if (source == srE) setHSize(x + sWidth, false);
    else if (source == srSE) { setHSize(x + sWidth, false); setVSize(y + sHeight, false); }
    else if (source == srS) setVSize(y + sHeight, false);
    else if (source == srSW) { setHSize(x, true); setVSize(y + sHeight, false); }
    else if (source == srW) setHSize(x, true);
    me.consume();
  }

  private void mouseReleased(MouseEvent me) {
    me.consume();
  }

  void setHSize(double h, boolean moveBox) {
    double x = getLayoutX(), w = getWidth(), width;
    if (h < area.getMinX()) h = area.getMinX();
    if (h > area.getMaxX()) h = area.getMaxX();
    if (moveBox) {
      width = w + x - h;
      if (width < minSize) { width = minSize; h = x + w - minSize; }
      setLayoutX(h);
    } else {
      width = h - x;
      if (width < minSize) width = minSize;
    }
    widthProperty().set(width);
  }

  void setVSize(double v, boolean b) {
    double y = getLayoutY(), h = getHeight(), height;
    if (v < area.getMinY()) v = area.getMinY();
    if (v > area.getMaxY()) v = area.getMaxY();
    if (b) {
      height = h + y - v;
      if (height < minSize) { height = minSize; v = y + h - minSize; }
      setLayoutY(v);
    } else {
      height = v - y;
      if (height < minSize) height = minSize;
    }
    heightProperty().set(height);
  }

  void relocateInArea(double x, double y) {
    double maxX = area.getMaxX() - getWidth();
    double maxY = area.getMaxY() - getHeight();
    if (x < area.getMinX()) x = area.getMinX();
    if (y < area.getMinY()) y = area.getMinY();
    if (x > maxX) x = maxX;
    if (y > maxY) y = maxY;
    setLayoutX(x);
    setLayoutY(y);
  }
}

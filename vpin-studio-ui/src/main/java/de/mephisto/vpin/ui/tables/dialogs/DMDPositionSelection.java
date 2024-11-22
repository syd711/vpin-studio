package de.mephisto.vpin.ui.tables.dialogs;

import java.util.function.Consumer;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.geometry.Rectangle2D;

public class DMDPositionSelection {

  private static double minSize = 8;

  Rectangle2D area;

  Rectangle selection;
  double initX, initY;

  Runnable onDragStart;
  Consumer<Rectangle2D> onRelease;

  public DMDPositionSelection(Pane pane, Rectangle2D area, Color color, Runnable onDragStart, Consumer<Rectangle2D> onDragEnd) {
    this.area = area;
    this.onDragStart = onDragStart;
    this.onRelease = onDragEnd;

    pane.setOnMousePressed(me -> {
      this.initX = checkAreaX(me.getX());
      this.initY = checkAreaY(me.getY());
    });

    pane.setOnMouseDragged(me -> {
      // detect a sighifiant move to start selection, else ignore
      if (selection == null && (Math.abs(me.getX() - initX) > minSize || Math.abs(me.getY() - initY) > minSize)) {
        onDragStart.run();
        selection = new Rectangle(initX, initY, 0, 0);
        selection.setStroke(color);
        selection.setStrokeType(StrokeType.INSIDE);
        selection.setStrokeWidth(1);
        selection.getStrokeDashArray().addAll(2d, 4d);
        selection.setFill(Color.TRANSPARENT);
    
        pane.getChildren().add(selection);
      }
      if (selection != null) {
        double meX = checkAreaX(me.getX());
        if (meX >= initX) {
          selection.setWidth(meX - initX);
        }
        else {
          selection.setX(meX);
          selection.setWidth(initX - meX);
        }

        double meY = checkAreaY(me.getY());
        if (meY >= initY) {
          selection.setHeight(meY - initY);
        }
        else {
          selection.setY(meY);
          selection.setHeight(initY - meY);
        }
        me.consume();
      }
    });

    pane.setOnMouseReleased(me -> {
      if (selection != null) {
        pane.getChildren().remove(selection);
        selection = null;
        double meX = checkAreaX(me.getX());
        double meY = checkAreaY(me.getY());
        double x = meX > initX ? initX: meX;
        double y = meY > initY ? initY: meY;
        double width = meX > initX ? meX - initX : initX - meX;
        double height = meY > initY ? meY - initY : initY - meY;
        onDragEnd.accept(new Rectangle2D(x, y, width, height));
        me.consume();
      }
    });
  }

  private double checkAreaX(double meX) {
    if (meX < area.getMinX()) {
      return area.getMinX();
    }
    else if (meX > area.getMaxX()) {
      return area.getMaxX();
    }
    return meX;
  }   
  private double checkAreaY(double meY) {
    if (meY < area.getMinY()) {
      return area.getMinY();
    }
    else if (meY > area.getMaxY()) {
      return area.getMaxY();
    }
    return meY;
  }   
}

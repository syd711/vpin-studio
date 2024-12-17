package de.mephisto.vpin.ui.cards.panels;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

import java.util.function.Consumer;

public class CanvasPositionSelection {

  private static double minSize = 8;

  /** The constrained area for mouse drag */
  private ObjectProperty<Bounds> areaProperty;

  /** The selection representing the drawn region */
  private Rectangle selection;
  /** The initial point */
  private double initX, initY;

  /** Whether aspect ratio has to be enforced */
  private BooleanProperty aspectRatio;

  public CanvasPositionSelection(Pane pane, ObjectProperty<Bounds> areaProperty, BooleanProperty aspectRatio, ObjectProperty<Color> color, Runnable onDragStart, Consumer<Rectangle2D> onDragEnd) {
    this.areaProperty = areaProperty;
    this.aspectRatio = aspectRatio;

    pane.setOnMousePressed(me -> {
      this.initX = checkAreaX(me.getX());
      this.initY = checkAreaY(me.getY());
    });

    pane.setOnMouseDragged(me -> {
      // detect a sighifiant move to start selection, else ignore
      if (selection == null && (Math.abs(me.getX() - initX) > minSize || Math.abs(me.getY() - initY) > minSize)) {
        onDragStart.run();
        selection = new Rectangle(initX, initY, 0, 0);
        selection.strokeProperty().bind(color);
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
          checkAspectRatio(false);
        }
        else {
          selection.setY(meY);
          selection.setHeight(initY - meY);
          checkAspectRatio(true);
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
        double height = (aspectRatio != null && aspectRatio.getValue()) ?
          width / 4 : meY > initY ? meY - initY : initY - meY;

        onDragEnd.accept(new Rectangle2D(x, y, width, height));
        me.consume();
      }
    });
  }

  private void checkAspectRatio(boolean moveUp) {
    if (aspectRatio != null && aspectRatio.getValue()) {
      double width = selection.getWidth();
      double height = width / 4;
      if (moveUp) {
        selection.setY(selection.getY() + selection.getHeight() - height);
      }
      selection.setHeight(height);
    }
  }


  private double checkAreaX(double meX) {
    Bounds area = areaProperty.get();
    if (meX < area.getMinX()) {
      return area.getMinX();
    }
    else if (meX > area.getMaxX()) {
      return area.getMaxX();
    }
    return meX;
  }   
  private double checkAreaY(double meY) {
    Bounds area = areaProperty.get();
    if (meY < area.getMinY()) {
      return area.getMinY();
    }
    else if (meY > area.getMaxY()) {
      return area.getMaxY();
    }
    return meY;
  }   
}

package de.mephisto.vpin.ui.tables.dialogs;

import java.util.function.Consumer;

import de.mephisto.vpin.restclient.dmd.DMDAspectRatio;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;

public class DMDPositionSelection {

  private static double minSize = 8;

  /** The constrained area for mouse drag */
  private ObservableValue<Bounds> areaProperty;

  /** The selection representing the drawn region */
  private Rectangle selection;
  /** The initial point */
  private double initX, initY;

  /** Whether aspect ratio has to be enforced */
  private ObservableValue<DMDAspectRatio> aspectRatio;

  public DMDPositionSelection(Pane pane, ObservableValue<Bounds> areaProperty, ObservableValue<DMDAspectRatio> aspectRatio, ObservableValue<Color> color, Runnable onDragStart, Consumer<Rectangle2D> onDragEnd) {
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

        DMDAspectRatio ratio = (DMDAspectRatio) aspectRatio.getValue();
        double height = ratio.isKeepRatio() ?
          width / ratio.getValue() : meY > initY ? meY - initY : initY - meY;

        onDragEnd.accept(new Rectangle2D(x, y, width, height));
        me.consume();
      }
    });
  }

  private void checkAspectRatio(boolean moveUp) {
    DMDAspectRatio ratio = (DMDAspectRatio) aspectRatio.getValue();
    if (ratio.isKeepRatio()) {
      double width = selection.getWidth();
      double height = width / ratio.getValue();
      if (moveUp) {
        selection.setY(selection.getY() + selection.getHeight() - height);
      }
      selection.setHeight(height);
    }
  }


  private double checkAreaX(double meX) {
    Bounds area = areaProperty.getValue();
    if (meX < area.getMinX()) {
      return area.getMinX();
    }
    else if (meX > area.getMaxX()) {
      return area.getMaxX();
    }
    return meX;
  }   
  private double checkAreaY(double meY) {
    Bounds area = areaProperty.getValue();
    if (meY < area.getMinY()) {
      return area.getMinY();
    }
    else if (meY > area.getMaxY()) {
      return area.getMaxY();
    }
    return meY;
  }   
}

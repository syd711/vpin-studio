package de.mephisto.vpin.ui.tables.dialogs;

import java.util.function.Consumer;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;

public class DMDPositionSelection {

  private static double minSize = 8;

  /** The selection representing the drawn region */
  private Rectangle selection;
  /** The initial point in screen coordinate */
  private double initX, initY;

  /** The internal zoom to transform the coordinate from caller coordinates into screen coordinates */
  private double zoom = 1.0;

  /** Whether aspect ratio has to be enforced */
  private Double aspectRatio = null;
  
  private ObjectProperty<Color> colorProperty = new SimpleObjectProperty<>(Color.LIME);

  /** The constrained area for mouse drag in caller coordinates */
  private int areaMinX = -1, areaMaxX = -1, areaMinY = -1, areaMaxY = -1;

  public DMDPositionSelection(Pane pane, Runnable onDragStart, Consumer<Rectangle2D> onDragEnd) {

    pane.setOnMousePressed(me -> {
      this.initX = checkAreaX(me.getX());
      this.initY = checkAreaY(me.getY());
    });

    pane.setOnMouseDragged(me -> {
      // detect a sighifiant move to start selection, else ignore
      if (selection == null && (Math.abs(me.getX() - initX) > minSize || Math.abs(me.getY() - initY) > minSize)) {
        onDragStart.run();
        selection = new Rectangle(initX, initY, 0, 0);
        selection.strokeProperty().bind(colorProperty);
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

        double height = aspectRatio != null ?
          width / aspectRatio : meY > initY ? meY - initY : initY - meY;

        onDragEnd.accept(new Rectangle2D(x / zoom, y / zoom, width / zoom, height / zoom));
        me.consume();
      }
    });
  }

  private void checkAspectRatio(boolean moveUp) {
    if (aspectRatio != null) {
      double width = selection.getWidth();
      double height = width / aspectRatio;
      if (moveUp) {
        selection.setY(selection.getY() + selection.getHeight() - height);
      }
      selection.setHeight(height);
    }
  }


  private double checkAreaX(double meX) {
    if (areaMinX >=0 && meX < areaMinX * zoom) {
      return areaMinX * zoom;
    }
    else if (areaMaxX >= 0 && meX > areaMaxX * zoom) {
      return areaMaxX * zoom;
    }
    return meX;
  }   
  private double checkAreaY(double meY) {
    if (areaMinY >= 0 && meY < areaMinY * zoom) {
      return areaMinY * zoom;
    }
    else if (areaMaxY >= 0 && meY > areaMaxY * zoom) {
      return areaMaxY * zoom;
    }
    return meY;
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
    areaMinX = minX;
    areaMinY = minY;
    areaMaxX = maxX;
    areaMaxY = maxY;
  }

  public void setZoom(double zoom) {
    this.zoom = zoom;
  }

  public void setAspectRatio(Double aspectRatio) {
    this.aspectRatio = aspectRatio;
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
}

package de.mephisto.vpin.ui.tables.dialogs;

import javafx.application.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.geometry.*;

/**
 * Very inspired from 
 */
public class DMDPositionResizerDemo extends Application {

  @Override
  public void start(final Stage stage) {
  
    BorderPane layout = new BorderPane();
    stage.setScene(new Scene(layout, 500, 300));

    ObjectProperty<Bounds> area = new SimpleObjectProperty<>(new BoundingBox(15, 15, 500-15*2, 300-15*2));
    Pane pane = new Pane();

    DoubleProperty zoom = new SimpleDoubleProperty(1.0);

    createElementHandler(pane, area, 150, 30, 105, 105, zoom, new SimpleObjectProperty<>(Color.AQUA));
    createElementHandler(pane, area, 45, 30, 45, 105, zoom, new SimpleObjectProperty<>(Color.VIOLET));
    createElementHandler(pane, area, 45, 180, 45, 45, zoom, new SimpleObjectProperty<>(Color.TAN));
    createElementHandler(pane, area, 150, 180, 105, 45, zoom, new SimpleObjectProperty<>(Color.LIME));
    
    layout.setCenter(pane);
    stage.show();
  }
    
  private void createElementHandler(Pane pane, ObjectProperty<Bounds> area, int x, int y, int width, int height, DoubleProperty zoom, ObjectProperty<Color> color) {
    DMDPositionResizer eh = new DMDPositionResizer(area, zoom, null, color);
    eh.setX(x);
    eh.setY(y);
    eh.setWidth(width);
    eh.setHeight(height);
    eh.addToPane(pane);
  }

  public static void main(String[] args) {
    Application.launch(args);
  }
}

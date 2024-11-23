package de.mephisto.vpin.ui.tables.dialogs;

import javafx.application.*;
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

    Bounds area = new BoundingBox(15, 15, 500-15*2, 300-15*2);
    Pane pane = new Pane();

    createElementHandler(pane, area, 150, 30, 105, 105, Color.AQUA);
    createElementHandler(pane, area, 45, 30, 45, 105, Color.VIOLET);
    createElementHandler(pane, area, 45, 180, 45, 45, Color.TAN);
    createElementHandler(pane, area, 150, 180, 105, 45, Color.LIME);
    
    layout.setCenter(pane);
    stage.show();
  }
    
  private void createElementHandler(Pane pane, Bounds area, int x, int y, int width, int height, Color color) {
    DMDPositionResizer eh = new DMDPositionResizer(pane, area, null, color);
    eh.setX(x);
    eh.setY(y);
    eh.setWidth(width);
    eh.setHeight(height);
  }

  public static void main(String[] args) {
    Application.launch(args);
  }
}

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

    createElementHandler(pane, area, 150, 30, 105, 105, Color.AQUA, 3.0/2);
    createElementHandler(pane, area, 45, 30, 45, 105, Color.VIOLET, 2.0 / 3);
    createElementHandler(pane, area, 45, 180, 45, 45, Color.TAN, 4.0);
    createElementHandler(pane, area, 150, 180, 105, 45, Color.LIME, null);
    
    layout.setCenter(pane);
    stage.show();
  }
    
  private void createElementHandler(Pane pane, Bounds area, int x, int y, int width, int height, Color color, Double aspectRatio) {
    DMDPositionResizer eh = new DMDPositionResizer();
    eh.setX(x);
    eh.setY(y);
    eh.setWidth(width);
    eh.setHeight(height);
    eh.setColor(color);
    eh.setAspectRatio(aspectRatio);
    eh.setBounds(area);
    eh.addToPane(pane);
  }

  public static void main(String[] args) {
    Application.launch(args);
  }
}

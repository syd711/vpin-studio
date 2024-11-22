package de.mephisto.vpin.ui.tables.dialogs;

import javafx.application.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.geometry.*;

/**
 * Very inspired from 
 */
public class DMDPositionResizerDemo2 extends Application {

  DMDPositionResizer resizer;

  @Override
  public void start(final Stage stage) {
  
    BorderPane layout = new BorderPane();
    stage.setScene(new Scene(layout, 500, 300));

    Pane pane = new Pane();
    layout.setCenter(pane);
    stage.show();

    // add a selector in the pane
    Rectangle2D area = new Rectangle2D(15, 15, 500-15*2, 300-15*2);
    new DMDPositionSelection(pane, area, Color.LIME, 
      () -> {
        if (resizer != null) {
          pane.getChildren().remove(resizer);
          resizer = null;
        }  
      }, 
      rect -> {
        resizer = new DMDPositionResizer(pane, area, Color.LIME);
        resizer.setLayoutX((int) rect.getMinX());
        resizer.setLayoutY((int) rect.getMinY());
        resizer.setWidth((int) rect.getWidth());
        resizer.setHeight((int) rect.getHeight());
      });  
  }

  public static void main(String[] args) {
    Application.launch(args);
  }

}

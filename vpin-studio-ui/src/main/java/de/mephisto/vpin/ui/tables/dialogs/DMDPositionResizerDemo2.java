package de.mephisto.vpin.ui.tables.dialogs;

import javafx.application.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.geometry.*;

/**
 * Very inspired from 
 */
public class DMDPositionResizerDemo2 extends Application {

  DMDPositionResizer resizer;

  @Override
  public void start(final Stage stage) {
  
    BorderPane layout = new BorderPane();
    stage.setScene(new Scene(layout));

    Label label = new Label("Draw a rectangle, then move and scale, it should stays within the dashed-grey area");
    HBox topbar = new HBox(label);
    topbar.setPadding(new Insets(15));
    layout.setTop(topbar);

    CheckBox aspectRatio = new CheckBox("Keep aspect ratio");
    aspectRatio.setSelected(true);
    HBox toolbar = new HBox(aspectRatio);
    toolbar.setPadding(new Insets(15));
    layout.setBottom(toolbar);

    Pane pane = new Pane();
    pane.setPrefWidth(500);
    pane.setPrefHeight(300);
    layout.setCenter(pane);

    stage.show();

    // add a selector in the pane
    Bounds area = new BoundingBox(15, 15, 500-15*2, 300-15*2);

    addAreaBorder(pane, area);
    
    new DMDPositionSelection(pane, area, aspectRatio.selectedProperty(),  Color.LIME, 
      () -> {
        if (resizer != null) {
          pane.getChildren().remove(resizer);
          resizer = null;
        }  
      }, 
      rect -> {
        resizer = new DMDPositionResizer(pane, area, aspectRatio.selectedProperty(), Color.LIME);
        resizer.setX((int) rect.getMinX());
        resizer.setY((int) rect.getMinY());
        resizer.setWidth((int) rect.getWidth());
        resizer.setHeight((int) rect.getHeight());
      });  
  }

  private void addAreaBorder(Pane pane, Bounds area) {
    Rectangle rect = new Rectangle(area.getMinX(), area.getMinY(), area.getWidth(), area.getHeight());
    rect.setStroke(Color.GREY);
    rect.setStrokeType(StrokeType.INSIDE);
    rect.setStrokeWidth(1);
    rect.getStrokeDashArray().addAll(2d, 4d);
    rect.setFill(Color.TRANSPARENT);
    pane.getChildren().add(rect);
  }

  public static void main(String[] args) {
    Application.launch(args);
  }

}

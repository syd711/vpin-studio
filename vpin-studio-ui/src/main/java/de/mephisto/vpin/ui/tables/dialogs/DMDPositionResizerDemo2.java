package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.dmd.DMDAspectRatio;
import javafx.application.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
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

    RadioButton aspectRatio = new RadioButton("Keep aspect ratio");
    aspectRatio.setSelected(true);
    aspectRatio.setUserData(DMDAspectRatio.ratio4x1);
    HBox toolbar = new HBox(aspectRatio);
    toolbar.setPadding(new Insets(15));
    layout.setBottom(toolbar);

    ToggleGroup tg = new ToggleGroup();
    aspectRatio.setToggleGroup(tg);

    BooleanProperty p = new SimpleBooleanProperty(false);

    Pane pane = new Pane();
    pane.setPrefWidth(500);
    pane.setPrefHeight(300);
    layout.setCenter(pane);

    stage.show();

    stage.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
      if (resizer != null) {
        resizer.keyPressed(event);
      }
    });

    // add a selector in the pane
    ObjectProperty<Bounds> area = new SimpleObjectProperty<>(new BoundingBox(15, 15, 500-15*2, 300-15*2));
    ObjectProperty<Color> color = new SimpleObjectProperty<>(Color.LIME);

    addAreaBorder(pane, area.get());
    
    new DMDPositionSelection(pane, area, tg.selectedToggleProperty(),  color,
      () -> {
        if (resizer != null) {
          resizer.removeFromPane(pane);
          resizer = null;
        }  
      }, 
      rect -> {
        resizer = new DMDPositionResizer(area, tg.selectedToggleProperty(), p, color);
        resizer.setX((int) rect.getMinX());
        resizer.setY((int) rect.getMinY());
        resizer.setWidth((int) rect.getWidth());
        resizer.setHeight((int) rect.getHeight());
        resizer.addToPane(pane);
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

package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.dmd.DMDAspectRatio;
import javafx.application.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.Button;
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

    Label label = new Label("Draw a rectangle, then move and resize, it should stay within the dashed-grey area");
    HBox topbar = new HBox(label);
    topbar.setPadding(new Insets(15));
    layout.setTop(topbar);

    CheckBox aspectRatio = new CheckBox("Keep aspect ratio (4:1)");
    aspectRatio.setSelected(false);

    Button snapCenter = new Button("Snap Center");
    snapCenter.setOnAction(e -> {
      if (resizer != null) {
        resizer.centerHorizontally();
      }
    });

    HBox toolbar = new HBox(aspectRatio, snapCenter);
    toolbar.setPadding(new Insets(15));
    toolbar.setSpacing(20);
    layout.setBottom(toolbar);

    SimpleObjectProperty<DMDAspectRatio> aspectRatioProperty = new SimpleObjectProperty<>(DMDAspectRatio.ratioOff);
    aspectRatio.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        aspectRatioProperty.setValue(newValue ? DMDAspectRatio.ratio4x1 : DMDAspectRatio.ratioOff);
      }
    });


    Pane pane = new Pane();
    pane.setPrefWidth(500);
    pane.setPrefHeight(300);
    layout.setCenter(pane);

    stage.show();

    stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
      if (resizer != null) {
        resizer.keyPressed(event);
      }
    });

    // add a selector in the pane
    ObjectProperty<Bounds> area = new SimpleObjectProperty<>(new BoundingBox(15, 15, 500-15*2, 300-15*2));
    ObjectProperty<Color> color = new SimpleObjectProperty<>(Color.LIME);
    DoubleProperty zoom = new SimpleDoubleProperty(1.0);

    addAreaBorder(pane, area.get());
    
    new DMDPositionSelection(pane, area, aspectRatioProperty,  color,
      () -> {
        if (resizer != null) {
          resizer.removeFromPane(pane);
          resizer = null;
        }  
      }, 
      rect -> {
        resizer = new DMDPositionResizer(area, zoom, aspectRatioProperty, color);
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

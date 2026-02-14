package de.mephisto.vpin.ui.util;

import javafx.application.*;
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
public class PositionResizerDemo2 extends Application {

  PositionResizer resizer;
  PositionSelection selection;

  @Override
  public void start(final Stage stage) {
  
    BorderPane layout = new BorderPane();
    stage.setScene(new Scene(layout));

    int margin = 50;

    Label label = new Label("Draw a rectangle, then move and resize, it should stay within the dashed-grey area");
    HBox topbar = new HBox(label);
    topbar.setPadding(new Insets(margin));
    layout.setTop(topbar);

    // add pane
    Pane pane = new Pane();
    pane.setPrefWidth(800);
    pane.setPrefHeight(400);
    layout.setCenter(pane);

    Bounds area = new BoundingBox(100 + margin, margin, 600-margin*2, 400-margin*2);
    addAreaBorder(pane, area);

    CheckBox aspectRatio = new CheckBox("Keep aspect ratio (4:1)");
    aspectRatio.setSelected(false);
    aspectRatio.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        selection.setAspectRatio(newValue? 4.0 : null);
        resizer.setAspectRatio(newValue? 4.0 : null);
      }
    });
    
    CheckBox keepInBox = new CheckBox("Keep in Box");
    keepInBox.setSelected(true);
    keepInBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        selection.setBounds(newValue? area : null);
        resizer.setBounds(newValue? area : null);
      }
    });

    CheckBox acceptOutsidePart = new CheckBox("Accept Outside Part");
    acceptOutsidePart.setSelected(true);
    acceptOutsidePart.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        resizer.setAcceptOutsidePart(newValue, 30);
      }
    });


    Button snapCenter = new Button("Snap Center");
    snapCenter.setOnAction(e -> {
      resizer.centerHorizontally();
    });

    HBox toolbar = new HBox(aspectRatio, snapCenter, keepInBox, acceptOutsidePart);
    toolbar.setPadding(new Insets(15));
    toolbar.setSpacing(20);
    layout.setBottom(toolbar);

    stage.show();

    stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
      resizer.keyPressed(event);
    });

    resizer = new PositionResizer();

    // add a selector in the pane
    selection = new PositionSelection(pane,
      () -> {
        resizer.removeFromPane(pane);
      }, 
      rect -> {
        resizer.setX((int) rect.getMinX());
        resizer.setY((int) rect.getMinY());
        resizer.setWidth((int) rect.getWidth());
        resizer.setHeight((int) rect.getHeight());
        resizer.setBounds(area);
        resizer.addToPane(pane);
      });
      selection.setBounds(area);
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

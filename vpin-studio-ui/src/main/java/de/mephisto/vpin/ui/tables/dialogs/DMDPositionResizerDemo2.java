package de.mephisto.vpin.ui.tables.dialogs;

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
public class DMDPositionResizerDemo2 extends Application {

  DMDPositionResizer resizer;
  DMDPositionSelection selection;

  @Override
  public void start(final Stage stage) {
  
    BorderPane layout = new BorderPane();
    stage.setScene(new Scene(layout));

    Label label = new Label("Draw a rectangle, then move and resize, it should stay within the dashed-grey area");
    HBox topbar = new HBox(label);
    topbar.setPadding(new Insets(15));
    layout.setTop(topbar);

    // add pane
    Pane pane = new Pane();
    pane.setPrefWidth(500);
    pane.setPrefHeight(300);
    layout.setCenter(pane);

    Bounds area = new BoundingBox(15, 15, 500-15*2, 300-15*2);
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

    Button snapCenter = new Button("Snap Center");
    snapCenter.setOnAction(e -> {
      if (resizer != null) {
        resizer.centerHorizontally();
      }
    });

    HBox toolbar = new HBox(aspectRatio, snapCenter, keepInBox);
    toolbar.setPadding(new Insets(15));
    toolbar.setSpacing(20);
    layout.setBottom(toolbar);

    stage.show();

    stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
      if (resizer != null) {
        resizer.keyPressed(event);
      }
    });

    // add a selector in the pane
    selection = new DMDPositionSelection(pane,
      () -> {
        if (resizer != null) {
          resizer.removeFromPane(pane);
          resizer = null;
        }  
      }, 
      rect -> {
        resizer = new DMDPositionResizer();
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

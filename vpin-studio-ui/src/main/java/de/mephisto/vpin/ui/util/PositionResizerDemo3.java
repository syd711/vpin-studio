package de.mephisto.vpin.ui.util;

import javafx.application.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.geometry.*;

/**
 * Test linkage with Spinner
 */
public class PositionResizerDemo3 extends Application {

  PositionResizer resizer;

  @Override
  public void start(final Stage stage) {
  
    BorderPane layout = new BorderPane();
    stage.setScene(new Scene(layout));

    int margin = 50;

    Label label = new Label("Demo the link between Resizer and Spinners. Click shift for resizing on center and Ctrl for going outside bounds");
    HBox topbar = new HBox(label);
    topbar.setPadding(new Insets(margin));
    layout.setTop(topbar);

    // add pane
    Pane pane = new Pane();
    pane.setPrefWidth(800);
    pane.setPrefHeight(400);
    layout.setCenter(pane);

    Spinner<Integer> xSpinner = new Spinner<>();
    Label xMin = new Label(), xMax = new Label();
    Spinner<Integer> ySpinner = new Spinner<>();
    Label yMin = new Label(), yMax = new Label();
    Spinner<Integer> widthSpinner = new Spinner<>();
    Label wMin = new Label(), wMax = new Label();
    Spinner<Integer> heightSpinner = new Spinner<>();
    Label hMin = new Label(), hMax = new Label();

    VBox vbox = new VBox(
        new Label("x"), xSpinner, xMin, xMax,
        new Label("y"), ySpinner, yMin, yMax,
        new Label("width"), widthSpinner, wMin, wMax,
        new Label("height"), heightSpinner, hMin, hMax);
    layout.setRight(vbox);

    Bounds area = new BoundingBox(100 + margin, margin, 600-margin*2, 400-margin*2);
    addAreaBorder(pane, area);

    CheckBox aspectRatio = new CheckBox("Keep aspect ratio (4:1)");
    aspectRatio.setSelected(false);
    aspectRatio.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        resizer.setAspectRatio(newValue? 4.0 : null);
      }
    });
    
    CheckBox keepInBox = new CheckBox("Keep in Box");
    keepInBox.setSelected(true);
    keepInBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        resizer.setBounds(newValue? area : null);
      }
    });

    CheckBox acceptOutsidePart = new CheckBox("Accept Outside Part (allow ctrl key)");
    acceptOutsidePart.setSelected(false);
    acceptOutsidePart.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        resizer.setAcceptOutsidePart(newValue, 30);
      }
    });

    HBox toolbar = new HBox(aspectRatio, keepInBox, acceptOutsidePart);
    toolbar.setPadding(new Insets(15));
    toolbar.setSpacing(20);
    layout.setBottom(toolbar);

    stage.show();

    stage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
      resizer.keyPressed(event);
    });

    resizer = new PositionResizer();
    resizer.setX(200);
    resizer.setY(200);
    resizer.setWidth(200);
    resizer.setHeight(120);
    resizer.addToPane(pane);
    resizer.setBounds(area);

    configureSpinner(xSpinner, xMin, xMax, resizer.xProperty(), resizer.xMinProperty(), resizer.xMaxProperty());
    configureSpinner(ySpinner, yMin, yMax, resizer.yProperty(), resizer.yMinProperty(), resizer.yMaxProperty());
    configureSpinner(widthSpinner, wMin, wMax, resizer.widthProperty(), resizer.widthMinProperty(), resizer.widthMaxProperty());
    configureSpinner(heightSpinner, hMin, hMax, resizer.heightProperty(), resizer.heightMinProperty(), resizer.heightMaxProperty());
  }

  private void configureSpinner(Spinner<Integer> spinner, Label min, Label max, ObjectProperty<Integer> property,
                              ReadOnlyObjectProperty<Integer> minProperty, ReadOnlyObjectProperty<Integer> maxProperty) {

    IntegerSpinnerValueFactory factory = new IntegerSpinnerValueFactory(minProperty.get(), maxProperty.get());
    spinner.setValueFactory(factory);
    spinner.setEditable(true);
    factory.valueProperty().bindBidirectional(property);
    factory.minProperty().bind(minProperty);
    factory.maxProperty().bind(maxProperty);
    min.textProperty().bind(minProperty.asString());
    max.textProperty().bind(maxProperty.asString());
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

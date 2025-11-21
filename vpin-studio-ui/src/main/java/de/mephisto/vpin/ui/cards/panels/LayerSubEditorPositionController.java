package de.mephisto.vpin.ui.cards.panels;

import org.apache.commons.beanutils.PropertyUtils;

import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.ui.util.PositionResizer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.layout.HBox;

public class LayerSubEditorPositionController {

  @FXML
  private Spinner<Integer> xSpinner;
  @FXML
  private Spinner<Integer> ySpinner;
  @FXML
  private Spinner<Integer> widthSpinner;
  @FXML
  private Spinner<Integer> heightSpinner;

  @FXML
  private HBox alignBox;

  @FXML
  private ToggleButton alignLeftButton;
  @FXML
  private ToggleButton alignCenterButton;
  @FXML
  private ToggleButton alignRightButton;

  @FXML
  private ToggleButton alignTopButton;
  @FXML
  private ToggleButton alignMiddleButton;
  @FXML
  private ToggleButton alignBottomButton;

  public void initBindings(String prefix, CardTemplateBinder binder, boolean useAlignment) {
    bindSpinner(xSpinner, prefix + "X", binder, 0, 16000, true);
    bindSpinner(ySpinner, prefix + "Y", binder, 0, 9000, false);
    bindSpinner(widthSpinner, prefix + "Width", binder, 0, 16000, true);
    bindSpinner(heightSpinner, prefix + "Height", binder, 0, 9000, false);

    alignBox.setManaged(useAlignment);
    alignBox.setVisible(useAlignment);

    if (useAlignment) {
      ToggleGroup hgrp = new ToggleGroup();
      bindButton(alignLeftButton, hgrp, prefix + "Alignment", binder, CardTemplate.LEFT, true);
      bindButton(alignCenterButton, hgrp, prefix + "Alignment", binder, CardTemplate.CENTER, true);
      bindButton(alignRightButton, hgrp, prefix + "Alignment", binder, CardTemplate.RIGHT, true);

      ToggleGroup vgrp = new ToggleGroup();
      bindButton(alignTopButton, vgrp, prefix + "Alignment", binder, CardTemplate.TOP, false);
      bindButton(alignMiddleButton, vgrp, prefix + "Alignment", binder, CardTemplate.MIDDLE, false);
      bindButton(alignBottomButton, vgrp, prefix + "Alignment", binder, CardTemplate.BOTTOM, false);
    }
  }


  private void bindButton(ToggleButton button, ToggleGroup grp, String property, CardTemplateBinder binder, int value, boolean useWidth) {
    button.setToggleGroup(grp);
    button.selectedProperty().addListener((observableValue, old, selected) -> {
      if (selected) {
        int alignment = binder.getProperty(property, CardTemplate.CENTER | CardTemplate.MIDDLE);
        if (useWidth) {
          alignment = CardTemplate.setHorizontalAlignment(alignment, value);
        }
        else {
          alignment = CardTemplate.setVerticalAlignment(alignment, value);
        }
        binder.setProperty(property, alignment);
      }
    });
  }

  protected static void bindSpinner(Spinner<Integer> spinner, String property, CardTemplateBinder binder, int min, int max, boolean useWidth) {
    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, 0);
    spinner.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      CardResolution res = binder.getResolution();
      int size = useWidth ? res.toWidth(): res.toHeight();
      double val = Double.parseDouble(String.valueOf(t1)) / size;
      binder.setProperty(property, val);
    });
  }

  //------------

  public void setTemplate(String prefix, CardTemplate cardTemplate, CardResolution res, boolean useAlignment) {
    setValue(xSpinner, cardTemplate, prefix + "X", res.toWidth());
    setValue(ySpinner, cardTemplate, prefix + "Y", res.toHeight());
    setValue(widthSpinner, cardTemplate, prefix + "Width", res.toWidth());
    setValue(heightSpinner, cardTemplate, prefix + "Height", res.toHeight());

    if (useAlignment) {
      setAlignment(alignLeftButton, cardTemplate, prefix + "Alignment", CardTemplate.LEFT);
      setAlignment(alignCenterButton, cardTemplate, prefix + "Alignment", CardTemplate.CENTER);
      setAlignment(alignRightButton, cardTemplate, prefix + "Alignment", CardTemplate.RIGHT);
      setAlignment(alignTopButton, cardTemplate, prefix + "Alignment", CardTemplate.TOP);
      setAlignment(alignMiddleButton, cardTemplate, prefix + "Alignment", CardTemplate.MIDDLE);
      setAlignment(alignBottomButton, cardTemplate, prefix + "Alignment", CardTemplate.BOTTOM);
    }
  }

  private void setAlignment(ToggleButton button, CardTemplate cardTemplate, String property, int value) {
    try {
      int alignment = (int) PropertyUtils.getProperty(cardTemplate, property);
      button.setSelected((alignment & value) > 0);

    } catch (Exception e) {
      throw new RuntimeException("Failed to read property " + property + ": " + e.getMessage());
    }
  }


  protected static void setValue(Spinner<Integer> spinner, CardTemplate cardTemplate, String property, int size) {
    try {
      IntegerSpinnerValueFactory factory = (IntegerSpinnerValueFactory) spinner.getValueFactory();

      double percent = (double) PropertyUtils.getProperty(cardTemplate, property);
      int val = (int) (percent * size);

      if (!factory.maxProperty().isBound()) {
        factory.maxProperty().set(size);
      }
      else {
        size = factory.maxProperty().get();
      }

      if (val < 0) {
        val = 0;
      }
      if (val > size) {
        val = size;
      }
      factory.setValue(val);

    } catch (Exception e) {
      throw new RuntimeException("Failed to read property " + property + ": " + e.getMessage());
    }
  }

  //------------

  public void bindDragBox(PositionResizer dragBox) {
    bindSpinner(xSpinner, dragBox.xProperty(), dragBox.xMinProperty(), dragBox.xMaxProperty());
    bindSpinner(ySpinner, dragBox.yProperty(), dragBox.yMinProperty(), dragBox.yMaxProperty());
    bindSpinner(widthSpinner, dragBox.widthProperty(), dragBox.widthMinProperty(), dragBox.widthMaxProperty());
    bindSpinner(heightSpinner, dragBox.heightProperty(), dragBox.heightMinProperty(), dragBox.heightMaxProperty());
  }

  protected static void bindSpinner(Spinner<Integer> spinner, ObjectProperty<Integer> property,
                             ReadOnlyObjectProperty<Integer> minProperty, ReadOnlyObjectProperty<Integer> maxProperty) {
    IntegerSpinnerValueFactory factory = (IntegerSpinnerValueFactory) spinner.getValueFactory();
    spinner.setEditable(true);
    factory.valueProperty().bindBidirectional(property);
    factory.minProperty().bind(minProperty);
    factory.maxProperty().bind(maxProperty);
  }


  public void unbindDragBox(PositionResizer dragBox) {
    unbindSpinner(xSpinner);
    unbindSpinner(ySpinner);
    unbindSpinner(widthSpinner);
    unbindSpinner(heightSpinner);
  }

  protected static void unbindSpinner(Spinner<Integer> spinner) {
    IntegerSpinnerValueFactory factory = (IntegerSpinnerValueFactory) spinner.getValueFactory();
    factory.valueProperty().unbind();
    factory.minProperty().unbind();
    factory.maxProperty().unbind();
  }

}

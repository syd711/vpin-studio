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

public class LayerSubEditorPositionController {

  @FXML
  private Spinner<Integer> xSpinner;
  @FXML
  private Spinner<Integer> ySpinner;
  @FXML
  private Spinner<Integer> widthSpinner;
  @FXML
  private Spinner<Integer> heightSpinner;

  public void initBindings(String prefix, CardTemplateBinder binder) {
    bindSpinner(xSpinner, prefix + "X", binder, 0, 1920, true);
    bindSpinner(ySpinner, prefix + "Y", binder, 0, 1080, false);
    bindSpinner(widthSpinner, prefix + "Width", binder, 0, 1920, true);
    bindSpinner(heightSpinner, prefix + "Height", binder, 0, 1080, false);
  }

  protected static void bindSpinner(Spinner<Integer> spinner, String property, CardTemplateBinder binder, int min, int max, boolean useWidth) {
    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, 0);
    spinner.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      CardResolution res = binder.getResolution();
      int size = useWidth ? res.toWidth(): res.toHeight();
      double val = Double.parseDouble(String.valueOf(t1)) / size * 100;
      binder.setProperty(property, val);
    });
  }

  //------------

  public void setTemplate(String prefix, CardTemplate cardTemplate, CardResolution res) {
    setValue(xSpinner, cardTemplate, prefix + "X", res.toWidth());
    setValue(ySpinner, cardTemplate, prefix + "Y", res.toHeight());
    setValue(widthSpinner, cardTemplate, prefix + "Width", res.toWidth());
    setValue(heightSpinner, cardTemplate, prefix + "Height", res.toHeight());
  }

  protected static void setValue(Spinner<Integer> spinner, CardTemplate cardTemplate, String property, int size) {
    try {
      double percent = (double) PropertyUtils.getProperty(cardTemplate, property);
      int val = (int) (percent * size / 100.0);
      spinner.getValueFactory().setValue(val);
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

  protected void bindSpinner(Spinner<Integer> spinner, ObjectProperty<Integer> property,
                             ReadOnlyObjectProperty<Integer> minProperty, ReadOnlyObjectProperty<Integer> maxProperty) {
    IntegerSpinnerValueFactory factory = (IntegerSpinnerValueFactory) spinner.getValueFactory();
    spinner.setEditable(true);
    factory.valueProperty().bindBidirectional(property);
    factory.minProperty().bind(minProperty);
    factory.maxProperty().bind(maxProperty);
  }
}

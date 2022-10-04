package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.restclient.ObservedProperties;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;

public class BindingUtil {

  public static void bindTextField(TextField textField, ObservedProperties properties, String property) {
    String value = properties.getProperty(property);
    StringProperty stringProperty = new SimpleStringProperty();
    textField.setText(value);
    Bindings.bindBidirectional(stringProperty, textField.textProperty());
    textField.textProperty().addListener((observableValue, s, t1) -> properties.set(property, textField.getText()));
  }

  public static void bindSpinner(Spinner spinner, ObservedProperties properties, String property) {
    int value = Integer.parseInt(properties.getProperty(property));
    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, value);
    spinner.setValueFactory(factory);
    factory.valueProperty().addListener((ChangeListener) (observable, oldValue, newValue) -> {
      int value1 = Integer.parseInt(String.valueOf(newValue));
      properties.set(property, String.valueOf(value1));
    });
  }

  public static void bindSlider(Slider slider, ObservedProperties properties, String property) {
    int value = Integer.parseInt(properties.getProperty(property));
    slider.setValue(value);
    slider.valueProperty().addListener((ChangeListener) (observable, oldValue, newValue) -> {
      int value1 = ((Double)newValue).intValue();
      properties.set(property, String.valueOf(value1));
    });
  }
}

package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.restclient.util.properties.ObservedProperties;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;

import static de.mephisto.vpin.ui.Studio.client;

public class PreferenceBindingUtil {

  public static Debouncer debouncer = new Debouncer();

  public static void bindTextField(TextField textField, String preference, String defaultValue) {
    PreferenceEntryRepresentation systemNameEntry = client.getPreference(preference);
    StringProperty stringProperty = new SimpleStringProperty();
    stringProperty.setValue(defaultValue);
    if (!StringUtils.isEmpty(systemNameEntry.getValue())) {
      stringProperty.setValue(systemNameEntry.getValue());
    }
    textField.setText(stringProperty.getValue());
    Bindings.bindBidirectional(stringProperty, textField.textProperty());
    textField.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce(preference, () -> {
      client.getPreferenceService().setPreference(preference, t1);
    }, 500));
  }

  public static void bindComboBox(ComboBox<String> comboBox, ObservedProperties properties, String property) {
    bindComboBox(comboBox, properties, property, "");
  }

  public static void bindComboBox(ComboBox<String> comboBox, ObservedProperties properties, String property, String defaultValue) {
    String value = properties.getProperty(property, defaultValue);
    StringProperty stringProperty = new SimpleStringProperty();
    Bindings.bindBidirectional(stringProperty, comboBox.valueProperty());
    comboBox.setValue(value);
    comboBox.valueProperty().addListener((observableValue, s, t1) -> properties.set(property, t1));
  }

  public static void bindComboBox(ComboBox<String> comboBox, String preference) {
    PreferenceEntryRepresentation entry = client.getPreference(preference);
    String value = entry.getValue();
    StringProperty stringProperty = new SimpleStringProperty();
    Bindings.bindBidirectional(stringProperty, comboBox.valueProperty());
    comboBox.setValue(value);
    comboBox.valueProperty().addListener((observableValue, s, t1) -> client.getPreferenceService().setPreference(preference, t1));
  }

  public static void bindCheckbox(CheckBox checkbox, String preference, boolean defaultValue) {
    PreferenceEntryRepresentation entry = client.getPreference(preference);
    boolean checked = entry.getBooleanValue(defaultValue);
    BooleanProperty booleanProperty = new SimpleBooleanProperty();
    Bindings.bindBidirectional(booleanProperty, checkbox.selectedProperty());
    booleanProperty.set(checked);
    checkbox.selectedProperty().addListener((observableValue, s, t1) -> client.getPreferenceService().setPreference(preference, t1));
  }


  public static void bindSpinner(Spinner spinner, ObservedProperties properties, String property, int min, int max) {
    int value = properties.getProperty(property, 0);
    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, value);
    spinner.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(property, () -> {
      int value1 = Integer.parseInt(String.valueOf(t1));
      properties.set(property, String.valueOf(value1));
    }, 500));
  }


  private static String format(double val) {
    String in = Integer.toHexString((int) Math.round(val * 255));
    return in.length() == 1 ? "0" + in : in;
  }

  public static String toHexString(Color value) {
    return "#" + (format(value.getRed()) + format(value.getGreen()) + format(value.getBlue()));
  }
}

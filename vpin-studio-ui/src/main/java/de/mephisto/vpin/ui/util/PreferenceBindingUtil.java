package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;

import static de.mephisto.vpin.ui.Studio.client;

public class PreferenceBindingUtil {

  public static Debouncer debouncer = new Debouncer();

  public static void bindTextField(TextField textField, String preference, String defaultValue) {
    PreferenceEntryRepresentation systemNameEntry = client.getPreferenceService().getPreference(preference);
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

  public static void bindComboBox(ComboBox<String> comboBox, String preference) {
    PreferenceEntryRepresentation entry = client.getPreferenceService().getPreference(preference);
    String value = entry.getValue();
    StringProperty stringProperty = new SimpleStringProperty();
    Bindings.bindBidirectional(stringProperty, comboBox.valueProperty());
    comboBox.setValue(value);
    comboBox.valueProperty().addListener((observableValue, s, t1) -> {
      client.getPreferenceService().setPreference(preference, t1);
    });
  }

  public static void bindCheckbox(CheckBox checkbox, String preference, boolean defaultValue) {
    PreferenceEntryRepresentation entry = client.getPreferenceService().getPreference(preference);
    boolean checked = entry.getBooleanValue(defaultValue);
    BooleanProperty booleanProperty = new SimpleBooleanProperty();
    Bindings.bindBidirectional(booleanProperty, checkbox.selectedProperty());
    booleanProperty.set(checked);
    checkbox.selectedProperty().addListener((observableValue, s, t1) -> client.getPreferenceService().setPreference(preference, t1));
  }

  private static String format(double val) {
    String in = Integer.toHexString((int) Math.round(val * 255));
    return in.length() == 1 ? "0" + in : in;
  }

  public static String toHexString(Color value) {
    return "#" + (format(value.getRed()) + format(value.getGreen()) + format(value.getBlue()));
  }
}

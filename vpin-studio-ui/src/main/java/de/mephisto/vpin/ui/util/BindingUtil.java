package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.restclient.ObservedProperties;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;

public class BindingUtil {

  private static Debouncer debouncer = new Debouncer();

  public static void bindTableComboBox(VPinStudioClient client, ComboBox<GameRepresentation> comboBox, ObservedProperties properties, String property) {
    String pupId = properties.getProperty(property, null);
    GameRepresentation game = null;
    if(!StringUtils.isEmpty(pupId)) {
      game = client.getGame(Integer.parseInt(pupId));
    }
    ObjectProperty objectProperty = new SimpleObjectProperty<GameRepresentation>();
    comboBox.setValue(game);
    Bindings.bindBidirectional(objectProperty, comboBox.valueProperty());
    comboBox.valueProperty().addListener((observableValue, gameRepresentation, t1) -> properties.set(property, String.valueOf(t1.getId())));
  }

  public static void bindTextField(TextField textField, ObservedProperties properties, String property) {
    String value = properties.getProperty(property, "");
    StringProperty stringProperty = new SimpleStringProperty();
    textField.setText(value);
    Bindings.bindBidirectional(stringProperty, textField.textProperty());
    textField.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce(property, () -> {
      properties.set(property, textField.getText());
    }, 400));
  }

  public static void bindComboBox(ComboBox<String> comboBox, ObservedProperties properties, String property) {
    String value = properties.getProperty(property, "");
    StringProperty stringProperty = new SimpleStringProperty();
    comboBox.setValue(value);
    Bindings.bindBidirectional(stringProperty, comboBox.valueProperty());
    comboBox.valueProperty().addListener((observableValue, s, t1) -> properties.set(property, t1));
  }

  public static void bindCheckbox(CheckBox checkbox, ObservedProperties properties, String property) {
    boolean value = properties.getProperty(property, false);
    BooleanProperty booleanProperty = new SimpleBooleanProperty();
    booleanProperty.set(value);
    Bindings.bindBidirectional(booleanProperty, checkbox.selectedProperty());
    checkbox.selectedProperty().addListener((observableValue, s, t1) -> properties.set(property, String.valueOf(t1)));
  }

  public static void bindSpinner(Spinner spinner, ObservedProperties properties, String property) {
    int value = properties.getProperty(property, 0);
    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 2000, value);
    spinner.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(property, () -> {
      int value1 = Integer.parseInt(String.valueOf(t1));
      properties.set(property, String.valueOf(value1));
    }, 1000));
  }

  public static void bindSlider(Slider slider, ObservedProperties properties, String property) {
    int value = properties.getProperty(property, 0);
    slider.setValue(value);
    slider.valueProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
        debouncer.debounce(property, () -> {
          int value1 = ((Double) t1).intValue();
          properties.set(property, String.valueOf(value1));
        }, 100);
      }
    });
  }
}

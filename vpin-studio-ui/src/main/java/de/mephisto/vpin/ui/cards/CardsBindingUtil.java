package de.mephisto.vpin.ui.cards;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardTemplates;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.util.properties.ObservedProperties;
import de.mephisto.vpin.ui.util.FontSelectorDialog;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static de.mephisto.vpin.ui.Studio.client;

public class CardsBindingUtil {
  private final static Logger LOG = LoggerFactory.getLogger(CardsBindingUtil.class);

  public static Debouncer debouncer = new Debouncer();

  public static void bindHighscoreTablesComboBox(VPinStudioClient client, ComboBox<GameRepresentation> comboBox, CardTemplate cardTemplate, String property) {
    String pupId = getProperty(cardTemplate, property);
    GameRepresentation game = null;
    if (!StringUtils.isEmpty(pupId)) {
      game = client.getGame(Integer.parseInt(pupId));
    }

    if (game != null) {
      comboBox.setValue(game);
    }
    ObjectProperty objectProperty = new SimpleObjectProperty<GameRepresentation>();
    Bindings.bindBidirectional(objectProperty, comboBox.valueProperty());
    comboBox.valueProperty().addListener((observableValue, gameRepresentation, t1) -> {
      if (t1 != null) {
        properties.set(property, String.valueOf(t1.getId()));
      }
    });
  }

  public static void bindTextField(TextField textField, CardTemplate cardTemplate, String property, String defaultValue) {
    String value = (String) PropertyUtils.getProperty(cardTemplate, property);
    StringProperty stringProperty = new SimpleStringProperty();
    textField.setText(value);
    Bindings.bindBidirectional(stringProperty, textField.textProperty());
    textField.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce(property, () -> {
      properties.set(property, textField.getText());
    }, 1000));

    if (StringUtils.isEmpty(value)) {
      textField.setText(defaultValue);
    }
  }

  public static void bindComboBox(ComboBox<String> comboBox, CardTemplate cardTemplate, String property) {
    bindComboBox(comboBox, cardTemplate, property, "");
  }

  public static void bindComboBox(ComboBox<String> comboBox, CardTemplate cardTemplate, String property, String defaultValue) {
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

  public static void bindCheckbox(CheckBox checkbox, CardTemplate cardTemplate, String property) {
    boolean value = properties.getProperty(property, false);
    BooleanProperty booleanProperty = new SimpleBooleanProperty();
    Bindings.bindBidirectional(booleanProperty, checkbox.selectedProperty());
    booleanProperty.set(value);
    checkbox.selectedProperty().addListener((observableValue, s, t1) -> properties.set(property, String.valueOf(t1)));
  }

  public static void bindCheckbox(CheckBox checkbox, String preference, boolean defaultValue) {
    PreferenceEntryRepresentation entry = client.getPreference(preference);
    boolean checked = entry.getBooleanValue(defaultValue);
    BooleanProperty booleanProperty = new SimpleBooleanProperty();
    Bindings.bindBidirectional(booleanProperty, checkbox.selectedProperty());
    booleanProperty.set(checked);
    checkbox.selectedProperty().addListener((observableValue, s, t1) -> client.getPreferenceService().setPreference(preference, t1));
  }


  public static void bindSpinner(Spinner spinner, CardTemplate cardTemplate, String property, int min, int max) {
    int value = properties.getProperty(property, 0);
    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, value);
    spinner.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(property, () -> {
      int value1 = Integer.parseInt(String.valueOf(t1));
      properties.set(property, String.valueOf(value1));
    }, 500));
  }

  public static void bindSpinner(Spinner spinner, CardTemplate cardTemplate, String property) {
    bindSpinner(spinner, properties, property, 0, 2000);
  }

  public static void bindFontLabel(Label label, CardTemplate cardTemplate, String key) {
    String name = properties.getProperty(key + "FontName", "Arial");
    int size = properties.getProperty(key + "FontSize", 72);
    String style = properties.getProperty(key + "FontStyle", FontPosture.REGULAR.name());

    String text = name + ", " + style + ", " + size + "px";
    Font font = Font.font(name, FontPosture.findByName(style), 14);
    label.setFont(font);
    label.setText(text);
    label.setTooltip(new Tooltip(text));
  }

  public static void bindFontSelector(CardTemplate cardTemplate, String key, Label label) {
    String name = properties.getProperty(key + "FontName", "Arial");
    int size = properties.getProperty(key + "FontSize", 72);
    String style = properties.getProperty(key + "FontStyle", FontPosture.REGULAR.name());

    Font font = Font.font(name, FontPosture.findByName(style), size);

    FontSelectorDialog fs = new FontSelectorDialog(font);
    fs.setHeight(500);
    fs.setTitle("Select Font");
    fs.setHeaderText("");
    fs.show();

    fs.setOnCloseRequest(e -> {
      if (fs.getResult() != null) {
        Font result = fs.getResult();
        debouncer.debounce("font", () -> {
          Map<String, String> values = new HashMap<>();
          values.put(key + "FontName", result.getFamily());
          values.put(key + "FontSize", String.valueOf((int) result.getSize()));
          values.put(key + "FontStyle", result.getStyle());
          properties.set(values);

          Font labelFont = Font.font(result.getFamily(), FontPosture.findByName(result.getStyle()), 14);
          label.setFont(labelFont);
          String labelText = result.getFamily() + ", " + result.getStyle() + ", " + result.getSize() + "px";
          Platform.runLater(() -> {
            label.setText(labelText);
            label.setTooltip(new Tooltip(labelText));
          });
        }, 1000);

      }
    });
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
        }, 1000);
      }
    });
  }

  public static void bindColorPicker(ColorPicker colorPicker, ObservedProperties properties, String property) {
    String value = properties.getProperty(property, "#FFFFFF");
    Color colorValue = Color.web(value);
    colorPicker.setValue(colorValue);
    colorPicker.valueProperty().addListener((observableValue, color, t1) -> {
      String hex = toHexString(t1);
      properties.set(property, hex);
    });
  }

  private static String format(double val) {
    String in = Integer.toHexString((int) Math.round(val * 255));
    return in.length() == 1 ? "0" + in : in;
  }

  public static String toHexString(Color value) {
    return "#" + (format(value.getRed()) + format(value.getGreen()) + format(value.getBlue()));
  }

  private static String getProperty(CardTemplate cardTemplate, String property) {
    try {
      return (String) PropertyUtils.getProperty(cardTemplate, property);
    } catch (Exception e) {
      LOG.error("faile to read property " + property + ": " + e.getMessage());
    }
    return null;
  }
}

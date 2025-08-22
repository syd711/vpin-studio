package de.mephisto.vpin.ui.util.binding;

import de.mephisto.vpin.ui.util.FontSelectorDialog;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanBinder<T> {
  private final static Logger LOG = LoggerFactory.getLogger(BeanBinder.class);

  private List<BindingChangedListener> listeners = new ArrayList<>();

  private T bean;

  private boolean paused;

  public void addListener(BindingChangedListener listener) {
    this.listeners.add(listener);
  }
  public void removeListener(BindingChangedListener listener) {
    this.listeners.remove(listener);
  }

  public void setBean(T bean) {
    this.bean = bean;
  }

  public T getBean() {
    return bean;
  }

  public void setPaused(boolean paused) {
    this.paused = paused;
  }


  //------------------------------

  public void bindTextField(TextField textField, String property) {
    textField.textProperty().addListener((observableValue, s, t1) -> {
      setProperty(property, textField.getText());
    });
  }

  public void bindComboBox(ComboBox<String> comboBox, String property) {
    bindComboBox(comboBox, property, "");
  }

  public void bindComboBox(ComboBox<String> comboBox, String property, String defaultValue) {
    comboBox.valueProperty().addListener((observableValue, s, t1) -> {
      Platform.runLater(() -> {
        setProperty(property, t1);
      });
    });
  }

  public void bindRadioButton(RadioButton radio, String property) {
    radio.selectedProperty().addListener((observableValue, s, t1) -> {
      setProperty(property, t1);
    });
  }

  public void bindCheckbox(CheckBox checkbox, String property) {
    checkbox.selectedProperty().addListener((observableValue, s, t1) -> {
      setProperty(property, t1);
    });
  }

  public void bindSpinner(Spinner<Integer> spinner, String property, int min, int max) {
    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, 0);
    spinner.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> {
      setProperty(property, Integer.parseInt(String.valueOf(t1)));
    });
  }

  public void bindSpinner(Spinner<Integer> spinner, String property) {
    bindSpinner(spinner, property, 0, 2000);
  }

  public static void setFontLabel(Label label, Object bean, String key) {
    String name = getProperty(bean, key + "FontName", "Arial");
    int size = 14;
    String style = getProperty(bean, key + "FontStyle", FontPosture.REGULAR.name());
    Font font = resolveFont(name, style, size);
    String text = name + ", " + style + ", " + size + "px";
    label.setFont(font);
    label.setText(text);
    label.setTooltip(new Tooltip(text));
  }

  public void openFontSelector(String key, Label label) {
    String name = getProperty(key + "FontName", "Arial");
    int size = getIntProperty(key + "FontSize", 72);
    String style = getProperty(key + "FontStyle", FontWeight.NORMAL.name());

    Font font = resolveFont(name, style, size);
    FontSelectorDialog fs = new FontSelectorDialog(font);
    fs.setHeight(500);
    fs.setTitle("Select Font");
    fs.setHeaderText("");
    fs.show();

    fs.setOnCloseRequest(e -> {
      if (fs.getResult() != null) {
        Font result = fs.getResult();
        setProperty(key + "FontName", result.getFamily(), true);
        setProperty(key + "FontSize", (int) result.getSize(), true);
        setProperty(key + "FontStyle", result.getStyle());

        Platform.runLater(() -> {
          setFontLabel(label, bean, key);
        });
      }
    });
  }

  public static Font resolveFont(String name, String style, int size) {
    FontPosture posture = FontPosture.findByName(style);
    Font font = Font.font(name, posture, size);
    if (posture == null) {
      font = Font.font(name, FontWeight.findByName(style), size);
    }

    if (style.contains(" ")) {
      String[] split = style.split(" ");
      FontWeight weight = FontWeight.findByName(split[0]);
      posture = FontPosture.findByName(split[1]);
      font = Font.font(name, weight, posture, size);
    }
    return font;
  }

  public void bindSlider(Slider slider, String property) {
    slider.valueProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
        setProperty(property, ((Double) t1).intValue());
      }
    });
  }

  public static void setColorPickerValue(ColorPicker colorPicker, Object beanObject, String property) {
    String value = getProperty(beanObject, property, "#FFFFFF");
    Color colorValue = Color.web(value);
    colorPicker.setValue(colorValue);
  }

  public void bindColorPicker(ColorPicker colorPicker, String property) {
    colorPicker.valueProperty().addListener((observableValue, color, t1) -> {
      String hex = toHexString(t1);
      setProperty(property, hex);
    });
  }

  private static String format(double val) {
    String in = Integer.toHexString((int) Math.round(val * 255));
    return in.length() == 1 ? "0" + in : in;
  }

  public static String toHexString(Color value) {
    return "#" + (format(value.getRed()) + format(value.getGreen()) + format(value.getBlue()));
  }

  //------------------------------------------------------

  private static <P> P getProperty(Object beanObject, String property, P defaultValue) {
    try {
      @SuppressWarnings("unchecked")
      P value = (P) PropertyUtils.getProperty(beanObject, property);
      return value != null ? value : defaultValue;
    }
    catch (Exception e) {
      LOG.error("Failed to read property " + property + ": " + e.getMessage());
      return defaultValue;
    }
  }

  public <P> P getProperty(String property, P defaultValue) {
    return getProperty(bean, property, defaultValue);
  }

  private boolean getBooleanProperty(String property, boolean defaultValue) {
    try {
      return (Boolean) PropertyUtils.getProperty(bean, property);
    }
    catch (Exception e) {
      LOG.error("Failed to read property " + property + ": " + e.getMessage());
    }
    return defaultValue;
  }

  private int getIntProperty(String property, int defaultValue) {
    try {
      String value = getProperty(property, null);
      if (value != null) {
        return Integer.parseInt(value);
      }
    }
    catch (Exception e) {
      LOG.error("Failed to read property " + property + ": " + e.getMessage());
    }
    return defaultValue;
  }

  public void setProperty(String property, Object value) {
    setProperty(property, value, false);
  }

  private void setProperty(String property, Object value, boolean skipChangeEvent) {
    try {
      PropertyUtils.setProperty(bean, property, value);
      if (!skipChangeEvent && !paused) {
        for (BindingChangedListener listener : listeners) {
          listener.beanPropertyChanged(bean, property, value);
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to set property " + property + ": " + e.getMessage());
    }
  }
}

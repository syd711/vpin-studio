package de.mephisto.vpin.ui.util.binding;

import de.mephisto.vpin.ui.util.FontSelectorDialog;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.internal.util.Objects;

public class BeanBinder<T> {
  private final static Logger LOG = LoggerFactory.getLogger(BeanBinder.class);

  private List<BindingChangedListener> listeners = new ArrayList<>();

  private List<Runnable> onBeanSet = new ArrayList<>();

  protected T bean;

  private boolean paused;

  public void addListener(BindingChangedListener listener) {
    this.listeners.add(listener);
  }

  public void removeListener(BindingChangedListener listener) {
    this.listeners.remove(listener);
  }

  public void setBean(T bean, boolean runSetters) {
    this.bean = bean;

    // notify all setters that a bean has been set
    if (runSetters) {
      boolean oldPause = paused;
      paused = true;
      for (Runnable r : onBeanSet) {
        r.run();
      }
      paused = oldPause;
    }
  }

  public T getBean() {
    return bean;
  }

  public void setPaused(boolean paused) {
    this.paused = paused;
  }

  public boolean isPaused() {
    return this.paused;
  }

  //------------------------------

  public void bindTextField(TextInputControl textField, String property) {
    bindTextField(textField, property, (observableValue, s, t1) -> {
      String text = textField.getText();
      setProperty(property, text != null ? text.trim() : null);
    });
    onBeanSet.add(() -> textField.setText(getProperty(property, null)));
  }

  public void bindTextField(TextInputControl textField, String property, ChangeListener<String> listener) {
    textField.textProperty().addListener((obs, oldValue, newValue) -> {
      if (!paused) {
        listener.changed(obs, oldValue, newValue);
      }
    });
    onBeanSet.add(() -> {
      Object value = getProperty(property, null);
      textField.setText(value != null? value.toString() : null);
    });
  }


  public <U> void bindComboBoxList(ComboBox<U> comboBox, String property, boolean addEmpty) {
    onBeanSet.add(() -> {
      List<U> items = getProperty(property, null);
      if (items != null) {
        items = new ArrayList<>(items);
        if (addEmpty) {
          items.add(0, null);
        }
        comboBox.setItems(FXCollections.observableList(items));
      }
    });
  }

  public <U> void bindComboBox(ComboBox<U> comboBox, String property) {
    bindComboBox(comboBox, property, null, t -> t);
  }

  public <U> void bindComboBox(ComboBox<U> comboBox, String property, String defaultValue, Function<U, ?> mapper) {
    comboBox.valueProperty().addListener((observableValue, s, t1) -> {
      setProperty(property, t1 != null ? mapper.apply(t1): null);
    });
    onBeanSet.add(() -> {
      Object value = getProperty(property, defaultValue);
      if (value != null) {
        for (U item : comboBox.getItems()) {
          if (item != null && value.equals(mapper.apply(item))) {
            comboBox.setValue(item);
            return;
          }
        }
        // not found in items
        if (comboBox.isEditable()) {
          try {
            comboBox.setValue((U) value);
          }
          catch (ClassCastException cce) {
            LOG.warn("Cannot set value {} for {} as it is of wrong type: {}", value, property, cce.getMessage());
          }
        }
      }
    });
  }

  public void bindRadioButton(RadioButton radio, String property) {
    radio.selectedProperty().addListener((observableValue, s, t1) -> {
      setProperty(property, t1);
    });
    onBeanSet.add(() -> radio.setSelected(getBooleanProperty(property, false)));
  }

  public void bindCheckbox(CheckBox checkbox, String property) {
    checkbox.selectedProperty().addListener((observableValue, s, t1) -> {
      setProperty(property, t1);
    });
    onBeanSet.add(() -> checkbox.setSelected(getBooleanProperty(property, false)));
  }

  public void bindToggleButton(ToggleButton btn, String property) {
    btn.selectedProperty().addListener((observableValue, s, t1) -> {
      setProperty(property, t1);
    });
    onBeanSet.add(() -> btn.setSelected(getBooleanProperty(property, false)));
  }

  public void bindSpinner(Spinner<Integer> spinner, String property) {
    bindSpinner(spinner, property, 0, 2000);
  }

  public void bindSpinner(Spinner<Integer> spinner, String property, int min, int max) {
    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, 0);
    spinner.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, oldValue, newValue) -> {
      setProperty(property, newValue);
    });
    onBeanSet.add(() -> {
      int value = getIntProperty(property, 0);
      factory.setValue(value);
    });
  }


  public void bindIntSlider(Slider slider, String property) {
    bindSlider(slider, property, v -> v.intValue(), v -> (Integer) v);
  }

  public void bindDoubleSlider(Slider slider, String property) {
    bindSlider(slider, property, v -> v, v -> (Double) v);
  }

  public void bindSlider(Slider slider, String property, Function<Number, Object> mapperToValue, Function<Object, Number> mapperToNumber) {
    slider.valueProperty().addListener((obs, oldValue, newValue) -> {
      setProperty(property, mapperToValue.apply(newValue));
    });
    onBeanSet.add(() -> {
      Object value = getProperty(property, null);
      try {
        slider.valueProperty().setValue(mapperToNumber.apply(value));
      }
      catch (NumberFormatException e) {
        LOG.error("Failed to convert {} {} to Number : {}", property, value, e.getMessage());
      }
    });
  }

  //--------------------------------------

  public void bindVisibility(Node node, String property) {
    node.managedProperty().bindBidirectional(node.visibleProperty());
    onBeanSet.add(() -> {
      boolean value = getBooleanProperty(property, false);
      node.setVisible(value);
    });
  }

  //--------------------------------------

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
    return bean != null ? getProperty(bean, property, defaultValue) : defaultValue;
  }

  private boolean getBooleanProperty(String property, boolean defaultValue) {
    try {
      Object value = getProperty(property, null);
      if (value instanceof Boolean) {
        return (Boolean) value;
      }
    }
    catch (Exception e) {
      LOG.error("Failed to read property " + property + ": " + e.getMessage());
    }
    return defaultValue;
  }

  private int getIntProperty(String property, int defaultValue) {
    try {
      Object value = getProperty(property, null);
      if (value instanceof Number) {
        return ((Number) value).intValue();
      }
      else if (value instanceof String) {
        return Integer.parseInt((String) value);
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
    if (bean != null) {
      try {
        Object oldValue = PropertyUtils.getProperty(bean, property);
        PropertyUtils.setProperty(bean, property, value);
        if (!skipChangeEvent && !paused && !Objects.equal(oldValue, value)) {
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
}

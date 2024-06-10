package de.mephisto.vpin.ui.util.binding;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.ui.util.FontSelectorDialog;
import javafx.application.Platform;
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

public class BeanBinder {
  private final static Logger LOG = LoggerFactory.getLogger(BeanBinder.class);

  public static Debouncer debouncer = new Debouncer();

  private BindingChangedListener listener;
  private Object bean;
  private boolean paused;

  private final static int MAX_DEBOUNCE = 500;

  public BeanBinder(BindingChangedListener listener) {
    this.listener = listener;
  }

  public void bindTextField(TextField textField, Object beanObject, String property, String defaultValue) {
    String value = getProperty(beanObject, property);
    textField.setText(value);
    textField.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce(property, () -> {
      setProperty(property, textField.getText());
    }, MAX_DEBOUNCE));

    if (StringUtils.isEmpty(value)) {
      textField.setText(defaultValue);
    }
  }

  public void bindComboBox(ComboBox<String> comboBox, Object beanObject, String property) {
    bindComboBox(comboBox, beanObject, property, "");
  }

  public void bindComboBox(ComboBox<String> comboBox, Object beanObject, String property, String defaultValue) {
    String value = getProperty(beanObject, property, defaultValue);
    comboBox.setValue(value);
    comboBox.valueProperty().addListener((observableValue, s, t1) -> {
      Platform.runLater(()-> {
        setProperty(property, t1);
      });
    });
  }

  public void bindCheckbox(CheckBox checkbox, Object beanObject, String property) {
    boolean value = getBooleanProperty(beanObject, property, false);
    checkbox.setSelected(value);
    checkbox.selectedProperty().addListener((observableValue, s, t1) -> {
      setProperty(property, t1);
    });
  }

  public void bindSpinner(Spinner spinner, Object beanObject, String property, int min, int max) {
    int value = getIntProperty(beanObject, property);
    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, value);
    spinner.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(property, () -> {
      int value1 = Integer.parseInt(String.valueOf(t1));
      setProperty(property, value1);
    }, MAX_DEBOUNCE));
  }

  public void bindSpinner(Spinner spinner, Object beanObject, String property) {
    bindSpinner(spinner, beanObject, property, 0, 2000);
  }

  public void bindFontLabel(Label label, Object beanObject, String key) {
    String name = getProperty(beanObject, key + "FontName", "Arial");
    int size = getIntProperty(beanObject, key + "FontSize", 72);
    String style = getProperty(beanObject, key + "FontStyle", FontPosture.REGULAR.name());

    String text = name + ", " + style + ", " + size + "px";
    Font font = Font.font(name, FontPosture.findByName(style), 14);
    label.setFont(font);
    label.setText(text);
    label.setTooltip(new Tooltip(text));
  }

  public void bindFontSelector(Object beanObject, String key, Label label) {
    String name = getProperty(beanObject, key + "FontName", "Arial");
    int size = getIntProperty(beanObject, key + "FontSize", 72);
    String style = getProperty(beanObject, key + "FontStyle", FontPosture.REGULAR.name());

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
          setProperty(key + "FontName", result.getFamily(), true);
          setProperty(key + "FontSize", (int) result.getSize(), true);
          setProperty(key + "FontStyle", result.getStyle());

          Font labelFont = Font.font(result.getFamily(), FontPosture.findByName(result.getStyle()), 14);
          label.setFont(labelFont);
          String labelText = result.getFamily() + ", " + result.getStyle() + ", " + result.getSize() + "px";
          Platform.runLater(() -> {
            label.setText(labelText);
            label.setTooltip(new Tooltip(labelText));
          });
        }, 50);

      }
    });
  }

  public void bindSlider(Slider slider, Object beanObject, String property) {
    int value = getIntProperty(beanObject, property, 0);
    slider.setValue(value);
    slider.valueProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
        debouncer.debounce(property, () -> {
          int value1 = ((Double) t1).intValue();
          setProperty(property, value1);
        }, MAX_DEBOUNCE);
      }
    });
  }

  public void setColorPickerValue(ColorPicker colorPicker, Object beanObject, String property) {
    String value = getProperty(beanObject, property, "#FFFFFF");
    Color colorValue = Color.web(value);
    colorPicker.setValue(colorValue);
  }

  public void bindColorPicker(ColorPicker colorPicker, Object beanObject, String property) {
    String value = getProperty(beanObject, property, "#FFFFFF");
    Color colorValue = Color.web(value);
    colorPicker.setValue(colorValue);
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

  private String getProperty(Object beanObject, String property) {
    try {
      return (String) PropertyUtils.getProperty(beanObject, property);
    } catch (Exception e) {
      LOG.error("Failed to read string property " + property + ": " + e.getMessage());
    }
    return null;
  }

  private String getProperty(Object beanObject, String property, String defaultValue) {
    try {
      String value = (String) PropertyUtils.getProperty(beanObject, property);
      if (!StringUtils.isEmpty(value)) {
        return value;
      }
    } catch (Exception e) {
      LOG.error("Failed to read property " + property + ": " + e.getMessage());
    }
    return defaultValue;
  }

  private boolean getBooleanProperty(Object beanObject, String property, boolean defaultValue) {
    try {
      return (Boolean) PropertyUtils.getProperty(beanObject, property);
    } catch (Exception e) {
      LOG.error("Failed to read property " + property + ": " + e.getMessage());
    }
    return defaultValue;
  }

  private int getIntProperty(Object beanObject, String property) {
    try {
      return (int) PropertyUtils.getProperty(beanObject, property);
    } catch (Exception e) {
      LOG.error("Failed to read property " + property + ": " + e.getMessage());
    }
    return 0;
  }

  private int getIntProperty(Object beanObject, String property, int defaultValue) {
    try {
      String value = String.valueOf(PropertyUtils.getProperty(beanObject, property));
      return Integer.parseInt(value);
    } catch (Exception e) {
      LOG.error("Failed to read property " + property + ": " + e.getMessage());
    }
    return defaultValue;
  }

  private void setProperty(String property, Object value) {
    setProperty(property, value, false);
  }

  private void setProperty(String property, Object value, boolean skipChangeEvent) {
    try {
      PropertyUtils.setProperty(bean, property, value);
      if (listener != null && !skipChangeEvent && !paused) {
        listener.beanPropertyChanged(bean, property, value);
      }
    } catch (Exception e) {
      LOG.error("Failed to set property " + property + ": " + e.getMessage());
    }
  }

  public void setBean(Object bean) {
    this.bean = bean;
  }

  public Object getBean() {
    return bean;
  }

  public void setPaused(boolean paused) {
    if(!paused) {
      debouncer.debounce("delay", () -> {
        this.paused = paused;
      }, MAX_DEBOUNCE + 100);
    }
    else {
      this.paused = paused;
    }
  }
}

package de.mephisto.vpin.ui.preferences.dialogs;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.system.SystemSummary;
import de.mephisto.vpin.restclient.util.ini.IniSettings;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.FontSelectorDialog;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class PINemHiUIPreferenceController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(PINemHiUIPreferenceController.class);

  public static Debouncer debouncer = new Debouncer();

  private final static String SETTING_FONTCOLOR = "fontcolor";
  private final static String SETTING_BGCOLOR = "bgcolor";
  private final static String SETTING_FONT = "font";
  private final static String SETTING_FONTSIZE = "fontsize";
  private final static String SETTING_FONTSPECIAL = "fontspecial";

  private final static String SETTING_NO_BACKGROUND = "no_background";
  private final static String SETTING_NO_BADGES_BACKGROUND = "no_badges_background";

  private final static String SETTING_CABINETVIEW = "cabinetview";

  private final static String SETTING_SCREEN = "screen";

  private final static String SETTING_MANUALOFFSET = "manualoffset";
  private final static String SETTING_X = "x";
  private final static String SETTING_Y = "y";

  private final static String SETTING_MANUALOFFSET_X_CENTERED = "manualoffset_x_centered";
  private final static String SETTING_X_START = "x_start";
  private final static String SETTING_X_END = "x_end";
  private final static String SETTING_Y_VALUE = "y_value";

  private final static String SETTING_MANUALOFFSET_Y_CENTERED = "manualoffset_y_centered";
  private final static String SETTING_Y_START = "y_start";
  private final static String SETTING_Y_END = "y_end";
  private final static String SETTING_X_VALUE = "x_value";

  private final static String SETTING_ON_AT_START = "on_at_start";

  private final static String SETTING_AUTOROTATE = "autorotate";
  private final static String SETTING_AUTOROTATE_SECONDS = "autorotate_seconds";

  private final static String SETTING_EXTRA_BACKGROUND = "extra_background";
  private final static String SETTING_X_POS = "x_pos";
  private final static String SETTING_Y_POS = "y_pos";
  private final static String SETTING_X_SIZE = "x_size";
  private final static String SETTING_Y_SIZE = "y_size";
  private final static String SETTING_ON_BETWEEN_ROTATION = "on_between_rotation";
  private final int SPINNER_DELAY = 500;

  private final static int MAX_SCREEN_WIDTH = 3840 * 3;

  @FXML
  private Button cancelBtn;

  @FXML
  private ColorPicker fontColorSelector;

  @FXML
  private ColorPicker backgroundColorSelector;

  @FXML
  private Label sampleLabel;


  @FXML
  private CheckBox noBackground;

  @FXML
  private CheckBox noBadgesBackground;

  @FXML
  private CheckBox cabinetView;

  @FXML
  private CheckBox onAtStart;

  @FXML
  private CheckBox autoRotate;


  @FXML
  private Spinner<Integer> autoRotateSeconds;

  @FXML
  private CheckBox manualOffset;

  @FXML
  private Spinner<Integer> x;

  @FXML
  private Spinner<Integer> y;


  @FXML
  private CheckBox manualoffsetXCentered;

  @FXML
  private Spinner<Integer> xStart;

  @FXML
  private Spinner<Integer> xEnd;

  @FXML
  private Spinner<Integer> yValue;


  @FXML
  private CheckBox manualoffsetYCentered;

  @FXML
  private Spinner<Integer> yStart;

  @FXML
  private Spinner<Integer> yEnd;

  @FXML
  private Spinner<Integer> xValue;


  @FXML
  private CheckBox extraBackground;

  @FXML
  private CheckBox onBetweenRotation;


  @FXML
  private Spinner<Integer> xPos;

  @FXML
  private Spinner<Integer> yPos;

  @FXML
  private Spinner<Integer> xSize;

  @FXML
  private Spinner<Integer> ySize;

  @FXML
  private ComboBox<MonitorInfo> screenInfoComboBox;

  @FXML
  private Button restartBtn;

  @FXML
  private Button stopBtn;

  private IniSettings settings;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onFontSelect() {
    String name = settings.getString(SETTING_FONT);
    int size = Integer.parseInt(settings.getString(SETTING_FONTSIZE).replaceAll("s", ""));
    String style = settings.getString(SETTING_FONTSPECIAL);

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

          String special = result.getStyle().toLowerCase();
          if (special.equals("regular")) {
            special = "";
          }
          else if (special.equals("bold regular")) {
            special = "bold";
          }
          else if (special.equals("bold italic")) {
            special = "bold";
          }

          Map<String, Object> values = new HashMap<>();

          values.put(SETTING_FONT, result.getFamily());
          values.put(SETTING_FONTSIZE, "s" + (int) result.getSize());
          values.put(SETTING_FONTSPECIAL, special);

          settings.setValues(values);

          if (StringUtils.isEmpty(special)) {
            special = "regular";
          }

          Font labelFont = Font.font(result.getFamily(), FontPosture.findByName(result.getStyle()), 14);
          sampleLabel.setFont(labelFont);
          String labelText = result.getFamily() + ", " + special + ", " + (int) result.getSize() + "px";
          Platform.runLater(() -> {
            sampleLabel.setText(labelText);
            sampleLabel.setTooltip(new Tooltip(labelText));
          });
        }, 1000);
      }
    });
  }

  @FXML
  private void onRestart() {
    restartBtn.setDisable(true);
    Platform.runLater(() -> {
      stopBtn.setDisable(!Studio.client.getPINemHiService().restart());
      stopBtn.setDisable(false);
      restartBtn.setDisable(false);
    });
  }

  @FXML
  private void onStop() {
    stopBtn.setDisable(!Studio.client.getPINemHiService().kill());
    stopBtn.setDisable(true);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    stopBtn.setDisable(!Studio.client.getPINemHiService().isRunning());
  }

  public void setSettings(IniSettings settings) {
    this.settings = settings;

    String fontSize = settings.getString(SETTING_FONTSIZE).substring(1);
    String fontFamily = settings.getString(SETTING_FONT);
    String special = settings.getString(SETTING_FONTSPECIAL);
    FontPosture posture = null;
    if (!StringUtils.isEmpty(special) && !special.equals("bold")) {
      special = StringUtils.capitalize(special);
      posture = FontPosture.findByName(special);

      if (posture == null) {
        posture = FontPosture.REGULAR;
      }
    }

    if (StringUtils.isEmpty(special)) {
      special = "regular";
    }

    Font labelFont = Font.font(fontFamily, posture, 14);
    sampleLabel.setFont(labelFont);
    String labelText = fontFamily + ", " + special + ", " + fontSize + "px";
    Platform.runLater(() -> {
      sampleLabel.setText(labelText);
      sampleLabel.setTooltip(new Tooltip(labelText));
    });

    SystemSummary systemSummary = Studio.client.getSystemService().getSystemSummary();
    screenInfoComboBox.setItems(FXCollections.observableList(systemSummary.getMonitorInfos()));
    screenInfoComboBox.valueProperty().setValue(systemSummary.getMonitorInfo(settings.getInt(SETTING_SCREEN)));
    screenInfoComboBox.valueProperty().addListener(new ChangeListener<MonitorInfo>() {
      @Override
      public void changed(ObservableValue<? extends MonitorInfo> observableValue, MonitorInfo monitorInfo, MonitorInfo t1) {
        settings.set(SETTING_SCREEN, t1.getId());
      }
    });

    fontColorSelector.valueProperty().setValue(Color.web("#" + settings.getString(SETTING_FONTCOLOR).substring(1)));
    fontColorSelector.valueProperty().addListener(new ChangeListener<Color>() {
      @Override
      public void changed(ObservableValue<? extends Color> observableValue, Color color, Color t1) {
        settings.set(SETTING_FONTCOLOR, toHexString(t1));
      }
    });

    backgroundColorSelector.valueProperty().setValue(Color.web("#" + settings.getString(SETTING_BGCOLOR).substring(1)));
    backgroundColorSelector.valueProperty().addListener(new ChangeListener<Color>() {
      @Override
      public void changed(ObservableValue<? extends Color> observableValue, Color color, Color t1) {
        settings.set(SETTING_BGCOLOR, toHexString(t1));
      }
    });


    noBackground.setSelected(settings.getBoolean(SETTING_NO_BACKGROUND));
    noBackground.selectedProperty().addListener((observableValue, aBoolean, t1) -> settings.set(SETTING_NO_BACKGROUND, t1));

    noBadgesBackground.setSelected(settings.getBoolean(SETTING_NO_BADGES_BACKGROUND));
    noBadgesBackground.selectedProperty().addListener((observableValue, aBoolean, t1) -> settings.set(SETTING_NO_BADGES_BACKGROUND, t1));

    cabinetView.setSelected(settings.getBoolean(SETTING_CABINETVIEW));
    cabinetView.selectedProperty().addListener((observableValue, aBoolean, t1) -> settings.set(SETTING_CABINETVIEW, t1));

    onAtStart.setSelected(settings.getBoolean(SETTING_ON_AT_START));
    onAtStart.selectedProperty().addListener((observableValue, aBoolean, t1) -> settings.set(SETTING_ON_AT_START, t1));

    autoRotate.setSelected(settings.getBoolean(SETTING_AUTOROTATE));
    autoRotate.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      settings.set(SETTING_AUTOROTATE, t1);
      autoRotateSeconds.setDisable(!t1);
    });

    autoRotateSeconds.setDisable(!autoRotate.isSelected());

    SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, settings.getInt(SETTING_AUTOROTATE_SECONDS));
    autoRotateSeconds.setValueFactory(factory);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(SETTING_AUTOROTATE_SECONDS, () -> {
      int value1 = Integer.parseInt(String.valueOf(t1));
      settings.set(SETTING_AUTOROTATE_SECONDS, value1);
    }, SPINNER_DELAY));

    manualOffset.setSelected(settings.getBoolean(SETTING_MANUALOFFSET));
    manualOffset.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      settings.set(SETTING_MANUALOFFSET, t1);
      screenInfoComboBox.setDisable(t1);
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, MAX_SCREEN_WIDTH, settings.getInt(SETTING_X));
    x.setValueFactory(factory);
    x.setEditable(true);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(SETTING_X, () -> {
      int value1 = Integer.parseInt(String.valueOf(t1));
      settings.set(SETTING_X, value1);
    }, SPINNER_DELAY));

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, MAX_SCREEN_WIDTH, settings.getInt(SETTING_Y));
    y.setValueFactory(factory);
    y.setEditable(true);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(SETTING_Y, () -> {
      int value1 = Integer.parseInt(String.valueOf(t1));
      settings.set(SETTING_Y, value1);
    }, SPINNER_DELAY));


    manualoffsetXCentered.setSelected(settings.getBoolean(SETTING_MANUALOFFSET_X_CENTERED));
    manualoffsetXCentered.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      settings.set(SETTING_MANUALOFFSET_X_CENTERED, t1);
      screenInfoComboBox.setDisable(t1);
    });


    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, MAX_SCREEN_WIDTH, settings.getInt(SETTING_X_START));
    xStart.setValueFactory(factory);
    xStart.setEditable(true);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(SETTING_X_START, () -> {
      int value1 = Integer.parseInt(String.valueOf(t1));
      settings.set(SETTING_X_START, value1);
    }, SPINNER_DELAY));

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, MAX_SCREEN_WIDTH, settings.getInt(SETTING_X_END));
    xEnd.setValueFactory(factory);
    xEnd.setEditable(true);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(SETTING_X_END, () -> {
      int value1 = Integer.parseInt(String.valueOf(t1));
      settings.set(SETTING_X_END, value1);
    }, SPINNER_DELAY));

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, MAX_SCREEN_WIDTH, settings.getInt(SETTING_Y_VALUE));
    yValue.setValueFactory(factory);
    yValue.setEditable(true);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(SETTING_Y_VALUE, () -> {
      int value1 = Integer.parseInt(String.valueOf(t1));
      settings.set(SETTING_Y_VALUE, value1);
    }, SPINNER_DELAY));


    manualoffsetYCentered.setSelected(settings.getBoolean(SETTING_MANUALOFFSET_Y_CENTERED));
    manualoffsetYCentered.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      settings.set(SETTING_MANUALOFFSET_Y_CENTERED, t1);
      screenInfoComboBox.setDisable(t1);
    });

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, MAX_SCREEN_WIDTH, settings.getInt(SETTING_Y_START));
    yStart.setValueFactory(factory);
    yStart.setEditable(true);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(SETTING_Y_START, () -> {
      int value1 = Integer.parseInt(String.valueOf(t1));
      settings.set(SETTING_Y_START, value1);
    }, SPINNER_DELAY));

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, MAX_SCREEN_WIDTH, settings.getInt(SETTING_Y_END));
    yEnd.setValueFactory(factory);
    yEnd.setEditable(true);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(SETTING_Y_END, () -> {
      int value1 = Integer.parseInt(String.valueOf(t1));
      settings.set(SETTING_Y_END, value1);
    }, SPINNER_DELAY));

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, MAX_SCREEN_WIDTH, settings.getInt(SETTING_X_VALUE));
    xValue.setValueFactory(factory);
    xValue.setEditable(true);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(SETTING_X_VALUE, () -> {
      int value1 = Integer.parseInt(String.valueOf(t1));
      settings.set(SETTING_X_VALUE, value1);
    }, SPINNER_DELAY));

    extraBackground.setSelected(settings.getBoolean(SETTING_EXTRA_BACKGROUND));
    extraBackground.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      settings.set(SETTING_EXTRA_BACKGROUND, t1);
      xPos.setDisable(!t1);
      yPos.setDisable(!t1);
      xSize.setDisable(!t1);
      ySize.setDisable(!t1);
    });

    onBetweenRotation.setSelected(settings.getBoolean(SETTING_ON_BETWEEN_ROTATION));
    onBetweenRotation.selectedProperty().addListener((observableValue, aBoolean, t1) -> settings.set(SETTING_ON_BETWEEN_ROTATION, t1));


    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, MAX_SCREEN_WIDTH, settings.getInt(SETTING_X_POS));
    xPos.setValueFactory(factory);
    xPos.setEditable(true);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(SETTING_X_POS, () -> {
      int value1 = Integer.parseInt(String.valueOf(t1));
      settings.set(SETTING_X_POS, value1);
    }, SPINNER_DELAY));

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, MAX_SCREEN_WIDTH, settings.getInt(SETTING_Y_POS));
    yPos.setValueFactory(factory);
    yPos.setEditable(true);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(SETTING_Y_POS, () -> {
      int value1 = Integer.parseInt(String.valueOf(t1));
      settings.set(SETTING_Y_POS, value1);
    }, SPINNER_DELAY));

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, MAX_SCREEN_WIDTH, settings.getInt(SETTING_X_SIZE));
    xSize.setValueFactory(factory);
    xSize.setEditable(true);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(SETTING_X_SIZE, () -> {
      int value1 = Integer.parseInt(String.valueOf(t1));
      settings.set(SETTING_X_SIZE, value1);
    }, SPINNER_DELAY));

    factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, MAX_SCREEN_WIDTH, settings.getInt(SETTING_Y_SIZE));
    ySize.setValueFactory(factory);
    ySize.setEditable(true);
    factory.valueProperty().addListener((observableValue, integer, t1) -> debouncer.debounce(SETTING_Y_SIZE, () -> {
      int value1 = Integer.parseInt(String.valueOf(t1));
      settings.set(SETTING_Y_SIZE, value1);
    }, SPINNER_DELAY));


    screenInfoComboBox.setDisable(manualOffset.isSelected() || manualoffsetXCentered.isSelected() || manualoffsetYCentered.isSelected());

    xPos.setDisable(!extraBackground.isSelected());
    yPos.setDisable(!extraBackground.isSelected());
    xSize.setDisable(!extraBackground.isSelected());
    ySize.setDisable(!extraBackground.isSelected());
  }

  @Override
  public void onDialogCancel() {
  }

  private static String toHexString(Color value) {
    return "c" + (format(value.getRed()) + format(value.getGreen()) + format(value.getBlue())).toUpperCase();
  }

  private static String format(double val) {
    String in = Integer.toHexString((int) Math.round(val * 255));
    return in.length() == 1 ? "0" + in : in;
  }

}

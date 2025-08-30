package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.util.PositionResizer;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.Optional;

public class LayerEditorFrameController extends LayerEditorBaseController {

  @FXML
  private Slider zoomSlider;
  @FXML
  private VBox backgroundPositionBox;
  @FXML
  private Spinner<Integer> backgroundXSpinner;
   @FXML
  private Spinner<Integer> backgroundYSpinner;

  @FXML
  private Spinner<Integer> borderSizeSpinner;
  @FXML
  private Spinner<Integer> borderRadiusSpinner;
  @FXML
  private ColorPicker borderColorSelector;

  @FXML
  private Spinner<Integer> marginTopSpinner;
  @FXML
  private Spinner<Integer> marginRightSpinner;
  @FXML
  private Spinner<Integer> marginBottomSpinner;
  @FXML
  private Spinner<Integer> marginLeftSpinner;


  public void setTemplate(CardTemplate cardTemplate, CardResolution res, Optional<GameRepresentation> game) {
    setIconVisibility(cardTemplate.isRenderFrame());
    setIconLock(cardTemplate.isLockFrame(), cardTemplate.isTemplate());

    // wrong old zoom values
    if (cardTemplate.getZoom() == 0) {
      cardTemplate.setZoom(1.0);
    }
    zoomSlider.setValue(cardTemplate.getZoom());

    IntegerSpinnerValueFactory factoryX = (IntegerSpinnerValueFactory) backgroundXSpinner.getValueFactory();
    factoryX.setValue((int) cardTemplate.getBackgroundX());
    IntegerSpinnerValueFactory factoryY = (IntegerSpinnerValueFactory) backgroundYSpinner.getValueFactory();
    factoryY.setValue((int) cardTemplate.getBackgroundY());

    borderSizeSpinner.getValueFactory().setValue(cardTemplate.getBorderWidth());
    borderRadiusSpinner.getValueFactory().setValue(cardTemplate.getBorderRadius());
    CardTemplateBinder.setColorPickerValue(borderColorSelector, cardTemplate, "borderColor");

    marginTopSpinner.getValueFactory().setValue(cardTemplate.getMarginTop());
    marginRightSpinner.getValueFactory().setValue(cardTemplate.getMarginRight());
    marginBottomSpinner.getValueFactory().setValue(cardTemplate.getMarginBottom());
    marginLeftSpinner.getValueFactory().setValue(cardTemplate.getMarginLeft());
  }

  @Override
  public void initBindings(CardTemplateBinder templateBeanBinder) {
    bindVisibilityIcon(templateBeanBinder, "renderFrame");
    bindLockIcon(templateBeanBinder, "lockFrame");

    templateBeanBinder.addListener((bean, key, value) -> {
      if ("renderBackground".equals(key)) {
        setIconVisibility((boolean) value);
      }
    });

    templateBeanBinder.bindSlider(zoomSlider, "zoom");
    // a 0 zoom is useless as the image would nt be drawn at all
    zoomSlider.setMin(1);
    StringConverter<Integer> converter = new StringConverter<>() {
      @Override
      public String toString(Integer object) {
        return object+"%";
      }
      @Override
      public Integer fromString(String str) {
        return Integer.parseInt(str.replace("%", "").trim());
      }
    };

    SpinnerValueFactory.IntegerSpinnerValueFactory factoryX = new SpinnerValueFactory.IntegerSpinnerValueFactory(-100, 100, 0);
    backgroundXSpinner.setValueFactory(factoryX);
    factoryX.setConverter(converter);
    factoryX.valueProperty().addListener((observableValue, integer, t1) -> {
      templateBeanBinder.setProperty("backgroundX", Double.parseDouble(String.valueOf(t1)) / 100);
    });

    SpinnerValueFactory.IntegerSpinnerValueFactory factoryY = new SpinnerValueFactory.IntegerSpinnerValueFactory(-100, 100, 0);
    backgroundYSpinner.setValueFactory(factoryY);
    factoryY.setConverter(converter);
    factoryY.valueProperty().addListener((observableValue, integer, t1) -> {
      templateBeanBinder.setProperty("backgroundY", Double.parseDouble(String.valueOf(t1)) / 100);
    });

    templateBeanBinder.bindSpinner(borderSizeSpinner, "borderWidth");
    templateBeanBinder.bindSpinner(borderRadiusSpinner, "borderRadius");
    templateBeanBinder.bindColorPicker(borderColorSelector, "borderColor");

    templateBeanBinder.bindSpinner(marginTopSpinner, "marginTop");
    templateBeanBinder.bindSpinner(marginRightSpinner, "marginRight");
    templateBeanBinder.bindSpinner(marginBottomSpinner, "marginBottom");
    templateBeanBinder.bindSpinner(marginLeftSpinner, "marginLeft");
  }

  @Override
  public void bindDragBox(PositionResizer dragBox) {
  }
  @Override
  public void unbindDragBox(PositionResizer dragBox) {
  }

}

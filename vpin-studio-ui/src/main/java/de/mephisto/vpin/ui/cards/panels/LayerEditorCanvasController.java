package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.ui.util.PositionResizer;
import de.mephisto.vpin.ui.util.binding.BeanBinder;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LayerEditorCanvasController extends LayerEditorBaseController {

  @FXML
  private Slider canvasAlphaPercentageSlider;
  @FXML
  private ColorPicker canvasColorSelector;
  @FXML
  private Spinner<Integer> canvasXSpinner;
  @FXML
  private Spinner<Integer> canvasYSpinner;
  @FXML
  private Spinner<Integer> canvasWidthSpinner;
  @FXML
  private Spinner<Integer> canvasHeightSpinner;
  @FXML
  private Spinner<Integer> canvasBorderRadiusSpinner;

  @Override
  public void setTemplate(CardTemplate cardTemplate) {
    BeanBinder.setIconVisibility(settingsPane, cardTemplate.isRenderCanvas());

    BeanBinder.setColorPickerValue(canvasColorSelector, cardTemplate, "canvasBackground");
    canvasXSpinner.getValueFactory().setValue(cardTemplate.getCanvasX());
    canvasYSpinner.getValueFactory().setValue(cardTemplate.getCanvasY());
    canvasWidthSpinner.getValueFactory().setValue(cardTemplate.getCanvasWidth());
    canvasHeightSpinner.getValueFactory().setValue(cardTemplate.getCanvasHeight());
    canvasBorderRadiusSpinner.getValueFactory().setValue(cardTemplate.getCanvasBorderRadius());
    canvasAlphaPercentageSlider.setValue(cardTemplate.getCanvasAlphaPercentage());
  }

  @Override
  public void initBindings(BeanBinder templateBeanBinder) {
    templateBeanBinder.bindVisibilityIcon(settingsPane, "renderCanvas");

    templateBeanBinder.bindColorPicker(canvasColorSelector, "canvasBackground");
    templateBeanBinder.bindSlider(canvasAlphaPercentageSlider, "canvasAlphaPercentage");
    templateBeanBinder.bindSpinner(canvasXSpinner, "canvasX", 0, 1920);
    templateBeanBinder.bindSpinner(canvasYSpinner, "canvasY", 0, 1920);
    templateBeanBinder.bindSpinner(canvasWidthSpinner, "canvasWidth", 0, 1920);
    templateBeanBinder.bindSpinner(canvasHeightSpinner, "canvasHeight", 0, 1080);
    templateBeanBinder.bindSpinner(canvasBorderRadiusSpinner, "canvasBorderRadius", 0, 100);
  }

  public void bindDragBox(PositionResizer dragBox) {
    bindSpinner(canvasXSpinner, dragBox.xProperty(), dragBox.xMinProperty(), dragBox.xMaxProperty());
    bindSpinner(canvasYSpinner, dragBox.yProperty(), dragBox.yMinProperty(), dragBox.yMaxProperty());
    bindSpinner(canvasWidthSpinner, dragBox.widthProperty(), dragBox.widthMinProperty(), dragBox.widthMaxProperty());
    bindSpinner(canvasHeightSpinner, dragBox.heightProperty(), dragBox.heightMinProperty(), dragBox.heightMaxProperty());
  }

}

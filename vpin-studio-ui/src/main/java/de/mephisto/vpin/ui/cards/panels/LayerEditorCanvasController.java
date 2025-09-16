package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.util.PositionResizer;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Optional;

public class LayerEditorCanvasController extends LayerEditorBaseController {

  @FXML
  private Slider canvasAlphaPercentageSlider;
  @FXML
  private ColorPicker canvasColorSelector;
  @FXML
  private Spinner<Integer> canvasBorderRadiusSpinner;

  public void setTemplate(CardTemplate cardTemplate, CardResolution res, Optional<GameRepresentation> game) {
    setIconVisibility(cardTemplate.isRenderCanvas());
    setIconLock(cardTemplate.isLockCanvas(), cardTemplate.isTemplate());

    CardTemplateBinder.setColorPickerValue(canvasColorSelector, cardTemplate, "canvasBackground");

    positionController.setTemplate("canvas", cardTemplate, res);
    canvasBorderRadiusSpinner.getValueFactory().setValue(cardTemplate.getCanvasBorderRadius());
    canvasAlphaPercentageSlider.setValue(cardTemplate.getCanvasAlphaPercentage());
  }

  @Override
  public void initBindings(CardTemplateBinder templateBeanBinder) {
    bindVisibilityIcon(templateBeanBinder, "renderCanvas");
    bindLockIcon(templateBeanBinder, "lockCanvas");

    templateBeanBinder.bindColorPicker(canvasColorSelector, "canvasBackground");
    templateBeanBinder.bindIntSlider(canvasAlphaPercentageSlider, "canvasAlphaPercentage");
    
    positionController.initBindings("canvas", templateBeanBinder);
    templateBeanBinder.bindSpinner(canvasBorderRadiusSpinner, "canvasBorderRadius", 0, 100);
  }

  @Override
  public void bindDragBox(PositionResizer dragBox) {
    positionController.bindDragBox(dragBox);
  }
  @Override
  public void unbindDragBox(PositionResizer dragBox) {
    positionController.unbindDragBox(dragBox);
  }

}

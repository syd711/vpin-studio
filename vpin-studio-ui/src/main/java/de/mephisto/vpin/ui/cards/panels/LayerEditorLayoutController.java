package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.util.PositionResizer;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class LayerEditorLayoutController extends LayerEditorBaseController {

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


  @Override
  public void setTemplate(CardTemplate cardTemplate, CardResolution res, Optional<GameRepresentation> game) {
    setIconVisibility(cardTemplate.isRenderBackground());

    zoomSlider.setValue(cardTemplate.getZoom());
    LayerSubEditorPositionController.setValue(backgroundXSpinner, cardTemplate, "backgroundX", res.toWidth());
    LayerSubEditorPositionController.setValue(backgroundYSpinner, cardTemplate, "backgroundY", res.toHeight());

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
    templateBeanBinder.addListener((bean, key, value) -> {
      if ("renderBackground".equals(key)) {
        setIconVisibility((boolean) value);
      }
    });

    templateBeanBinder.bindSlider(zoomSlider, "zoom");
    LayerSubEditorPositionController.bindSpinner(backgroundXSpinner, "backgroundX", templateBeanBinder, 0, 1920, true);
    LayerSubEditorPositionController.bindSpinner(backgroundYSpinner, "backgroundY", templateBeanBinder, 0, 1920, false);

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
    // background connot be distord
    dragBox.setAspectRatio(1.0);
    //FIXME OLE add capability in dragBox.setResizable(false);

    LayerSubEditorPositionController.bindSpinner(backgroundXSpinner, dragBox.xProperty(), dragBox.xMinProperty(), dragBox.xMaxProperty());
    LayerSubEditorPositionController.bindSpinner(backgroundYSpinner, dragBox.yProperty(), dragBox.yMinProperty(), dragBox.yMaxProperty());
  }
  @Override
  public void unbindDragBox(PositionResizer dragBox) {
    LayerSubEditorPositionController.unbindSpinner(backgroundXSpinner);
    LayerSubEditorPositionController.unbindSpinner(backgroundYSpinner);
  }

}

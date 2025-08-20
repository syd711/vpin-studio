package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.ui.util.PositionResizer;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LayerEditorWheelController extends LayerEditorBaseController {

  @FXML
  private Spinner<Integer> wheelSizeSpinner;
  @FXML
  private Spinner<Integer> wheelImageXSpinner;
  @FXML
  private Spinner<Integer> wheelImageYSpinner;

  @Override
  public void setTemplate(CardTemplate cardTemplate, CardResolution res) {
    setIconVisibility(cardTemplate.isRenderWheelIcon());

    LayerSubEditorPositionController.setValue(wheelSizeSpinner, cardTemplate, "wheelSize", res.toWidth());
    LayerSubEditorPositionController.setValue(wheelImageXSpinner, cardTemplate, "wheelX", res.toWidth());
    LayerSubEditorPositionController.setValue(wheelImageYSpinner, cardTemplate, "wheelY", res.toHeight());
  }

  @Override
  public void initBindings(CardTemplateBinder templateBeanBinder) {
    bindVisibilityIcon(templateBeanBinder, "renderWheelIcon");

    LayerSubEditorPositionController.bindSpinner(wheelSizeSpinner, "wheelSize", templateBeanBinder, 0, 1920, true);
    LayerSubEditorPositionController.bindSpinner(wheelImageXSpinner, "wheelX", templateBeanBinder, 0, 1920, true);
    LayerSubEditorPositionController.bindSpinner(wheelImageYSpinner, "wheelY", templateBeanBinder, 0, 1920, false);
  }

  @Override
  public void bindDragBox(PositionResizer dragBox) {
    // force aspect ratio of 1 for Wheel
    dragBox.setAspectRatio(1.0);

    LayerSubEditorPositionController.bindSpinner(wheelImageXSpinner, dragBox.xProperty(), dragBox.xMinProperty(), dragBox.xMaxProperty());
    LayerSubEditorPositionController.bindSpinner(wheelImageYSpinner, dragBox.yProperty(), dragBox.yMinProperty(), dragBox.yMaxProperty());
    LayerSubEditorPositionController.bindSpinner(wheelSizeSpinner, dragBox.widthProperty(), dragBox.widthMinProperty(), dragBox.widthMaxProperty());
  }
  @Override
  public void unbindDragBox(PositionResizer dragBox) {
    LayerSubEditorPositionController.unbindSpinner(wheelImageXSpinner);
    LayerSubEditorPositionController.unbindSpinner(wheelImageYSpinner);
    LayerSubEditorPositionController.unbindSpinner(wheelSizeSpinner);
  }
}

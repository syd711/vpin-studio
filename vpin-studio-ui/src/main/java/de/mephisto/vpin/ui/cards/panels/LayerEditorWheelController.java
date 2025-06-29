package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.ui.util.PositionResizer;
import de.mephisto.vpin.ui.util.binding.BeanBinder;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LayerEditorWheelController extends LayerEditorBaseController {

  @FXML
  private Spinner<Integer> wheelSizeSpinner;
  @FXML
  private Spinner<Integer> wheelImageSpinner;

  @Override
  public void setTemplate(CardTemplate cardTemplate) {
    BeanBinder.setIconVisibility(settingsPane, cardTemplate.isRenderWheelIcon());

    wheelImageSpinner.getValueFactory().setValue(cardTemplate.getWheelPadding());
    wheelSizeSpinner.getValueFactory().setValue(cardTemplate.getWheelSize());
  }

  @Override
  public void initBindings(BeanBinder templateBeanBinder) {
    templateBeanBinder.bindVisibilityIcon(settingsPane, "renderWheelIcon");

    templateBeanBinder.bindSpinner(wheelSizeSpinner, "wheelSize");
    templateBeanBinder.bindSpinner(wheelImageSpinner, "wheelPadding");
  }

  public void bindDragBox(PositionResizer dragBox) {
    // force aspect ratio of 1 for Wheel
    dragBox.setAspectRatio(1.0);

    bindSpinner(wheelSizeSpinner, dragBox.widthProperty(), dragBox.widthMinProperty(), dragBox.widthMaxProperty());
  }
}

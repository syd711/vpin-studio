package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.ui.util.PositionResizer;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LayerEditorLayoutController extends LayerEditorBaseController {

  @FXML
  private Slider borderSlider;
  @FXML
  private Spinner<Integer> paddingSpinner;
  @FXML
  private Spinner<Integer> marginTopSpinner;
  @FXML
  private Spinner<Integer> marginRightSpinner;
  @FXML
  private Spinner<Integer> marginBottomSpinner;
  @FXML
  private Spinner<Integer> marginLeftSpinner;


  @Override
  public void setTemplate(CardTemplate cardTemplate, CardResolution res) {
    borderSlider.setValue(cardTemplate.getBorderWidth());
    paddingSpinner.getValueFactory().setValue(cardTemplate.getPadding());

    marginTopSpinner.getValueFactory().setValue(cardTemplate.getMarginTop());
    marginRightSpinner.getValueFactory().setValue(cardTemplate.getMarginRight());
    marginBottomSpinner.getValueFactory().setValue(cardTemplate.getMarginBottom());
    marginLeftSpinner.getValueFactory().setValue(cardTemplate.getMarginLeft());
  }

  @Override
  public void initBindings(CardTemplateBinder templateBeanBinder) {
    templateBeanBinder.bindSlider(borderSlider, "borderWidth");
    templateBeanBinder.bindSpinner(paddingSpinner, "padding");

    templateBeanBinder.bindSpinner(marginTopSpinner, "marginTop");
    templateBeanBinder.bindSpinner(marginRightSpinner, "marginRight");
    templateBeanBinder.bindSpinner(marginBottomSpinner, "marginBottom");
    templateBeanBinder.bindSpinner(marginLeftSpinner, "marginLeft");
  }

  @Override
  public void bindDragBox(PositionResizer dragBox) {
    //positionController.bindDragBox(dragBox);
  }
  @Override
  public void unbindDragBox(PositionResizer dragBox) {
    //positionController.unbindDragBox(dragBox);
  }

}

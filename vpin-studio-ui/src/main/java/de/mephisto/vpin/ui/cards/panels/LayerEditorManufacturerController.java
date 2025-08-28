package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.util.PositionResizer;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

import java.util.*;

public class LayerEditorManufacturerController extends LayerEditorBaseController {
  
  @FXML
  private CheckBox manufacturerLogoUseYearCheckBox;
  //@FXML
  //private CheckBox manufacturerLogoKeepARCheckBox;

  public void setTemplate(CardTemplate cardTemplate, CardResolution res, Optional<GameRepresentation> game) {
    super.setTemplate(cardTemplate, res, game);
    setIconVisibility(cardTemplate.isRenderManufacturerLogo());
    lockBtn.setSelected(cardTemplate.isLockManufacturerLogo());

    positionController.setTemplate("manufacturerLogo", cardTemplate, res);

    manufacturerLogoUseYearCheckBox.setSelected(cardTemplate.isManufacturerLogoUseYear());
    //manufacturerLogoKeepARCheckBox.setSelected(cardTemplate.isManufacturerLogoKeepAspectRatio());

  }

  @Override
  public void initBindings(CardTemplateBinder templateBeanBinder) {
    bindVisibilityIcon(templateBeanBinder, "renderManufacturerLogo");
    positionController.initBindings("manufacturerLogo", templateBeanBinder);

    templateBeanBinder.bindCheckbox(manufacturerLogoUseYearCheckBox, "manufacturerLogoUseYear");
    //templateBeanBinder.bindCheckbox(manufacturerLogoKeepARCheckBox, "manufacturerLogoKeepAspectRatio");
  }

  @Override
  public void bindDragBox(PositionResizer dragBox) {
    // force aspect ratio of 1 for Wheel
    //dragBox.setAspectRatio(-1.0);

    positionController.bindDragBox(dragBox);
  }
  @Override
  public void unbindDragBox(PositionResizer dragBox) {
    positionController.unbindDragBox(dragBox);
  }
}

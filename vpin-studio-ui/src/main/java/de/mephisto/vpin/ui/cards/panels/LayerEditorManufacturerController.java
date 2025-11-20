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
  private CheckBox manufacturerLogoUseHistoricalCheckBox;
  @FXML
  private CheckBox manufacturerLogoKeepARCheckBox;

  public void setTemplate(CardTemplate cardTemplate, CardResolution res, Optional<GameRepresentation> game) {
    setIconVisibility(cardTemplate.isRenderManufacturerLogo());
    setIconLock(cardTemplate.isLockManufacturerLogo(), cardTemplate.isTemplate());

    positionController.setTemplate("manufacturerLogo", cardTemplate, res, true);

    manufacturerLogoUseHistoricalCheckBox.setSelected(cardTemplate.isManufacturerLogoUseYear());
    manufacturerLogoKeepARCheckBox.setSelected(cardTemplate.isManufacturerLogoKeepAspectRatio());
  }

  @Override
  public void initBindings(CardTemplateBinder templateBeanBinder) {
    bindVisibilityIcon(templateBeanBinder, "renderManufacturerLogo");
    bindLockIcon(templateBeanBinder, "lockManufacturerLogo");

    positionController.initBindings("manufacturerLogo", templateBeanBinder, true);

    templateBeanBinder.bindCheckbox(manufacturerLogoUseHistoricalCheckBox, "manufacturerLogoUseYear");
    templateBeanBinder.bindCheckbox(manufacturerLogoKeepARCheckBox, "manufacturerLogoKeepAspectRatio");
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

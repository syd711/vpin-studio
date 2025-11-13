package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.util.PositionResizer;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class LayerEditorManufacturerController extends LayerEditorBaseController {

  @FXML
  private CheckBox manufacturerLogoUseYearCheckBox;
  @FXML
  private CheckBox manufacturerLogoKeepARCheckBox;

  private CardTemplate cardTemplate;
  private Optional<GameRepresentation> game;

  public void setTemplate(CardTemplate cardTemplate, CardResolution res, Optional<GameRepresentation> game) {
    this.cardTemplate = cardTemplate;
    this.game = game;
    setIconVisibility(cardTemplate.isRenderManufacturerLogo());
    setIconLock(cardTemplate.isLockManufacturerLogo(), cardTemplate.isTemplate());

    positionController.setTemplate("manufacturerLogo", cardTemplate, res);

    manufacturerLogoUseYearCheckBox.setSelected(cardTemplate.isManufacturerLogoUseYear());
    manufacturerLogoKeepARCheckBox.setSelected(cardTemplate.isManufacturerLogoKeepAspectRatio());

  }

  @Override
  public void initBindings(CardTemplateBinder templateBeanBinder) {
    bindVisibilityIcon(templateBeanBinder, "renderManufacturerLogo");
    bindLockIcon(templateBeanBinder, "lockManufacturerLogo");

    positionController.initBindings("manufacturerLogo", templateBeanBinder);

    templateBeanBinder.bindCheckbox(manufacturerLogoUseYearCheckBox, "manufacturerLogoUseYear");
    templateBeanBinder.bindCheckbox(manufacturerLogoKeepARCheckBox, "manufacturerLogoKeepAspectRatio");
  }

  @Override
  public void bindDragBox(PositionResizer dragBox) {
    if (game.isPresent()) {
      byte[] manufacturerLogos = client.getHighscoreCardsService().getHighscoreImage(game.get(), cardTemplate, "manufacturerLogo");
      Image image = new Image(new ByteArrayInputStream(manufacturerLogos));
      double ratio = image.getWidth() / image.getHeight();
      dragBox.setAspectRatio(ratio);
    }
    positionController.bindDragBox(dragBox);
  }

  @Override
  public void unbindDragBox(PositionResizer dragBox) {
    positionController.unbindDragBox(dragBox);
  }
}

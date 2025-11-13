package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.util.PositionResizer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class LayerEditorManufacturerController extends LayerEditorBaseController {

  @FXML
  private CheckBox manufacturerLogoUseHistoricalCheckBox;
  @FXML
  private CheckBox manufacturerLogoKeepARCheckBox;

  private CardTemplate cardTemplate;
  private Optional<GameRepresentation> game;
  private Long aspectRatio = null;

  public void setTemplate(CardTemplate cardTemplate, CardResolution res, Optional<GameRepresentation> game) {
    this.cardTemplate = cardTemplate;
    this.game = game;
    setIconVisibility(cardTemplate.isRenderManufacturerLogo());
    setIconLock(cardTemplate.isLockManufacturerLogo(), cardTemplate.isTemplate());

    positionController.setTemplate("manufacturerLogo", cardTemplate, res);

    manufacturerLogoUseHistoricalCheckBox.setSelected(cardTemplate.isManufacturerLogoUseYear());
    manufacturerLogoKeepARCheckBox.setSelected(cardTemplate.isManufacturerLogoKeepAspectRatio());

  }

  @Override
  public void initBindings(CardTemplateBinder templateBeanBinder) {
    bindVisibilityIcon(templateBeanBinder, "renderManufacturerLogo");
    bindLockIcon(templateBeanBinder, "lockManufacturerLogo");

    positionController.initBindings("manufacturerLogo", templateBeanBinder);

    manufacturerLogoKeepARCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        refreshAR();
      }
    });
    manufacturerLogoUseHistoricalCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        refreshAR();
      }
    });

    templateBeanBinder.bindCheckbox(manufacturerLogoUseHistoricalCheckBox, "manufacturerLogoUseYear");
    templateBeanBinder.bindCheckbox(manufacturerLogoKeepARCheckBox, "manufacturerLogoKeepAspectRatio");
  }

  private void refreshAR() {
    this.aspectRatio = null;
    if (manufacturerLogoKeepARCheckBox.isSelected() && game.isPresent()) {
      byte[] manufacturerLogos = client.getHighscoreCardsService().getHighscoreImage(game.get(), cardTemplate, "manufacturerLogo");
      Image image = new Image(new ByteArrayInputStream(manufacturerLogos));
      this.aspectRatio = Math.round(image.getWidth() / image.getHeight());
    }
    templateEditorController.selectLayer(this);
  }

  @Override
  public void bindDragBox(PositionResizer dragBox) {
    dragBox.setAspectRatio(aspectRatio != null ? aspectRatio.doubleValue() : null);
    positionController.bindDragBox(dragBox);
  }

  @Override
  public void unbindDragBox(PositionResizer dragBox) {
    positionController.unbindDragBox(dragBox);
  }
}

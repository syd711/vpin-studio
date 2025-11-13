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
  private Double aspectRatio = null;
  private PositionResizer dragBox;

  public void setTemplate(CardTemplate cardTemplate, CardResolution res, Optional<GameRepresentation> game) {
    this.cardTemplate = cardTemplate;
    this.game = game;
    setIconVisibility(cardTemplate.isRenderManufacturerLogo());
    setIconLock(cardTemplate.isLockManufacturerLogo(), cardTemplate.isTemplate());

    positionController.setTemplate("manufacturerLogo", cardTemplate, res);

    manufacturerLogoUseHistoricalCheckBox.setSelected(cardTemplate.isManufacturerLogoUseYear());
    manufacturerLogoKeepARCheckBox.setSelected(cardTemplate.isManufacturerLogoKeepAspectRatio());

    refreshAR();
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
      if (manufacturerLogos != null) {
        Image image = new Image(new ByteArrayInputStream(manufacturerLogos));
        aspectRatio = Math.round(image.getWidth() / image.getHeight() * 10.0) / 10.0;
      }
    }

    if (dragBox != null) {
      dragBox.setAspectRatio(aspectRatio != null ? aspectRatio : null);
    }
  }

  @Override
  public void bindDragBox(PositionResizer dragBox) {
    this.dragBox = dragBox;
    positionController.bindDragBox(dragBox);
  }

  @Override
  public void unbindDragBox(PositionResizer dragBox) {
    positionController.unbindDragBox(dragBox);
  }
}

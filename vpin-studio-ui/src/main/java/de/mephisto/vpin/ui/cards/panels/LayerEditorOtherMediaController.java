package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.util.PositionResizer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;

import static de.mephisto.vpin.ui.Studio.client;

import java.io.ByteArrayInputStream;
import java.util.*;

public class LayerEditorOtherMediaController extends LayerEditorBaseController {

  @FXML
  private CheckBox otherMediaKeepARCheckBox;

  @FXML
  private ComboBox<VPinScreen> otherMediaScreensComboBox;

  private CardTemplate cardTemplate;
  private Optional<GameRepresentation> game;
  private Double aspectRatio = null;
  private PositionResizer dragBox;

  public void setTemplate(CardTemplate cardTemplate, CardResolution res, Optional<GameRepresentation> game) {
    this.cardTemplate = cardTemplate;
    this.game = game;

    setIconVisibility(cardTemplate.isRenderOtherMedia());
    setIconLock(cardTemplate.isLockOtherMedia(), cardTemplate.isTemplate());

    positionController.setTemplate("otherMedia", cardTemplate, res);

    otherMediaKeepARCheckBox.setSelected(cardTemplate.isOtherMediaKeepAspectRatio());
    otherMediaScreensComboBox.getSelectionModel().select(cardTemplate.getOtherMediaScreen());

    refreshAR();
  }

  @Override
  public void initBindings(CardTemplateBinder templateBeanBinder) {
    bindVisibilityIcon(templateBeanBinder, "renderOtherMedia");
    bindLockIcon(templateBeanBinder, "lockOtherMedia");

    positionController.initBindings("otherMedia", templateBeanBinder);


    otherMediaKeepARCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        refreshAR();
      }
    });

    templateBeanBinder.bindCheckbox(otherMediaKeepARCheckBox, "otherMediaKeepAspectRatio");

    Frontend frontend = client.getFrontendService().getFrontend();
    List<VPinScreen> VPinScreens = new ArrayList<>(frontend.getSupportedScreens());
    VPinScreens.remove(VPinScreen.Audio);
    VPinScreens.remove(VPinScreen.AudioLaunch);
    //VPinScreens.remove(VPinScreen.Other2);
    //VPinScreens.remove(VPinScreen.GameInfo);
    //VPinScreens.remove(VPinScreen.GameHelp);
    //VPinScreens.remove(VPinScreen.Topper);
    //VPinScreens.remove(VPinScreen.DMD);
    //VPinScreens.remove(VPinScreen.Logo);
    VPinScreens.remove(VPinScreen.Wheel);
    VPinScreens.remove(VPinScreen.Menu);
    VPinScreens.remove(VPinScreen.BackGlass);
    VPinScreens.remove(VPinScreen.PlayField);
    VPinScreens.remove(VPinScreen.Loading);
    otherMediaScreensComboBox.setItems(FXCollections.observableList(VPinScreens));

    templateBeanBinder.bindComboBox(otherMediaScreensComboBox, "otherMediaScreen");
  }


  private void refreshAR() {
    this.aspectRatio = null;
    if (otherMediaKeepARCheckBox.isSelected() && game.isPresent()) {
      byte[] otherMediaImage = client.getHighscoreCardsService().getHighscoreImage(game.get(), cardTemplate, "otherMedia");
      if (otherMediaImage != null) {
        Image image = new Image(new ByteArrayInputStream(otherMediaImage));
        aspectRatio = Math.round(image.getWidth()/ image.getHeight() * 10.0) / 10.0;
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

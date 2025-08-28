package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.util.PositionResizer;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.*;
import java.util.stream.Collectors;

public class LayerEditorOverlayController extends LayerEditorBaseController {

  @FXML
  private ComboBox<String> screensComboBox;


  public void setTemplate(CardTemplate cardTemplate, CardResolution res, Optional<GameRepresentation> game) {
    super.setTemplate(cardTemplate, res, game, false);
    setIconVisibility(cardTemplate.isOverlayMode());

  }

  public void initBindings(CardTemplateBinder templateBeanBinder) {
    bindVisibilityIcon(templateBeanBinder, "overlayMode");

    List<VPinScreen> VPinScreens = new ArrayList<>(Arrays.asList(VPinScreen.values()));
    VPinScreens.remove(VPinScreen.Audio);
    VPinScreens.remove(VPinScreen.AudioLaunch);
    VPinScreens.remove(VPinScreen.GameInfo);
    VPinScreens.remove(VPinScreen.GameHelp);
    VPinScreens.remove(VPinScreen.DMD);
    VPinScreens.remove(VPinScreen.Wheel);
    VPinScreens.remove(VPinScreen.Other2);
    VPinScreens.remove(VPinScreen.PlayField);
    VPinScreens.remove(VPinScreen.Loading);
    screensComboBox.setItems(FXCollections.observableList(VPinScreens.stream().map(p -> p.name()).collect(Collectors.toList())));

    templateBeanBinder.bindComboBox(screensComboBox, "overlayScreen");
  }

  @Override
  public void bindDragBox(PositionResizer dragBox) {
  }
  @Override
  public void unbindDragBox(PositionResizer dragBox) {
  }
}

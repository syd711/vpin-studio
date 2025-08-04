package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.ui.util.binding.BeanBinder;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.*;
import java.util.stream.Collectors;

public class LayerEditorOverlayController extends LayerEditorBaseController {

  @FXML
  private ComboBox<String> screensComboBox;


  @Override
  public void setTemplate(CardTemplate cardTemplate) {
    BeanBinder.setIconVisibility(settingsPane, cardTemplate.isOverlayMode());

    // background
    screensComboBox.setDisable(!cardTemplate.isOverlayMode());
  }

  public void initBindings(BeanBinder templateBeanBinder) {
    templateBeanBinder.bindVisibilityIcon(settingsPane, "overlayMode");

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
}

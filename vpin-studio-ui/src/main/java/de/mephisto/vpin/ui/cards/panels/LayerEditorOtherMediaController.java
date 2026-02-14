package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.util.PositionResizer;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;

import static de.mephisto.vpin.ui.Studio.client;

import java.util.*;

public class LayerEditorOtherMediaController extends LayerEditorBaseController {

  @FXML
  private CheckBox otherMediaKeepARCheckBox;

  @FXML
  private ComboBox<VPinScreen> otherMediaScreensComboBox;

  public void setTemplate(CardTemplate cardTemplate, CardResolution res, Optional<GameRepresentation> game) {
    setIconVisibility(cardTemplate.isRenderOtherMedia());
    setIconLock(cardTemplate.isLockOtherMedia(), cardTemplate.isTemplate());

    positionController.setTemplate("otherMedia", cardTemplate, res, true);

    otherMediaKeepARCheckBox.setSelected(cardTemplate.isOtherMediaKeepAspectRatio());
    otherMediaScreensComboBox.getSelectionModel().select(cardTemplate.getOtherMediaScreen());
  }

  @Override
  public void initBindings(CardTemplateBinder templateBeanBinder) {
    bindVisibilityIcon(templateBeanBinder, "renderOtherMedia");
    bindLockIcon(templateBeanBinder, "lockOtherMedia");

    positionController.initBindings("otherMedia", templateBeanBinder, true);

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

  @Override
  public void bindDragBox(PositionResizer dragBox) {
    positionController.bindDragBox(dragBox);
  }

  @Override
  public void unbindDragBox(PositionResizer dragBox) {
    positionController.unbindDragBox(dragBox);
  }
}

package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.ui.util.PositionResizer;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.*;

import static de.mephisto.vpin.ui.Studio.stage;

public class LayerEditorTitleController extends LayerEditorBaseController {

  @FXML
  private TextField titleText;
  @FXML
  private Label titleFontLabel;

  @FXML
  private CheckBox titleUseDefaultColor;
  @FXML
  private VBox titleColorBox;
  @FXML
  private ColorPicker titleFontColorSelector;


  @FXML
  private void onFontTitleSelect() {
    CardTemplateBinder templateBeanBinder = templateEditorController.getBeanBinder();
    templateBeanBinder.openFontSelector("title", titleFontLabel);
  }

  @FXML
  private void onFontTitleApplyAll() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Apply To All", "Apply selected font settings to all templates?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      CardTemplate selection = templateEditorController.getCardTemplate();
      templateEditorController.applyFontOnAllTemplates(item -> {
        item.setTitleFontName(selection.getTitleFontName());
        item.setTitleFontSize(selection.getTitleFontSize());
        item.setTitleFontStyle(selection.getTitleFontStyle());
      });
    }
  }

  @Override
  public void setTemplate(CardTemplate cardTemplate, CardResolution res) {
    CardTemplateBinder.setIconVisibility(settingsPane, cardTemplate.isRenderTitle());

    titleText.setText(cardTemplate.getTitle());
    CardTemplateBinder.setFontLabel(titleFontLabel, cardTemplate, "title");
    
    titleUseDefaultColor.setSelected(cardTemplate.isRawScore());
    CardTemplateBinder.setColorPickerValue(titleFontColorSelector, cardTemplate, "titleColor");

    positionController.setTemplate("title", cardTemplate, res);
  }

  @Override
  public void initBindings(CardTemplateBinder templateBeanBinder) {
    templateBeanBinder.bindVisibilityIcon(settingsPane, "renderTitle");

    templateBeanBinder.bindTextField(titleText, "title");

    templateBeanBinder.bindCheckbox(titleUseDefaultColor, "titleUseDefaultColor");
    titleUseDefaultColor.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      titleColorBox.setDisable(t1);
    });
    templateBeanBinder.bindColorPicker(titleFontColorSelector, "titleColor");

    positionController.initBindings("title", templateBeanBinder);
  }

  @Override
  public void bindDragBox(PositionResizer dragBox) {
    positionController.bindDragBox(dragBox);
  }
}

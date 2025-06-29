package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.ui.util.binding.BeanBinder;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.*;

import static de.mephisto.vpin.ui.Studio.stage;

public class LayerEditorTitleController extends LayerEditorBaseController {

  @FXML
  private TextField titleText;
  @FXML
  private Label titleFontLabel;

  @FXML
  private void onFontTitleSelect() {
    BeanBinder templateBeanBinder = templateEditorController.getBeanBinder();
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
  public void setTemplate(CardTemplate cardTemplate) {
    BeanBinder.setIconVisibility(settingsPane, cardTemplate.isRenderTitle());

    titleText.setText(cardTemplate.getTitle());
    BeanBinder.setFontLabel(titleFontLabel, cardTemplate, "title");
  }

  @Override
  public void initBindings(BeanBinder templateBeanBinder) {
    templateBeanBinder.bindVisibilityIcon(settingsPane, "renderTitle");

    templateBeanBinder.bindTextField(titleText, "title");
  }
}

package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.ui.util.binding.BeanBinder;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.*;

import static de.mephisto.vpin.ui.Studio.stage;

public class LayerEditorTableNameController extends LayerEditorBaseController {

  @FXML
  private Label tableFontLabel;

    @FXML
  private void onFontTableSelect() {
    BeanBinder templateBeanBinder = templateEditorController.getBeanBinder();
    templateBeanBinder.openFontSelector("table", tableFontLabel);
  }

  @FXML
  private void onFontTableApplyAll() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Apply To All", "Apply selected font settings to all templates?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      CardTemplate selection = templateEditorController.getCardTemplate();
      templateEditorController.applyFontOnAllTemplates(item -> {
        item.setTableFontName(selection.getTableFontName());
        item.setTableFontSize(selection.getTableFontSize());
        item.setTableFontStyle(selection.getTableFontStyle());
      });
    }
  }

  @Override
  public void setTemplate(CardTemplate cardTemplate) {
    BeanBinder.setIconVisibility(settingsPane, cardTemplate.isRenderTableName());

    BeanBinder.setFontLabel(tableFontLabel, cardTemplate, "table");
  }

  @Override
  public void initBindings(BeanBinder templateBeanBinder) {
    templateBeanBinder.bindVisibilityIcon(settingsPane, "renderTableName");
  }
}

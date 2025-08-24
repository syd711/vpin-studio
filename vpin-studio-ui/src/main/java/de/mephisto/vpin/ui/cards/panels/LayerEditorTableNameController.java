package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.util.PositionResizer;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.*;

import static de.mephisto.vpin.ui.Studio.stage;

public class LayerEditorTableNameController extends LayerEditorBaseController {

  @FXML
  private Label tableFontLabel;

  @FXML
  private CheckBox tableUseDefaultColor;
  @FXML
  private VBox tableColorBox;
  @FXML
  private ColorPicker tableFontColorSelector;

  @FXML
  private CheckBox tableUseVpsNameCheckbox;
  @FXML
  private HBox tableRenderManufacturerBox;
  @FXML
  private HBox tableRenderYearBox;
  @FXML
  private CheckBox tableRenderManufacturerCheckbox;
  @FXML
  private CheckBox tableRenderYearCheckbox;

  @FXML
  private void onFontTableSelect() {
    CardTemplateBinder templateBeanBinder = templateEditorController.getBeanBinder();
    templateBeanBinder.openFontSelector("table", tableFontLabel);
  }

  @FXML
  private void onFontTableApplyAll() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(stage, "Apply To All", "Apply selected font settings to all templates?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      CardTemplate selection = templateEditorController.getSelectedCardTemplate();
      templateEditorController.applyFontOnAllTemplates(item -> {
        item.setTableFontName(selection.getTableFontName());
        item.setTableFontSize(selection.getTableFontSize());
        item.setTableFontStyle(selection.getTableFontStyle());
      });
    }
  }

  @Override
  public void setTemplate(CardTemplate cardTemplate, CardResolution res, Optional<GameRepresentation> game) {
    setIconVisibility(cardTemplate.isRenderTableName());

    CardTemplateBinder.setFontLabel(tableFontLabel, cardTemplate, "table");
    tableUseVpsNameCheckbox.setSelected(cardTemplate.isTableUseVpsName());
    tableRenderManufacturerCheckbox.setSelected(cardTemplate.isTableRenderManufacturer());
    tableRenderYearCheckbox.setSelected(cardTemplate.isTableRenderYear());

    tableUseDefaultColor.setSelected(cardTemplate.isRawScore());
    CardTemplateBinder.setColorPickerValue(tableFontColorSelector, cardTemplate, "tableColor");

    positionController.setTemplate("table", cardTemplate, res);
  }

  @Override
  public void initBindings(CardTemplateBinder templateBeanBinder) {
    bindVisibilityIcon(templateBeanBinder, "renderTableName");

    templateBeanBinder.bindCheckbox(tableUseVpsNameCheckbox, "tableUseVpsName");
    tableUseVpsNameCheckbox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      tableRenderManufacturerBox.setDisable(!t1);
      tableRenderYearBox.setDisable(!t1);
    });
    templateBeanBinder.bindCheckbox(tableRenderManufacturerCheckbox, "tableRenderManufacturer");
    templateBeanBinder.bindCheckbox(tableRenderYearCheckbox, "tableRenderYear");

    templateBeanBinder.bindCheckbox(tableUseDefaultColor, "tableUseDefaultColor");
    tableUseDefaultColor.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
      tableColorBox.setDisable(t1);
    });
    templateBeanBinder.bindColorPicker(tableFontColorSelector, "tableColor");

    positionController.initBindings("table", templateBeanBinder);
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

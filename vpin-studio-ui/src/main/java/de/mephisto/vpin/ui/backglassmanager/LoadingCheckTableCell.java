package de.mephisto.vpin.ui.backglassmanager;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingTableCell;
import javafx.scene.control.Tooltip;

abstract public class LoadingCheckTableCell  extends BaseLoadingTableCell<DirectB2SEntryModel> {

  /**
   * should return true if the checked mark is visible. Model is never null
   */
  protected abstract int isChecked(DirectB2SEntryModel model);

  /**
   * should return a contextualized tooltip for given model. Model is never null
   */
  protected abstract String getTooltip(DirectB2SEntryModel model);

  @Override
  protected void renderItem(DirectB2SEntryModel model) {
    int check = isChecked(model);
    if (check == 1) {
      setText(null);
      setTooltip(new Tooltip(getTooltip(model)));
      setGraphic(WidgetFactory.createCheckboxIcon());
    }
    else if (check == 2) {
      setText(null);
      setTooltip(new Tooltip(getTooltip(model)));
      setGraphic(WidgetFactory.createExclamationIcon());
    }
    else {
      setText("");
      setTooltip(null);
      setGraphic(null);
    }
  }
}
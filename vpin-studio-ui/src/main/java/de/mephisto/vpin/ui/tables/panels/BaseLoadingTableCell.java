package de.mephisto.vpin.ui.tables.panels;

import javafx.scene.control.TableCell;

public abstract class BaseLoadingTableCell<T extends BaseLoadingModel<?>> extends TableCell<T, T> {

    protected abstract String getLoading(T model);

    @Override
    protected void updateItem(T model, boolean empty) {
      super.updateItem(model, empty);
      setText(null);
      setTooltip(null);
      setGraphic(null);
      
      if (model!=null && model.isLoaded()) {
        renderItem(model);
      }
      else if (model!=null) {
        setText(getLoading(model));
      }
    }

    protected abstract void renderItem(T model);
}


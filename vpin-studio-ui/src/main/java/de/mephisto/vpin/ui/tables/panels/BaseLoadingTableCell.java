package de.mephisto.vpin.ui.tables.panels;

import javafx.scene.control.TableCell;

public abstract class BaseLoadingTableCell<M extends BaseLoadingModel<?, ?>> extends TableCell<M, M> {

  protected String getLoading(M model) {
    return "";
  }

  @Override
  protected void updateItem(M model, boolean empty) {
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

  protected abstract void renderItem(M model);
}


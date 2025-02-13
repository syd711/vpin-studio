package de.mephisto.vpin.ui.tables.panels;

import de.mephisto.vpin.ui.tables.TablesController;

public abstract class BaseSideBarController<T> {

  protected TablesController tablesController;

  public abstract void setVisible(boolean visible);

  public void setRootController(TablesController tablesController) {
    this.tablesController = tablesController;
  }

}

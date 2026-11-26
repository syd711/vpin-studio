package de.mephisto.vpin.ui.util.table;

import javafx.scene.control.TreeTableView;
import javafx.scene.control.skin.TreeTableViewSkin;

/**
 * Default skin of every TreeTableView (set via -fx-skin in stylesheet-tableview.css): uppercase column headers.
 */
public class StudioTreeTableViewSkin<T> extends TreeTableViewSkin<T> {

  public StudioTreeTableViewSkin(TreeTableView<T> control) {
    super(control);
  }

  @Override
  protected void layoutChildren(double x, double y, double w, double h) {
    super.layoutChildren(x, y, w, h);
    TableHeaderUppercase.apply(getTableHeaderRow());
  }
}

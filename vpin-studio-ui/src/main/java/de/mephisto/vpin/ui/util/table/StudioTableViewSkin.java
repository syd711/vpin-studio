package de.mephisto.vpin.ui.util.table;

import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableViewSkin;

/**
 * Default skin of every TableView (set via -fx-skin in stylesheet-tableview.css): uppercase column headers.
 */
public class StudioTableViewSkin<T> extends TableViewSkin<T> {

  public StudioTableViewSkin(TableView<T> control) {
    super(control);
  }

  @Override
  protected void layoutChildren(double x, double y, double w, double h) {
    super.layoutChildren(x, y, w, h);
    TableHeaderUppercase.apply(getTableHeaderRow());
  }
}

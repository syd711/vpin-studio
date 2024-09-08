package de.mephisto.vpin.ui.tables.panels;

import java.util.Comparator;

import javafx.scene.control.TableView;

public interface BaseColumnSorter<M> {

  Comparator<M> buildComparator(TableView<M> tableView);

}

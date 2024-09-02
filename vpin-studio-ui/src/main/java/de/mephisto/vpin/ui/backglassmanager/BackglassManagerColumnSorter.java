package de.mephisto.vpin.ui.backglassmanager;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Comparator;

public class BackglassManagerColumnSorter {

  private final BackglassManagerController backglassManagerController;

  public BackglassManagerColumnSorter(BackglassManagerController backglassManagerController) {
    this.backglassManagerController = backglassManagerController;
  }

  public Comparator<DirectB2SEntryModel> buildComparator(TableView<DirectB2SEntryModel> tableView) {

    Comparator<DirectB2SEntryModel> comp = null;

    if (!tableView.getSortOrder().isEmpty()) {
      TableColumn<DirectB2SEntryModel, ?> column = tableView.getSortOrder().get(0);

      if (column.equals(backglassManagerController.displayNameColumn)) {
        comp = Comparator.comparing(o -> o.getName());
      }
      else if (column.equals(backglassManagerController.statusColumn)) {
        comp = Comparator.comparing(o -> o.isVpxAvailable());
      }
      else if (column.equals(backglassManagerController.fullDmdColumn)) {
        comp = Comparator.comparing(o -> o.hasDmd());
      }
      else if (column.equals(backglassManagerController.grillColumn)) {
        comp = Comparator.comparing(o -> o.getGrillHeight() > 0);
      }
      else if (column.equals(backglassManagerController.scoreColumn)) {
        comp = Comparator.comparing(o -> o.getNbScores() > 0);
      }

      // optionally reverse order 
      if (comp != null && column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
        comp = comp.reversed();
      }
    }
    return comp;
  }
}

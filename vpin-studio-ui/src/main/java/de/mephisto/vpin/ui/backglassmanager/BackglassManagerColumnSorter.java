package de.mephisto.vpin.ui.backglassmanager;

import de.mephisto.vpin.ui.tables.panels.BaseColumnSorter;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Comparator;

public class BackglassManagerColumnSorter implements BaseColumnSorter<DirectB2SModel> {

  private final BackglassManagerController backglassManagerController;

  public BackglassManagerColumnSorter(BackglassManagerController backglassManagerController) {
    this.backglassManagerController = backglassManagerController;
  }

  @Override
  public Comparator<DirectB2SModel> buildComparator(TableView<DirectB2SModel> tableView) {

    Comparator<DirectB2SModel> comp = null;

    if (!tableView.getSortOrder().isEmpty()) {
      TableColumn<DirectB2SModel, ?> column = tableView.getSortOrder().get(0);

      if (column.equals(backglassManagerController.displayNameColumn)) {
        comp = Comparator.comparing(o -> o.getName());
      }
      else if (column.equals(backglassManagerController.statusColumn)) {
        comp = Comparator.comparing(o -> String.valueOf(o.getValidationCode()));
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
      else if (column.equals(backglassManagerController.resColumn)) {
        comp = Comparator.comparing(o -> o.getResPath() != null);
      }
      else if (column.equals(backglassManagerController.frameColumn)) {
        comp = Comparator.comparing(o -> o.getFramePath() != null);
      }

      // optionally reverse order 
      if (comp != null && column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
        comp = comp.reversed();
      }
    }
    return comp;
  }
}

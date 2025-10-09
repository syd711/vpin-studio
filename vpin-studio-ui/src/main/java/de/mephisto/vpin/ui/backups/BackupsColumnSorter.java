package de.mephisto.vpin.ui.backups;

import de.mephisto.vpin.ui.tables.panels.BaseColumnSorter;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Comparator;

public class BackupsColumnSorter implements BaseColumnSorter<BackupModel> {

  private final BackupsController tableOverviewController;

  public BackupsColumnSorter(BackupsController backupsController) {
    this.tableOverviewController = backupsController;
  }

  public Comparator<BackupModel> buildComparator(TableView<BackupModel> tableView) {
    Comparator<BackupModel> comp = null;

    if (!tableView.getSortOrder().isEmpty()) {
      TableColumn<BackupModel, ?> column = tableView.getSortOrder().get(0);

      if (column.equals(tableOverviewController.nameColumn)) {
        comp = Comparator.comparing(o -> o.getName().toLowerCase());
      }
      else if (column.equals(tableOverviewController.directB2SColumn)) {
        comp = Comparator.comparing(o -> o.getBean().getPackageInfo() != null && o.getBean().getPackageInfo().getDirectb2s() != null);
      }
      // optionally reverse order
      if (comp != null && column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
        comp = comp.reversed();
      }
    }
    return comp;
  }
}

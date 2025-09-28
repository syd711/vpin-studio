package de.mephisto.vpin.ui.backups;

import de.mephisto.vpin.restclient.backups.BackupDescriptorRepresentation;
import de.mephisto.vpin.ui.tables.panels.BaseColumnSorter;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Comparator;

public class BackupsColumnSorter implements BaseColumnSorter<BackupDescriptorRepresentation> {

  private final BackupsController tableOverviewController;

  public BackupsColumnSorter(BackupsController backupsController) {
    this.tableOverviewController = backupsController;
  }

  public Comparator<BackupDescriptorRepresentation> buildComparator(TableView<BackupDescriptorRepresentation> tableView) {
    Comparator<BackupDescriptorRepresentation> comp = null;

    if (!tableView.getSortOrder().isEmpty()) {
      TableColumn<BackupDescriptorRepresentation, ?> column = tableView.getSortOrder().get(0);

      if (column.equals(tableOverviewController.nameColumn)) {
        comp = Comparator.comparing(o -> o.getFilename().toLowerCase());
      }
      // optionally reverse order
      if (comp != null && column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
        comp = comp.reversed();
      }
    }
    return comp;
  }
}

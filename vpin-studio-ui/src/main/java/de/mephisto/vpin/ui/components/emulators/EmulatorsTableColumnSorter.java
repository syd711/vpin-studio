package de.mephisto.vpin.ui.components.emulators;

import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.ui.tables.panels.BaseColumnSorter;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Comparator;

public class EmulatorsTableColumnSorter implements BaseColumnSorter<EmulatorModel> {

  private final EmulatorsTableController tableController;

  public EmulatorsTableColumnSorter(EmulatorsTableController tableController) {
    this.tableController = tableController;
  }

  @Override
  public Comparator<EmulatorModel> buildComparator(TableView<EmulatorModel> tableView) {
    Comparator<EmulatorModel> comp = null;
    if (!tableView.getSortOrder().isEmpty()) {
      TableColumn<EmulatorModel, ?> column = tableView.getSortOrder().get(0);

      if (column.equals(tableController.columnName)) {
        comp = Comparator.comparing(o -> o.getName());
      }
      else if (column.equals(tableController.columnGamesDir)) {
        comp = Comparator.comparing(o -> o.getGamesDirectory());
      }
      else if (column.equals(tableController.columnExtension)) {
        comp = Comparator.comparing(o -> o.getGameExt());
      }

      // optionally reverse order 
      if (comp != null && column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
        comp = comp.reversed();
      }
    }
    return comp;
  }
}

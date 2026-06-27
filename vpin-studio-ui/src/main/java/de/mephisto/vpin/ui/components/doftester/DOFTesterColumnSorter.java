package de.mephisto.vpin.ui.components.doftester;

import de.mephisto.vpin.ui.tables.GameRepresentationModel;
import de.mephisto.vpin.ui.tables.panels.BaseColumnSorter;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Comparator;

public class DOFTesterColumnSorter implements BaseColumnSorter<GameRepresentationModel> {

  private final DOFTesterController dofTesterController;

  public DOFTesterColumnSorter(DOFTesterController dofTesterController) {
    this.dofTesterController = dofTesterController;
  }

  @Override
  public Comparator<GameRepresentationModel> buildComparator(TableView<GameRepresentationModel> tableView) {
    Comparator<GameRepresentationModel> comp = null;

    if (!tableView.getSortOrder().isEmpty()) {
      TableColumn<GameRepresentationModel, ?> column = tableView.getSortOrder().getFirst();

      if (column.equals(dofTesterController.columnDisplayName)) {
        comp = Comparator.comparing(o -> o.getName());
      }

      // optionally reverse order 
      if (comp != null && column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
        comp = comp.reversed();
      }
    }
    return comp;
  }
}

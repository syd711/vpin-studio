package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.ui.tables.TableOverviewController.GameRepresentationModel;
import de.mephisto.vpin.ui.tables.panels.BaseColumnSorter;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Comparator;
import java.util.Date;

public class TableOverviewColumnSorter implements BaseColumnSorter<GameRepresentationModel> {

  private final TableOverviewController tableOverviewController;

  public TableOverviewColumnSorter(TableOverviewController tableOverviewController) {
    this.tableOverviewController = tableOverviewController;
  }

  public Comparator<GameRepresentationModel> buildComparator(TableView<GameRepresentationModel> tableView) {

    Comparator<GameRepresentationModel> comp = null;

    if (!tableView.getSortOrder().isEmpty()) {
      TableColumn<GameRepresentationModel, ?> column = tableView.getSortOrder().get(0);

      if (column.equals(tableOverviewController.columnDisplayName)) {
        comp = Comparator.comparing(o -> o.getGame().getGameDisplayName());
      }
      else if (column.equals(tableOverviewController.columnVersion)) {
        comp = Comparator.comparing(o -> String.valueOf(o.getGame().getVersion()));
      }
      else if (column.equals(tableOverviewController.columnStatus)) {
        comp = Comparator.comparing(o -> String.valueOf(o.getGame().getValidationState().getCode()));
      }
      else if (column.equals(tableOverviewController.columnEmulator)) {
        comp = Comparator.comparing(o -> o.getGame().getEmulatorId());
      }
      else if (column.equals(tableOverviewController.columnDateAdded)) {
        comp = (o1, o2) -> {
          if (o1.getGame().getDateAdded() == null) {
            return o2.getGame().getDateAdded() == null ? 0 : 1;
          }
          else {
            return o2.getGame().getDateAdded() == null ? -1 : o1.getGame().getDateAdded().compareTo(o2.getGame().getDateAdded());
          }
        };
      }
      else if (column.equals(tableOverviewController.columnDateModified)) {
        comp = (o1, o2) -> {
          if (o1.getGame().getModified() == null) {
            return o2.getGame().getModified() == null ? 0 : -1;
          }
          else {
            return o2.getGame().getModified() == null ? 1 : o1.getGame().getModified().compareTo(o2.getGame().getModified());
          }
        };
      }
      else if (column.equals(tableOverviewController.columnB2S)) {
        comp = Comparator.comparing(o -> o.getGame().getDirectB2SPath() != null);
      }
      else if (column.equals(tableOverviewController.columnVPS)) {
        comp = (o1, o2) -> (o1.getGame().getVpsUpdates().isEmpty()) ? -1 : 1;
      }
      else if (column.equals(tableOverviewController.columnPUPPack)) {
        comp = Comparator.comparing(o -> String.valueOf(o.getGame().getPupPackName()));
      }
      else if (column.equals(tableOverviewController.columnAltColor)) {
        comp = Comparator.comparing(o -> String.valueOf(o.getGame().getAltColorType()));
      }
      else if (column.equals(tableOverviewController.columnAltSound)) {
        comp = Comparator.comparing(o -> o.getGame().isAltSoundAvailable());
      }
      else if (column.equals(tableOverviewController.columnRom)) {
        comp = Comparator.comparing(o -> String.valueOf(o.getGame().getRom()));
      }
      else if (column.equals(tableOverviewController.columnPOV)) {
        comp = Comparator.comparing(o -> o.getGame().getPovPath() != null);
      }
      else if (column.equals(tableOverviewController.columnRES)) {
        comp = Comparator.comparing(o -> o.getGame().getResPath() != null);
      }
      else if (column.equals(tableOverviewController.columnINI)) {
        comp = Comparator.comparing(o -> o.getGame().getIniPath() != null);
      }
      else if (column.equals(tableOverviewController.columnHSType)) {
        comp = Comparator.comparing(o -> String.valueOf(o.getGame().getHighscoreType()));
      }
      else if (column.equals(tableOverviewController.columnLauncher)) {
        comp = Comparator.comparing(o -> String.valueOf(o.getGame().getLauncher()));
      }

      // optionally reverse order 
      if (comp != null && column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
        comp = comp.reversed();
      }
    }
    return comp;
  }
}

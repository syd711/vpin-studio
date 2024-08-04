package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.ui.tables.TableOverviewController.GameRepresentationModel;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Comparator;
import java.util.Date;

public class TableOverviewColumnSorter {

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
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          comp = comp.reversed();
        }
      }
      else if (column.equals(tableOverviewController.columnVersion)) {
        comp = Comparator.comparing(o -> String.valueOf(o.getGame().getVersion()));
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          comp = comp.reversed();
        }
      }
      else if (column.equals(tableOverviewController.columnStatus)) {
        comp = Comparator.comparing(o -> String.valueOf(o.getGame().getValidationState().getCode()));
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          comp = comp.reversed();
        }
      }
      else if (column.equals(tableOverviewController.columnEmulator)) {
        comp = Comparator.comparing(o -> o.getGame().getEmulatorId());
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          comp = comp.reversed();
        }
      }
      else if (column.equals(tableOverviewController.columnDateAdded)) {
        comp = (o1, o2) -> {
          Date date1 = o1.getGame().getDateAdded() == null ? new Date() : o1.getGame().getDateAdded();
          Date date2 = o2.getGame().getDateAdded() == null ? new Date() : o2.getGame().getDateAdded();
          return date1.compareTo(date2);
        };
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          comp = comp.reversed();
        }
      }
      else if (column.equals(tableOverviewController.columnB2S)) {
        comp = Comparator.comparing(o -> o.getGame().getDirectB2SPath() != null);
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          comp = comp.reversed();
        }
      }
      else if (column.equals(tableOverviewController.columnVPS)) {
        comp = (o1, o2) -> {
          if (o1.getGame().getVpsUpdates().isEmpty()) {
            return -1;
          }
          return 1;
        };
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          comp = comp.reversed();
        }
      }
      else if (column.equals(tableOverviewController.columnPUPPack)) {
        comp = Comparator.comparing(o -> String.valueOf(o.getGame().getPupPackName()));
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          comp = comp.reversed();
        }
      }
      else if (column.equals(tableOverviewController.columnAltColor)) {
        comp = Comparator.comparing(o -> String.valueOf(o.getGame().getAltColorType()));
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          comp = comp.reversed();
        }
      }
      else if (column.equals(tableOverviewController.columnAltSound)) {
        comp = Comparator.comparing(o -> o.getGame().isAltSoundAvailable());
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          comp = comp.reversed();
        }
      }
      else if (column.equals(tableOverviewController.columnRom)) {
        comp = Comparator.comparing(o -> String.valueOf(o.getGame().getRom()));
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          comp = comp.reversed();
        }
      }
      else if (column.equals(tableOverviewController.columnPOV)) {
        comp = Comparator.comparing(o -> o.getGame().getPovPath() != null);
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          comp = comp.reversed();
        }
      }
      else if (column.equals(tableOverviewController.columnRES)) {
        comp = Comparator.comparing(o -> o.getGame().getResPath() != null);
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          comp = comp.reversed();
        }
      }
      else if (column.equals(tableOverviewController.columnINI)) {
        comp = Comparator.comparing(o -> o.getGame().getIniPath() != null);
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          comp = comp.reversed();
        }
      }
      else if (column.equals(tableOverviewController.columnHSType)) {
        comp = Comparator.comparing(o -> String.valueOf(o.getGame().getHighscoreType()));
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          comp = comp.reversed();
        }
      }
    }
    return comp;
  }
}

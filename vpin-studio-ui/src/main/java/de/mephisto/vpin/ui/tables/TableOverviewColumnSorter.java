package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class TableOverviewColumnSorter {

  private final TableOverviewController tableOverviewController;

  public TableOverviewColumnSorter(TableOverviewController tableOverviewController) {
    this.tableOverviewController = tableOverviewController;
  }

  public Boolean sort(TableView<GameRepresentation> tableView) {
    GameRepresentation selectedItem = tableView.getSelectionModel().getSelectedItem();
    if (!tableView.getSortOrder().isEmpty()) {
      TableColumn<GameRepresentation, ?> column = tableView.getSortOrder().get(0);
      if (column.equals(tableOverviewController.columnDisplayName)) {
        Collections.sort(tableView.getItems(), Comparator.comparing(o -> o.getGameDisplayName()));
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          Collections.reverse(tableView.getItems());
        }
        return true;
      }
      else if (column.equals(tableOverviewController.columnVersion)) {
        Collections.sort(tableView.getItems(), Comparator.comparing(o -> String.valueOf(o.getVersion())));
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          Collections.reverse(tableView.getItems());
        }
        return true;
      }
      else if (column.equals(tableOverviewController.columnStatus)) {
        Collections.sort(tableView.getItems(), Comparator.comparing(o -> String.valueOf(o.getValidationState().getCode())));
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          Collections.reverse(tableView.getItems());
        }
        return true;
      }
      else if (column.equals(tableOverviewController.columnEmulator)) {
        Collections.sort(tableView.getItems(), Comparator.comparing(o -> o.getEmulatorId()));
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          Collections.reverse(tableView.getItems());
        }
        return true;
      }
      else if (column.equals(tableOverviewController.columnDateAdded)) {
        Collections.sort(tableView.getItems(), (o1, o2) -> {
          Date date1 = o1.getDateAdded() == null ? new Date() : o1.getDateAdded();
          Date date2 = o2.getDateAdded() == null ? new Date() : o2.getDateAdded();
          return date1.compareTo(date2);
        });
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          Collections.reverse(tableView.getItems());
        }
        return true;
      }
      else if (column.equals(tableOverviewController.columnB2S)) {
        Collections.sort(tableView.getItems(), Comparator.comparing(GameRepresentation::isDirectB2SAvailable));
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          Collections.reverse(tableView.getItems());
        }
        return true;
      }
      else if (column.equals(tableOverviewController.columnVPS)) {
        Collections.sort(tableView.getItems(), (o1, o2) -> {
          if (o1.getVpsUpdates().isEmpty()) {
            return -1;
          }
          return 1;
        });
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          Collections.reverse(tableView.getItems());
        }
        return true;
      }
      else if (column.equals(tableOverviewController.columnPUPPack)) {
        Collections.sort(tableView.getItems(), Comparator.comparing(o -> String.valueOf(o.getPupPackName())));
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          Collections.reverse(tableView.getItems());
        }
        return true;
      }
      else if (column.equals(tableOverviewController.columnAltColor)) {
        Collections.sort(tableView.getItems(), Comparator.comparing(o -> String.valueOf(o.getAltColorType())));
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          Collections.reverse(tableView.getItems());
        }
        return true;
      }
      else if (column.equals(tableOverviewController.columnAltSound)) {
        Collections.sort(tableView.getItems(), Comparator.comparing(GameRepresentation::isAltSoundAvailable));
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          Collections.reverse(tableView.getItems());
        }
        return true;
      }
      else if (column.equals(tableOverviewController.columnRom)) {
        Collections.sort(tableView.getItems(), Comparator.comparing(o -> String.valueOf(o.getRom())));
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          Collections.reverse(tableView.getItems());
        }
        return true;
      }
      else if (column.equals(tableOverviewController.columnPOV)) {
        Collections.sort(tableView.getItems(), Comparator.comparing(GameRepresentation::isPovAvailable));
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          Collections.reverse(tableView.getItems());
        }
        return true;
      }
      else if (column.equals(tableOverviewController.columnHSType)) {
        Collections.sort(tableView.getItems(), Comparator.comparing(o -> String.valueOf(o.getHighscoreType())));
        if (column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
          Collections.reverse(tableView.getItems());
        }
        return true;
      }
    }
    return true;
  }
}

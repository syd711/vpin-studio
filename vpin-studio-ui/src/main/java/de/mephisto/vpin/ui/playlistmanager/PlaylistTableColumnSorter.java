package de.mephisto.vpin.ui.playlistmanager;

import de.mephisto.vpin.ui.tables.GameRepresentationModel;
import de.mephisto.vpin.ui.tables.panels.BaseColumnSorter;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Comparator;

public class PlaylistTableColumnSorter implements BaseColumnSorter<GameRepresentationModel> {

  private final PlaylistTableController playlistTableController;

  public PlaylistTableColumnSorter(PlaylistTableController playlistTableController) {
    this.playlistTableController = playlistTableController;
  }

  @Override
  public Comparator<GameRepresentationModel> buildComparator(TableView<GameRepresentationModel> tableView) {
    Comparator<GameRepresentationModel> comp = null;
    if (!tableView.getSortOrder().isEmpty()) {
      TableColumn<GameRepresentationModel, ?> column = tableView.getSortOrder().get(0);

      if (column.equals(playlistTableController.columnName)) {
        comp = Comparator.comparing(o -> o.getName());
      }
      else if (column.equals(playlistTableController.columnEmulator)) {
        comp = Comparator.comparing(o -> o.getGameEmulator().getName());
      }

      // optionally reverse order 
      if (comp != null && column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
        comp = comp.reversed();
      }
    }
    return comp;
  }
}

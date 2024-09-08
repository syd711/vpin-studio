package de.mephisto.vpin.ui.vps;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Comparator;
import java.util.Date;

import org.apache.commons.collections4.CollectionUtils;

import de.mephisto.vpin.ui.tables.panels.BaseColumnSorter;
import de.mephisto.vpin.ui.vps.VpsTablesController.VpsTableModel;

public class VpsTablesColumnSorter implements BaseColumnSorter<VpsTableModel> {

  private final VpsTablesController vpsTablesController;

  public VpsTablesColumnSorter(VpsTablesController vpsTablesController) {
    this.vpsTablesController = vpsTablesController;
  }

  public Comparator<VpsTableModel> buildComparator(TableView<VpsTableModel> tableView) {

    Comparator<VpsTableModel> comp = null;

    if (!tableView.getSortOrder().isEmpty()) {
      TableColumn<VpsTableModel, ?> column = tableView.getSortOrder().get(0);

      if (column.equals(vpsTablesController.installedColumn)) {
        comp = Comparator.comparing(o -> !o.isInstalled());
      }
      else if (column.equals(vpsTablesController.nameColumn)) {
        comp = Comparator.comparing(o -> o.getVpsTable().getDisplayName());
      }
      else if (column.equals(vpsTablesController.versionsColumn)) {
        comp = Comparator.comparing(o -> CollectionUtils.size(o.getVpsTable().getTableFiles()));
      }
      else if (column.equals(vpsTablesController.statusColumn)) {
        comp = Comparator.comparing(o -> !o.isInstalled());
      }
      else if (column.equals(vpsTablesController.directB2SColumn)) {
        comp = Comparator.comparing(o -> VpsUtil.isDataAvailable(o.getVpsTable().getB2sFiles()));
      }
      else if (column.equals(vpsTablesController.pupPackColumn)) {
        comp = Comparator.comparing(o -> VpsUtil.isDataAvailable(o.getVpsTable().getPupPackFiles()));
      }
      else if (column.equals(vpsTablesController.romColumn)) {
        comp = Comparator.comparing(o -> VpsUtil.isDataAvailable(o.getVpsTable().getRomFiles()));
      }
      else if (column.equals(vpsTablesController.topperColumn)) {
        comp = Comparator.comparing(o -> VpsUtil.isDataAvailable(o.getVpsTable().getTopperFiles()));
      }
      else if (column.equals(vpsTablesController.povColumn)) {
        comp = Comparator.comparing(o -> VpsUtil.isDataAvailable(o.getVpsTable().getPovFiles()));
      }
      else if (column.equals(vpsTablesController.altSoundColumn)) {
        comp = Comparator.comparing(o -> VpsUtil.isDataAvailable(o.getVpsTable().getAltSoundFiles()));
      }
      else if (column.equals(vpsTablesController.altColorColumn)) {
        comp = Comparator.comparing(o -> VpsUtil.isDataAvailable(o.getVpsTable().getAltColorFiles()));
      }
      else if (column.equals(vpsTablesController.tutorialColumn)) {
        comp = Comparator.comparing(o -> VpsUtil.isDataAvailable(o.getVpsTable().getTutorialFiles()));
      }
      else if (column.equals(vpsTablesController.updatedColumn)) {
        comp = Comparator.comparing(o -> new Date(o.getVpsTable().getUpdatedAt()));
      }

      // optionally reverse order 
      if (comp != null && column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
        comp = comp.reversed();
      }
    }
    return comp;
  }
}

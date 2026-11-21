package de.mephisto.vpin.ui.backups;

import de.mephisto.vpin.restclient.backups.BackupPackageInfo;
import de.mephisto.vpin.ui.tables.panels.BaseColumnSorter;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Comparator;
import java.util.function.Function;

public class BackupsColumnSorter implements BaseColumnSorter<BackupModel> {

  private final BackupsController tableOverviewController;

  public BackupsColumnSorter(BackupsController backupsController) {
    this.tableOverviewController = backupsController;
  }

  public Comparator<BackupModel> buildComparator(TableView<BackupModel> tableView) {
    Comparator<BackupModel> comp = null;

    if (!tableView.getSortOrder().isEmpty()) {
      TableColumn<BackupModel, ?> column = tableView.getSortOrder().getFirst();
      BackupsController c = tableOverviewController;

      if (column.equals(c.nameColumn)) {
        comp = Comparator.comparing(o -> o.getName().toLowerCase());
      }
      else if (column.equals(c.directB2SColumn)) {
        comp = hasFile(p -> p.getDirectb2s());
      }
      else if (column.equals(c.iniColumn)) {
        comp = hasFile(p -> p.getIni() != null ? p.getIni() : p.getVrIni());
      }
      else if (column.equals(c.vbsColumn)) {
        comp = hasFile(p -> p.getVbs());
      }
      else if (column.equals(c.resColumn)) {
        comp = hasFile(p -> p.getRes());
      }
      else if (column.equals(c.povColumn)) {
        comp = hasFile(p -> p.getPov());
      }
      else if (column.equals(c.pupPackColumn)) {
        comp = hasFile(p -> p.getPupPack());
      }
      else if (column.equals(c.popperColumn)) {
        comp = hasFile(p -> p.getPopperMedia());
      }
      else if (column.equals(c.romColumn)) {
        comp = hasFile(p -> p.getRom());
      }
      else if (column.equals(c.altSoundColumn)) {
        comp = hasFile(p -> p.getAltSound());
      }
      else if (column.equals(c.altColorColumn)) {
        comp = hasFile(p -> p.getAltColor());
      }
      else if (column.equals(c.musicColumn)) {
        comp = hasFile(p -> p.getMusic());
      }
      else if (column.equals(c.dmdColumn)) {
        comp = hasFile(p -> p.getDmd());
      }
      else if (column.equals(c.registryColumn)) {
        comp = hasFile(p -> p.getMameData());
      }
      else if (column.equals(c.highscoreColumn)) {
        comp = hasFile(p -> p.getHighscore());
      }
      else if (column.equals(c.sizeColumn)) {
        comp = Comparator.comparingLong(o -> o.getBean().getSize());
      }
      else if (column.equals(c.createdAtColumn)) {
        comp = Comparator.comparing(o -> o.getBean().getCreatedAt(), Comparator.nullsFirst(Comparator.naturalOrder()));
      }
      // optionally reverse order
      if (comp != null && column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
        comp = comp.reversed();
      }
    }
    return comp;
  }

  private static Comparator<BackupModel> hasFile(Function<BackupPackageInfo, Object> getter) {
    return Comparator.comparing(o -> {
      BackupPackageInfo info = o.getBean().getPackageInfo();
      return info != null && getter.apply(info) != null;
    });
  }
}

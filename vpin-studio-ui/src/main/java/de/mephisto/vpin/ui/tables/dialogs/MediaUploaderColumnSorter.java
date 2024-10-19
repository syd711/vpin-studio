package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.ui.tables.panels.BaseColumnSorter;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Comparator;

public class MediaUploaderColumnSorter implements BaseColumnSorter<MediaUploadController.ArchiveItem> {

  private final MediaUploadController mediaUploadController;

  public MediaUploaderColumnSorter(MediaUploadController mediaUploadController) {
    this.mediaUploadController = mediaUploadController;
  }

  @Override
  public Comparator<MediaUploadController.ArchiveItem> buildComparator(TableView<MediaUploadController.ArchiveItem> tableView) {
    Comparator<MediaUploadController.ArchiveItem> comp = null;
    if (!tableView.getSortOrder().isEmpty()) {
      TableColumn<MediaUploadController.ArchiveItem, ?> column = tableView.getSortOrder().get(0);

      if (column.equals(mediaUploadController.columnAssetType)) {
        comp = Comparator.comparing(o -> o.getAssetType());
      }
      else if (column.equals(mediaUploadController.columnFilename)) {
        comp = Comparator.comparing(o -> o.getName());
      }
      else if (column.equals(mediaUploadController.columnTarget)) {
        comp = Comparator.comparing(o -> o.getTarget());
      }

      // optionally reverse order 
      if (comp != null && column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
        comp = comp.reversed();
      }
    }
    return comp;
  }
}

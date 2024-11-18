package de.mephisto.vpin.ui.recorder;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.validation.ValidationSettings;
import de.mephisto.vpin.ui.tables.GameRepresentationModel;
import de.mephisto.vpin.ui.tables.panels.BaseColumnSorter;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Comparator;

import static de.mephisto.vpin.ui.Studio.client;

public class RecorderColumnSorter implements BaseColumnSorter<GameRepresentationModel> {

  private final RecorderController recorderController;

  public RecorderColumnSorter(RecorderController recorderController) {
    this.recorderController = recorderController;
  }

  public Comparator<GameRepresentationModel> buildComparator(TableView<GameRepresentationModel> tableView) {
    Comparator<GameRepresentationModel> comp = null;
    ValidationSettings validationSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.VALIDATION_SETTINGS, ValidationSettings.class);

    if (!tableView.getSortOrder().isEmpty()) {
      TableColumn<GameRepresentationModel, ?> column = tableView.getSortOrder().get(0);

      if (column.equals(recorderController.columnDisplayName)) {
        comp = Comparator.comparing(o -> o.getGame().getGameDisplayName());
      }      
      else {
        VPinScreen screen = recorderController.screenFromColumn(column);
        if (screen != null) {
          comp = Comparator.comparing(o -> o.getStatusColor(screen, validationSettings));
        }
      }

      // optionally reverse order 
      if (comp != null && column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
        comp = comp.reversed();
      }
    }
    return comp;
  }
}

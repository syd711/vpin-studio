package de.mephisto.vpin.ui.cards;

import de.mephisto.vpin.ui.tables.GameRepresentationModel;
import de.mephisto.vpin.ui.tables.panels.BaseColumnSorter;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Comparator;

public class HighscoreCardsColumnSorter implements BaseColumnSorter<GameRepresentationModel> {

  private final HighscoreCardsController HighscoreCardsController;

  public HighscoreCardsColumnSorter(HighscoreCardsController HighscoreCardsController) {
    this.HighscoreCardsController = HighscoreCardsController;
  }

  @Override
  public Comparator<GameRepresentationModel> buildComparator(TableView<GameRepresentationModel> tableView) {

    Comparator<GameRepresentationModel> comp = null;

    if (!tableView.getSortOrder().isEmpty()) {
      TableColumn<GameRepresentationModel, ?> column = tableView.getSortOrder().get(0);

      if (column.equals(HighscoreCardsController.columnDisplayName)) {
        comp = Comparator.comparing(o -> o.getName());
      }
      else if (column.equals(HighscoreCardsController.columnTemplate)) {
        comp = Comparator.comparing(o -> String.valueOf(o.getGame().getTemplateId()));
      }
      else if (column.equals(HighscoreCardsController.columnStatus)) {
        comp = Comparator.comparing(o -> String.valueOf(o.getGame().getValidationState().getCode()));
      }

      // optionally reverse order 
      if (comp != null && column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
        comp = comp.reversed();
      }
    }
    return comp;
  }
}

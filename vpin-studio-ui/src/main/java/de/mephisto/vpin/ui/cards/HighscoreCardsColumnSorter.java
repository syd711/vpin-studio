package de.mephisto.vpin.ui.cards;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.ui.tables.GameRepresentationModel;
import de.mephisto.vpin.ui.tables.panels.BaseColumnSorter;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Comparator;

public class HighscoreCardsColumnSorter implements BaseColumnSorter<GameRepresentationModel> {

  private final HighscoreCardsController highscoreCardsController;

  public HighscoreCardsColumnSorter(HighscoreCardsController highscoreCardsController) {
    this.highscoreCardsController = highscoreCardsController;
  }

  @Override
  public Comparator<GameRepresentationModel> buildComparator(TableView<GameRepresentationModel> tableView) {
    Comparator<GameRepresentationModel> comp = null;

    if (!tableView.getSortOrder().isEmpty()) {
      TableColumn<GameRepresentationModel, ?> column = tableView.getSortOrder().get(0);

      if (column.equals(highscoreCardsController.columnDisplayName)) {
        comp = Comparator.comparing(o -> o.getName());
      }
      else if (column.equals(highscoreCardsController.columnTemplate)) {
        comp = Comparator.comparing(o -> {
          CardTemplate template = highscoreCardsController.getCardTemplateForGame(o.getGame());
          return template != null ? String.valueOf(template.isTemplate()) : "";
        });
      }
      else if (column.equals(highscoreCardsController.columnBaseTemplate)) {
        comp = Comparator.comparing(o -> {
          CardTemplate template = highscoreCardsController.getBaseCardTemplateForGame(o.getGame());
          return template != null ? template.getName() : "";
        });
      }

      // optionally reverse order 
      if (comp != null && column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
        comp = comp.reversed();
      }
    }
    return comp;
  }
}

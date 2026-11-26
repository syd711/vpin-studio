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
      TableColumn<GameRepresentationModel, ?> column = tableView.getSortOrder().getFirst();

      if (column.equals(highscoreCardsController.columnDisplayName)) {
        comp = Comparator.comparing(o -> o.getName());
      }
      else if (column.equals(highscoreCardsController.columnBaseTemplate)) {
        // custom designs first, then by template name
        comp = Comparator.comparing(o -> {
          CardTemplate template = highscoreCardsController.getCardTemplateForGame(o.getGame());
          if (template != null && !template.isTemplate()) {
            return "";
          }
          CardTemplate baseTemplate = highscoreCardsController.getBaseCardTemplateForGame(o.getGame());
          return baseTemplate != null ? baseTemplate.getName() : CardTemplate.DEFAULT;
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

package de.mephisto.vpin.ui.cards;

import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.ui.tables.GameRepresentationModel;
import de.mephisto.vpin.ui.tables.panels.BaseColumnSorter;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Comparator;

import static de.mephisto.vpin.ui.Studio.client;

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
        comp = Comparator.comparing(o -> {
          CardTemplate templateById = client.getHighscoreCardTemplatesClient().getTemplateById(o.getGame().getTemplateId());
          return String.valueOf(templateById.isTemplate());
        });
      }
      else if (column.equals(HighscoreCardsController.columnBaseTemplate)) {
        comp = Comparator.comparing(o -> {
          CardTemplate templateById = client.getHighscoreCardTemplatesClient().getTemplateById(o.getGame().getTemplateId());
          String value = null;
          if (templateById.isTemplate()) {
            value = client.getHighscoreCardTemplatesClient().getTemplateById(templateById.getParentId()).getName();
          }
          return String.valueOf(value);
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

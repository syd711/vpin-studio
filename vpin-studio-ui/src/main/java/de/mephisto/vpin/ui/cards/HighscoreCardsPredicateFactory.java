package de.mephisto.vpin.ui.cards;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.playlists.PlaylistRepresentation;
import de.mephisto.vpin.ui.tables.GameRepresentationModel;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class HighscoreCardsPredicateFactory {

  private final List<Integer> emulatorIds = new ArrayList<>();

  //--------------- Filters

  public void selectEmulator(Integer id) {
    emulatorIds.add(id);
  }
  public void unselectEmulator(Integer id) {
    emulatorIds.remove(id);
  }

  /**
   * We need a new Predicate each time else TableView does not detect the changes
   */
  public Predicate<GameRepresentationModel> buildPredicate(String searchTerm, PlaylistRepresentation playlist) {
    return new Predicate<GameRepresentationModel>() {
      @Override
      public boolean test(GameRepresentationModel model) {
        GameRepresentation game = model.getBean();

        if (emulatorIds!=null && !emulatorIds.isEmpty() && !emulatorIds.contains(game.getEmulatorId())) {
          return false;
        }

        if (playlist != null && !playlist.containsGame(game.getId())) {
          return false;
        }

        if (StringUtils.isNotEmpty(searchTerm) && !StringUtils.containsIgnoreCase(model.getName(), searchTerm)) {
          return false;
        }

        // else not filtered
        return true;
      }

    };
  }

}

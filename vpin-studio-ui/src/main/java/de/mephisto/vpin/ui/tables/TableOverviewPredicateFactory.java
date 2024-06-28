package de.mephisto.vpin.ui.tables;

import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.frontend.Playlist;
import de.mephisto.vpin.ui.tables.TableOverviewController.GameRepresentationModel;

public class TableOverviewPredicateFactory {

  private String filterValue;

  private List<Integer> filteredIds;

  private Playlist playlist;

  GameEmulatorRepresentation emulator;

  public void setFilterTerm(String filterTerm) {
    this.filterValue = filterTerm;
  }

  public void setFilterIds(List<Integer> filterIds) {
    this.filteredIds = filterIds;
  }

  public void setFilterPlaylist(Playlist playlist) {
    this.playlist = playlist;
  }

  public void setFilterEmulator(GameEmulatorRepresentation emulator) {
    this.emulator = emulator;
  }

  /**
   * We need a new Predicate each time else TableView does not detect the changes
   */
  public Predicate<GameRepresentationModel> buildPredicate() {
    return new Predicate<GameRepresentationModel>() {
      @Override
      public boolean test(GameRepresentationModel model) {
        GameRepresentation game = model.getGame();

        if (filteredIds != null && !filteredIds.isEmpty() && !filteredIds.contains(game.getId())) {
          return false;
        }

        if (emulator != null && game.getEmulatorId() != emulator.getId()) {
          return false;
        }
        if (emulator != null && !game.isVpxGame()) {
          return false;
        }

        if (playlist != null && !playlist.containsGame(game.getId())) {
          return false;
        }

        if (StringUtils.isNotEmpty(filterValue)
            && !StringUtils.containsIgnoreCase(game.getGameDisplayName(), filterValue)
            && !StringUtils.containsIgnoreCase(game.getRom(), filterValue)) {
          return false;
        }

        // else not filtered
        return true;
      }
    };
  }

}

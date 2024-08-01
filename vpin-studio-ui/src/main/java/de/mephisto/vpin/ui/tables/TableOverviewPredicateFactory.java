package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.PlaylistRepresentation;
import de.mephisto.vpin.ui.tables.TableOverviewController.GameRepresentationModel;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.function.Predicate;

public class TableOverviewPredicateFactory {

  private String filterValue;

  private List<Integer> filteredIds;

  private PlaylistRepresentation playlist;

  GameEmulatorRepresentation emulator;

  public void setFilterTerm(String filterTerm) {
    this.filterValue = filterTerm;
  }

  public void setFilterIds(List<Integer> filterIds) {
    this.filteredIds = filterIds;
  }

  public void setFilterPlaylist(PlaylistRepresentation playlist) {
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

        if (filteredIds != null && !filteredIds.contains(game.getId())) {
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
            && !StringUtils.containsIgnoreCase(String.valueOf(game.getId()), filterValue)
            && !StringUtils.containsIgnoreCase(game.getRom(), filterValue)) {
          return false;
        }

        // else not filtered
        return true;
      }
    };
  }

}

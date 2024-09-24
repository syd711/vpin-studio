package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.games.FilterSettings;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.NoteType;
import de.mephisto.vpin.restclient.games.PlaylistRepresentation;
import de.mephisto.vpin.ui.tables.TableOverviewController.GameRepresentationModel;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Predicate;

public class TableOverviewPredicateFactory {

  private PlaylistRepresentation playlist;

  private GameEmulatorRepresentation emulator;

  public void setFilterPlaylist(PlaylistRepresentation playlist) {
    this.playlist = playlist;
  }

  public void setFilterEmulator(GameEmulatorRepresentation emulator) {
    this.emulator = emulator;
  }

  /**
   * We need a new Predicate each time else TableView does not detect the changes
   */
  public Predicate<GameRepresentationModel> buildPredicate(String searchTerm, FilterSettings filterSettings) {
    return new Predicate<GameRepresentationModel>() {
      @Override
      public boolean test(GameRepresentationModel model) {
        GameRepresentation game = model.getGame();

        boolean vpxGame = game.isVpxGame();
        if (vpxGame) {
          if (filterSettings.isNoHighscoreSettings() && (!StringUtils.isEmpty(game.getRom()) || !StringUtils.isEmpty(game.getHsFileName()) || !StringUtils.isEmpty(game.getHsFileName()))) {
            return false;
          }
          if (filterSettings.isWithNVOffset() && game.getNvOffset() == 0) {
            return false;
          }
          if (filterSettings.isWithAlias() && StringUtils.isEmpty(game.getRomAlias())) {
            return false;
          }
          if (filterSettings.isWithAltSound() && !game.isAltSoundAvailable()) {
            return false;
          }
          if (filterSettings.isWithAltColor() && game.getAltColorType() == null) {
            return false;
          }
          if (filterSettings.isWithBackglass() && game.getDirectB2SPath() == null) {
            return false;
          }
          if (filterSettings.isWithPupPack() && game.getPupPackPath() == null) {
            return false;
          }
          if (filterSettings.isWithIni() && game.getIniPath() == null) {
            return false;
          }
          if (filterSettings.isWithPov() && game.getPovPath() == null) {
            return false;
          }
          if (filterSettings.isWithRes() && game.getResPath() == null) {
            return false;
          }
          if (filterSettings.isVpsUpdates() && game.getVpsUpdates() != null && game.getVpsUpdates().isEmpty()) {
            return false;
          }
          if (filterSettings.isVersionUpdates() && !game.isUpdateAvailable()) {
            return false;
          }
        }

        NoteType noteType = filterSettings.getNoteType();
        if (noteType != null) {
          if (noteType.equals(NoteType.Any) && StringUtils.isEmpty(game.getNotes())) {
            return false;
          }
          if (noteType.equals(NoteType.Errors) && (StringUtils.isEmpty(game.getNotes()) || !game.getNotes().contains("//ERROR"))) {
            return false;
          }
          if (noteType.equals(NoteType.Outdated) && (StringUtils.isEmpty(game.getNotes()) || !game.getNotes().contains("//OUTDATED"))) {
            return false;
          }
          if (noteType.equals(NoteType.Todos) && (StringUtils.isEmpty(game.getNotes()) || !game.getNotes().contains("//TODO"))) {
            return false;
          }
        }

        if (filterSettings.isMissingAssets() && !game.isHasMissingAssets()) {
          return false;
        }

        if (vpxGame) {
          if (filterSettings.isOtherIssues() && !game.isHasOtherIssues()) {
            return false;
          }
          if (filterSettings.isNoVpsTableMapping() && !StringUtils.isEmpty(game.getExtTableId())) {
            return false;
          }
          if (filterSettings.isNoVpsVersionMapping() && !StringUtils.isEmpty(game.getExtTableVersionId())) {
            return false;
          }

          if (filterSettings.isNotPlayed() && game.isPlayed()) {
            return false;
          }

          if (filterSettings.getGameStatus() != -1 && game.getGameStatus() != filterSettings.getGameStatus()) {
            return false;
          }

          if (filterSettings.isNoHighscoreSupport() && game.isValidScoreConfiguration()) {
            return false;
          }
        }

        //--------------------------

        if (emulator != null && game.getEmulatorId() != emulator.getId()) {
          return false;
        }
        if (emulator != null && !game.isVpxGame()) {
          return false;
        }

        if (playlist != null && !playlist.containsGame(game.getId())) {
          return false;
        }

        if (StringUtils.isNotEmpty(searchTerm)
            && !StringUtils.containsIgnoreCase(game.getGameDisplayName(), searchTerm)
            && !StringUtils.containsIgnoreCase(String.valueOf(game.getId()), searchTerm)
            && !StringUtils.containsIgnoreCase(game.getRom(), searchTerm)) {
          return false;
        }

        // else not filtered
        return true;
      }
    };
  }

}

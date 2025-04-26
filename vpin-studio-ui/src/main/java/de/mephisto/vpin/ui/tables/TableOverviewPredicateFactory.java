package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.connectors.vps.model.VPSChange;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.*;
import de.mephisto.vpin.restclient.playlists.PlaylistRepresentation;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.ui.tables.vps.VpsTableColumn;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Predicate;

public class TableOverviewPredicateFactory {
  /**
   * We need a new Predicate each time else TableView does not detect the changes
   */
  public Predicate<GameRepresentationModel> buildPredicate(String searchTerm, PlaylistRepresentation playlist, GameEmulatorRepresentation emulator, FilterSettings filterSettings, UISettings uiSettings) {
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
          if (filterSettings.isWithIni() && game.getIniPath() == null) {
            return false;
          }
          if (filterSettings.isWithPov() && game.getPovPath() == null) {
            return false;
          }
          if (filterSettings.isIScored() && game.getCompetitionTypes().isEmpty()) {
            return false;
          }
        }

        if (filterSettings.isWithBackglass() && game.getDirectB2SPath() == null) {
          return false;
        }
        if (filterSettings.isWithRes() && game.getResPath() == null) {
          return false;
        }

        if (filterSettings.isWithPupPack() && game.getPupPackPath() == null) {
          return false;
        }

        if (filterSettings.isVpsUpdates() && (StringUtils.isEmpty(game.getExtTableId()) || game.getVpsUpdates() == null || game.getVpsUpdates().isEmpty())) {
          return false;
        }

        if (filterSettings.isVpsUpdates() && game.getVpsUpdates() != null) {
          for (VPSChange change : game.getVpsUpdates().getChanges()) {
            if (!VpsTableColumn.isFiltered(uiSettings, change)) {
              continue;
            }
            return false;
          }
        }

        if (filterSettings.isVersionUpdates() && !game.isUpdateAvailable()) {
          return false;
        }

        CommentType noteType = filterSettings.getNoteType();
        if (noteType != null) {
          if (noteType.equals(CommentType.None) && !StringUtils.isEmpty(game.getComment())) {
            return false;
          }
          if (noteType.equals(CommentType.Any) && StringUtils.isEmpty(game.getComment())) {
            return false;
          }
          if (noteType.equals(CommentType.Errors) && (StringUtils.isEmpty(game.getComment()) || !game.getComment().toLowerCase().contains("//error"))) {
            return false;
          }
          if (noteType.equals(CommentType.Outdated) && (StringUtils.isEmpty(game.getComment()) || !game.getComment().toLowerCase().contains("//outdated"))) {
            return false;
          }
          if (noteType.equals(CommentType.Todos) && (StringUtils.isEmpty(game.getComment()) || !game.getComment().toLowerCase().contains("//todo"))) {
            return false;
          }
        }

        if (filterSettings.isMissingAssets() && !game.isHasMissingAssets()) {
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

        if (vpxGame) {
          if (filterSettings.isOtherIssues() && !game.isHasOtherIssues()) {
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

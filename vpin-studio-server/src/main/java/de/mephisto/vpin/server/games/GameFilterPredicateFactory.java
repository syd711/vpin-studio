package de.mephisto.vpin.server.games;

import de.mephisto.vpin.connectors.vps.model.VPSChange;
import de.mephisto.vpin.restclient.games.CommentType;
import de.mephisto.vpin.restclient.games.FilterSettings;
import de.mephisto.vpin.restclient.vps.VpsChangeFilter;
import de.mephisto.vpin.restclient.vps.VpsSettings;
import de.mephisto.vpin.server.backups.BackupService;
import de.mephisto.vpin.server.playlists.Playlist;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Predicate;

/**
 * Server-side, structural port of the UI's TableOverviewPredicateFactory (vpin-studio-ui),
 * operating on the server's Game entity instead of the client's GameRepresentation DTO.
 * Keep the condition order in sync with that class when either one changes.
 */
public class GameFilterPredicateFactory {

  public Predicate<Game> buildPredicate(String searchTerm, Playlist playlist, Integer emulatorId, FilterSettings filterSettings, VpsSettings vpsSettings, BackupService backupService) {
    return new Predicate<Game>() {
      @Override
      public boolean test(Game game) {
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

        if (!filterSettings.getTags().isEmpty()) {
          if (game.getTags().isEmpty()) {
            return false;
          }
          for (String tag : filterSettings.getTags()) {
            if (!game.getTags().contains(tag)) {
              return false;
            }
          }
        }

        if (filterSettings.isWithBackglass() && game.getDirectB2SPath() == null) {
          return false;
        }
        if (filterSettings.isWithRes() && game.getResPath() == null) {
          return false;
        }

        if (filterSettings.isWithPupPack() && game.getPupPackName() == null) {
          return false;
        }

        if (filterSettings.isVpsUpdates() && (StringUtils.isEmpty(game.getExtTableId()) || game.getVpsUpdates() == null || game.getVpsUpdates().isEmpty())) {
          return false;
        }

        if (filterSettings.isVpsUpdates() && game.getVpsUpdates() != null) {
          boolean hasVisibleUpdate = false;
          for (VPSChange change : game.getVpsUpdates().getChanges()) {
            if (!VpsChangeFilter.isFiltered(vpsSettings, change)) {
              hasVisibleUpdate = true;
              break;
            }
          }
          if (!hasVisibleUpdate) {
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

        if (filterSettings.isNotBackedUp() && !backupService.getBackupDescriptorForGame(game.getId()).isEmpty()) {
          return false;
        }

        if (filterSettings.getGameStatus() != -1 && game.getGameStatus() != filterSettings.getGameStatus()) {
          return false;
        }

        if (vpxGame) {
          if (filterSettings.getIssueType() != -1 && !game.getIssueTypes().contains(filterSettings.getIssueType())) {
            return false;
          }

          if (filterSettings.isNoHighscoreSupport() && game.isValidScoreConfiguration()) {
            return false;
          }
        }

        //--------------------------

        if (emulatorId != null && game.getEmulatorId() != emulatorId) {
          return false;
        }

        if (playlist != null && !playlist.containsGame(game.getId())) {
          return false;
        }

        if (StringUtils.isNotEmpty(searchTerm)
            && !StringUtils.containsIgnoreCase(game.getGameDisplayName(), searchTerm)
            && !StringUtils.containsIgnoreCase(String.valueOf(game.getId()), searchTerm)
            && !StringUtils.containsIgnoreCase(game.getRomAlias(), searchTerm)
            && !StringUtils.containsIgnoreCase(game.getRom(), searchTerm)) {
          return false;
        }

        // else not filtered
        return true;
      }
    };
  }
}

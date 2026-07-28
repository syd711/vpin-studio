package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.games.FilterSettings;
import de.mephisto.vpin.restclient.games.GameFilterRequest;
import de.mephisto.vpin.restclient.vps.VpsSettings;
import de.mephisto.vpin.server.playlists.Playlist;
import de.mephisto.vpin.server.playlists.PlaylistService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class GameFilterService {

  @Autowired
  private GameService gameService;

  @Autowired
  private PlaylistService playlistService;

  @Autowired
  private PreferencesService preferencesService;

  private final GameFilterPredicateFactory predicateFactory = new GameFilterPredicateFactory();

  public List<Integer> filterGameIds(GameFilterRequest request) {
    FilterSettings filterSettings = request.getFilterSettings();
    if (filterSettings == null) {
      filterSettings = new FilterSettings();
    }

    List<Game> candidates = gameService.getKnownGames(translateEmulatorId(request.getEmulatorId()));

    Playlist playlist = null;
    if (request.getPlaylistId() != null) {
      playlist = playlistService.getPlaylist(request.getPlaylistId());
    }

    VpsSettings vpsSettings = preferencesService.getJsonPreference(PreferenceNames.VPS_SETTINGS, VpsSettings.class);

    Integer emulatorId = request.getEmulatorId() == GameFilterRequest.ALL_VPX_ID ? null : request.getEmulatorId();
    Predicate<Game> predicate = predicateFactory.buildPredicate(request.getSearchTerm(), playlist, emulatorId, filterSettings, vpsSettings);

    return candidates.stream()
        .filter(predicate)
        .map(Game::getId)
        .collect(Collectors.toList());
  }

  /**
   * Translates the client-facing "all vpx emulators" sentinel (GameFilterRequest.ALL_VPX_ID, -10)
   * to the server-internal convention used by GameCachingService.getKnownGames (-1).
   */
  private int translateEmulatorId(int emulatorId) {
    if (emulatorId == GameFilterRequest.ALL_VPX_ID) {
      return -1;
    }
    return emulatorId;
  }
}

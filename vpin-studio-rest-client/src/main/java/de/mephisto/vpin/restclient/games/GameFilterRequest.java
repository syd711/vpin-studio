package de.mephisto.vpin.restclient.games;

/**
 * Request body for POST games/filter: resolves to the list of game ids matching
 * the given filter criteria, without returning any game payload.
 */
public class GameFilterRequest {

  /**
   * Mirrors de.mephisto.vpin.restclient.emulators.EmulatorServiceClient.ALL_VPX_ID so the
   * client never has to translate between client- and server-side "all vpx" sentinels.
   */
  public static final int ALL_VPX_ID = -10;

  private FilterSettings filterSettings;
  private String searchTerm;
  private int emulatorId = ALL_VPX_ID;
  private Integer playlistId;

  public GameFilterRequest() {
  }

  public FilterSettings getFilterSettings() {
    return filterSettings;
  }

  public void setFilterSettings(FilterSettings filterSettings) {
    this.filterSettings = filterSettings;
  }

  public String getSearchTerm() {
    return searchTerm;
  }

  public void setSearchTerm(String searchTerm) {
    this.searchTerm = searchTerm;
  }

  public int getEmulatorId() {
    return emulatorId;
  }

  public void setEmulatorId(int emulatorId) {
    this.emulatorId = emulatorId;
  }

  public Integer getPlaylistId() {
    return playlistId;
  }

  public void setPlaylistId(Integer playlistId) {
    this.playlistId = playlistId;
  }
}

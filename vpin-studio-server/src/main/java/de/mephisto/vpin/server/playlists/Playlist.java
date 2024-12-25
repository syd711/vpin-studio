package de.mephisto.vpin.server.playlists;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.frontend.PlaylistGame;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Playlist {
  private int id = -1;
  private int parentId;
  private Integer menuColor;
  private String name;
  private String playListSQL;
  private boolean sqlPlayList = false;
  private boolean visible = true;
  private boolean uglyList = false;
  private String mediaName;
  private int passCode;
  private boolean hideSysLists = false;
  private boolean useDefaults = true;
  private boolean addFavCheckboxes;
  private String dofCommand;
  private List<PlaylistGame> games = new ArrayList<>();
  private List<Playlist> children = new ArrayList<>();

  private String sqlError;
  // optional emulator id when playlist is linked to an emulator, leave null if not
  private Integer emulatorId;

  public String getSqlError() {
    return sqlError;
  }

  public void setSqlError(String sqlError) {
    this.sqlError = sqlError;
  }

  public String getDofCommand() {
    return dofCommand;
  }

  public void setDofCommand(String dofCommand) {
    this.dofCommand = dofCommand;
  }

  public boolean isUseDefaults() {
    return useDefaults;
  }

  public void setUseDefaults(boolean useDefaults) {
    this.useDefaults = useDefaults;
  }

  public boolean isHideSysLists() {
    return hideSysLists;
  }

  public void setHideSysLists(boolean hideSysLists) {
    this.hideSysLists = hideSysLists;
  }

  public boolean isUglyList() {
    return uglyList;
  }

  public void setUglyList(boolean uglyList) {
    this.uglyList = uglyList;
  }

  public int getPassCode() {
    return passCode;
  }

  public void setPassCode(int passCode) {
    this.passCode = passCode;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public List<Playlist> getChildren() {
    return children;
  }

  public void setChildren(List<Playlist> children) {
    this.children = children;
  }

  public int getParentId() {
    return parentId;
  }

  public void setParentId(int parentId) {
    this.parentId = parentId;
  }

  public String getMediaName() {
    return mediaName;
  }

  public void setMediaName(String mediaName) {
    this.mediaName = mediaName;
  }

  public List<PlaylistGame> getGames() {
    return games;
  }

  public void setGames(List<PlaylistGame> games) {
    this.games = games;
  }

  public boolean isSqlPlayList() {
    return sqlPlayList;
  }

  public void setSqlPlayList(boolean sqlPlayList) {
    this.sqlPlayList = sqlPlayList;
  }

  public String getPlayListSQL() {
    return playListSQL;
  }

  public void setPlayListSQL(String playListSQL) {
    this.playListSQL = playListSQL;
  }

  public Integer getMenuColor() {
    return menuColor;
  }

  public void setMenuColor(Integer menuColor) {
    this.menuColor = menuColor;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isAddFavCheckboxes() {
    return addFavCheckboxes;
  }
  public void setAddFavCheckboxes(boolean addFavCheckboxes) {
    this.addFavCheckboxes = addFavCheckboxes;
  }

  public Integer getEmulatorId() {
    return emulatorId;
  }
  public void setEmulatorId(Integer emuId) {
    this.emulatorId = emuId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Playlist)) return false;

    Playlist playlist = (Playlist) o;

    return id == playlist.id;
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public String toString() {
    return name;
  }

  public boolean containsGame(int id) {
    return getGame(id) != null;
  }

  public PlaylistGame getGame(int id) {
    for (PlaylistGame game : this.games) {
      if (game.getId() == id) {
        return game;
      }
    }
    return null;
  }

  public void addGame(PlaylistGame game) {
    games.add(game);
  }

  public boolean removeGame(int id) {
    for (Iterator<PlaylistGame> iter = this.games.iterator(); iter.hasNext();) {
      if (iter.next().getId() == id) {
        iter.remove();
        return true;
      }
    }
    return false;
  }

  public boolean isFavGame(int id) {
    PlaylistGame game = getGame(id);
    return game != null && getGame(id).isFav();
  }

  public boolean isGlobalFavGame(int id) {
    PlaylistGame game = getGame(id);
    return game != null && getGame(id).isGlobalFav();
  }

  @JsonIgnore
  public boolean wasPlayed(int id) {
    PlaylistGame game = getGame(id);
    return game != null && game.isPlayed();
  }
}

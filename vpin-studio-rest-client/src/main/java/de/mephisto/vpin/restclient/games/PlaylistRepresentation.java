package de.mephisto.vpin.restclient.games;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.frontend.PlaylistGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlaylistRepresentation {
  private int id;
  private int parentId;
  private Integer menuColor;
  private String name;
  private String playListSQL;
  private String mediaName;
  private boolean sqlPlayList;
  private int passCode;
  private boolean hideSysLists;
  private boolean useDefaults;
  private boolean addFavCheckboxes;
  private String dofCommand;
  private List<PlaylistGame> games = new ArrayList<>();
  private List<PlaylistRepresentation> children = new ArrayList<>();

  public int getParentId() {
    return parentId;
  }

  public void setParentId(int parentId) {
    this.parentId = parentId;
  }

  public int getPassCode() {
    return passCode;
  }

  public void setPassCode(int passCode) {
    this.passCode = passCode;
  }

  public boolean isHideSysLists() {
    return hideSysLists;
  }

  public void setHideSysLists(boolean hideSysLists) {
    this.hideSysLists = hideSysLists;
  }

  public boolean isUseDefaults() {
    return useDefaults;
  }

  public void setUseDefaults(boolean useDefaults) {
    this.useDefaults = useDefaults;
  }

  public String getDofCommand() {
    return dofCommand;
  }

  public void setDofCommand(String dofCommand) {
    this.dofCommand = dofCommand;
  }

  public List<PlaylistRepresentation> getChildren() {
    return children;
  }

  public void setChildren(List<PlaylistRepresentation> children) {
    this.children = children;
  }

  private Integer emulatorId;

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
  public void setEmulatorId(Integer emulatorId) {
    this.emulatorId = emulatorId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PlaylistRepresentation that = (PlaylistRepresentation) o;
    return id == that.id && sqlPlayList == that.sqlPlayList && Objects.equals(menuColor, that.menuColor) 
              && Objects.equals(name, that.name) && Objects.equals(playListSQL, that.playListSQL) 
              && Objects.equals(mediaName, that.mediaName) && Objects.equals(games, that.games);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, menuColor, name, playListSQL, mediaName, sqlPlayList, games);
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

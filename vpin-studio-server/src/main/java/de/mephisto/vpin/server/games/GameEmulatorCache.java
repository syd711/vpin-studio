package de.mephisto.vpin.server.games;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

class GameEmulatorCache {
  private int emulatorId;
  private final List<Game> games = new ArrayList<>();

  public GameEmulatorCache(int emulatorId) {
    this.emulatorId = emulatorId;
  }

  public GameEmulatorCache(int id, List<Game> gamesByEmulator) {
    this(id);
    this.games.addAll(gamesByEmulator);
  }

  public int getEmulatorId() {
    return emulatorId;
  }

  public void setEmulatorId(int emulatorId) {
    this.emulatorId = emulatorId;
  }

  public void addGame(Game game) {
    if (!this.games.contains(game)) {
      this.games.add(game);
    }
  }

  public void removeGame(Game game) {
    this.games.remove(game);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    GameEmulatorCache that = (GameEmulatorCache) o;
    return emulatorId == that.emulatorId;
  }

  public List<Game> getGames() {
    return new ArrayList<>(games);
  }

  public void delete(int gameId) {
    Optional<Game> first = games.stream().filter(g -> g.getId() == gameId).findFirst();
    first.ifPresent(games::remove);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(emulatorId);
  }

  public List<Game> invalidateByRom(String rom) {
    List<Game> invalidated = new ArrayList<>();
    for (Game game : new ArrayList<>(games)) {
      if (rom.trim().equals(game.getRom()) || rom.trim().equals(game.getRomAlias())) {
        games.remove(game);
        invalidated.add(game);
      }
    }
    return invalidated;
  }
}

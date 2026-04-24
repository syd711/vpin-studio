package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.frontend.EmulatorType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

class GameEmulatorCache {
  private final List<Game> games = new ArrayList<>();
  private EmulatorType emulatorType;
  private int emulatorId;

  public GameEmulatorCache(EmulatorType emulatorType, int emulatorId) {
    this.emulatorType = emulatorType;
    this.emulatorId = emulatorId;
  }

  public GameEmulatorCache(EmulatorType emulatorType, int emulatorId, List<Game> gamesByEmulator) {
    this(emulatorType, emulatorId);
    this.games.addAll(gamesByEmulator);
  }

  public EmulatorType getEmulatorType() {
    return emulatorType;
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

  public boolean isRomShared(Game game) {
    String rom = game.getRom();
    if (rom == null || rom.isBlank()) {
      return false;
    }
    return games.stream()
        .filter(g -> g.getId() != game.getId())
        .filter(g -> !String.valueOf(g.getExtTableId()).equals(game.getExtTableId()))
        .anyMatch(g -> rom.trim().equals(g.getRom()) || rom.trim().equals(g.getRomAlias()));
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

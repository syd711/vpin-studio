package de.mephisto.vpin.restclient.recorder;

import de.mephisto.vpin.restclient.frontend.VPinScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RecordingData {
  private int gameId;

  private List<VPinScreen> screens = new ArrayList<>();

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public List<VPinScreen> getScreens() {
    return screens;
  }

  public void setScreens(List<VPinScreen> screens) {
    this.screens = screens;
  }

  public boolean containsScreen(VPinScreen screen) {
    return this.screens.contains(screen);
  }

  public void removeScreen(VPinScreen screen) {
    this.screens.remove(screen);
  }

  public void addScreen(VPinScreen screen) {
    if (!screens.contains(screen)) {
      screens.add(screen);
    }
  }

  public void clear() {
    this.screens.clear();
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    RecordingData that = (RecordingData) object;
    return gameId == that.gameId;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(gameId);
  }
}

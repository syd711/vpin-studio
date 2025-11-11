package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Objects;

public class GameScreenAssetChangedEvent {
  private int gameId;
  @NonNull
  private final VPinScreen vPinScreen;
  @Nullable
  private final Object asset;

  public GameScreenAssetChangedEvent(int gameId, @NonNull VPinScreen vPinScreen, @Nullable Object asset) {
    this.gameId = gameId;
    this.vPinScreen = vPinScreen;
    this.asset = asset;
  }

  @NonNull
  public VPinScreen getVPinScreen() {
    return vPinScreen;
  }

  @Nullable
  public Object getAsset() {
    return asset;
  }

  public int getGameId() {
    return gameId;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    GameScreenAssetChangedEvent that = (GameScreenAssetChangedEvent) o;
    return gameId == that.gameId && vPinScreen == that.vPinScreen && Objects.equals(asset, that.asset);
  }

  @Override
  public int hashCode() {
    return Objects.hash(gameId, vPinScreen, asset);
  }
}

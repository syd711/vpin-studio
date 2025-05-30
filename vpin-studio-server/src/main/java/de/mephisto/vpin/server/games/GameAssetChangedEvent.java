package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class GameAssetChangedEvent {
  private int gameId;
  @NonNull
  private final AssetType assetType;
  @Nullable
  private final Object asset;

  public GameAssetChangedEvent(int gameId, @NonNull AssetType assetType, @Nullable Object asset) {
    this.gameId = gameId;
    this.assetType = assetType;
    this.asset = asset;
  }

  public GameAssetChangedEvent(@NonNull AssetType assetType, @Nullable Object asset) {
    this.assetType = assetType;
    this.asset = asset;
  }

  @Nullable
  public Object getAsset() {
    return asset;
  }

  public int getGameId() {
    return gameId;
  }

  @NonNull
  public AssetType getAssetType() {
    return assetType;
  }
}

package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static de.mephisto.vpin.restclient.webhooks.WebhookType.game;

@Service
public class GameLifecycleService {
  private final static Logger LOG = LoggerFactory.getLogger(GameLifecycleService.class);

  private final List<GameLifecycleListener> lifecycleListeners = new ArrayList<>();
  private final List<GameDataChangedListener> gameDataChangedListeners = new ArrayList<>();


  public void addGameLifecycleListener(@NonNull GameLifecycleListener lifecycleListener) {
    this.lifecycleListeners.add(lifecycleListener);
  }

  public void addGameDataChangedListener(@NonNull GameDataChangedListener listener) {
    this.gameDataChangedListeners.add(listener);
  }

  public void notifyGameCreated(int gameId) {
    for (GameLifecycleListener lifecycleListener : lifecycleListeners) {
      lifecycleListener.gameCreated(gameId);
    }
  }

  public void notifyGameUpdated(int gameId) {
    for (GameLifecycleListener lifecycleListener : lifecycleListeners) {
      lifecycleListener.gameUpdated(gameId);
    }
  }

  public void notifyGameDeleted(int gameId) {
    for (GameLifecycleListener lifecycleListener : lifecycleListeners) {
      lifecycleListener.gameDeleted(gameId);
    }
  }

  public void notifyGameDataChanged(int gameId, @NonNull TableDetails oldData, @NonNull TableDetails newData) {
    GameDataChangedEvent event = new GameDataChangedEvent(gameId, oldData, newData);
    for (GameDataChangedListener listener : gameDataChangedListeners) {
      listener.gameDataChanged(event);
    }
  }

  public void notifyGameAssetsChanged(int gameId, @NonNull AssetType assetType, @Nullable Object asset) {
    GameAssetChangedEvent event = new GameAssetChangedEvent(gameId, assetType, asset);
    for (GameDataChangedListener listener : gameDataChangedListeners) {
      listener.gameAssetChanged(event);
    }
  }

  public void notifyGameAssetsChanged(@NonNull AssetType assetType, @NonNull Object asset) {
    GameAssetChangedEvent event = new GameAssetChangedEvent(assetType, asset);
    for (GameDataChangedListener listener : gameDataChangedListeners) {
      listener.gameAssetChanged(event);
    }
  }
}

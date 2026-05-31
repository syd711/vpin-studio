package de.mephisto.vpin.server.games;

import org.jspecify.annotations.NonNull;

public interface GameDataChangedListener {

  void gameDataChanged(@NonNull GameDataChangedEvent changedEvent);

  void gameAssetChanged(@NonNull GameAssetChangedEvent changedEvent);

  default void gameScreenAssetChanged(@NonNull GameScreenAssetChangedEvent changedEvent) {

  }
}

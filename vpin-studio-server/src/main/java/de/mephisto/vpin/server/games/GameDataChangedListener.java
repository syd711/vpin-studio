package de.mephisto.vpin.server.games;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface GameDataChangedListener {

  void gameDataChanged(@NonNull GameDataChangedEvent changedEvent);

  void gameAssetChanged(@NonNull GameAssetChangedEvent changedEvent);
}

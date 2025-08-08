package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;

abstract public class DefaultTableAssetAdapter implements TableAssetsAdapter<Game> {

  @NonNull
  protected final TableAssetSource source;

  public DefaultTableAssetAdapter(@NonNull TableAssetSource source) {
    this.source = source;
  }

  protected boolean matches(String screenSegment, String path) {
    path = path.toLowerCase();
    screenSegment = screenSegment.toLowerCase();

    if (path.contains(screenSegment)) {
      return true;
    }

    if (screenSegment.equals("menu")) {
      if (path.contains("fulldmd") || path.contains("apron")) {
        return true;
      }
    }

    if (screenSegment.equals("gameinfo")) {
      if (path.contains("flyer")) {
        return true;
      }
    }

    return false;
  }
}

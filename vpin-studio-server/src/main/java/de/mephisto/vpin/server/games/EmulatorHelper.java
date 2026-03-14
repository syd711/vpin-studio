package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.assets.AssetType;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class EmulatorHelper {

  public static String getBackglassFileName(@NonNull Game game) {
    String fileName = FilenameUtils.getBaseName(game.getGameFileName()) + "." + AssetType.DIRECTB2S.name().toLowerCase();
    if (game.isZenGame()) {
      fileName = game.getGameDisplayName() + "." + AssetType.DIRECTB2S.name().toLowerCase();
    }
    return fileName;
  }

  public static File getBackglassFile(@NonNull Game game) {
    String fileName = getBackglassFileName(game);
    if (game.isZenGame() && game.getEmulator().getBackglassDirectory() != null) {
      return new File(game.getEmulator().getBackglassDirectory(), fileName);
    }
    return new File(game.getGameFile().getParentFile(), fileName);
  }
}

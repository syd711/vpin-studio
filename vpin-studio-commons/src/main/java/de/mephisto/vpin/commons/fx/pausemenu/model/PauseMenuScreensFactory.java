package de.mephisto.vpin.commons.fx.pausemenu.model;

import de.mephisto.vpin.commons.PopperScreensManager;
import de.mephisto.vpin.commons.fx.pausemenu.PauseMenu;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.games.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.popper.PinUPPlayerDisplay;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory class to create Popper screens for the assets found in the Popper resource folders.
 */
public class PauseMenuScreensFactory {
  private final static Logger LOG = LoggerFactory.getLogger(PauseMenuScreensFactory.class);

  public static List<PopperScreenAsset> createAssetScreens(@NonNull GameRepresentation game, @NonNull VPinStudioClient client, List<PinUPPlayerDisplay> displays) {
    List<PopperScreenAsset> screens = new ArrayList<>();
    PopperScreenAsset screenStage = createScreenStage(client, game, displays, PopperScreen.GameHelp);
    if (screenStage != null) {
      screens.add(screenStage);
    }
    screenStage = createScreenStage(client, game, displays, PopperScreen.GameInfo);
    if (screenStage != null) {
      screens.add(screenStage);
    }
    screenStage = createScreenStage(client, game, displays, PopperScreen.Other2);
    if (screenStage != null) {
      screens.add(screenStage);
    }
    return screens;
  }

  @Nullable
  private static PopperScreenAsset createScreenStage(VPinStudioClient client, GameRepresentation game, List<PinUPPlayerDisplay> displays, PopperScreen screen) {
    GameMediaItemRepresentation defaultMediaItem = game.getGameMedia().getDefaultMediaItem(screen);
    if (defaultMediaItem != null) {
      InputStream imageStream = PauseMenu.client.getGameMediaItem(game.getId(), screen);
      if (imageStream != null) {
        PinUPPlayerDisplay display = PopperScreen.valueOfScreen(displays, screen);

        PopperScreenAsset asset = new PopperScreenAsset();
        asset.setDisplay(display);
        asset.setRotation(0);
        asset.setDuration(0);
        asset.setMimeType(defaultMediaItem.getMimeType());
        asset.setInputStream(imageStream);
        asset.setName(defaultMediaItem.getName());
        asset.setUrl(client.getURL(defaultMediaItem.getUri()));

        PopperScreensManager.getInstance().showScreen(asset);
        LOG.info("Created stage for screen " + screen + ", asset \"" + defaultMediaItem.getName() + "\"");
        return asset;
      }
    }
    return null;
  }
}

package de.mephisto.vpin.commons.fx.pausemenu.model;

import de.mephisto.vpin.commons.FrontendScreensManager;
import de.mephisto.vpin.restclient.OverlayClient;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory class to create frontend screens for the assets found in the Popper resource folders.
 */
public class PauseMenuScreensFactory {
  private final static Logger LOG = LoggerFactory.getLogger(PauseMenuScreensFactory.class);

  public static List<FrontendScreenAsset> createAssetScreens(@NonNull GameRepresentation game, @NonNull OverlayClient client, FrontendMediaRepresentation frontendMedia) {
    List<FrontendScreenAsset> screens = new ArrayList<>();
    FrontendPlayerDisplay display = client.getScreenDisplay(VPinScreen.GameHelp);
    FrontendScreenAsset screenStage = createScreenStage(client, game, display, VPinScreen.GameHelp, frontendMedia);
    if (screenStage != null) {
      screens.add(screenStage);
    }
    display = client.getScreenDisplay(VPinScreen.GameInfo);
    screenStage = createScreenStage(client, game, display, VPinScreen.GameInfo, frontendMedia);
    if (screenStage != null) {
      screens.add(screenStage);
    }
    display = client.getScreenDisplay(VPinScreen.Other2);
    screenStage = createScreenStage(client, game, display, VPinScreen.Other2, frontendMedia);
    if (screenStage != null) {
      screens.add(screenStage);
    }
    return screens;
  }

  @Nullable
  private static FrontendScreenAsset createScreenStage(OverlayClient client, GameRepresentation game, FrontendPlayerDisplay display, VPinScreen screen, FrontendMediaRepresentation frontendMedia) {
    FrontendMediaItemRepresentation defaultMediaItem = frontendMedia.getDefaultMediaItem(screen);
    if (defaultMediaItem != null) {
      InputStream imageStream = client.getGameMediaItem(game.getId(), screen);
      if (imageStream != null) {

        FrontendScreenAsset asset = new FrontendScreenAsset();
        asset.setDisplay(display);
        asset.setRotation(0);
        asset.setDuration(0);
        asset.setMimeType(defaultMediaItem.getMimeType());
        asset.setInputStream(imageStream);
        asset.setName(defaultMediaItem.getName());
        asset.setUrl(client.getURL(defaultMediaItem.getUri()));

        FrontendScreensManager.getInstance().showScreen(asset);
        LOG.info("Created stage for screen " + screen + ", asset \"" + defaultMediaItem.getName() + "\"");
        return asset;
      }
    }
    return null;
  }
}

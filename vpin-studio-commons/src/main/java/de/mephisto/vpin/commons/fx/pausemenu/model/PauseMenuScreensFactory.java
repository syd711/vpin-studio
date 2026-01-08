package de.mephisto.vpin.commons.fx.pausemenu.model;

import de.mephisto.vpin.commons.FrontendScreensManager;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Factory class to create frontend screens for the assets found in the Popper resource folders.
 */
public class PauseMenuScreensFactory {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

//  public static List<FrontendScreenAsset> createAssetScreens(@NonNull GameRepresentation game, @NonNull VPinStudioClient client, FrontendMediaRepresentation frontendMedia) {
//    List<FrontendScreenAsset> screens = new ArrayList<>();
//    FrontendPlayerDisplay display = client.getFrontendService().getScreenDisplay(VPinScreen.GameHelp);
//    FrontendScreenAsset screenStage = createScreenStage(client, game, display, VPinScreen.GameHelp, frontendMedia);
//    if (screenStage != null) {
//      screens.add(screenStage);
//    }
//    display = client.getFrontendService().getScreenDisplay(VPinScreen.GameInfo);
//    screenStage = createScreenStage(client, game, display, VPinScreen.GameInfo, frontendMedia);
//    if (screenStage != null) {
//      screens.add(screenStage);
//    }
//    display = client.getFrontendService().getScreenDisplay(VPinScreen.Other2);
//    screenStage = createScreenStage(client, game, display, VPinScreen.Other2, frontendMedia);
//    if (screenStage != null) {
//      screens.add(screenStage);
//    }
//    return screens;
//  }

//  @Nullable
//  public static FrontendScreenAsset createScreenStage(VPinStudioClient client, GameRepresentation game, FrontendPlayerDisplay display, VPinScreen screen, FrontendMediaRepresentation frontendMedia) {
//    FrontendMediaItemRepresentation defaultMediaItem = frontendMedia.getDefaultMediaItem(screen);
//    if (defaultMediaItem != null) {
//      FrontendScreenAsset asset = createScreenStage(client, game, display, screen, defaultMediaItem);
//      if (asset != null) {
//        return asset;
//      }
//    }
//    return null;
//  }

  @NonNull
  public static FrontendScreenAsset createScreenStage(VPinStudioClient client, GameRepresentation game, FrontendPlayerDisplay display, VPinScreen screen, FrontendMediaItemRepresentation defaultMediaItem, int rotation) throws MalformedURLException {
    FrontendScreenAsset asset = new FrontendScreenAsset();
    asset.setDisplay(display);
    asset.setRotation(rotation);
    asset.setDuration(0);
    asset.setMimeType(defaultMediaItem.getMimeType());
    asset.setName(defaultMediaItem.getName());
    asset.setUrl(new URL(client.getURL(defaultMediaItem.getUri())));

    FrontendScreensManager.getInstance().showScreen(asset);
    LOG.info("Created stage for screen " + screen + ", asset \"" + defaultMediaItem.getName() + "\"");
    return asset;
  }
}

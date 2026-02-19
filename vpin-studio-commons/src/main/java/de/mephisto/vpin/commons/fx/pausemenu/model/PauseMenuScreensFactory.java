package de.mephisto.vpin.commons.fx.pausemenu.model;

import de.mephisto.vpin.commons.FrontendScreensManager;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
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

  @NonNull
  public static FrontendScreenAsset createScreenStage(VPinStudioClient client, GameRepresentation game, FrontendPlayerDisplay display, VPinScreen screen, FrontendMediaItemRepresentation defaultMediaItem, PauseMenuSettings pauseMenuSettings) throws MalformedURLException {
    FrontendScreenAsset asset = new FrontendScreenAsset();
    asset.setDisplay(display);
    asset.setRotation(pauseMenuSettings.getTutorialsRotation());
    asset.setOffsetX(pauseMenuSettings.getTutorialMarginLeft());
    asset.setOffsetY(pauseMenuSettings.getTutorialMarginTop());
    asset.setDuration(0);
    asset.setMimeType(defaultMediaItem.getMimeType());
    asset.setName(defaultMediaItem.getName());
    asset.setUrl(new URL(client.getURL(defaultMediaItem.getUri())));

    FrontendScreensManager.getInstance().showScreen(asset);
    LOG.info("Created stage for screen " + screen + ", asset \"" + defaultMediaItem.getName() + "\"");
    return asset;
  }
}

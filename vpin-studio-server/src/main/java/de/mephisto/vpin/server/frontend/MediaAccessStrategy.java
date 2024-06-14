package de.mephisto.vpin.server.frontend;

import java.io.File;

import de.mephisto.vpin.restclient.frontend.VPinScreen;

@FunctionalInterface
public interface MediaAccessStrategy {

  File buildMediaFolder(File mediaDirectory, String gameFileName, VPinScreen screen);

}

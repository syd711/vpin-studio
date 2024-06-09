package de.mephisto.vpin.server.frontend;

import java.io.File;

import de.mephisto.vpin.restclient.popper.PopperScreen;

@FunctionalInterface
public interface MediaAccessStrategy {

  File buildMediaFolder(File mediaDirectory, String gameFileName, PopperScreen screen);

}

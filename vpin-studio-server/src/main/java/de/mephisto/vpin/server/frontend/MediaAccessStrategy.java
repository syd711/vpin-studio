package de.mephisto.vpin.server.frontend;

import java.io.File;
import java.util.List;

import de.mephisto.vpin.server.games.*;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import edu.umd.cs.findbugs.annotations.NonNull;

public interface MediaAccessStrategy {

  File getScreenMediaFolder(File mediaDirectory, String gameFileName, VPinScreen screen);

  List<File> getScreenMediaFiles(@NonNull Game game, @NonNull File mediaDirectory, @NonNull VPinScreen screen);
}

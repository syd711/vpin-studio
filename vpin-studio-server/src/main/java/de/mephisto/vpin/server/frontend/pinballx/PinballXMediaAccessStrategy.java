package de.mephisto.vpin.server.frontend.pinballx;

import java.io.File;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.server.frontend.MediaAccessStrategy;

public class PinballXMediaAccessStrategy implements MediaAccessStrategy {

  @Override
  public File buildMediaFolder(File mediaDirectory, String gameFileName, VPinScreen screen) {
    switch (screen) {
    case Audio: return new File(mediaDirectory, "Table Audio");
    case AudioLaunch: return new File(mediaDirectory, "Launch Audio");
    case Other2: return new File(mediaDirectory, "FullDMD Videos");
    case GameInfo: return new File(mediaDirectory, "../Flyer Images/Front");    // TODO support several folders
    case GameHelp: return new File(mediaDirectory, "../Instruction Cards");
    case Topper: return selectFolder(mediaDirectory, gameFileName, "Topper Videos", "Topper Images");
    case BackGlass: return selectFolder(mediaDirectory, gameFileName, "Backglass Videos", "Backglass Images");
    case Menu: return new File(mediaDirectory, "...");
    case DMD: return new File(mediaDirectory, "DMD Videos");
    case Loading: return new File(mediaDirectory, "../Loading Videos");
    case Wheel: return new File(mediaDirectory, "Wheel Images");
    case PlayField: return new File(mediaDirectory, "Table Videos");
    default: return null;
    }
  }

  private File selectFolder(File mediaDirectory, String gameFileName, String... folders) {
    File firstFolder = null;
    for (String folder: folders) {
        File mediafolder = new File(mediaDirectory, folder);
        if (firstFolder==null) {
          firstFolder = mediafolder;
        }
        File[] files = mediafolder.listFiles((dir, name) -> name.startsWith(gameFileName));
        if (files.length>0) {
            return mediafolder;
        }
    }
    return firstFolder;
  }
}

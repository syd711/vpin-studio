package de.mephisto.vpin.server.frontend.pinballx;

import java.io.File;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.server.frontend.MediaAccessStrategy;

public class PinballXMediaAccessStrategy implements MediaAccessStrategy {

  // TODO support several folders for flyers ?
  @Override
  public File buildMediaFolder(File mediaDirectory, String gameFileName, VPinScreen screen) {
    switch (screen) {
    case Audio: return new File(mediaDirectory, "Table Audio");
    case AudioLaunch: return new File(mediaDirectory, "Launch Audio");
    case GameInfo: return selectFolder(mediaDirectory, gameFileName, "../Flyer Images/Front", "../Flyer Images/Back",
      "../Flyer Images/Inside1",  "../Flyer Images/Inside2", "../Flyer Images/Inside3", "../Flyer Images/Inside4", "../Flyer Images/Inside5", "../Flyer Images/Inside6"); 
    case GameHelp: return new File(mediaDirectory, "../Instruction Cards");
    case Topper: return selectFolder(mediaDirectory, gameFileName, "Topper Videos", "Topper Images");
    case BackGlass: return selectFolder(mediaDirectory, gameFileName, "Backglass Videos", "Backglass Images");
    case Other2: return null;
    case Menu: return new File(mediaDirectory, "FullDMD Videos");
    case DMD: return selectFolder(mediaDirectory, gameFileName, "DMD Videos", "DMD Images");
    case Loading: return new File(mediaDirectory, "../Loading Videos");
    case Wheel: return new File(mediaDirectory, "Wheel Images");
    case PlayField: return selectFolder(mediaDirectory, gameFileName, "Table Videos", "Table Images");
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

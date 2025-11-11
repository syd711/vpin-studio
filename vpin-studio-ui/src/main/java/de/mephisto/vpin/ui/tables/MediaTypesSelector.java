package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.frontend.VPinScreen;

import java.util.ArrayList;
import java.util.List;

public class MediaTypesSelector {

  public static List<String> getFileSelection(VPinScreen screen) {
    List<String> fileSelection = new ArrayList<>();
    if (screen.equals(VPinScreen.Audio) || screen.equals(VPinScreen.AudioLaunch)) {
      fileSelection.add("*.mp3");
    }
    else if (screen.equals(VPinScreen.Logo)) {
      fileSelection.add("*.jpg");
      fileSelection.add("*.png");
    }
    else if (screen.equals(VPinScreen.Wheel)) {
      fileSelection.add("*.jpg");
      fileSelection.add("*.png");
      fileSelection.add("*.apng");
    }
    else {
      fileSelection.add("*.jpg");
      fileSelection.add("*.png");
      fileSelection.add("*.apng");
      fileSelection.add("*.mp4");
    }
    return fileSelection;
  }
}

package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.frontend.VPinScreen;

import java.util.ArrayList;
import java.util.List;

public class PopperMediaTypesSelector {

  public static List<String> getFileSelection(VPinScreen VPinScreen) {
    List<String> fileSelection = new ArrayList<>();
    if (VPinScreen.equals(VPinScreen.Audio) || VPinScreen.equals(VPinScreen.AudioLaunch)) {
      fileSelection.add("*.mp3");
    }
    else if (VPinScreen.equals(VPinScreen.Wheel)) {
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

package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.popper.PopperScreen;

import java.util.ArrayList;
import java.util.List;

public class PopperMediaTypesSelector {

  public static List<String> getFileSelection(PopperScreen popperScreen) {
    List<String> fileSelection = new ArrayList<>();
    if (popperScreen.equals(PopperScreen.Audio) || popperScreen.equals(PopperScreen.AudioLaunch)) {
      fileSelection.add("*.mp3");
    }
    else if (popperScreen.equals(PopperScreen.Wheel)) {
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

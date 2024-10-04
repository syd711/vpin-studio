package de.mephisto.vpin.restclient.recorder;

import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;

public class RecordingScreen {
  private VPinScreen screen;
  private FrontendPlayerDisplay display;

  public VPinScreen getScreen() {
    return screen;
  }

  public void setScreen(VPinScreen screen) {
    this.screen = screen;
  }

  public FrontendPlayerDisplay getDisplay() {
    return display;
  }

  public void setDisplay(FrontendPlayerDisplay display) {
    this.display = display;
  }
}

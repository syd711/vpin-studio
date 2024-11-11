package de.mephisto.vpin.restclient.recorder;

import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

  @JsonIgnore
  public String getName() {
    return display != null? display.getName() : screen.getSegment();
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    RecordingScreen that = (RecordingScreen) object;
    return screen == that.screen && Objects.equals(display, that.display);
  }

  @Override
  public int hashCode() {
    return Objects.hash(screen, display);
  }

  @Override
  public String toString() {
    return "Recording Screen '" + screen.name() + "'";
  }

}

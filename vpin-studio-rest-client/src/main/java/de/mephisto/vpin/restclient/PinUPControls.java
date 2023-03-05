package de.mephisto.vpin.restclient;

import java.util.HashMap;
import java.util.Map;

public class PinUPControls {

  Map<String, PinUPControl> controls = new HashMap<>();

  public void addControl(PinUPControl control) {
    this.controls.put(control.getDescription(), control);
  }

  public int getKeyCode(String description) {
    return controls.get(description).getCtrlKey();
  }

  public Map<String, PinUPControl> getControls() {
    return controls;
  }

  public void setControls(Map<String, PinUPControl> controls) {
    this.controls = controls;
  }
}

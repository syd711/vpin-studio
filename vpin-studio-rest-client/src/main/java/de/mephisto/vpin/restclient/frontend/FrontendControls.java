package de.mephisto.vpin.restclient.frontend;

import java.util.HashMap;
import java.util.Map;

public class FrontendControls {

  Map<String, FrontendControl> controls = new HashMap<>();

  public void addControl(FrontendControl control) {
    this.controls.put(control.getDescription(), control);
  }

  public int getKeyCode(String description) {
    return controls.get(description).getCtrlKey();
  }

  public Map<String, FrontendControl> getControls() {
    return controls;
  }

  public void setControls(Map<String, FrontendControl> controls) {
    this.controls = controls;
  }
}

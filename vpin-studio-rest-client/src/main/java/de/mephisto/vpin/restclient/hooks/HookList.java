package de.mephisto.vpin.restclient.hooks;

import java.util.ArrayList;
import java.util.List;

public class HookList {
  private List<String> hooks = new ArrayList<>();

  public List<String> getHooks() {
    return hooks;
  }

  public void setHooks(List<String> hooks) {
    this.hooks = hooks;
  }
}

package de.mephisto.vpin.restclient.hooks;

import java.util.ArrayList;
import java.util.List;

public class HookCommand {
  private String hooks;
  private List<String> commands = new ArrayList<>();
  private String result;

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public String getHooks() {
    return hooks;
  }

  public void setHooks(String hooks) {
    this.hooks = hooks;
  }

  public List<String> getCommands() {
    return commands;
  }

  public void setCommands(List<String> commands) {
    this.commands = commands;
  }
}

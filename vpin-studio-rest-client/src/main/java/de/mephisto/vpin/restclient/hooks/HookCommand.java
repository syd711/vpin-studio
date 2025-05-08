package de.mephisto.vpin.restclient.hooks;

import java.util.ArrayList;
import java.util.List;

public class HookCommand {
  private String name;
  private List<String> commands = new ArrayList<>();
  private String result;
  private int gameId;

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getCommands() {
    return commands;
  }

  public void setCommands(List<String> commands) {
    this.commands = commands;
  }
}

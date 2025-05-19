package de.mephisto.vpin.restclient.assets;

import de.mephisto.vpin.restclient.frontend.VPinScreen;

public class AssetRequest {
  private int gameId;
  private VPinScreen screen;
  private String name;
  private String result;
  private AssetMetaData metaData;

  public AssetMetaData getMetaData() {
    return metaData;
  }

  public void setMetaData(AssetMetaData metaData) {
    this.metaData = metaData;
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

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public VPinScreen getScreen() {
    return screen;
  }

  public void setScreen(VPinScreen screen) {
    this.screen = screen;
  }
}

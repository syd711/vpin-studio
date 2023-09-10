package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.restclient.popper.PopperScreen;

import java.util.List;

public class TableAssetSearch {

  private PopperScreen screen;
  private String term;

  List<TableAsset> result;

  public PopperScreen getScreen() {
    return screen;
  }

  public void setScreen(PopperScreen screen) {
    this.screen = screen;
  }

  public String getTerm() {
    return term;
  }

  public void setTerm(String term) {
    this.term = term;
  }

  public List<TableAsset> getResult() {
    return result;
  }

  public void setResult(List<TableAsset> result) {
    this.result = result;
  }
}

package de.mephisto.vpin.restclient.popper;

import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.restclient.popper.PopperScreen;

import java.util.List;

public class TableAssetSearch {

  private PopperScreen screen;
  private String term;

  private List<TableAsset> result;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TableAssetSearch)) return false;

    TableAssetSearch that = (TableAssetSearch) o;

    if (screen != that.screen) return false;
    if (term != null ? !term.equals(that.term) : that.term != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = screen != null ? screen.hashCode() : 0;
    result = 31 * result + (term != null ? term.hashCode() : 0);
    return result;
  }
}

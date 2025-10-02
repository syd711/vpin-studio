package de.mephisto.vpin.restclient.games;

import de.mephisto.vpin.restclient.frontend.VPinScreen;

public class AssetCopy {
  private FrontendMediaItemRepresentation item;
  private VPinScreen target;

  public FrontendMediaItemRepresentation getItem() {
    return item;
  }

  public void setItem(FrontendMediaItemRepresentation item) {
    this.item = item;
  }

  public VPinScreen getTarget() {
    return target;
  }

  public void setTarget(VPinScreen target) {
    this.target = target;
  }
}

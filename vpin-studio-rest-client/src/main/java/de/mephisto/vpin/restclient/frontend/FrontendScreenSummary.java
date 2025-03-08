package de.mephisto.vpin.restclient.frontend;

import java.util.ArrayList;
import java.util.List;

public class FrontendScreenSummary {
  private List<FrontendPlayerDisplay> screenResDisplays = new ArrayList<>();
  private List<FrontendPlayerDisplay> vpxDisplaysDisplays = new ArrayList<>();
  private List<FrontendPlayerDisplay> frontendDisplays = new ArrayList<>();
  private List<String> errors = new ArrayList<>();

  public List<FrontendPlayerDisplay> getScreenResDisplays() {
    return screenResDisplays;
  }

  public void setScreenResDisplays(List<FrontendPlayerDisplay> screenResDisplays) {
    this.screenResDisplays = screenResDisplays;
  }

  public List<FrontendPlayerDisplay> getVpxDisplaysDisplays() {
    return vpxDisplaysDisplays;
  }

  public void setVpxDisplaysDisplays(List<FrontendPlayerDisplay> vpxDisplaysDisplays) {
    this.vpxDisplaysDisplays = vpxDisplaysDisplays;
  }

  public List<FrontendPlayerDisplay> getFrontendDisplays() {
    return frontendDisplays;
  }

  public void setFrontendDisplays(List<FrontendPlayerDisplay> frontendDisplays) {
    this.frontendDisplays = frontendDisplays;
  }

  public List<String> getErrors() {
    return errors;
  }

  public void setErrors(List<String> errors) {
    this.errors = errors;
  }
}

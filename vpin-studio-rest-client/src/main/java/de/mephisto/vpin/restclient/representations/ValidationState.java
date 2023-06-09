package de.mephisto.vpin.restclient.representations;

import java.util.ArrayList;
import java.util.List;

public class ValidationState {
  private int code;
  private List<String> options = new ArrayList<>();

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public List<String> getOptions() {
    return options;
  }

  public void setOptions(List<String> options) {
    this.options = options;
  }
}

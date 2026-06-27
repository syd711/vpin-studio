package de.mephisto.vpin.restclient.doftester;

import java.util.ArrayList;
import java.util.List;

public class ToySummary {
  private List<String> toys = new ArrayList<>();
  private String error;
  private boolean dofMapped;

  public boolean isDofMapped() {
    return dofMapped;
  }

  public void setDofMapped(boolean dofMapped) {
    this.dofMapped = dofMapped;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public List<String> getToys() {
    return toys;
  }

  public void setToys(List<String> toys) {
    this.toys = toys;
  }
}

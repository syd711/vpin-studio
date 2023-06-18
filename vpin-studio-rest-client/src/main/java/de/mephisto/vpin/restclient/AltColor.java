package de.mephisto.vpin.restclient;

import de.mephisto.vpin.restclient.representations.ValidationState;

import java.util.Date;
import java.util.List;

public class AltColor {
  private String name;
  private Date modificationDate;
  private List<ValidationState> validationStates;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getModificationDate() {
    return modificationDate;
  }

  public void setModificationDate(Date modificationDate) {
    this.modificationDate = modificationDate;
  }

  public List<ValidationState> getValidationStates() {
    return validationStates;
  }

  public void setValidationStates(List<ValidationState> validationStates) {
    this.validationStates = validationStates;
  }
}

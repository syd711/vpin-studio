package de.mephisto.vpin.restclient;

import de.mephisto.vpin.restclient.representations.ValidationState;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AltColor {
  private String name;
  private AltColorTypes altColorType;
  private Date modificationDate;
  private List<ValidationState> validationStates;
  private List<String> files = new ArrayList<>();

  public List<String> getFiles() {
    return files;
  }

  public void setFiles(List<String> files) {
    this.files = files;
  }

  public AltColorTypes getAltColorType() {
    return altColorType;
  }

  public void setAltColorType(AltColorTypes altColorType) {
    this.altColorType = altColorType;
  }

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

package de.mephisto.vpin.restclient.altcolor;

import de.mephisto.vpin.restclient.validation.ValidationState;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AltColor {
  private String name;
  private AltColorTypes altColorType;
  private Date modificationDate;
  private List<ValidationState> validationStates;
  private List<String> files = new ArrayList<>();
  private List<String> backedUpFiles = new ArrayList<>();
  private boolean available = false;

  public boolean isAvailable() {
    return available;
  }

  public void setAvailable(boolean available) {
    this.available = available;
  }

  public List<String> getBackedUpFiles() {
    return backedUpFiles;
  }

  public void setBackedUpFiles(List<String> backedUpFiles) {
    this.backedUpFiles = backedUpFiles;
  }

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

  public boolean contains(String name) {
    for (String file : files) {
      if(file.equalsIgnoreCase(name)) {
        return true;
      }
    }

    return false;
  }
}

package de.mephisto.vpin.restclient.ini;

import java.util.ArrayList;
import java.util.List;

public class IniRepresentation {

  private String fileName;
  private List<IniSectionRepresentation> sections = new ArrayList<>();

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public List<IniSectionRepresentation> getSections() {
    return sections;
  }

  public void setSections(List<IniSectionRepresentation> sections) {
    this.sections = sections;
  }
}

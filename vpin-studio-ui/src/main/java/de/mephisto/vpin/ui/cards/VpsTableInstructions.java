package de.mephisto.vpin.ui.cards;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class VpsTableInstructions {
  @JsonIgnore
  private String id;
  private String language;
  private String[] instructions;
  private String imageBase64;

  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public String getLanguage() {
    return language;
  }
  public void setLanguage(String locale) {
    this.language = locale;
  }
  public String[] getInstructions() {
    return instructions;
  }
  public void setInstructions(String[] instructions) {
    this.instructions = instructions;
  }
  public String getImageBase64() {
    return imageBase64;
  }
  public void setImageBase64(String imageBase64) {
    this.imageBase64 = imageBase64;
  }
}

package de.mephisto.vpin.restclient.frontend;

import java.util.ArrayList;
import java.util.List;

public class FieldLookups {
  private List<String> gameType = new ArrayList<>();
  private List<String> gameTheme = new ArrayList<>();
  private List<String> category = new ArrayList<>();
  private List<String> manufacturer = new ArrayList<>();
  private List<String> custom1 = new ArrayList<>();
  private List<String> custom2 = new ArrayList<>();
  private List<String> custom3 = new ArrayList<>();

  public List<String> getCategory() {
    return category;
  }

  public void setCategory(List<String> category) {
    this.category = category;
  }

  public List<String> getGameType() {
    return gameType;
  }

  public void setGameType(List<String> gameType) {
    this.gameType = gameType;
  }

  public List<String> getGameTheme() {
    return gameTheme;
  }

  public void setGameTheme(List<String> gameTheme) {
    this.gameTheme = gameTheme;
  }

  public List<String> getManufacturer() {
    return manufacturer;
  }

  public void setManufacturer(List<String> manufacturer) {
    this.manufacturer = manufacturer;
  }

  public List<String> getCustom1() {
    return custom1;
  }

  public void setCustom1(List<String> custom1) {
    this.custom1 = custom1;
  }

  public List<String> getCustom2() {
    return custom2;
  }

  public void setCustom2(List<String> custom2) {
    this.custom2 = custom2;
  }

  public List<String> getCustom3() {
    return custom3;
  }

  public void setCustom3(List<String> custom3) {
    this.custom3 = custom3;
  }
}

package de.mephisto.vpin.restclient.frontend;

public class FrontendControl {
  public static String FUNCTION_SHOW_OTHER = "Show Other";
  public static String FUNCTION_SHOW_HELP = "Game Help";
  public static String FUNCTION_SHOW_FLYER = "Game Info/Flyer";

  private String description;
  private int ctrlKey;
  private int id;
  private int joyCode;
  private boolean active;

  public int getJoyCode() {
    return joyCode;
  }

  public void setJoyCode(int joyCode) {
    this.joyCode = joyCode;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getCtrlKey() {
    return ctrlKey;
  }

  public void setCtrlKey(int ctrlKey) {
    this.ctrlKey = ctrlKey;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}

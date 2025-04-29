package de.mephisto.vpin.restclient.directb2s;

/**
  <Score Parent="DMD" ID="1" ReelType="Dream7LED8" ReelLitColor="255.0.0" ReelDarkColor="15.15.15" 
      Glow="1500" Thickness="1000" Shear="6" 
      Digits="7" Spacing="25" DisplayState="0" LocX="58" LocY="568" Width="585" Height="70" />
*/
public class DirectB2SDataScore {
  private String id;

  private String parent;

  private int nbDigits;

  private int displayState;

  private int x;
  private int y;
  private int width;
  private int height;
  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  
  public String getParent() {
    return parent;
  }
  public void setParent(String parent) {
    this.parent = parent;
  }
  
  public int getNbDigits() {
    return nbDigits;
  }
  public void setNbDigits(int nbDigits) {
    this.nbDigits = nbDigits;
  }
  
  public int getDisplayState() {
    return displayState;
  }
  public void setDisplayState(int displayState) {
    this.displayState = displayState;
  }
  
  public int getX() {
    return x;
  }
  public void setX(int x) {
    this.x = x;
  }
  
  public int getY() {
    return y;
  }
  public void setY(int y) {
    this.y = y;
  }
  
  public int getWidth() {
    return width;
  }
  public void setWidth(int width) {
    this.width = width;
  }
  
  public int getHeight() {
    return height;
  }
  public void setHeight(int height) {
    this.height = height;
  }  
}

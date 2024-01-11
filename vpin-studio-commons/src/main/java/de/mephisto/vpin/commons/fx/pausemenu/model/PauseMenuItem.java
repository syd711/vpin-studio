package de.mephisto.vpin.commons.fx.pausemenu.model;

import javafx.scene.image.Image;

public class PauseMenuItem {
  private final PauseMenuItemTypes itemType;
  private final String name;
  private final String description;
  private final Image image;

  private Image dataImage;
  private String youTubeUrl;

  public PauseMenuItem(PauseMenuItemTypes itemType, String name, String description, Image image) {
    this.itemType = itemType;
    this.name = name;
    this.description = description;
    this.image = image;
  }

  public String getYouTubeUrl() {
    return youTubeUrl;
  }

  public void setYouTubeUrl(String youTubeUrl) {
    this.youTubeUrl = youTubeUrl;
  }

  public PauseMenuItemTypes getItemType() {
    return itemType;
  }

  public Image getDataImage() {
    return dataImage;
  }

  public void setDataImage(Image dataImage) {
    this.dataImage = dataImage;
  }

  public Image getImage() {
    return image;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }
}

package de.mephisto.vpin.commons.fx.pausemenu.model;

import javafx.scene.image.Image;

public class PauseMenuItem {
  private final String name;
  private final String description;
  private final Image image;

  public PauseMenuItem(String name, String description, Image image) {
    this.name = name;
    this.description = description;
    this.image = image;
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

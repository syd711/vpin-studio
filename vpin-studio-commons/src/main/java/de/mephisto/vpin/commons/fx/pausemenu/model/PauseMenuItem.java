package de.mephisto.vpin.commons.fx.pausemenu.model;

import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import javafx.scene.image.Image;

public class PauseMenuItem {
  private final PauseMenuItemTypes itemType;
  private final String name;
  private final String description;
  private final Image image;

  private Image dataImage;
  private String youTubeUrl;
  private String videoUrl;
  private CompetitionRepresentation competition;

  public PauseMenuItem(PauseMenuItemTypes itemType, String name, String description, Image image) {
    this.itemType = itemType;
    this.name = name;
    this.description = description;
    this.image = image;
  }

  public CompetitionRepresentation getCompetition() {
    return competition;
  }

  public void setCompetition(CompetitionRepresentation competition) {
    this.competition = competition;
  }

  public String getVideoUrl() {
    return videoUrl;
  }

  public void setVideoUrl(String videoUrl) {
    this.videoUrl = videoUrl;
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

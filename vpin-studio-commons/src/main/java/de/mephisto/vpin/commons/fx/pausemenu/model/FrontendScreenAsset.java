package de.mephisto.vpin.commons.fx.pausemenu.model;

import de.mephisto.vpin.commons.fx.FrontendScreenController;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import javafx.stage.Stage;

import java.net.URL;

public class FrontendScreenAsset {
  private Stage screenStage;
  private int rotation;
  private FrontendPlayerDisplay display;
  private String mimeType;
  private URL url;
  private int duration;
  private String name;
  private int offsetX;
  private int offsetY;

  private FrontendScreenController frontendScreenController;

  public void setFrontendScreenController(FrontendScreenController frontendScreenController) {
    this.frontendScreenController = frontendScreenController;
  }

  public FrontendScreenController getFrontendScreenController() {
    return frontendScreenController;
  }

  public int getOffsetX() {
    return offsetX;
  }

  public void setOffsetX(int offsetX) {
    this.offsetX = offsetX;
  }

  public int getOffsetY() {
    return offsetY;
  }

  public void setOffsetY(int offsetY) {
    this.offsetY = offsetY;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getDuration() {
    return duration;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

  public int getRotation() {
    return rotation;
  }

  public void setRotation(int rotation) {
    this.rotation = rotation;
  }

  public Stage getScreenStage() {
    return screenStage;
  }

  public void setScreenStage(Stage screenStage) {
    this.screenStage = screenStage;
  }

  public FrontendPlayerDisplay getDisplay() {
    return display;
  }

  public void setDisplay(FrontendPlayerDisplay display) {
    this.display = display;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public URL getUrl() {
    return url;
  }

  public void setUrl(URL url) {
    this.url = url;
  }
}

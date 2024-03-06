package de.mephisto.vpin.restclient.cards;

import de.mephisto.vpin.restclient.JsonSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class CardSettings extends JsonSettings {
  private String popperScreen = "Other2";
  private int notificationTime = 0;
  private String notificationRotation = "0";
  private boolean notificationOnPopperScreen = false;
  private boolean transparentBackground = false;
  private int transparentPercentage = 0;
  private boolean renderTableName = true;

  public String getPopperScreen() {
    return popperScreen;
  }

  public void setPopperScreen(String popperScreen) {
    this.popperScreen = popperScreen;
  }

  public int getNotificationTime() {
    return notificationTime;
  }

  public void setNotificationTime(int notificationTime) {
    this.notificationTime = notificationTime;
  }

  public String getNotificationRotation() {
    return notificationRotation;
  }

  public void setNotificationRotation(String notificationRotation) {
    this.notificationRotation = notificationRotation;
  }

  public boolean isNotificationOnPopperScreen() {
    return notificationOnPopperScreen;
  }

  public void setNotificationOnPopperScreen(boolean notificationOnPopperScreen) {
    this.notificationOnPopperScreen = notificationOnPopperScreen;
  }

  public boolean isTransparentBackground() {
    return transparentBackground;
  }

  public void setTransparentBackground(boolean transparentBackground) {
    this.transparentBackground = transparentBackground;
  }

  public int getTransparentPercentage() {
    return transparentPercentage;
  }

  public void setTransparentPercentage(int transparentPercentage) {
    this.transparentPercentage = transparentPercentage;
  }

  public boolean isRenderTableName() {
    return renderTableName;
  }

  public void setRenderTableName(boolean renderTableName) {
    this.renderTableName = renderTableName;
  }
}

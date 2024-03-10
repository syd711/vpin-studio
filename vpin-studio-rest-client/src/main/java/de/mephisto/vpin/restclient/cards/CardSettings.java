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
}

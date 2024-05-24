package de.mephisto.vpin.restclient.notifications;

import de.mephisto.vpin.restclient.JsonSettings;

public class NotificationSettings extends JsonSettings {
  private boolean startupNotification = true;
  private boolean highscoreUpdatedNotification = true;
  private boolean iScoredNotification = true;

  private int durationSec = 5;

  public boolean isiScoredNotification() {
    return iScoredNotification;
  }

  public void setiScoredNotification(boolean iScoredNotification) {
    this.iScoredNotification = iScoredNotification;
  }

  public int getDurationSec() {
    return durationSec;
  }

  public void setDurationSec(int durationSec) {
    this.durationSec = durationSec;
  }

  public boolean isStartupNotification() {
    return startupNotification;
  }

  public void setStartupNotification(boolean startupNotification) {
    this.startupNotification = startupNotification;
  }

  public boolean isHighscoreUpdatedNotification() {
    return highscoreUpdatedNotification;
  }

  public void setHighscoreUpdatedNotification(boolean highscoreUpdatedNotification) {
    this.highscoreUpdatedNotification = highscoreUpdatedNotification;
  }
}

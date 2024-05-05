package de.mephisto.vpin.restclient.notifications;

import de.mephisto.vpin.restclient.JsonSettings;

public class NotificationSettings extends JsonSettings {
  private boolean startupNotification = true;
  private boolean highscoreUpdatedNotification = true;
  private boolean discordMessageNotification = true;

  private int durationSec = 5;

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

  public boolean isDiscordMessageNotification() {
    return discordMessageNotification;
  }

  public void setDiscordMessageNotification(boolean discordMessageNotification) {
    this.discordMessageNotification = discordMessageNotification;
  }
}

package de.mephisto.vpin.restclient.webhooks;

import java.util.Objects;

public class WebhookSet {
  private String uuid;
  private String name;
  private boolean enabled = true;
  private Webhook scores = new Webhook();
  private Webhook games = new Webhook();
  private Webhook players = new Webhook();

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public Webhook getScores() {
    return scores;
  }

  public void setScores(Webhook scores) {
    this.scores = scores;
  }

  public Webhook getGames() {
    return games;
  }

  public void setGames(Webhook games) {
    this.games = games;
  }

  public Webhook getPlayers() {
    return players;
  }

  public void setPlayers(Webhook players) {
    this.players = players;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    WebhookSet that = (WebhookSet) o;
    return Objects.equals(uuid, that.uuid);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(uuid);
  }

  @Override
  public String toString() {
    return getName();
  }
}

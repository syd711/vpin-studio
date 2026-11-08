package de.mephisto.vpin.restclient.mania;

/**
 * Opt-in or opt-out of the VPin Mania Discord challenges of a server or a single channel.
 */
public class ManiaChallengeParticipation {
  private String guildId;

  /**
   * The channel to opt in or out, null for the whole server.
   */
  private String channelId;

  private boolean optOut;

  public ManiaChallengeParticipation() {
  }

  public ManiaChallengeParticipation(String guildId, String channelId, boolean optOut) {
    this.guildId = guildId;
    this.channelId = channelId;
    this.optOut = optOut;
  }

  public String getGuildId() {
    return guildId;
  }

  public void setGuildId(String guildId) {
    this.guildId = guildId;
  }

  public String getChannelId() {
    return channelId;
  }

  public void setChannelId(String channelId) {
    this.channelId = channelId;
  }

  public boolean isOptOut() {
    return optOut;
  }

  public void setOptOut(boolean optOut) {
    this.optOut = optOut;
  }
}

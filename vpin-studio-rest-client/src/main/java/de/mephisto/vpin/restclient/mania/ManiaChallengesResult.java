package de.mephisto.vpin.restclient.mania;

import de.mephisto.vpin.connectors.mania.model.DiscordChallengeOptOut;
import de.mephisto.vpin.connectors.mania.model.DiscordServer;

import java.util.ArrayList;
import java.util.List;

/**
 * The VPin Mania Discord challenges of the user as seen by the server, right after they have been synchronized
 * into competitions.
 */
public class ManiaChallengesResult {
  private List<ManiaChallengeEntry> challenges = new ArrayList<>();
  private List<DiscordChallengeOptOut> optOuts = new ArrayList<>();
  private List<DiscordServer> servers = new ArrayList<>();

  /**
   * False if the user disabled the participation in all Discord challenges in the VPin Mania user settings.
   */
  private boolean participating = true;

  /**
   * Set if the challenges could not be loaded from VPin Mania.
   */
  private String error;

  public List<ManiaChallengeEntry> getChallenges() {
    return challenges;
  }

  public void setChallenges(List<ManiaChallengeEntry> challenges) {
    this.challenges = challenges;
  }

  public List<DiscordChallengeOptOut> getOptOuts() {
    return optOuts;
  }

  public void setOptOuts(List<DiscordChallengeOptOut> optOuts) {
    this.optOuts = optOuts;
  }

  public List<DiscordServer> getServers() {
    return servers;
  }

  public void setServers(List<DiscordServer> servers) {
    this.servers = servers;
  }

  public boolean isParticipating() {
    return participating;
  }

  public void setParticipating(boolean participating) {
    this.participating = participating;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}

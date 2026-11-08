package de.mephisto.vpin.restclient.mania;

import de.mephisto.vpin.connectors.mania.model.DiscordChallenge;

/**
 * A VPin Mania Discord challenge together with the state of its competition on the server.
 */
public class ManiaChallengeEntry {
  private DiscordChallenge challenge;

  /**
   * The id of the competition the challenge is synchronized into, null if the challenge is not active
   * (finished, canceled, opted out or no participation).
   */
  private Long competitionId;

  /**
   * The game matching the challenge, -1 if none. For active challenges this is the game assigned to the competition,
   * so the one with the competition id (and so part of the challenge playlist) and badge.
   */
  private int gameId = -1;

  /**
   * An installed game of another version of the challenge table. It is not assigned to the challenge, as its
   * scores do not count for it. Only set if no game is assigned, -1 otherwise.
   */
  private int versionMismatchGameId = -1;

  /**
   * The issues of the competition, separated by "|".
   */
  private String issues;

  public DiscordChallenge getChallenge() {
    return challenge;
  }

  public void setChallenge(DiscordChallenge challenge) {
    this.challenge = challenge;
  }

  public Long getCompetitionId() {
    return competitionId;
  }

  public void setCompetitionId(Long competitionId) {
    this.competitionId = competitionId;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public int getVersionMismatchGameId() {
    return versionMismatchGameId;
  }

  public void setVersionMismatchGameId(int versionMismatchGameId) {
    this.versionMismatchGameId = versionMismatchGameId;
  }

  public String getIssues() {
    return issues;
  }

  public void setIssues(String issues) {
    this.issues = issues;
  }
}

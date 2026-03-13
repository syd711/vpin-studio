package de.mephisto.vpin.connectors.wovp.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
public class ScoreBoardItemPositionValues {
  @JsonProperty("approvalNote")
  private String approvalNote;

  @JsonProperty("ApprovalStatus")
  private int approvalStatus;

  @JsonProperty("IsPending")
  private boolean isPending;

  @JsonProperty("ParticipantDivision")
  private String participantDivision;

  @JsonProperty("ParticipantId")
  private String participantId;

  @JsonProperty("ParticipantName")
  private String participantName;

  @JsonProperty("Position")
  private int position;

  @JsonProperty("Score")
  private double score;

  @JsonProperty("UserId")
  private String userId;

  @JsonProperty("ParticipantPlayingPlatform")
  private int participantPlayingPlatform;

  @JsonProperty("ParticipantProfilePicture")
  private String participantProfilePicture;

  @JsonProperty("ParticipantCountryCode")
  private String participantCountryCode;

  public String getParticipantProfilePicture() {
    return participantProfilePicture;
  }

  public void setParticipantProfilePicture(String participantProfilePicture) {
    this.participantProfilePicture = participantProfilePicture;
  }

  public String getParticipantCountryCode() {
    return participantCountryCode;
  }

  public void setParticipantCountryCode(String participantCountryCode) {
    this.participantCountryCode = participantCountryCode;
  }

  public int getParticipantPlayingPlatform() {
    return participantPlayingPlatform;
  }

  public void setParticipantPlayingPlatform(int participantPlayingPlatform) {
    this.participantPlayingPlatform = participantPlayingPlatform;
  }

  public String getApprovalNote() {
    return approvalNote;
  }

  public void setApprovalNote(String approvalNote) {
    this.approvalNote = approvalNote;
  }

  public int getApprovalStatus() {
    return approvalStatus;
  }

  public void setApprovalStatus(int approvalStatus) {
    this.approvalStatus = approvalStatus;
  }

  public boolean isPending() {
    return isPending;
  }

  public void setPending(boolean pending) {
    isPending = pending;
  }

  public String getParticipantDivision() {
    return participantDivision;
  }

  public void setParticipantDivision(String participantDivision) {
    this.participantDivision = participantDivision;
  }

  public String getParticipantId() {
    return participantId;
  }

  public void setParticipantId(String participantId) {
    this.participantId = participantId;
  }

  public String getParticipantName() {
    return participantName;
  }

  public void setParticipantName(String participantName) {
    this.participantName = participantName;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public double getScore() {
    return score;
  }

  public void setScore(double score) {
    this.score = score;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }
}

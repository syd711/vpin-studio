package de.mephisto.vpin.server.preferences;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.mephisto.vpin.server.assets.Asset;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = "Preferences")
@EntityListeners(AuditingEntityListener.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Preferences {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "id", nullable = false)
  private Long id;

  @OneToOne(cascade = CascadeType.ALL)
  private Asset avatar;

  private String ignoredValidations;

  private String ignoredMedia;

  private String systemName;

  private String resetKey;

  private String overlayKey;

  private String showOverlayOnStartup;

  private String discordWebHookUrl;

  private String discordGuildId;

  private String discordChannelId;

  private String discordBotToken;

  private String discordBotAllowList;

  private String rankingPoints;

  @Column(length = 1024)
  private String highscoreTitles;

  private int idleTimeout;

  public String getDiscordChannelId() {
    return discordChannelId;
  }

  public void setDiscordChannelId(String discordChannelId) {
    this.discordChannelId = discordChannelId;
  }

  public String getRankingPoints() {
    return rankingPoints;
  }

  public void setRankingPoints(String rankingPoints) {
    this.rankingPoints = rankingPoints;
  }

  public String getDiscordBotAllowList() {
    return discordBotAllowList;
  }

  public void setDiscordBotAllowList(String discordBotAllowList) {
    this.discordBotAllowList = discordBotAllowList;
  }

  public int getIdleTimeout() {
    return idleTimeout;
  }

  public void setIdleTimeout(int idleTimeout) {
    this.idleTimeout = idleTimeout;
  }

  public String getHighscoreTitles() {
    return highscoreTitles;
  }

  public void setHighscoreTitles(String highscoreTitles) {
    this.highscoreTitles = highscoreTitles;
  }

  public String getDiscordGuildId() {
    return discordGuildId;
  }

  public void setDiscordGuildId(String discordGuildId) {
    this.discordGuildId = discordGuildId;
  }

  public String getDiscordBotToken() {
    return discordBotToken;
  }

  public void setDiscordBotToken(String discordBotToken) {
    this.discordBotToken = discordBotToken;
  }

  public String getDiscordWebHookUrl() {
    return discordWebHookUrl;
  }

  public void setDiscordWebHookUrl(String discordWebHookUrl) {
    this.discordWebHookUrl = discordWebHookUrl;
  }

  public String getShowOverlayOnStartup() {
    return showOverlayOnStartup;
  }

  public void setShowOverlayOnStartup(String showOverlayOnStartup) {
    this.showOverlayOnStartup = showOverlayOnStartup;
  }

  public String getResetKey() {
    return resetKey;
  }

  public void setResetKey(String resetKey) {
    this.resetKey = resetKey;
  }

  public String getOverlayKey() {
    return overlayKey;
  }

  public void setOverlayKey(String overlayKey) {
    this.overlayKey = overlayKey;
  }

  public Asset getAvatar() {
    return avatar;
  }

  public void setAvatar(Asset avatar) {
    this.avatar = avatar;
  }

  public String getSystemName() {
    return systemName;
  }

  public void setSystemName(String systemName) {
    this.systemName = systemName;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getIgnoredMedia() {
    return ignoredMedia;
  }

  public void setIgnoredMedia(String ignoredMedia) {
    this.ignoredMedia = ignoredMedia;
  }

  public String getIgnoredValidations() {
    return ignoredValidations;
  }

  public void setIgnoredValidations(String ignoredValidations) {
    this.ignoredValidations = ignoredValidations;
  }


}

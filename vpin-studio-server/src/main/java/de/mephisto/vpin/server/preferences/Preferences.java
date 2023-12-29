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

  private String systemPreset;

  private String resetKey;

  private String overlayKey;

  private String overlayDesign;

  private String overlayPageUrl;

  private String showOverlayOnStartup;

  private String discordWebHookUrl;

  private String discordGuildId;

  private String discordChannelId;

  private String discordCategoryId;

  private String discordBotToken;

  private String discordBotAllowList;

  private String discordVpsChannelId;

  @Column(name = "discordVpsTableFilterEnabled", nullable = false, columnDefinition = "boolean default true")
  private boolean discordVpsTableFilterEnabled;

  @Column(name = "discordVpsRefreshIntervalMin", nullable = false, columnDefinition = "int default 60")
  private int discordVpsRefreshIntervalMin;

  private String vpbmInternalHostId;

  private String vpbmExternalHostId;

  private String rankingPoints;

  @Column(length = 1024)
  private String highscoreTitles;

  private String highscoreCardSettings;

  @Column(length = 1024)
  private String uiSettings;

  @Column(length = 1024)
  private String doNotShowAgains;

  private int idleTimeout;

  @Column(name = "highscoreFilterEnabled", nullable = false, columnDefinition = "boolean default false")
  private boolean highscoreFilterEnabled;

  @Column(name = "activeGame", nullable = false, columnDefinition = "int default -1")
  private int activeGame = -1;

  @Column(name = "previewEnabled", nullable = false, columnDefinition = "boolean default true")
  private boolean previewEnabled;

  @Column(name = "pinVolAutoStartEnabled", nullable = false, columnDefinition = "boolean default false")
  private boolean pinVolAutoStartEnabled;

  @Column(name = "pinemhiAutoStartEnabled", nullable = false, columnDefinition = "boolean default false")
  private boolean pinemhiAutoStartEnabled;

  @Column(name = "discordBotCommandsEnabled", nullable = false, columnDefinition = "boolean default true")
  private boolean discordBotCommandsEnabled;

  @Column(name = "discordDynamicSubscriptions", nullable = false, columnDefinition = "boolean default false")
  private boolean discordDynamicSubscriptions;

  @Column(name = "tournamentEnabled", nullable = false, columnDefinition = "boolean default false")
  private boolean tournamentEnabled;

  @Column(length = 2048)
  private String tournamentDiscordLink;

  @Column(length = 2048)
  private String tournamentDashboardUrl;

  @Column(length = 4096)
  private String tournamentDescription;

  public String getTournamentDescription() {
    return tournamentDescription;
  }

  public void setTournamentDescription(String tournamentDescription) {
    this.tournamentDescription = tournamentDescription;
  }

  public boolean getTournamentEnabled() {
    return tournamentEnabled;
  }

  public void setTournamentEnabled(boolean tournamentEnabled) {
    this.tournamentEnabled = tournamentEnabled;
  }

  public String getTournamentDiscordLink() {
    return tournamentDiscordLink;
  }

  public void setTournamentDiscordLink(String tournamentDiscordLink) {
    this.tournamentDiscordLink = tournamentDiscordLink;
  }

  public String getTournamentDashboardUrl() {
    return tournamentDashboardUrl;
  }

  public void setTournamentDashboardUrl(String tournamentDashboardUrl) {
    this.tournamentDashboardUrl = tournamentDashboardUrl;
  }

  public String getDiscordVpsChannelId() {
    return discordVpsChannelId;
  }

  public void setDiscordVpsChannelId(String discordVpsChannelId) {
    this.discordVpsChannelId = discordVpsChannelId;
  }

  public boolean getDiscordVpsTableFilterEnabled() {
    return discordVpsTableFilterEnabled;
  }

  public void setDiscordVpsTableFilterEnabled(boolean discordVpsTableFilterEnabled) {
    this.discordVpsTableFilterEnabled = discordVpsTableFilterEnabled;
  }

  public int getDiscordVpsRefreshIntervalMin() {
    return discordVpsRefreshIntervalMin;
  }

  public void setDiscordVpsRefreshIntervalMin(int discordVpsRefreshIntervalMin) {
    this.discordVpsRefreshIntervalMin = discordVpsRefreshIntervalMin;
  }

  public String getHighscoreCardSettings() {
    return highscoreCardSettings;
  }

  public void setHighscoreCardSettings(String highscoreCardSettings) {
    this.highscoreCardSettings = highscoreCardSettings;
  }

  public String getOverlayPageUrl() {
    return overlayPageUrl;
  }

  public void setOverlayPageUrl(String overlayPageUrl) {
    this.overlayPageUrl = overlayPageUrl;
  }

  public String getDoNotShowAgains() {
    return doNotShowAgains;
  }

  public void setDoNotShowAgains(String doNotShowAgains) {
    this.doNotShowAgains = doNotShowAgains;
  }

  public String getSystemPreset() {
    return systemPreset;
  }

  public void setSystemPreset(String systemPreset) {
    this.systemPreset = systemPreset;
  }

  public String getUiSettings() {
    return uiSettings;
  }

  public void setUiSettings(String uiSettings) {
    this.uiSettings = uiSettings;
  }

  public String getOverlayDesign() {
    return overlayDesign;
  }

  public void setOverlayDesign(String overlayDesign) {
    this.overlayDesign = overlayDesign;
  }

  public boolean getHighscoreFilterEnabled() {
    return highscoreFilterEnabled;
  }

  public void setHighscoreFilterEnabled(boolean highscoreFilterEnabled) {
    this.highscoreFilterEnabled = highscoreFilterEnabled;
  }

  public boolean getDiscordDynamicSubscriptions() {
    return discordDynamicSubscriptions;
  }

  public void setDiscordDynamicSubscriptions(boolean discordDynamicSubscriptions) {
    this.discordDynamicSubscriptions = discordDynamicSubscriptions;
  }

  public String getDiscordCategoryId() {
    return discordCategoryId;
  }

  public void setDiscordCategoryId(String discordCategoryId) {
    this.discordCategoryId = discordCategoryId;
  }

  public String getVpbmInternalHostId() {
    return vpbmInternalHostId;
  }

  public void setVpbmInternalHostId(String vpbmInternalHostId) {
    this.vpbmInternalHostId = vpbmInternalHostId;
  }

  public String getVpbmExternalHostId() {
    return vpbmExternalHostId;
  }

  public void setVpbmExternalHostId(String vpbmExternalHostId) {
    this.vpbmExternalHostId = vpbmExternalHostId;
  }

  public boolean isDiscordBotCommandsEnabled() {
    return discordBotCommandsEnabled;
  }

  public void setDiscordBotCommandsEnabled(boolean discordBotCommandsEnabled) {
    this.discordBotCommandsEnabled = discordBotCommandsEnabled;
  }

  public boolean getPinemhiAutoStartEnabled() {
    return pinVolAutoStartEnabled;
  }

  public void setPinemhiAutoStartEnabled(boolean enabled) {
    this.pinemhiAutoStartEnabled = enabled;
  }

  public boolean getPinVolAutoStartEnabled() {
    return pinVolAutoStartEnabled;
  }

  public void setPinVolAutoStartEnabled(boolean pinVolAutoStartEnabled) {
    this.pinVolAutoStartEnabled = pinVolAutoStartEnabled;
  }

  public boolean getPreviewEnabled() {
    return previewEnabled;
  }

  public void setPreviewEnabled(boolean previewEnabled) {
    this.previewEnabled = previewEnabled;
  }

  public int getActiveGame() {
    return activeGame;
  }

  public void setActiveGame(int activeGame) {
    this.activeGame = activeGame;
  }

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

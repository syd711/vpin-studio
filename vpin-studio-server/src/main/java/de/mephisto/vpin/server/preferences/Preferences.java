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

  private String systemName;

  private String overlaySettings;

  private String discordWebHookUrl;

  private String discordGuildId;

  private String discordChannelId;

  private String discordUpdatesChannelId;

  private String discordCategoryId;

  private String discordBotToken;

  private String discordBotAllowList;

  private String backupSettings;

  private String rankingPoints;

  private String dofSettings;

  private String dofLinxSettings;

  private String pinballXSettings;

  private String pinballYSettings;

  private String highscoreCardSettings;

  private String highscoreCardTemplates;

  private String pauseMenuSettings;

  private String validationSettings;

  private String notificationSettings;

  private String recorderSettings;

  private String monitoringSettings;

  private String uiSettings;

  private String serverSettings;

  private String filterSettings;

  private String vpuSettings;

  private String vpfSettings;

  private String vpsSettings;

  private String authenticationSettings;

  private String assetSourcesSettings;

  private String webhookSettings;

  private String iScoredSettings;

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

  @Column(name = "pinVolInstallationFolder", nullable = true, columnDefinition = "PinVol Installation folder")
  private String pinVolInstallationFolder;

  @Column(name = "pinemhiAutoStartEnabled", nullable = false, columnDefinition = "boolean default false")
  private boolean pinemhiAutoStartEnabled;

  @Column(name = "discordBotCommandsEnabled", nullable = false, columnDefinition = "boolean default true")
  private boolean discordBotCommandsEnabled;

  @Column(name = "discordDynamicSubscriptions", nullable = false, columnDefinition = "boolean default false")
  private boolean discordDynamicSubscriptions;

  public String getAssetSourcesSettings() {
    return assetSourcesSettings;
  }

  public void setAssetSourcesSettings(String assetSourcesSettings) {
    this.assetSourcesSettings = assetSourcesSettings;
  }

  public String getAuthenticationSettings() {
    return authenticationSettings;
  }

  public void setAuthenticationSettings(String authenticationSettings) {
    this.authenticationSettings = authenticationSettings;
  }

  public String getIScoredSettings() {
    return iScoredSettings;
  }

  public void setIScoredSettings(String iScoredSettings) {
    this.iScoredSettings = iScoredSettings;
  }

  public String getWebhookSettings() {
    return webhookSettings;
  }

  public void setWebhookSettings(String webhookSettings) {
    this.webhookSettings = webhookSettings;
  }

  public String getRecorderSettings() {
    return recorderSettings;
  }

  public void setRecorderSettings(String recorderSettings) {
    this.recorderSettings = recorderSettings;
  }

  public String getDofLinxSettings() {
    return dofLinxSettings;
  }

  public void setDofLinxSettings(String dofLinxSettings) {
    this.dofLinxSettings = dofLinxSettings;
  }

  public String getMonitoringSettings() {
    return monitoringSettings;
  }

  public void setMonitoringSettings(String monitoringSettings) {
    this.monitoringSettings = monitoringSettings;
  }

  public String getDiscordUpdatesChannelId() {
    return discordUpdatesChannelId;
  }

  public void setDiscordUpdatesChannelId(String discordUpdatesChannelId) {
    this.discordUpdatesChannelId = discordUpdatesChannelId;
  }

  public String getBackupSettings() {
    return backupSettings;
  }

  public void setBackupSettings(String backupSettings) {
    this.backupSettings = backupSettings;
  }

  public String getVpuSettings() {
    return vpuSettings;
  }
  public void setVpuSettings(String vpuSettings) {
    this.vpuSettings = vpuSettings;
  }

  public String getVpfSettings() {
    return vpfSettings;
  }
  public void setVpfSettings(String vpfSettings) {
    this.vpfSettings = vpfSettings;
  }

  public String getFilterSettings() {
    return filterSettings;
  }

  public void setFilterSettings(String filterSettings) {
    this.filterSettings = filterSettings;
  }

  public String getNotificationSettings() {
    return notificationSettings;
  }

  public void setNotificationSettings(String notificationSettings) {
    this.notificationSettings = notificationSettings;
  }

  public String getValidationSettings() {
    return validationSettings;
  }

  public void setValidationSettings(String validationSettings) {
    this.validationSettings = validationSettings;
  }

  public String getDofSettings() {
    return dofSettings;
  }

  public void setDofSettings(String dofSettings) {
    this.dofSettings = dofSettings;
  }

  public String getServerSettings() {
    return serverSettings;
  }

  public void setServerSettings(String serverSettings) {
    this.serverSettings = serverSettings;
  }

  @Column(length = 8192)
  private String tournamentSettings;

  public String getPauseMenuSettings() {
    return pauseMenuSettings;
  }

  public void setPauseMenuSettings(String pauseMenuSettings) {
    this.pauseMenuSettings = pauseMenuSettings;
  }

  public String getTournamentSettings() {
    return tournamentSettings;
  }

  public void setTournamentSettings(String tournamentSettings) {
    this.tournamentSettings = tournamentSettings;
  }

  public String getHighscoreCardSettings() {
    return highscoreCardSettings;
  }

  public void setHighscoreCardSettings(String highscoreCardSettings) {
    this.highscoreCardSettings = highscoreCardSettings;
  }

  public String getPinballXSettings() {
    return pinballXSettings;
  }

  public void setPinballXSettings(String pinballXSettings) {
    this.pinballXSettings = pinballXSettings;
  }

  public String getPinballYSettings() {
    return pinballYSettings;
  }

  public void setPinballYSettings(String pinballYSettings) {
    this.pinballYSettings = pinballYSettings;
  }

  public String getOverlaySettings() {
    return overlaySettings;
  }

  public void setOverlaySettings(String overlaySettings) {
    this.overlaySettings = overlaySettings;
  }

  public String getDoNotShowAgains() {
    return doNotShowAgains;
  }

  public void setDoNotShowAgains(String doNotShowAgains) {
    this.doNotShowAgains = doNotShowAgains;
  }

  public String getUiSettings() {
    return uiSettings;
  }

  public void setUiSettings(String uiSettings) {
    this.uiSettings = uiSettings;
  }

  public String getVpsSettings() {
    return vpsSettings;
  }

  public void setVpsSettings(String vpsSettings) {
    this.vpsSettings = vpsSettings;
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

  public boolean isDiscordBotCommandsEnabled() {
    return discordBotCommandsEnabled;
  }

  public void setDiscordBotCommandsEnabled(boolean discordBotCommandsEnabled) {
    this.discordBotCommandsEnabled = discordBotCommandsEnabled;
  }

  public boolean getPinemhiAutoStartEnabled() {
    return pinemhiAutoStartEnabled;
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
  
  public String getPinVolInstallationFolder() {
    return pinVolInstallationFolder;
  }

  public void setPinVolInstallationFolder(String pinVolInstallationFolder) {
    this.pinVolInstallationFolder = pinVolInstallationFolder;
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

  public String getIgnoredValidations() {
    return ignoredValidations;
  }

  public void setIgnoredValidations(String ignoredValidations) {
    this.ignoredValidations = ignoredValidations;
  }

  public String getHighscoreCardTemplates() {
    return highscoreCardTemplates;
  }

  public void setHighscoreCardTemplates(String highscoreCardTemplates) {
    this.highscoreCardTemplates = highscoreCardTemplates;
  }
}

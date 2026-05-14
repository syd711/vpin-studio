package de.mephisto.vpin.restclient;

import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.dof.DOFSettings;
import de.mephisto.vpin.restclient.doflinx.DOFLinxSettings;
import de.mephisto.vpin.restclient.frontend.pinballx.PinballXSettings;
import de.mephisto.vpin.restclient.frontend.pinbally.PinballYSettings;
import de.mephisto.vpin.restclient.games.FilterSettings;
import de.mephisto.vpin.restclient.iscored.IScoredSettings;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
import de.mephisto.vpin.restclient.monitor.MonitoringSettings;
import de.mephisto.vpin.restclient.notifications.NotificationSettings;
import de.mephisto.vpin.restclient.preferences.*;
import de.mephisto.vpin.restclient.recorder.RecorderFilterSettings;
import de.mephisto.vpin.restclient.recorder.RecorderSettings;
import de.mephisto.vpin.restclient.tagging.TaggingSettings;
import de.mephisto.vpin.restclient.validation.IgnoredValidationSettings;
import de.mephisto.vpin.restclient.validation.ValidationSettings;
import de.mephisto.vpin.restclient.vpauthenticators.AuthenticationSettings;
import de.mephisto.vpin.restclient.vpf.VPFSettings;
import de.mephisto.vpin.restclient.vps.VpsSettings;
import de.mephisto.vpin.restclient.vpu.VPUSettings;
import de.mephisto.vpin.restclient.webhooks.WebhookSettings;
import de.mephisto.vpin.restclient.wovp.WOVPSettings;

public interface PreferenceNames {
  String IGNORED_VALIDATION_SETTINGS = "ignoredValidations";
  String VALIDATION_SETTINGS = "validationSettings";

  String SYSTEM_NAME = "systemName";

  String HIGHSCORE_FILTER_ENABLED = "highscoreFilterEnabled";
  String HIGHSCORE_CARD_SETTINGS = "highscoreCardSettings";

  String PAUSE_MENU_SETTINGS = "pauseMenuSettings";

  String IDLE_TIMEOUT = "idleTimeout";
  String AVATAR = "avatar";

  String DOF_SETTINGS = "dofSettings";
  String DOFLINX_SETTINGS = "dofLinxSettings";

  String OVERLAY_SETTINGS = "overlaySettings";

  String UI_SETTINGS = "uiSettings";
  String VPS_SETTINGS = "vpsSettings";
  String SERVER_SETTINGS = "serverSettings";
  String NOTIFICATION_SETTINGS = "notificationSettings";
  String FILTER_SETTINGS = "filterSettings";
  String RECORDINGS_FILTER_SETTINGS = "recordingsFilterSettings";
  String MONITORING_SETTINGS = "monitoringSettings";

  String PINBALLX_SETTINGS = "pinballXSettings";
  String PINBALLY_SETTINGS = "pinballYSettings";
  String VPU_SETTINGS = "vpuSettings";
  String VPF_SETTINGS = "vpfSettings";

  String AUTHENTICATION_SETTINGS = "authenticationSettings";

  String ASSET_SOURCES_SETTINGS = "assetSourcesSettings";

  String DISCORD_BOT_TOKEN = "discordBotToken";
  String DISCORD_GUILD_ID = "discordGuildId";
  String DISCORD_CHANNEL_ID = "discordChannelId";
  String DISCORD_UPDATES_CHANNEL_ID = "discordUpdatesChannelId";
  String DISCORD_CATEGORY_ID = "discordCategoryId";
  String DISCORD_DYNAMIC_SUBSCRIPTIONS = "discordDynamicSubscriptions";
  String DISCORD_BOT_ALLOW_LIST = "discordBotAllowList";
  String DISCORD_BOT_COMMANDS_ENABLED = "discordBotCommandsEnabled";
  String MANIA_SETTINGS = "maniaSettings";
  String TAGGING_SETTINGS = "taggingSettings";

  String RECORDER_SETTINGS = "recorderSettings";
  String WEBHOOK_SETTINGS = "webhookSettings";
  String ISCORED_SETTINGS = "iScoredSettings";

  String RANKING_POINTS = "rankingPoints";
  String ACTIVE_GAME = "activeGame";
  String PREVIEW_ENABLED = "previewEnabled";
  String PINVOL_AUTOSTART_ENABLED = "pinVolAutoStartEnabled";
  String PINVOL_FOLDER = "pinVolInstallationFolder";

  String PINEMHI_AUTOSTART_ENABLED = "pinemhiAutoStartEnabled";

  String BACKUP_SETTINGS = "backupSettings";
  String VPXZ_SETTINGS = "vpxzSettings";
  String VR_SETTINGS = "vrSettings";
  String WOVP_SETTINGS = "wovpSettings";

  static Class<? extends JsonSettings> getClassFromKey(String key) {
      return switch (key) {
          case PreferenceNames.UI_SETTINGS -> UISettings.class;
          case PreferenceNames.SERVER_SETTINGS -> ServerSettings.class;
          case PreferenceNames.HIGHSCORE_CARD_SETTINGS -> CardSettings.class;
          case PreferenceNames.MANIA_SETTINGS -> ManiaSettings.class;
          case PreferenceNames.DOF_SETTINGS -> DOFSettings.class;
          case PreferenceNames.DOFLINX_SETTINGS -> DOFLinxSettings.class;
          case PreferenceNames.PAUSE_MENU_SETTINGS -> PauseMenuSettings.class;
          case PreferenceNames.VALIDATION_SETTINGS -> ValidationSettings.class;
          case PreferenceNames.IGNORED_VALIDATION_SETTINGS -> IgnoredValidationSettings.class;
          case PreferenceNames.NOTIFICATION_SETTINGS -> NotificationSettings.class;
          case PreferenceNames.PINBALLX_SETTINGS -> PinballXSettings.class;
          case PreferenceNames.PINBALLY_SETTINGS -> PinballYSettings.class;
          case PreferenceNames.FILTER_SETTINGS -> FilterSettings.class;
          case PreferenceNames.RECORDINGS_FILTER_SETTINGS -> RecorderFilterSettings.class;
          case PreferenceNames.VPU_SETTINGS -> VPUSettings.class;
          case PreferenceNames.OVERLAY_SETTINGS -> OverlaySettings.class;
          case PreferenceNames.VPF_SETTINGS -> VPFSettings.class;
          case PreferenceNames.BACKUP_SETTINGS -> BackupSettings.class;
          case PreferenceNames.RECORDER_SETTINGS -> RecorderSettings.class;
          case PreferenceNames.MONITORING_SETTINGS -> MonitoringSettings.class;
          case PreferenceNames.WEBHOOK_SETTINGS -> WebhookSettings.class;
          case PreferenceNames.ISCORED_SETTINGS -> IScoredSettings.class;
          case PreferenceNames.VPS_SETTINGS -> VpsSettings.class;
          case PreferenceNames.AUTHENTICATION_SETTINGS -> AuthenticationSettings.class;
          case PreferenceNames.ASSET_SOURCES_SETTINGS -> AuthenticationSettings.class;
          case PreferenceNames.TAGGING_SETTINGS -> TaggingSettings.class;
          case PreferenceNames.WOVP_SETTINGS -> WOVPSettings.class;
          case PreferenceNames.VPXZ_SETTINGS -> VPXZSettings.class;
          case PreferenceNames.VR_SETTINGS -> VRSettings.class;
          default -> throw new UnsupportedOperationException("JSON format not supported for preference '" + key + "'");
      };
  }
}

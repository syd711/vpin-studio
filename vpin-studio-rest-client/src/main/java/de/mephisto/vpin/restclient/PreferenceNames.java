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
import de.mephisto.vpin.restclient.preferences.BackupSettings;
import de.mephisto.vpin.restclient.preferences.OverlaySettings;
import de.mephisto.vpin.restclient.preferences.PauseMenuSettings;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.recorder.RecorderSettings;
import de.mephisto.vpin.restclient.validation.IgnoredValidationSettings;
import de.mephisto.vpin.restclient.validation.ValidationSettings;
import de.mephisto.vpin.restclient.vpauthenticators.AuthenticationSettings;
import de.mephisto.vpin.restclient.vpf.VPFSettings;
import de.mephisto.vpin.restclient.vps.VpsSettings;
import de.mephisto.vpin.restclient.vpu.VPUSettings;
import de.mephisto.vpin.restclient.webhooks.WebhookSettings;

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
  String MONITORING_SETTINGS = "monitoringSettings";

  String PINBALLX_SETTINGS = "pinballXSettings";
  String PINBALLY_SETTINGS = "pinballYSettings";
  String VPU_SETTINGS = "vpuSettings";
  String VPF_SETTINGS = "vpfSettings";

  String AUTHENTICATION_SETTINGS = "authenticationSettings";

  String DISCORD_BOT_TOKEN = "discordBotToken";
  String DISCORD_GUILD_ID = "discordGuildId";
  String DISCORD_CHANNEL_ID = "discordChannelId";
  String DISCORD_UPDATES_CHANNEL_ID = "discordUpdatesChannelId";
  String DISCORD_CATEGORY_ID = "discordCategoryId";
  String DISCORD_DYNAMIC_SUBSCRIPTIONS = "discordDynamicSubscriptions";
  String DISCORD_BOT_ALLOW_LIST = "discordBotAllowList";
  String DISCORD_BOT_COMMANDS_ENABLED = "discordBotCommandsEnabled";
  String MANIA_SETTINGS = "tournamentSettings";

  String RECORDER_SETTINGS = "recorderSettings";
  String WEBHOOK_SETTINGS = "webhookSettings";
  String ISCORED_SETTINGS = "iScoredSettings";

  String RANKING_POINTS = "rankingPoints";
  String ACTIVE_GAME = "activeGame";
  String PREVIEW_ENABLED = "previewEnabled";
  String PINVOL_AUTOSTART_ENABLED = "pinVolAutoStartEnabled";
  String PINEMHI_AUTOSTART_ENABLED = "pinemhiAutoStartEnabled";

  String BACKUP_SETTINGS = "backupSettings";

  static Class<? extends JsonSettings> getClassFromKey(String key) {
    switch (key) {
      case PreferenceNames.UI_SETTINGS: {
        return UISettings.class;
      }
      case PreferenceNames.SERVER_SETTINGS: {
        return ServerSettings.class;
      }
      case PreferenceNames.HIGHSCORE_CARD_SETTINGS: {
        return CardSettings.class;
      }
      case PreferenceNames.MANIA_SETTINGS: {
        return ManiaSettings.class;
      }
      case PreferenceNames.DOF_SETTINGS: {
        return DOFSettings.class;
      }
      case PreferenceNames.DOFLINX_SETTINGS: {
        return DOFLinxSettings.class;
      }
      case PreferenceNames.PAUSE_MENU_SETTINGS: {
        return PauseMenuSettings.class;
      }
      case PreferenceNames.VALIDATION_SETTINGS: {
        return ValidationSettings.class;
      }
      case PreferenceNames.IGNORED_VALIDATION_SETTINGS: {
        return IgnoredValidationSettings.class;
      }
      case PreferenceNames.NOTIFICATION_SETTINGS: {
        return NotificationSettings.class;
      }
      case PreferenceNames.PINBALLX_SETTINGS: {
        return PinballXSettings.class;
      }
      case PreferenceNames.PINBALLY_SETTINGS: {
        return PinballYSettings.class;
      }
      case PreferenceNames.FILTER_SETTINGS: {
        return FilterSettings.class;
      }
      case PreferenceNames.VPU_SETTINGS: {
        return VPUSettings.class;
      }
      case PreferenceNames.OVERLAY_SETTINGS: {
        return OverlaySettings.class;
      }
      case PreferenceNames.VPF_SETTINGS: {
        return VPFSettings.class;
      }
      case PreferenceNames.BACKUP_SETTINGS: {
        return BackupSettings.class;
      }
      case PreferenceNames.RECORDER_SETTINGS: {
        return RecorderSettings.class;
      }
      case PreferenceNames.MONITORING_SETTINGS: {
        return MonitoringSettings.class;
      }
      case PreferenceNames.WEBHOOK_SETTINGS: {
        return WebhookSettings.class;
      }
      case PreferenceNames.ISCORED_SETTINGS: {
        return IScoredSettings.class;
      }
      case PreferenceNames.VPS_SETTINGS: {
        return VpsSettings.class;
      }
      case PreferenceNames.AUTHENTICATION_SETTINGS: {
        return AuthenticationSettings.class;
      }
      default: {
        throw new UnsupportedOperationException("JSON format not supported for preference '" + key + "'");
      }
    }
  }
}

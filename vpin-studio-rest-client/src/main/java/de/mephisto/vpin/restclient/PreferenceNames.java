package de.mephisto.vpin.restclient;

public interface PreferenceNames {
  String IGNORED_VALIDATIONS = "ignoredValidations";
  String IGNORED_MEDIA = "ignoredMedia";

  String SYSTEM_NAME = "systemName";
  String SYSTEM_PRESET = "systemPreset";

  String SYSTEM_PRESET_32_BIT = "32-bit";
  String SYSTEM_PRESET_64_BIT = "64-bit";

  String HIGHSCORE_TITLES = "highscoreTitles";
  String HIGHSCORE_FILTER_ENABLED = "highscoreFilterEnabled";
  String HIGHSCORE_CARD_SETTINGS = "highscoreCardSettings";

  String IDLE_TIMEOUT = "idleTimeout";
  String AVATAR = "avatar";
  String RESET_KEY = "resetKey";

  String OVERLAY_KEY = "overlayKey";
  String OVERLAY_DESIGN = "overlayDesign";
  String OVERLAY_PAGE_URL = "overlayPageUrl";
  String SHOW_OVERLAY_ON_STARTUP = "showOverlayOnStartup";

  String UI_SETTINGS = "uiSettings";
  String UI_HIDE_VERSIONS = "hideVersions";
  String UI_HIDE_VPS_UPDATES = "uiHideVPSUpdates";

  String UI_DO_NOT_SHOW_AGAINS = "doNotShowAgains";
  String UI_DO_NOT_SHOW_AGAIN_COMPONENTS_WARNING = "componentsWarning";
  String UI_DO_NOT_SHOW_AGAIN_CONFIRM_DISMISSALS = "hideDismissConfirmations";
  String UI_DO_NOT_SHOW_AGAIN_CONFIRM_DISMISS_ALL = "hideDimissAllConfirmations";
  String UI_DO_NOT_SHOW_AGAIN_UPDATE_INFO = "updateInfo";

  String DISCORD_BOT_TOKEN = "discordBotToken";
  String DISCORD_GUILD_ID = "discordGuildId";
  String DISCORD_CHANNEL_ID = "discordChannelId";
  String DISCORD_CATEGORY_ID = "discordCategoryId";
  String DISCORD_DYNAMIC_SUBSCRIPTIONS = "discordDynamicSubscriptions";
  String DISCORD_BOT_ALLOW_LIST = "discordBotAllowList";
  String DISCORD_BOT_COMMANDS_ENABLED = "discordBotCommandsEnabled";
  String DISCORD_VPS_CHANNEL_ID = "discordVpsChannelId";
  String DISCORD_VPS_TABLE_FILTER_ENABLED = "discordVpsTableFilterEnabled";
  String DISCORD_VPS_REFRESH_INTERVAL_MIN = "discordVpsRefreshIntervalMin";

  String TOURNAMENTS_ENABLED = "tournamentEnabled";
  String TOURNAMENTS_DISCORD_LINK = "tournamentDiscordLink";
  String TOURNAMENTS_DASHBOARD_URL = "tournamentDashboardUrl";
  String TOURNAMENTS_DESCRIPTION = "tournamentDescription";

  String RANKING_POINTS = "rankingPoints";
  String ACTIVE_GAME = "activeGame";
  String PREVIEW_ENABLED = "previewEnabled";
  String PINVOL_AUTOSTART_ENABLED = "pinVolAutoStartEnabled";
  String PINEMHI_AUTOSTART_ENABLED = "pinemhiAutoStartEnabled";

  String VPBM_INTERNAL_HOST_IDENTIFIER = "vpbmInternalHostId";
  String VPBM_EXTERNAL_HOST_IDENTIFIER = "vpbmExternalHostId";
}

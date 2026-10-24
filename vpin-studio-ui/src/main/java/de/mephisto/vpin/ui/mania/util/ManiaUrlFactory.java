package de.mephisto.vpin.ui.mania.util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ManiaUrlFactory {

  private final static String BASE_URL = "https://app.vpin-mania.net/";
  private final static String DISCORD_BOT_INVITE_URL = "https://discord.com/oauth2/authorize?client_id=1562837168144253042";

  public static String createSettingsUrl() {
    return BASE_URL + "settings";
  }

  /**
   * The URL to add the VPin Mania Community Bot to a Discord server.
   */
  public static String createDiscordBotInviteUrl() {
    return DISCORD_BOT_INVITE_URL;
  }

  public static String createTableUrl(@NonNull String vpsTableId, @Nullable String vpsVersionId) {
    if (vpsVersionId != null) {
      return BASE_URL + "table/" + vpsTableId + "/" + vpsVersionId;
    }
    return BASE_URL + "table/" + vpsTableId;
  }
}

package de.mephisto.vpin.connectors.discord;

import net.dv8tion.jda.api.Permission;

public enum Permissions {
  MANAGE_CHANNEL,
  VIEW_CHANNEL,
  MESSAGE_SEND,
  MESSAGE_MANAGE,
  MESSAGE_EMBED_LINKS,
  MESSAGE_ATTACH_FILES,
  MESSAGE_HISTORY;

  static Permission toPermission(Permissions p) {
    return Permission.valueOf(p.name());
  }
}

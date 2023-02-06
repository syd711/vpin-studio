package de.mephisto.vpin.connectors.discord;

import net.dv8tion.jda.api.events.StatusChangeEvent;

public interface DiscordStatusListener {

  void onDisconnect();
}

package de.mephisto.vpin.server.discord;

import de.mephisto.vpin.connectors.discord.BotCommand;
import de.mephisto.vpin.connectors.discord.BotCommandResponse;

public interface DiscordBotCommandListener {

  BotCommandResponse onBotCommand(BotCommand cmd);
}

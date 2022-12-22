package de.mephisto.vpin.connectors.discord;

public interface DiscordCommandResolver {

  BotCommandResponse resolveCommand(BotCommand cmd);
}

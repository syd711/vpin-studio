package de.mephisto.vpin.connectors.discord;

public class BotCommand {
  public final static String CMD_COMMANDS = "commands";
  public final static String CMD_HELP = "help";
  public final static String CMD_RECENT = "recent";
  public final static String CMD_COMPETITIONS = "competitions";
  public final static String CMD_HS = "hs";
  public final static String CMD_FIND = "find";
  public final static String CMD_RANKS = "ranks";
  public final static String CMD_PLAYER = "player";

  private DiscordCommandResolver commandResolver;

  private String content;
  private String command;
  private String parameter;
  private long serverId;

  public BotCommand(long serverId, String content, DiscordCommandResolver commandResolver) {
    this.serverId = serverId;
    this.content = content;
    this.commandResolver = commandResolver;
    command = content.trim().substring(1);
    if (content.contains(" ")) {
      command = content.substring(1, content.indexOf(" ")).trim();
      parameter = content.substring(content.indexOf(" ")).trim();
    }
  }

  public long getServerId() {
    return serverId;
  }

  public String getContent() {
    return content;
  }

  public String getParameter() {
    return parameter;
  }

  public BotCommandResponse execute() {
    return commandResolver.resolveCommand(this);
  }

  public String getCommand() {
    return command;
  }

  @Override
  public String toString() {
    return "Bot Command '" + content + "'";
  }
}

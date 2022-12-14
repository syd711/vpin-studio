package de.mephisto.vpin.connectors.discord;

public class BotCommand {
  public final static String CMD_COMPETITIONS = "competitions";
  public final static String CMD_HS = "hs";
  public final static String CMD_RANKING = "rank";

  private DiscordCommandResolver commandResolver;

  private String content;
  private String command;
  private String parameter;

  public BotCommand(String content, DiscordCommandResolver commandResolver) {
    this.content = content;
    this.commandResolver = commandResolver;
    command = content.trim().substring(1);
    if (content.contains(" ")) {
      command = content.substring(1, content.indexOf(" ")).trim();
      parameter = content.substring(content.indexOf(" ")).trim();
    }
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

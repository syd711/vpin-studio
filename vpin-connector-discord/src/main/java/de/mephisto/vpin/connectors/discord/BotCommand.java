package de.mephisto.vpin.connectors.discord;

public class BotCommand {
  public final static String CMD_COMPETITIONS = "competitions";
  public final static String CMD_HS = "hs";
  public final static String CMD_RANKING = "ranking";

  private String content;
  private DiscordCommandResolver commandResolver;

  private String command;
  private String parameter;

  public BotCommand(String content, DiscordCommandResolver commandResolver) {
    this.content = content;
    this.commandResolver = commandResolver;
    command = content.trim().substring(1);
    if (content.contains(" ")) {
      String[] s = content.split(" ");
      if (s.length == 2) {
        command = s[0];
        parameter = s[1];
      }
    }
  }

  public BotCommandResponse execute() {
    return commandResolver.resolveCommand(this);
  }

  public String getName() {
    return command;
  }

  @Override
  public String toString() {
    return "Bot Command '" + content + "'";
  }
}

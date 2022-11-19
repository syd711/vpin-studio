package de.mephisto.vpin.connectors.discord;

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class JDAListener extends ListenerAdapter {

  @Override
  public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
    super.onGuildMemberJoin(event);
  }

  @Override
  public void onGuildMemberUpdate(@NotNull GuildMemberUpdateEvent event) {
    super.onGuildMemberUpdate(event);
    String effectiveName = event.getMember().getEffectiveName();
    System.out.println("New Name " + effectiveName);
  }
}

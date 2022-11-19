package de.mephisto.vpin.connectors.discord;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.concurrent.Task;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * BOT link:
 * https://discord.com/api/oauth2/authorize?client_id=1043190061165989979&permissions=2048&scope=bot
 */
public class DiscordClient {



  private DiscordClient() {

  }

  public static DiscordClient create() {

    String token = "MTA0MzE5MDA2MTE2NTk4OTk3OQ.GiolyO.WEedUxa5ijfnfjjtMwTKI03TPltTEKKILBM51g";
    String channelId = "1043199618172858500";

    try {
      JDA jda = JDABuilder.createDefault(token, Arrays.asList(GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS))
          .setEventPassthrough(true)
          .build();
      jda.awaitReady();

      List<Guild> guilds = jda.getGuilds();
      jda.addEventListener(new JDAListener());

      Guild guildById = jda.getGuildById(channelId);
      System.out.println("Guild: " + guildById);
      for (Guild guild : guilds) {
        System.out.println(guild.getName());
        guild.loadMembers().onSuccess(new Consumer<List<Member>>() {
          @Override
          public void accept(List<Member> members) {
            for (Member member : members) {
              String avatarUrl = member.getAvatarUrl();
              String nick  = member.getNickname();
              System.out.print(member.getEffectiveName() + " / ");
              System.out.println(member.getEffectiveAvatarUrl());

            }
          }
        });


        List<GuildChannel> channels = guild.getChannels();
        for (GuildChannel channel : channels) {
          if(channel instanceof TextChannel) {
            System.out.println(channel.getName());
            TextChannel textChannel = (TextChannel) channel;
//            textChannel.sendMessage("bubu").queue();
          }
        }

      }
      Thread.sleep(1000000);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }


    String url = "https://discord.com/api/webhooks/1043202008133423186/zWtLf7jqPi5C8GrNqB-svt9xQSJ6QFzQmPrEGBZ3ugezYy6NqvgK03EvZyJJTOTUlkR0";

    DiscordWebhook hook = new DiscordWebhook(url);
    hook.setTts(false);
    hook.setContent("bubu2");
    try {
//      hook.execute();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return new DiscordClient();
  }
}

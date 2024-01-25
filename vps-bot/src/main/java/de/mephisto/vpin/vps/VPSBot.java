package de.mephisto.vpin.vps;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.VpsSheetChangedListener;
import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.connectors.vps.VpsDiffer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.text.DateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class VPSBot implements VpsSheetChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(VPSBot.class);
  public static final int MAX_VPS_ENTRIES = 20;

  private final JDA jda;
  private final VPSDiscordListenerAdapter listenerAdapter;
  private final long botId;

  public VPSBot() throws InterruptedException {
    this.listenerAdapter = new VPSDiscordListenerAdapter(this);

    String token = System.getenv("VPS_BOT_TOKEN");
    jda = JDABuilder.createDefault(token, Arrays.asList(GatewayIntent.DIRECT_MESSAGES,
        GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT))
      .setEventPassthrough(true)
      .setStatus(OnlineStatus.ONLINE)
      .setMemberCachePolicy(MemberCachePolicy.ALL)
      .addEventListeners(this.listenerAdapter)
      .build();
    jda.awaitReady();

    this.botId = jda.getSelfUser().getIdLong();
    VPS.getInstance().addChangeListener(this);

    new Thread(() -> {
      Thread.currentThread().setName("VPS Sync Thread");
      while (true) {
        VPS.getInstance().update();
        try {
          LOG.info("Waiting 60 minutes");
          TimeUnit.MINUTES.sleep(60);
        } catch (InterruptedException e) {
          //
        }
      }
    }).start();
  }

  @Override
  public void vpsSheetChanged(List<VpsDiffer> diff) {
    new Thread(() -> {
      Thread.currentThread().setName("VPS Discord Notifier");
      try {
        if (!diff.isEmpty()) {
          Map<String, String> entries = new HashMap<>();
          int counter = 0;
          for (VpsDiffer tableDiff : diff) {
            counter++;

            List<VpsDiffTypes> differences = tableDiff.getDifferences();
            if (differences.isEmpty()) {
              continue;
            }

            String value = "\n" + String.join("", differences.stream().map(d -> "- " + d.toString() + "\n").collect(Collectors.toList()));
            String title = tableDiff.getDisplayName() + "    [" + DateFormat.getDateInstance().format(tableDiff.getLastModified()) + "]\n" + VPS.getVpsTableUrl(tableDiff.getId());
            entries.put(title, value);

            if (entries.size() > MAX_VPS_ENTRIES) {
              sendVpsUpdateSummary("VPS Update Summary", entries);
              counter = 0;
              entries.clear();
            }
          }
        }
      } catch (Exception e) {
        LOG.error("Failed to push Discord notifications for VPS updates: " + e.getMessage(), e);
      }
    }).start();
  }

//  private void sendVpsUpdateFull(String title, Date updated, String imgUrl, String gameLink, Map<String, String> fields) {
//    long serverId = 1099008746711175288l;
//    long vpsChannelId = 1198307431181193296l;
//    Guild guild = getGuild(serverId);
//    if (guild != null) {
//      TextChannel textChannel = jda.getChannelById(TextChannel.class, vpsChannelId);
//      if (textChannel != null) {
//        EmbedBuilder embed = new EmbedBuilder();
//        embed.setTitle(title);
//        embed.setDescription("**" + DateFormat.getDateInstance().format(updated) + "**");
//        try {
//          embed.setImage(imgUrl);
//        } catch (Exception e) {
//          LOG.info("Failed to attach image: '" + imgUrl + "' " + e.getMessage());
//        }
//        Set<Map.Entry<String, String>> entries = fields.entrySet();
//        for (Map.Entry<String, String> entry : entries) {
//          embed.addField(entry.getKey(), entry.getValue(), false);
//        }
//        embed.setColor(Color.GREEN);
//
//        textChannel.sendMessage("").addActionRow(
//          Button.link(gameLink, "Table")).setEmbeds(embed.build()).queue();
//      }
//      else {
//        LOG.error("No discord channel found.");
//      }
//    }
//    else {
//      throw new UnsupportedOperationException("No guild found for default guildId '" + serverId + "'");
//    }
//  }

  public long sendVpsUpdateSummary(String title, Map<String, String> values) {
    long serverId = Long.parseLong(System.getenv("VPS_BOT_SERVER"));
    long vpsChannelId = Long.parseLong(System.getenv("VPS_BOT_CHANNEL"));
    Guild guild = getGuild(serverId);
    if (guild != null) {
      TextChannel textChannel = jda.getChannelById(TextChannel.class, vpsChannelId);
      if (textChannel != null) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(title);
        embed.setDescription("**The following tables have updates:**");

        Set<Map.Entry<String, String>> entries = values.entrySet();
        for (Map.Entry<String, String> entry : entries) {
          embed.addField(entry.getKey(), entry.getValue(), false);
        }
        embed.setColor(Color.GREEN);

        Message complete = textChannel.sendMessage("").setEmbeds(embed.build()).complete();
        return complete.getIdLong();
      }
    }
    else {
      throw new UnsupportedOperationException("No guild found for default guildId '" + serverId + "'");
    }
    return -1;
  }

  private Guild getGuild(long serverId) {
    return jda.getGuildById(serverId);
  }

  public void shutdown() {
    jda.shutdownNow();
    LOG.info("VPS bot shutdown.");
  }
}

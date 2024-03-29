package de.mephisto.vpin.vps;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.VpsDiffer;
import de.mephisto.vpin.connectors.vps.VpsSheetChangedListener;
import de.mephisto.vpin.connectors.vps.model.VPSChange;
import de.mephisto.vpin.connectors.vps.model.VPSChanges;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.text.DateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class VPSBot implements VpsSheetChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(VPSBot.class);
  public static final int MAX_VPS_ENTRIES = 15;

  private final JDA jda;
  private final VPSDiscordListenerAdapter listenerAdapter;
  private Date lastUpdate = new Date();
  private int totalDiffCount = 0;

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
  public void vpsSheetChanged(List<VpsDiffer> tableDiffs) {
    VPS.getInstance().reload();

    LOG.info("VPS Bot emitting " + tableDiffs.size() + " updates");
    new Thread(() -> {
      Thread.currentThread().setName("VPS Discord Notifier");
      try {
        if (!tableDiffs.isEmpty()) {
          lastUpdate = new Date();

          Map<String, String> entries = new HashMap<>();
          int counter = 0;
          for (VpsDiffer tableDiff : tableDiffs) {
            counter++;

            VPSChanges changes = tableDiff.getChanges();
            if (changes.isEmpty()) {
              LOG.info("Skipped updated for \"" + tableDiff.getDisplayName() + "\", no updates found.");
              continue;
            }

            StringBuilder builder = new StringBuilder();
            for (VPSChange change : changes.getChanges()) {
              builder.append(change.toString(tableDiff.getId()));
              builder.append("\n");
            }

            String title = tableDiff.getDisplayName() + "    [" + DateFormat.getDateInstance().format(tableDiff.getLastModified()) + "]\n" + VPS.getVpsTableUrl(tableDiff.getId());
            entries.put(title, builder.toString());

            if (entries.size() > MAX_VPS_ENTRIES) {
              sendVpsUpdateSummary("VPS Update Summary", entries);
              counter = 0;
              entries.clear();
            }
          }

          if (!entries.isEmpty()) {
            sendVpsUpdateSummary("VPS Update Summary", entries);
          }
        }
      } catch (Exception e) {
        LOG.error("Failed to push Discord notifications for VPS updates: " + e.getMessage(), e);
      }
    }).start();
  }

  public long sendVpsUpdateSummary(String title, Map<String, String> values) {
    totalDiffCount += values.size();

    long serverId = Long.parseLong(System.getenv("VPS_BOT_SERVER"));
    long vpsChannelId = Long.parseLong(System.getenv("VPS_BOT_CHANNEL"));
    Guild guild = getGuild(serverId);
    LOG.info("Sending update to server " + guild);
    if (guild != null) {
      StandardGuildMessageChannel textChannel = jda.getNewsChannelById(vpsChannelId);
      //development workaround
      if (textChannel == null) {
        textChannel = jda.getTextChannelById(vpsChannelId);
      }
      LOG.info("Sending update to channel " + textChannel + " (" + vpsChannelId + ")");
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
        long idLong = complete.getIdLong();
        LOG.info("Message completed, sending crosspost next.");
        if (textChannel instanceof NewsChannel) {
          Message complete1 = ((NewsChannel) textChannel).crosspostMessageById(idLong).complete();
          LOG.info("Crossposted message completed.");
        }
        return idLong;
      }
      else {
        LOG.error("No text channel found: '" + vpsChannelId + "'");
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

  public String getStatus() {
    return "Last Update: " + DateFormat.getDateTimeInstance().format(lastUpdate) + "\nTotal Changes: " + totalDiffCount;
  }
}

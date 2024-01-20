package de.mephisto.vpin.vps;

import de.mephisto.vpin.connectors.vps.model.VpsTableDiff;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class VPSDiscordListenerAdapter extends ListenerAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(VPSDiscordListenerAdapter.class);
  private final VPSBot bot;

  public VPSDiscordListenerAdapter(VPSBot bot) {
    this.bot = bot;
  }


  @Override
  public void onReady(ReadyEvent event) {

  }
  /******************** Listener Methods ******************************************************************************/


  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    if (event.getAuthor().isBot()) {
      return;
    }

    Message message = event.getMessage();
    String content = message.getContentRaw();
    if(content.trim().equals("/reload")) {
      List<VpsTableDiff> sync = this.bot.sync();
      MessageChannel channel = event.getChannel();
      if(sync != null) {
        channel.sendMessage("Synchronization successful, found " + sync.size() + " update(s).").complete();
      }
      else {
        channel.sendMessage("Command failed, try again later.").complete();
      }
    }
  }
}

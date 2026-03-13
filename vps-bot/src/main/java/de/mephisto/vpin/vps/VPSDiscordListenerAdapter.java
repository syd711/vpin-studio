package de.mephisto.vpin.vps;

import de.mephisto.vpin.connectors.vps.VpsDiffer;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;

public class VPSDiscordListenerAdapter extends ListenerAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
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
    if (content.trim().equals("/reload")) {
      List<VpsDiffer> diffs = bot.update();
      MessageChannel channel = event.getChannel();
      if (diffs != null) {
        channel.sendMessage("Synchronization successful, found " + diffs.size() + " update(s).").complete();
      }
      else {
        channel.sendMessage("Command failed, try again later.").complete();
      }
    }
    else if (content.trim().equals("/status")) {
      String status = this.bot.getStatus();
      MessageChannel channel = event.getChannel();
      channel.sendMessage(status).complete();
    }
  }
}

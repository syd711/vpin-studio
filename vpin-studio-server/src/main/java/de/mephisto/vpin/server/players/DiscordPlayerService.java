package de.mephisto.vpin.server.players;

import de.mephisto.vpin.connectors.discord.DiscordClient;
import de.mephisto.vpin.connectors.discord.DiscordMember;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

@Service
public class DiscordPlayerService implements InitializingBean, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(DiscordPlayerService.class);

  private DiscordClient discordClient;

  @Autowired
  private PreferencesService preferencesService;

  private List<DiscordMember> lastMembers;

  public List<DiscordMember> getMembers() {
    return this.discordClient.getMembers();
  }

  public boolean isEnabled() {
    return this.discordClient != null;
  }

  private DiscordClient createDiscordClient() {
    String botToken = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_BOT_TOKEN);
    String guildId = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_GUILD_ID);
    try {
      if (this.discordClient != null) {
        this.discordClient.shutdown();
      }
      this.discordClient = null;
    } catch (Exception e) {
      LOG.warn("Error in JDA shutdown: " + e.getMessage());
    }

    try {
      if (!StringUtils.isEmpty(botToken) && !StringUtils.isEmpty(guildId)) {
        this.discordClient = new DiscordClient(botToken, guildId);
      }
      else {
        LOG.info("Skipped discord client creation, no botId or guildId set.");
      }
    } catch (Exception e) {
      LOG.error("Failed to create discord client: " + e.getMessage() + ". Try to update your settings to create a valid client.");
    }
    return this.discordClient;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    preferencesService.addChangeListener(this);
    this.createDiscordClient();
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) {
    if (propertyName.equals(PreferenceNames.DISCORD_GUILD_ID) || propertyName.equals(PreferenceNames.DISCORD_BOT_TOKEN)) {
      String botToken = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_BOT_TOKEN);
      String guildId = (String) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_GUILD_ID);

      if (!StringUtils.isEmpty(botToken) && !StringUtils.isEmpty(guildId)) {
        LOG.info("Re-creating discord client because of preference changes.");
        this.discordClient = createDiscordClient();
      }
    }
  }

  public boolean refreshMembers() {
    this.lastMembers = null;
    if (this.discordClient != null) {
      Consumer<List<DiscordMember>> consumer = discordMembers -> {
        lastMembers = discordMembers;
      };

      this.discordClient.refreshMembers(consumer, throwable -> notify());
      return this.lastMembers != null;
    }
    return false;
  }
}

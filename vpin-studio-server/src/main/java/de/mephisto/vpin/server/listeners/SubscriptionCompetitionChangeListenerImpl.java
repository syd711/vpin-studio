package de.mephisto.vpin.server.listeners;

import de.mephisto.vpin.connectors.discord.DiscordMember;
import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.discord.DiscordChannel;
import de.mephisto.vpin.server.assets.AssetService;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.discord.DiscordChannelMessageFactory;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.discord.DiscordSubscriptionMessageFactory;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.popper.PopperService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionCompetitionChangeListenerImpl extends DefaultCompetitionChangeListener implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionChangeListenerImpl.class);

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private DiscordService discordService;

  @Autowired
  private GameService gameService;

  @Autowired
  private PopperService popperService;

  @Autowired
  private AssetService assetService;

  @Autowired
  private DiscordSubscriptionMessageFactory discordSubscriptionMessageFactory;

  @Override
  public void competitionCreated(@NonNull Competition competition) {
    if (competition.getType().equals(CompetitionType.SUBSCRIPTION.name())) {
      Game game = gameService.getGame(competition.getGameId());
      boolean isOwner = competition.getOwner().equals(String.valueOf(discordService.getBotId()));
      DiscordMember bot = discordService.getBot();
      if (game != null && bot != null) {
        if (competition.isHighscoreReset()) {
          highscoreService.resetHighscore(game);
        }

        if (isOwner) {
          DiscordChannel subscriptionChannel = discordService.createSubscriptionChannel(competition, game);
          LOG.info("Created text channel " + subscriptionChannel);
          if (subscriptionChannel != null) {
            competition.setDiscordChannelId(subscriptionChannel.getId());
            competitionService.save(competition);

            long botId = discordService.getBotId();
            long serverId = competition.getDiscordServerId();
            long channelId = subscriptionChannel.getId();

            byte[] image = assetService.getSubscriptionCard(competition, game);
            String message = discordSubscriptionMessageFactory.createSubscriptionCreatedMessage(competition.getDiscordServerId(), botId);

            long messageId = discordService.sendMessage(serverId, channelId, message, image, competition.getName() + ".png", "The latest highscores will be pinned on this channel.");
            discordService.initCompetition(serverId, channelId, messageId);

            Optional<Highscore> highscore = highscoreService.getOrCreateHighscore(game);
            if(highscore.isPresent()) {
              String msg = discordSubscriptionMessageFactory.createFirstSubscriptionHighscoreMessage(game, competition, highscore.get().getRaw());
              long newHighscoreMessageId = discordService.sendMessage(serverId, channelId, msg);
              discordService.updateHighscoreMessage(serverId, channelId, newHighscoreMessageId);
            }
          }
        }
      }
    }
  }

  @Override
  public void competitionDeleted(@NonNull Competition competition) {
    if (competition.getType().equals(CompetitionType.SUBSCRIPTION.name())) {
      Game game = gameService.getGame(competition.getGameId());
      if (game != null) {
        runCheckedDeAugmentation(competitionService, gameService, popperService);

        long serverId = competition.getDiscordServerId();
        boolean isOwner = competition.getOwner().equals(String.valueOf(discordService.getBotId()));
        if (isOwner) {
          int count = 0;
          String rom = game.getRom();

          List<Competition> subscriptions = this.competitionService.getSubscriptions();
          for (Competition subscription : subscriptions) {
            Game g = gameService.getGame(subscription.getGameId());
            if (g != null) {
              String subscriptionRom = g.getRom();
              if (subscriptionRom != null && subscriptionRom.equals(rom)) {
                count++;
              }
            }
          }

          if (count > 1) {
            LOG.warn("There are multiple subscriptions for the same table, skipping channel deletion");
            return;
          }

          List<DiscordChannel> channels = discordService.getChannels(competition.getDiscordServerId());
          for (DiscordChannel channel : channels) {
            String channelName = channel.getName();
            if (channelName.endsWith("§" + game.getRom())) {
              discordService.deleteChannel(serverId, channel.getId());
            }
          }
        }
      }
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    competitionService.addCompetitionChangeListener(this);
  }
}

package de.mephisto.vpin.server.listeners;

import de.mephisto.vpin.connectors.discord.DiscordMember;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.discord.DiscordChannel;
import de.mephisto.vpin.server.assets.AssetService;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionLifecycleService;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.discord.DiscordSubscriptionMessageFactory;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreBackupService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.parsing.HighscoreParsingService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
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
  private CompetitionLifecycleService competitionLifecycleService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private DiscordService discordService;

  @Autowired
  private GameService gameService;

  @Autowired
  private FrontendStatusService frontendStatusService;

  @Autowired
  private AssetService assetService;

  @Autowired
  private DiscordSubscriptionMessageFactory discordSubscriptionMessageFactory;

  @Autowired
  private HighscoreParsingService highscoreParser;

  @Autowired
  private HighscoreBackupService highscoreBackupService;

  @Override
  public void competitionCreated(@NonNull Competition competition) {
    if (competition.getType().equals(CompetitionType.ISCORED.name())) {
      Game game = gameService.getGame(competition.getGameId());
      if (game != null) {
        if (competition.isHighscoreReset()) {
          if (highscoreBackupService.backup(game)) {
            highscoreService.resetHighscore(game);
          }
        }
      }
    }
    else if (competition.getType().equals(CompetitionType.SUBSCRIPTION.name())) {
      try {
        Game game = gameService.getGame(competition.getGameId());
        boolean isOwner = competition.getOwner().equals(String.valueOf(discordService.getBotId()));
        DiscordMember bot = discordService.getBot();
        if (game != null && bot != null) {
          if (competition.isHighscoreReset()) {
            if (highscoreBackupService.backup(game)) {
              highscoreService.resetHighscore(game);
            }
          }

          long botId = discordService.getBotId();
          long serverId = competition.getDiscordServerId();

          if (isOwner) {
            //check if the channel already exists, then simply re-join
            DiscordChannel subscriptionChannel = discordService.getSubscriptionChannel(competition, game);
            if (subscriptionChannel == null) {
              subscriptionChannel = discordService.createSubscriptionChannel(competition, game);
            }
            else {
              joinCompetition(competition, bot);
              return;
            }

            LOG.info("Created text channel " + subscriptionChannel);
            if (subscriptionChannel != null) {
              long channelId = subscriptionChannel.getId();

              competition.setDiscordChannelId(subscriptionChannel.getId());
              competitionService.save(competition);

              Optional<Highscore> highscore = highscoreService.getHighscore(game, true, EventOrigin.USER_INITIATED);

              byte[] image = assetService.getSubscriptionCard(competition, game);
              String message = discordSubscriptionMessageFactory.createSubscriptionCreatedMessage(competition.getDiscordServerId(), botId, competition.getUuid());

              long messageId = discordService.sendMessage(serverId, channelId, message, image, competition.getName() + ".png", "The subscription channel for table \"" + competition.getName() + "\" has been created.\n" +
                  "New highscores for this table will be posted here.\nOther player bots can subscribe to this channel.\nTheir highscores will compete with yours.");
              discordService.initCompetition(serverId, channelId, messageId, null);

              if (!competition.isHighscoreReset()) {
                if (highscore.isPresent()) {
                  Highscore hs = highscore.get();
                  List<Score> scores = highscoreParser.parseScores(hs.getCreatedAt(), hs.getRaw(), game, serverId);

                  if (!scores.isEmpty()) {
                    String msg = discordSubscriptionMessageFactory.createFirstSubscriptionHighscoreMessage(game, competition, scores.get(0), competition.getScoreLimit());
                    long newHighscoreMessageId = discordService.sendMessage(serverId, channelId, msg);
                    discordService.updateHighscoreMessage(serverId, channelId, newHighscoreMessageId);
                  }
                }
              }
            }
          }
          else {
            joinCompetition(competition, bot);
          }
        }
      }
      catch (Exception e) {
        LOG.error("Error creating table subscription: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public void competitionDeleted(@NonNull Competition competition) {
    if (competition.getType().equals(CompetitionType.SUBSCRIPTION.name())) {
      Game game = gameService.getGame(competition.getGameId());
      if (game != null) {
        runCheckedDeAugmentation(competitionService, gameService, frontendStatusService);

        long serverId = competition.getDiscordServerId();
        boolean isOwner = competition.getOwner().equals(String.valueOf(discordService.getBotId()));
        if (isOwner) {
          int count = 0;
          String rom = game.getRom();
          if (StringUtils.isEmpty(rom)) {
            rom = game.getTableName();
          }

          List<Competition> subscriptions = this.competitionService.getSubscriptions();
          for (Competition subscription : subscriptions) {
            Game g = gameService.getGame(subscription.getGameId());
            if (g != null) {
              String subscriptionRom = g.getRom();
              if (StringUtils.isEmpty(subscriptionRom)) {
                subscriptionRom = g.getTableName();
              }

              if (subscriptionRom != null && subscriptionRom.equalsIgnoreCase(rom)) {
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
            if (channelName.endsWith("§" + rom.toLowerCase())) {
              discordService.deleteChannel(serverId, channel.getId());
            }
          }
        }
      }
    }
  }

  private void joinCompetition(@NonNull Competition competition, DiscordMember bot) {
    //the bot is not the owner, so it has joined the subscription OR re-joined it
    long msgId = discordService.sendMessage(competition.getDiscordServerId(), competition.getDiscordChannelId(), discordSubscriptionMessageFactory.createSubscriptionJoinedMessage(competition, bot));
    discordService.addCompetitionPlayer(competition.getDiscordServerId(), competition.getDiscordChannelId(), msgId);
    LOG.info("Discord bot \"" + bot + "\" has joined \"" + competition + "\"");
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    competitionLifecycleService.addCompetitionChangeListener(this);
  }
}

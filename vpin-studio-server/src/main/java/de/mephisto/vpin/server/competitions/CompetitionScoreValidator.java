package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.JoinMode;
import de.mephisto.vpin.restclient.discord.DiscordBotStatus;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionData;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.vpx.VPXUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class CompetitionScoreValidator {

  @Nullable
  public static String validate(@Nullable DiscordCompetitionData competitionData, @NonNull Game game, @NonNull Competition competition, @NonNull Score newScore, DiscordBotStatus botStatus) {
    if (competition.getType().equals(CompetitionType.DISCORD.name()) && competitionData == null) {
      return "Failed to submit highscore, because no competion data was found.";
    }

    if (newScore.getPlayerInitials().contains("?")) {
      return "Highscore update has been skipped, initials with '?' are filtered.";
    }

    if (!newScore.getPlayerInitials().equalsIgnoreCase(botStatus.getBotInitials())) {
      return "Highscore update has been skipped, the initials '" + newScore.getPlayerInitials() + "' do not belong to the our bot ('" + botStatus.getBotInitials() + "').";
    }

    if(competitionData != null) {
      String mode = competitionData.getMode();
      String competitionRom = competitionData.getRom();

      String rom = game.getRom();
      if (!competitionRom.equalsIgnoreCase(rom)) {
        return "The ROM name of your table \"" + game.getGameDisplayName() + "\" does not match with \"" + competitionRom + "\".";
      }

      if (mode != null) {
        if (JoinMode.valueOf(mode).equals(JoinMode.STRICT)) {
          long tableSize = game.getGameFileSize();
          long competitionTableSize = competitionData.getFs();
          long min = competitionTableSize - (1024 * 1024);
          long max = competitionTableSize + (1024 * 1024);
          if (tableSize < min || tableSize > max) {
            return "The file size of table \"" + game.getGameDisplayName() + "\" differs by ~" + FileUtils.readableFileSize(competitionTableSize - tableSize) + ".";
          }
        }

        if (JoinMode.valueOf(mode).equals(JoinMode.CHECKSUM)) {
          String checksum = VPXUtil.getChecksum(game.getGameFile());
          String chksm = competitionData.getChksm();
          if (!chksm.equalsIgnoreCase(checksum)) {
            return "Your VPX script checksum of table \"" + game.getGameDisplayName() + "\" does not match with the one of the competition.";
          }
        }
      }
    }

    return null;
  }
}

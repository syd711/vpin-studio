package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.restclient.JoinMode;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionData;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.vpx.VPXUtil;
import edu.umd.cs.findbugs.annotations.Nullable;

public class CompetitionScoreValidator {

  @Nullable
  public static String validate(DiscordCompetitionData data, Game game) {
    String mode = data.getMode();
    String competitionRom = data.getRom();
    String msg = null;

    String rom = game.getRom();
    if (!competitionRom.equalsIgnoreCase(rom)) {
      msg = "The ROM name of your table \"" + game.getGameDisplayName() + "\" does not match with \"" + competitionRom + "\".";
    }

    if (JoinMode.valueOf(mode).equals(JoinMode.STRICT)) {
      long tableSize = game.getGameFileSize();
      long competitionTableSize = data.getFs();
      long min = competitionTableSize - (1024 * 1024);
      long max = competitionTableSize + (1024 * 1024);
      if (tableSize < min || tableSize > max) {
        msg = "The file size of table \"" + game.getGameDisplayName() + "\" differs by ~" + FileUtils.readableFileSize(competitionTableSize - tableSize) + ".";
      }
    }
    else if (JoinMode.valueOf(mode).equals(JoinMode.CHECKSUM)) {
      String checksum = VPXUtil.getChecksum(game.getGameFile());
      String chksm = data.getChksm();
      if (!chksm.equalsIgnoreCase(checksum)) {
        msg = "Your VPX script checksum of table \"" + game.getGameDisplayName() + "\" does not match with the one of the competition.";
      }
    }

    return msg;
  }
}

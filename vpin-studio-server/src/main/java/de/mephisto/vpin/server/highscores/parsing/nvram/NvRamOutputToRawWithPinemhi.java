package de.mephisto.vpin.server.highscores.parsing.nvram;

import de.mephisto.vpin.server.highscores.parsing.nvram.adapters.*;
import de.mephisto.vpin.server.pinemhi.PINemHiService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NvRamOutputToRawWithPinemhi implements NvRamOutputToRaw {
  private final static Logger LOG = LoggerFactory.getLogger(NvRamOutputToRawWithPinemhi.class);

  private final static List<ScoreNvRamAdapter> adapters = new ArrayList<>();

  static {
    adapters.add(new SinglePlayerScoreAdapter("algar_l1.nv", 1));
    adapters.add(new SinglePlayerScoreAdapter("alienstr.nv", 1));
    adapters.add(new SinglePlayerScoreAdapter("alpok_b6.nv", 1));
    adapters.add(new FourColumnScoreAdapter("monopoly.nv"));
    adapters.add(new SkipFirstListScoreAdapter("godzilla.nv"));
    adapters.add(new NewLineAfterFirstScoreAdapter("kiko_a10.nv"));
    adapters.add(new Anonymous5PlayerScoreAdapter("punchy.nv"));
    adapters.add(new FixTitleScoreAdapter("rs_l6.nv", "TODAY'S HIGHEST SCORES", "ALL TIME HIGHEST SCORES"));
    adapters.add(new SinglePlayerScoreAdapter());
    adapters.add(new MultiBlockAdapter("pool_l7.nv", 8));
    adapters.add(new AlteringLinesWithoutPosAdapter("wrldtou2.nv", 5));
  }

  @NonNull
  @Override
  public String convertOutputToRaw(@NonNull String nvRamFileName, File originalNVRamFile) throws Exception {

    String stdOut = PINemHiService.executePINemHi(originalNVRamFile);
    if (stdOut == null) {
      return null;
    }

    // replace french space character, displayed ÿ with "."
    /*stdOut = stdOut
        .replaceAll("\u00ff", ".")
        .replaceAll("\u00a0", ".")
        .replaceAll("\u202f", ".")
        .replaceAll("\ufffd", ".");*/

    //check for pre-formatting
    List<String> lines = Arrays.asList(stdOut.trim().split("\n"));
//    if (!lines.isEmpty()) {
//      //remove active codepage line
//      lines = lines.subList(1, lines.size());
//    }
//
//    //remove empty lines since we expect the highscore title first
//    while (!lines.isEmpty() && StringUtils.isEmpty(lines.get(0).trim())) {
//      lines = lines.subList(1, lines.size());
//    }
//
//    //restore the original formatted string in case no custom adapter is needed
//    stdOut = String.join("\n", lines);

    for (ScoreNvRamAdapter adapter : adapters) {
      if (adapter.isApplicable(nvRamFileName, lines)) {
        LOG.info("Converted score using {}", adapter.getClass().getSimpleName());
        return adapter.convert(nvRamFileName, lines);
      }
    }
    return stdOut;
  }
}

package de.mephisto.vpin.server.highscores.parsing.nvram;

import de.mephisto.vpin.server.highscores.parsing.nvram.adapters.*;
import de.mephisto.vpin.server.pinemhi.PINemHiService;
import org.jspecify.annotations.NonNull;
import net.nvrams.mapping.pinemhi.NVRamPinemhiParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class NvRamOutputToRawWithPinemhi implements NvRamOutputToRaw {
  private final static Logger LOG = LoggerFactory.getLogger(NvRamOutputToRawWithPinemhi.class);

  private final static List<ScoreNvRamAdapter> adapters = new ArrayList<>();
  private final Set<String> supportedNvRams = new HashSet<>();

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

  public Set<String> getSupportedRoms() {
    if (supportedNvRams.isEmpty()) {
      try {
        List<String> supportedNVRams = new NVRamPinemhiParser().getSupportedNVRams();
        this.supportedNvRams.addAll(supportedNVRams);
      }
      catch (IOException e) {
        LOG.error("Failed to fetch supported ROMs: {}", e.getMessage(), e);
      }
    }
    return supportedNvRams;
  }

  @NonNull
  @Override
  public String convertOutputToRaw(@NonNull String nvRamFileName, File originalNVRamFile) throws Exception {
    String stdOut = PINemHiService.executePINemHi(originalNVRamFile);
    if (stdOut == null) {
      return null;
    }

    //check for pre-formatting
    List<String> lines = Arrays.asList(stdOut.trim().split("\n"));

    for (ScoreNvRamAdapter adapter : adapters) {
      if (adapter.isApplicable(nvRamFileName, lines)) {
        LOG.info("Converted score using {}", adapter.getClass().getSimpleName());
        return adapter.convert(nvRamFileName, lines);
      }
    }
    return stdOut;
  }
}

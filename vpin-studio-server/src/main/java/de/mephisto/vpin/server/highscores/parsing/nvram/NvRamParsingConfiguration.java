package de.mephisto.vpin.server.highscores.parsing.nvram;

import static de.mephisto.vpin.server.VPinStudioServer.Features;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

import de.mephisto.vpin.server.highscores.parsing.ScoreListFactory;
import net.nvrams.mapping.NVRamParser;
import net.nvrams.mapping.map.NVRamMapParser;
import net.nvrams.mapping.pinemhi.NVRamPinemhiParser;
import net.nvrams.mapping.superhac.NVRamSuperhacParser;

/**
 * A bridge to NVRam map sub projects
 * The class is registering the various services leveraging pinemhi, Superhac and Tom's logic maps
 * - as NvRamOutputToRaw, to convert nvram to raw
 * - as ScoreListAdapter, to parse raw to Scores
 */
@Configuration
public class NvRamParsingConfiguration implements InitializingBean {

  @Override
  public void afterPropertiesSet() throws Exception {
    // Order defines how they should be chosen
    if (Features.NVRAM_PARSING_USE_PINEMHI) {
      NVRamParser pinemhiParser = new NVRamPinemhiParser();
      NvRamParsingWithParser svc = new NvRamParsingWithParser(pinemhiParser);

      // register this service as a converter to convert nvFile to Raw
      NvRamOutputToScoreTextConverter.registerParser(svc);
      // Use DefaultAdapter for pinhemi
    }

    if (Features.NVRAM_PARSING_USE_JAVAMAPS) {
      NVRamParser mapParser = new NVRamMapParser();
      NvRamParsingWithParser svc = new NvRamParsingWithParser(mapParser);

      // register this service as a converter to convert nvFile to Raw
      NvRamOutputToScoreTextConverter.registerParser(svc);
      // register this service as an adapter to parse Raw
      ScoreListFactory.registerScoreListAdapter(svc);
    }

    if (Features.NVRAM_PARSING_USE_SUPERHAC) {
      NVRamParser superhacParser = new NVRamSuperhacParser();
      NvRamParsingWithParser svc = new NvRamParsingWithParser(superhacParser);

      // register this service as a converter to convert nvFile to Raw
      NvRamOutputToScoreTextConverter.registerParser(svc);
      // register this service as an adapter to parse Raw
      ScoreListFactory.registerScoreListAdapter(svc);
    }
  }
}

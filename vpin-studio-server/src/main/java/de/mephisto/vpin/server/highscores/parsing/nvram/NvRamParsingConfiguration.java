package de.mephisto.vpin.server.highscores.parsing.nvram;

import static de.mephisto.vpin.server.VPinStudioServer.Features;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

import de.mephisto.vpin.server.highscores.parsing.ScoreListFactory;

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

    //----
    if (Features.NVRAM_PARSING_USE_SUPERHAC) {
      NvRamParsingWithParser svc = NvRamParsingWithParser.createSuperhacParser();

      // register this service as a converter to convert nvFile to Raw
      NvRamOutputToScoreTextConverter.registerConverterService(svc);
      // register this service as an adapter to parse Raw
      ScoreListFactory.registerScoreListAdapter(svc);
    }

    //----
    if (Features.NVRAM_PARSING_USE_JAVAMAPS) {
      NvRamParsingWithParser svc = NvRamParsingWithParser.createNvramMapParser();

      // register this service as a converter to convert nvFile to Raw
      NvRamOutputToScoreTextConverter.registerConverterService(svc);
      // register this service as an adapter to parse Raw
      ScoreListFactory.registerScoreListAdapter(svc);
    }

    //----
    if (Features.NVRAM_PARSING_USE_PINEMHI) {
      NvRamOutputToRawWithPinemhi svc = new NvRamOutputToRawWithPinemhi();

      // register this service as a converter to convert nvFile to Raw
      NvRamOutputToScoreTextConverter.registerConverterService(svc);
    }
  }
}

package de.mephisto.vpin.server.highscores.parsing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HighscoreParserConfiguration {

  @Bean
  public DotColonHighscoreParser dotColonHighscoreParser() {
    return new DotColonHighscoreParser();
  }
}

package de.mephisto.vpin.server;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import de.mephisto.vpin.restclient.system.FeaturesInfo;

@SpringBootApplication
@EnableTransactionManagement
@EnableJpaRepositories
@EnableJpaAuditing
@EnableCaching
public class VPinStudioServer extends SpringBootServletInitializer {
  public static final String API_VERSION = "1";
  public static final String API_SEGMENT = "api/v" + API_VERSION + "/";

  /** The global static features activated, static for a simple access in code */
  public static FeaturesInfo Features = new FeaturesInfo();

  public static void main(String[] args) {
    ServerUpdatePreProcessing.execute();

    SpringApplicationBuilder builder = new SpringApplicationBuilder(VPinStudioServer.class);
    builder.headless(false);
    builder.run(args);
  }
}
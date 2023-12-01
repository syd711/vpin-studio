package de.mephisto.vpin.server;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Locale;

@SpringBootApplication
@EnableTransactionManagement
@EnableJpaRepositories
@EnableJpaAuditing
@EnableCaching
public class VPinStudioServer extends SpringBootServletInitializer {
  public static final String API_VERSION = "1";
  public static final String API_SEGMENT = "api/v" + API_VERSION + "/";

  public static void main(String[] args) {
    ServerUpdatePreProcessing.execute();
    SpringApplicationBuilder builder = new SpringApplicationBuilder(VPinStudioServer.class);
    builder.headless(false);
    builder.run(args);
  }
}
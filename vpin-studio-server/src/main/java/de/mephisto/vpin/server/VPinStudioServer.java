package de.mephisto.vpin.server;

import de.mephisto.vpin.commons.SystemInfo;
import de.mephisto.vpin.commons.utils.PropertiesStore;
import de.mephisto.vpin.restclient.system.FeaturesInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.Optional;

@SpringBootApplication
@EnableTransactionManagement
@EnableJpaRepositories
@EnableJpaAuditing(dateTimeProviderRef = "dateTimeProvider")
@EnableCaching
public class VPinStudioServer extends SpringBootServletInitializer {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioServer.class);

  public static final String API_VERSION = "1";
  public static final String API_SEGMENT = "api/v" + API_VERSION + "/";

  /**
   * The global static features activated, static for a simple access in code
   */
  public static FeaturesInfo Features = new FeaturesInfo();

  public static void main(String[] args) {
    runDelayCheck();

    ServerUpdatePreProcessing.execute();

    // Start Spring Boot application first
    SpringApplicationBuilder builder = new SpringApplicationBuilder(VPinStudioServer.class);
    builder.headless(false);
    builder.run(args);
  }

  @Bean
  public CacheManager cacheManager() {
    return new ConcurrentMapCacheManager("preferences"); // You can add more cache names as needed
  }

  @Bean
  public DateTimeProvider dateTimeProvider() {
      return () -> Optional.of(OffsetDateTime.now());
  }

  private static void runDelayCheck() {
    try {
      File propertiesFile = new File(SystemInfo.RESOURCES + "system.properties");
      PropertiesStore store = PropertiesStore.create(propertiesFile);
      String delay = store.get("startup.delay");
      if (!StringUtils.isEmpty(delay)) {
        try {
          int delaySec = Integer.parseInt(delay);
          LOG.info("Initial delay sleep: {} seconds", delay);
          if (delaySec > 0) {
            Thread.sleep(delaySec * 1000L);
          }
        }
        catch (Exception e) {
          //ignore
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to run initial delay: {}", e.getMessage());
    }
  }
}

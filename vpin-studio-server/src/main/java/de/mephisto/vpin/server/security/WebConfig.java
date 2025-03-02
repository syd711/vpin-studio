package de.mephisto.vpin.server.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;

import de.mephisto.vpin.restclient.JsonArgResolver;

/**
 *
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
  private final static Logger LOG = LoggerFactory.getLogger(WebConfig.class);

  @Value("${server.debug.enabled}")
  private boolean debugMode;

  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    UrlPathHelper urlPathHelper = new UrlPathHelper();
    urlPathHelper.setRemoveSemicolonContent(false);
    configurer.setUrlPathHelper(urlPathHelper);
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins("*")
        .allowCredentials(false)
        .allowedMethods("*");
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    JsonArgResolver jsonArgResolver = new JsonArgResolver();
    resolvers.add(jsonArgResolver);
  }
}
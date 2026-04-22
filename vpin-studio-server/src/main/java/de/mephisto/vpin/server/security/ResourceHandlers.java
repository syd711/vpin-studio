package de.mephisto.vpin.server.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@Configuration
//@EnableWebMvc
public class ResourceHandlers implements WebMvcConfigurer {

//  @Value("${server.resources.folder}")
//  private String resourcesFolder;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
//    registry
//        .addResourceHandler("/ui/**")
//        .addResourceLocations(resourcesFolder);

  }
}

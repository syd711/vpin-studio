package de.mephisto.vpin.server.dmdscore;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import de.mephisto.vpin.restclient.dmd.DMDDeviceIniConfiguration;

@Configuration
@EnableWebSocket
public class DMDScoreWebSocketConfiguration implements WebSocketConfigurer {

  private final static Logger LOG = LoggerFactory.getLogger(DMDScoreWebSocketConfiguration.class);

  private DMDScoreWebSocketHandler handler = new DMDScoreWebSocketHandler();

  @Bean
  public DMDScoreWebSocketHandler getHandler() {
    return handler;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    LOG.info("Enable WebSocket and register handler");
    registry.addHandler(handler, DMDDeviceIniConfiguration.WEBSOCKET_DMD_PATH).setAllowedOrigins("*");
  }

}
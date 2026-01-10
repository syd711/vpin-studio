package de.mephisto.vpin.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import de.mephisto.vpin.server.dmdscore.DMDScoreWebSocketHandler;

@Configuration
@EnableWebSocket
public class VpinStudioWebSocketConfiuration implements WebSocketConfigurer {

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(new DMDScoreWebSocketHandler(), "/dmdsocket").setAllowedOrigins("*");
  }

}
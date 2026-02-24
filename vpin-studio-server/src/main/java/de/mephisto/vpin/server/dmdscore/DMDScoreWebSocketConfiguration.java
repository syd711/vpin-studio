package de.mephisto.vpin.server.dmdscore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

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

  @Bean
  public ServletServerContainerFactoryBean createServletServerContainerFactoryBean() {
    ServletServerContainerFactoryBean factoryBean = new ServletServerContainerFactoryBean();
    factoryBean.setMaxTextMessageBufferSize(2048 * 2048);
    factoryBean.setMaxBinaryMessageBufferSize(2048 * 2048);
    factoryBean.setMaxSessionIdleTimeout(2048L * 2048L);
    factoryBean.setAsyncSendTimeout(2048L * 2048L);
    return factoryBean;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    LOG.info("Enable WebSocket and register handler");
    registry.addHandler(handler, DMDDeviceIniConfiguration.WEBSOCKET_DMD_PATH).setAllowedOrigins("*");
  }
  
}
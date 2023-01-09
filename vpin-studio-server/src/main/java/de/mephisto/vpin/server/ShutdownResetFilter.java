package de.mephisto.vpin.server;

import de.mephisto.vpin.server.keyevent.KeyEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;

@Component
@Order(1)
public class ShutdownResetFilter implements Filter {

  @Autowired
  private KeyEventService keyEventService;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
    chain.doFilter(request, response);
    keyEventService.resetShutdownTimer();
  }
}
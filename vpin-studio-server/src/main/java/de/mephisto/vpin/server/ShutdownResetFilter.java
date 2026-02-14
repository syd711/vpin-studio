package de.mephisto.vpin.server;

import de.mephisto.vpin.server.inputs.InputEventService;
import org.apache.catalina.connector.RequestFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

@Component
@Order(1)
public class ShutdownResetFilter implements Filter {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private InputEventService keyEventService;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
    try {
      Thread.currentThread().setName(((RequestFacade) request).getRequestURI());
    }
    catch (Exception e) {
      LOG.error("Shutdown Thread naming failed: {}", e.getMessage());
    }
    chain.doFilter(request, response);
    keyEventService.resetShutdownTimer();
  }
}
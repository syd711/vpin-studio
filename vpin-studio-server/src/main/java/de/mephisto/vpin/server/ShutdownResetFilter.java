package de.mephisto.vpin.server;

import de.mephisto.vpin.server.inputs.InputEventService;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.catalina.connector.RequestFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartException;

import javax.servlet.*;
import java.io.EOFException;
import java.io.IOException;

@Component
@Order(1)
public class ShutdownResetFilter implements Filter {
  private final static Logger LOG = LoggerFactory.getLogger(ShutdownResetFilter.class);

  @Autowired
  private InputEventService keyEventService;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
    try {
      // Set the current thread name based on the request URI
      Thread.currentThread().setName(((RequestFacade) request).getRequestURI());

      // Proceed with the filter chain
      chain.doFilter(request, response);
    } catch (Exception e) {
      if (e.getCause() instanceof MultipartException) {
        LOG.warn("Request aborted by the client, possibly due to upload cancellation: {}", e.getMessage());
      } else {
        LOG.error("An unexpected error occurred", e);
      }
    } finally {
      keyEventService.resetShutdownTimer();
    }
  }
}
package de.mephisto.vpin.server.security;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;

//@ControllerAdvice
public class ResourceHandler {
  private final static Logger LOG = LoggerFactory.getLogger(ResourceHandler.class);

  @Value("${server.resources.folder}")
  private String resourcesFolder;

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<Object> renderDefaultPage() {
    try {
      HttpServletRequest currentHttpRequest = getCurrentHttpRequest();
      String requestURI = currentHttpRequest.getRequestURI();
      if (requestURI.equals("/")) {
        requestURI = "index.html";
      }
      File indexFile = ResourceUtils.getFile(resourcesFolder + requestURI);
      MediaType mimeType = getMimeType(requestURI);
      FileInputStream inputStream = new FileInputStream(indexFile);

      if (mimeType.getType().equals("image")) {
        byte[] media = IOUtils.toByteArray(inputStream);
        inputStream.close();
        return ResponseEntity.ok().contentType(mimeType).body(media);
      }

      String body = StreamUtils.copyToString(inputStream, Charset.defaultCharset());
      inputStream.close();
      return ResponseEntity.ok().contentType(mimeType).body(body);
    } catch (Exception e) {
      LOG.error("Failed to return resources: " + e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("There was an error completing the action.");
    }
  }

  private MediaType getMimeType(String requestURI) {
    if (requestURI.endsWith(".html")) {
      return MediaType.TEXT_HTML;
    }
    if (requestURI.endsWith(".js")) {
      return MediaType.TEXT_PLAIN;
    }
    if (requestURI.endsWith(".css")) {
      return MediaType.TEXT_PLAIN;
    }
    if (requestURI.endsWith(".json")) {
      return MediaType.APPLICATION_JSON;
    }
    if (requestURI.endsWith(".ico")) {
      return MediaType.valueOf("image/x-icon");
    }
    if (requestURI.equals(".jpg")) {
      return MediaType.IMAGE_JPEG;
    }
    if (requestURI.equals(".png")) {
      return MediaType.IMAGE_PNG;
    }
    return MediaType.TEXT_PLAIN;
  }

  public static HttpServletRequest getCurrentHttpRequest() {
    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    if (requestAttributes instanceof ServletRequestAttributes) {
      HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
      return request;
    }
    return null;
  }
}
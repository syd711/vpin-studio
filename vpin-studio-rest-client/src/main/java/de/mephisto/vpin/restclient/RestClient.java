package de.mephisto.vpin.restclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class RestClient implements ClientHttpRequestInterceptor {
  private final static Logger LOG = LoggerFactory.getLogger(RestClient.class);
  public static final String SCHEME = "http";
  public static final String HOST = "localhost";
  public static final int PORT = 8089;

  private String baseUrl;
  private String authenticationToken;
  private RestTemplate restTemplate;

  private static RestClient INSTANCE;

  public static RestClient getInstance(String scheme, String host, int port) {
    if(INSTANCE == null) {
      INSTANCE = new RestClient(scheme, host, port);
    }
    return INSTANCE;
  }

  public static RestClient getInstance() {
    if(INSTANCE == null) {
      INSTANCE = new RestClient(SCHEME, HOST, PORT);
    }
    return INSTANCE;
  }

  private RestClient(String scheme, String host, int port) {
    baseUrl = scheme + "://" + host + ":" + port + "/";
    List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
    interceptors.add(this);
    restTemplate = new RestTemplate();
    restTemplate.setInterceptors(interceptors);
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public <T> T get(String path, Class<T> entityType) {
    return get(path, entityType, new HashMap<>());
  }

  public <T> T get(String path, Class<T> entityType, Map<String,?> urlVariables) {
    String url = baseUrl + path;
    LOG.info("REST: " + url);
    return restTemplate.getForObject(url, entityType, urlVariables);
  }

  public Boolean delete(String path) {
    try {
      restTemplate.delete(path);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public <T> T post(String path, Object entity, Class<T> entityType) {
    String url = baseUrl + path;
    return restTemplate.postForObject(url, entity, entityType);
  }

  public Boolean put(String url, Map<String, String> model) {
    HttpEntity<Map> entity = new HttpEntity<>(model);
    return exchange(url, HttpMethod.PUT, entity, Boolean.class);
  }

  public <T> T exchange(String path, HttpMethod method, HttpEntity requestEntity, Class<T> entityClass) {
    String url = baseUrl + path;
    ResponseEntity<T> response = restTemplate.exchange(url, method, requestEntity, entityClass);
    return response.getBody();
  }

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
//    request.getHeaders().set("Authorization", authenticationToken);
    ClientHttpResponse execute = execution.execute(request, body);
//    if(execute.getStatusCode() == HttpStatus.UNAUTHORIZED) {
//      throw new BadCredentialsException("Authorization failed");
//    }
    return execute;
  }

  public byte[] readBinary(String resource) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    InputStream is = null;
    try {
      URL url = new URL(resource);
      HttpURLConnection con =(HttpURLConnection)url.openConnection();
      is = con.getInputStream();
      byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
      int n;

      while((n = is.read(byteChunk)) > 0) {
        baos.write(byteChunk, 0, n);
      }
      return baos.toByteArray();
    } catch (Exception e) {
      LOG.error("Failed while reading bytes from %s: %s", resource, e.getMessage(), e);
    }
    return null;
  }
}

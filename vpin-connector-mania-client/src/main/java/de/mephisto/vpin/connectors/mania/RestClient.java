package de.mephisto.vpin.connectors.mania;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

/**
 *
 */
public class RestClient implements ClientHttpRequestInterceptor {
  private final static Logger LOG = LoggerFactory.getLogger(RestClient.class);
  public static final String SCHEME = "http";
  public static final String HOST = "localhost";
  public static final int PORT = 80;

  public static final int TIMEOUT = 15000;

  private String baseUrl;
  private String cabinetId;
  private RestTemplate restTemplate;

  public static RestClient createInstance(String host, String context, String cabinetId) {
    return new RestClient(SCHEME, host, PORT, context, cabinetId);
  }

  private RestClient(String scheme, String host, int port, String context, String cabinetId) {
    this.cabinetId = cabinetId;
    if (host.startsWith("http")) {
      baseUrl = host + "/";
    }
    else {
      baseUrl = scheme + "://" + host + ":" + port + "/";
    }

    if (context != null && !context.trim().isEmpty()) {
      baseUrl += context;
    }
    SimpleClientHttpRequestFactory httpRequestFactory = new SimpleClientHttpRequestFactory();
    httpRequestFactory.setConnectTimeout(TIMEOUT);
    httpRequestFactory.setReadTimeout(TIMEOUT);
    restTemplate = new RestTemplate(httpRequestFactory);
    restTemplate.setInterceptors(Collections.singletonList(this));


    List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    converter.setPrettyPrint(true);
    converter.getObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    // Note: here we are making this converter to process any kind of response,
    // not only application/*json, which is the default behaviour
    converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
    messageConverters.add(converter);
    restTemplate.setMessageConverters(messageConverters);
  }

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                      ClientHttpRequestExecution execution) throws IOException {
    HttpHeaders headers = request.getHeaders();
    headers.add("VPin-Token", cabinetId);
    return execution.execute(request, body);
  }

  public <T> T get(String path, Class<T> entityType) {
    return get(path, entityType, new HashMap<>());
  }

  public <T> T get(String path, Class<T> entityType, Map<String, ?> urlVariables) {
    String url = baseUrl + path;
    try {
      long start = System.currentTimeMillis();
      T forObject = restTemplate.getForObject(url, entityType, urlVariables);
      LOG.info("HTTP GET " + url + " (" + (System.currentTimeMillis() - start) + "ms)");
      return forObject;
    } catch (ResourceAccessException e) {
      LOG.error("GET request failed: " + e.getMessage());
    }
    return null;
  }

  public Boolean delete(String path) {
    String url = baseUrl + path;
    LOG.info("HTTP DELETE " + url);
    restTemplate.delete(url);
    return true;
  }

  public Boolean delete(String path, Map<String, Object> values) {
    String url = baseUrl + path;
    restTemplate.delete(url, values);
    return true;
  }

  public <T> T post(String path, Object model, Class<T> entityType) throws Exception {
    long start = System.currentTimeMillis();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity entity = new HttpEntity<>(model, headers);
    T exchange = exchange(path, HttpMethod.POST, entity, entityType);
    LOG.info("HTTP POST " + path + " (" + (System.currentTimeMillis() - start) + "ms)");
    return exchange;
  }

  public Boolean put(String url, Map<String, Object> model) throws Exception {
    long start = System.currentTimeMillis();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map> entity = new HttpEntity<>(model, headers);
    Boolean exchange = exchange(url, HttpMethod.PUT, entity, Boolean.class);
    LOG.info("HTTP PUT " + url + " (" + (System.currentTimeMillis() - start) + "ms)");
    return exchange;
  }

  public <T> T put(String url, Map<String, Object> model, Class<T> entityType) throws Exception {
    long start = System.currentTimeMillis();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map> entity = new HttpEntity<>(model, headers);
    T exchange = exchange(url, HttpMethod.PUT, entity, entityType);
    LOG.info("HTTP PUT " + url + " (" + (System.currentTimeMillis() - start) + "ms)");
    return exchange;
  }

  public <T> T exchange(String path, HttpMethod method, HttpEntity requestEntity, Class<T> entityClass) throws Exception {
    String url = baseUrl + path;
    ResponseEntity<T> response = restTemplate.exchange(url, method, requestEntity, entityClass);
    return response.getBody();
  }

  public void setCabinetId(String id) {
    this.cabinetId = id;
  }
}

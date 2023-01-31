package de.mephisto.vpin.restclient;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 *
 */
public class RestClient implements ClientHttpRequestInterceptor {
  private final static Logger LOG = LoggerFactory.getLogger(RestTemplate.class);
  public static final String SCHEME = "http";
  public static final String HOST = "localhost";
  public static final int PORT = 8089;

  private String baseUrl;
  private String authenticationToken;
  private RestTemplate restTemplate;
  private ObjectCache cache = new ObjectCache();

  public static RestClient createInstance(String host) {
    return new RestClient(SCHEME, host, PORT);
  }

  private RestClient(String scheme, String host, int port) {
    baseUrl = scheme + "://" + host + ":" + port + "/";
    List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
    interceptors.add(this);
    restTemplate = new RestTemplate();
    restTemplate.setInterceptors(interceptors);

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

  public String getBaseUrl() {
    return baseUrl;
  }

  public void clearCache() {
    this.cache.invalidateAll();
  }

  public <T> T get(String path, Class<T> entityType) {
    return get(path, entityType, new HashMap<>());
  }


  public <T> T getCached(String path, Class<T> entityType) {
    if (cache.contains(path)) {
      return (T) cache.get(path);
    }
    T t = get(path, entityType, new HashMap<>());
    if (t != null) {
      cache.put(path, t);
    }
    return t;
  }

  public <T> T get(String path, Class<T> entityType, Map<String, ?> urlVariables) {
    String url = baseUrl + path;
    long start = System.currentTimeMillis();
    T forObject = restTemplate.getForObject(url, entityType, urlVariables);
    LOG.info("HTTP GET " + url + " (" + (System.currentTimeMillis() - start) + "ms)");
    return forObject;
  }

  public Boolean delete(String path) {
    String url = baseUrl + path;
    restTemplate.delete(url);
    return true;
  }

  public Boolean delete(String path, Map<String, Object> values) {
    String url = baseUrl + path;
    restTemplate.delete(url, values);
    return true;
  }

  public <T> T post(String path, Object model, Class<T> entityType) throws Exception {
    LOG.info("HTTP POST " + path);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity entity = new HttpEntity<>(model, headers);
    return exchange(path, HttpMethod.POST, entity, entityType);
  }

  public Boolean put(String url, Map<String, Object> model) throws Exception {
    LOG.info("HTTP PUT " + url + " " + model);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map> entity = new HttpEntity<>(model, headers);
    return exchange(url, HttpMethod.PUT, entity, Boolean.class);
  }

  public <T> T exchange(String path, HttpMethod method, HttpEntity requestEntity, Class<T> entityClass) throws Exception {
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
    if (!resource.contains("api/")) {
      resource = VPinStudioClient.API + resource;
    }

    if (!resource.startsWith(baseUrl)) {
      resource = baseUrl + resource;
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    InputStream is = null;
    try {
      URL url = new URL(resource);
      LOG.info("HTTP GET Binary: " + url);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      int responseCode = con.getResponseCode();
      if (responseCode == 404) {
        return null;
      }

      is = con.getInputStream();
      byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
      int n;

      while ((n = is.read(byteChunk)) > 0) {
        baos.write(byteChunk, 0, n);
      }
      return baos.toByteArray();
    } catch (Exception e) {
      LOG.error("Failed while reading bytes from %s: %s", resource, e.getMessage(), e);
    }
    return null;
  }
}

package de.mephisto.vpin.restclient;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientErrorHandler;
import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.ResourceAccessException;
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

  public static final int TIMEOUT = 15000;

  private String host;
  private String baseUrl;
  private RestTemplate restTemplate;
  private ObjectCache cache = new ObjectCache();
  private VPinStudioClientErrorHandler errorHandler;

  public static RestClient createInstance(String host, int port) {
    return new RestClient(SCHEME, host, port);
  }

  public static RestClient createInstance(String baseUrl) {
    return new RestClient(baseUrl);
  }

  private RestClient(String baseUrl) {
    this.baseUrl = baseUrl;
    initRestClient();
  }

  private RestClient(String scheme, String host, int port) {
    this.host = host;
    this.baseUrl = scheme + "://" + host + ":" + port + "/";
    initRestClient();
  }

  private void initRestClient() {
    List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
    interceptors.add(this);

//    SimpleClientHttpRequestFactory httpRequestFactory = new SimpleClientHttpRequestFactory();
//    httpRequestFactory.setConnectTimeout(TIMEOUT);
//    httpRequestFactory.setReadTimeout(TIMEOUT);
//    restTemplate = new RestTemplate(httpRequestFactory);
    HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(
        HttpClientBuilder.create().build());
    restTemplate = new RestTemplate(clientHttpRequestFactory);
    restTemplate.setInterceptors(interceptors);

    List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    converter.setPrettyPrint(true);
    converter.getObjectMapper()
        .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        .setTimeZone(TimeZone.getDefault())
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    // Note: here we are making this converter to process any kind of response,
    // not only application/*json, which is the default behaviour
    converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
    messageConverters.add(converter);
    restTemplate.setMessageConverters(messageConverters);
  }

  public void setErrorHandler(VPinStudioClientErrorHandler errorHandler) {
//    this.errorHandler = errorHandler;
  }

  public static RestTemplate createTimeoutBasedTemplate(int timeoutMs) {
    SimpleClientHttpRequestFactory httpRequestFactory = new SimpleClientHttpRequestFactory();
    httpRequestFactory.setConnectTimeout(timeoutMs);
    httpRequestFactory.setReadTimeout(timeoutMs);
    return new RestTemplate(httpRequestFactory);
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void clearCache() {
    this.cache.invalidateAll();
  }

  public void clearCache(String urlPrefix) {
    cache.invalidateMatching(urlPrefix);
  }

  public <T> T get(String path, Class<T> entityType) {
    return get(path, entityType, new HashMap<>());
  }

  public <T> T getCached(String path, Class<T> entityType) {
    return getCached(path, entityType, false);
  }

  @SuppressWarnings("unchecked")
  public <T> T getCached(String path, Class<T> entityType, boolean forceReload) {
    if (!forceReload && cache.contains(path)) {
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
    try {
      long start = System.currentTimeMillis();

      T forObject;
      if (String.class.equals(entityType)) {
        final RestTemplate _restTemplate = new RestTemplate();
        @SuppressWarnings("unchecked")
        T ret = (T) _restTemplate.getForObject(url, String.class, urlVariables);
        forObject = ret;
      }
      else {
        forObject = restTemplate.getForObject(url, entityType, urlVariables);
      }
      LOG.info("HTTP GET " + url + " (" + (System.currentTimeMillis() - start) + "ms)");
      return forObject;
    }
    catch (ResourceAccessException e) {
      LOG.error("GET request failed: " + e.getMessage());
      if (errorHandler != null) {
        errorHandler.onError(e);
      }
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
    long start = System.currentTimeMillis();
    String url = baseUrl + path;
    restTemplate.delete(url, values);
    LOG.info("HTTP DELETE " + path + " (" + (System.currentTimeMillis() - start) + "ms)");
    return true;
  }

  public <T> T post(String path, Object model, Class<T> entityType) {
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

  public <T> T exchange(String path, HttpMethod method, HttpEntity requestEntity, Class<T> entityClass) {
    String url = baseUrl + path;
    ResponseEntity<T> response = restTemplate.exchange(url, method, requestEntity, entityClass);
    return response.getBody();
  }


  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    request.getHeaders().add(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br");

//    if (request.getURI().toString().contains("knowns")) {
//      logRequest(request);
//    }
    ClientHttpResponse response = execution.execute(request, body);
//    if (request.getURI().toString().contains("knowns")) {
//      logResponse(response);
//    }
    return response;
  }

  private static void logResponse(ClientHttpResponse response) {
    System.out.println("--------- Response ---------------");
    Set<Map.Entry<String, List<String>>> entries = response.getHeaders().entrySet();
    for (Map.Entry<String, List<String>> entry : entries) {
      System.out.println(entry.getKey() + " => " + entry.getValue());
    }
  }

  private static void logRequest(HttpRequest request) {
    System.out.println("--------- Request ---------------");
    for (Map.Entry<String, List<String>> entry : request.getHeaders().entrySet()) {
      System.out.println(entry.getKey() + " => " + entry.getValue());
    }
  }

  public byte[] readBinary(String resource) {
    if (!resource.contains("api/") && !resource.startsWith("http")) {
      resource = VPinStudioClient.API + resource;
    }

    if (!resource.startsWith(baseUrl) && !resource.startsWith("http")) {
      resource = baseUrl + resource;
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    InputStream is = null;
    try {
      long start = System.currentTimeMillis();
      URL url = new URL(resource);

      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      int responseCode = con.getResponseCode();
      if (responseCode == 404) {
        LOG.warn("URL not found: " + url);
        return null;
      }

      is = con.getInputStream();
      byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
      int n;

      while ((n = is.read(byteChunk)) > 0) {
        baos.write(byteChunk, 0, n);
      }

      LOG.info("HTTP GET Binary: " + url + " (" + (System.currentTimeMillis() - start) + "ms)");
      return baos.toByteArray();
    }
    catch (Exception e) {
      LOG.error("Failed while reading bytes from %s: %s", resource, e.getMessage(), e);
    }
    return null;
  }

  public String getHost() {
    return this.host;
  }
}

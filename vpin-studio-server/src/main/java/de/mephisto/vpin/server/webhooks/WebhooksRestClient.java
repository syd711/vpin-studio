package de.mephisto.vpin.server.webhooks;

import de.mephisto.vpin.restclient.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class WebhooksRestClient {
  private final static Logger LOG = LoggerFactory.getLogger(WebhooksRestClient.class);


  public void onDelete(String url) {
    try {
      RestClient restClient = RestClient.createInstance(url);
      restClient.delete("");
    }
    catch (Exception e) {
      LOG.error("Webhook delete call failed: {}", e.getMessage());
    }
  }

  public void onUpdate(String url, Map<String, Object> parameters) {
    try {
      RestClient restClient = RestClient.createInstance(url);
      restClient.put("", parameters, Map.class);
    }
    catch (Exception e) {
      LOG.error("Webhook update call failed: {}", e.getMessage());
    }
  }

  public void onCreate(String url, Map<String, Object> parameters) {
    try {
      RestClient restClient = RestClient.createInstance(url);
      Map<String, Object> updatedParams = new HashMap<>(parameters);
      restClient.post("", updatedParams, Map.class);
    }
    catch (Exception e) {
      LOG.error("Webhook create call failed: {}", e.getMessage());
    }
  }
}

package de.mephisto.vpin.restclient.webhooks;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class Webhook {
  private String endpoint;
  private List<WebhookEventType> subscribe = new ArrayList<>(Arrays.asList(WebhookEventType.create, WebhookEventType.update, WebhookEventType.delete));
  private Map<String, Object> parameters = new HashMap<>();

  public Map<String, Object> getParameters() {
    return parameters;
  }

  public void setParameters(Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public List<WebhookEventType> getSubscribe() {
    return subscribe;
  }

  public void setSubscribe(List<WebhookEventType> subscribe) {
    this.subscribe = subscribe;
  }

  public String toParameterString() {
    StringBuilder builder = new StringBuilder();
    Set<Map.Entry<String, Object>> entries = parameters.entrySet();
    for (Map.Entry<String, Object> entry : entries) {
      String key = entry.getKey();
      Object value = entry.getValue();
      builder.append(key);
      builder.append("=");
      builder.append(value);
      builder.append(";");
    }
    return builder.toString();
  }

  public void applyParameterString(String text) {
    parameters.clear();
    if (!StringUtils.isEmpty(text)) {
      String[] prms = text.split(";");
      for (String paramString : prms) {
        if (!StringUtils.isEmpty(paramString) && paramString.contains("=")) {
          String[] split = paramString.split("=");
          if (split.length == 2) {
            parameters.put(split[0], split[1]);
          }
        }
      }
    }
  }
}

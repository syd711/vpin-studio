package de.mephisto.vpin.restclient.webhooks;

import java.util.*;

public class Webhook {
  private String endpoint;
  private String parameterValue;
  private List<WebhookEventType> subscribe = new ArrayList<>(Arrays.asList(WebhookEventType.create, WebhookEventType.update, WebhookEventType.delete));
  private Map<String,Object> parameters = new HashMap<>();

  public String getParameterValue() {
    return parameterValue;
  }

  public void setParameterValue(String parameterValue) {
    this.parameterValue = parameterValue;
  }

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
}

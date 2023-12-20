package de.mephisto.vpin.connectors.mania;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VPinManiaClient {
  private final static Logger LOG = LoggerFactory.getLogger(VPinManiaClient.class);

  private final RestClient restClient;

  private final AccountClient accountClient;

  public VPinManiaClient(String host, String context) {
    restClient = RestClient.createInstance(host, context);

    this.accountClient = new AccountClient(this);
  }

  public AccountClient getAccountClient() {
    return accountClient;
  }

  public RestClient getRestClient() {
    return restClient;
  }
}

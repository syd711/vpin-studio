package de.mephisto.vpin.connectors.mania;

public class VPinManiaClientService {
  public final static String API = "api/";

  private final VPinManiaClient client;

  public VPinManiaClientService(VPinManiaClient client) {
    this.client = client;
  }

  public RestClient getRestClient() {
    return client.getRestClient();
  }
}

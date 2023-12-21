package de.mephisto.vpin.connectors.mania;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VPinManiaClient {
  private final static Logger LOG = LoggerFactory.getLogger(VPinManiaClient.class);

  private final RestClient restClient;

  private final AccountClient accountClient;
  private final TournamentClient tournamentClient;

  public VPinManiaClient(String host, String context, String cabinetId) {
    restClient = RestClient.createInstance(host, context, cabinetId);

    this.accountClient = new AccountClient(this);
    this.tournamentClient = new TournamentClient(this);
  }

  public AccountClient getAccountClient() {
    return accountClient;
  }

  public TournamentClient getTournamentClient() {
    return tournamentClient;
  }

  public RestClient getRestClient() {
    return restClient;
  }

  public void setCabinetId(String id) {
    restClient.setCabinetId(id);
  }
}
